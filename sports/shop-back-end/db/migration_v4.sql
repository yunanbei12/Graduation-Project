-- =============================================
-- 迁移脚本：V4
-- 版本：migration_v4
-- 变更日期：2026-04-13
-- 变更说明：
--   1. user 表 phone 字段新增唯一索引 uk_phone（手机号作为用户唯一标识）
--   2. 新建 sms_log 表（短信验证码记录）
--   3. 删除 user 表旧的 uk_nick_name 唯一索引（昵称不再作为登录标识）
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v4.sql
-- 回滚方式：执行 migration_v4_rollback.sql
-- =============================================

USE `kinetic_sports`;

-- =============================================
-- 变更 1：user 表 phone 字段添加唯一索引
-- 目的：手机号作为用户注册/登录的唯一标识，
--       保证一个手机号只能注册一个账号
-- 注意：若已存在重复 phone 数据需先清理
-- =============================================

DROP PROCEDURE IF EXISTS add_phone_unique_index;

DELIMITER $$
CREATE PROCEDURE add_phone_unique_index()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND INDEX_NAME   = 'uk_phone'
    ) THEN
        -- 检查是否有重复 phone
        IF (SELECT COUNT(*) FROM (
                SELECT phone FROM `user`
                WHERE phone IS NOT NULL AND phone != '' AND is_deleted = 0
                GROUP BY phone HAVING COUNT(*) > 1
            ) t) = 0 THEN
            ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);
        ELSE
            -- 存在重复数据时降级为普通索引
            ALTER TABLE `user` ADD KEY `idx_phone` (`phone`);
        END IF;
    END IF;
END$$
DELIMITER ;

CALL add_phone_unique_index();
DROP PROCEDURE IF EXISTS add_phone_unique_index;

-- =============================================
-- 变更 2：删除 user 表 uk_nick_name 唯一索引
-- 目的：昵称不再作为登录标识，允许不同用户使用相同昵称，
--       新注册用户默认昵称为"用户XXXX"
-- =============================================

DROP PROCEDURE IF EXISTS drop_nick_name_unique_index;

DELIMITER $$
CREATE PROCEDURE drop_nick_name_unique_index()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND INDEX_NAME   = 'uk_nick_name'
    ) THEN
        ALTER TABLE `user` DROP INDEX `uk_nick_name`;
    END IF;
    -- 如果之前降级为普通索引，也一并删除
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND INDEX_NAME   = 'idx_nick_name'
    ) THEN
        ALTER TABLE `user` DROP INDEX `idx_nick_name`;
    END IF;
END$$
DELIMITER ;

CALL drop_nick_name_unique_index();
DROP PROCEDURE IF EXISTS drop_nick_name_unique_index;

-- =============================================
-- 变更 3：新建 sms_log 表
-- 目的：记录短信验证码发送日志，支持注册验证码和绑定手机验证码
--       用于验证码校验、频率限制、防刷
-- =============================================

CREATE TABLE IF NOT EXISTS `sms_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(20) NOT NULL COMMENT '手机号',
  `mobile_code` varchar(10) NOT NULL COMMENT '验证码',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '0=注册验证 1=绑定手机验证',
  `content` varchar(500) DEFAULT NULL COMMENT '短信内容',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=有效 0=已失效',
  `rec_date` datetime NOT NULL COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`user_phone`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 验证：查看变更结果
-- =============================================
-- 可手动执行以下语句确认变更是否生效：
-- SHOW INDEX FROM `user`;
-- SHOW CREATE TABLE `sms_log`;

-- =============================================
-- 回滚语句（请勿在本脚本中执行，仅供参考）
-- ALTER TABLE `user` DROP INDEX `uk_phone`;
-- ALTER TABLE `user` DROP INDEX `idx_phone`;
-- DROP TABLE IF EXISTS `sms_log`;
-- ALTER TABLE `user` ADD UNIQUE KEY `uk_nick_name` (`nick_name`);
-- =============================================
