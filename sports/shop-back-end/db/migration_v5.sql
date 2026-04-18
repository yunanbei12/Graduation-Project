-- =============================================
-- 迁移脚本：V5
-- 版本：migration_v5
-- 变更日期：2026-04-13
-- 变更说明：
--   1. user 表新增 register_type 字段（注册方式标识）
--      0=微信注册 1=手机号注册 2=短信验证码登录注册
--   2. 回填已有用户的 register_type（有密码的设为1，其余为0）
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v5.sql
-- =============================================

USE `kinetic_sports`;

-- =============================================
-- 变更 1：user 表新增 register_type 字段
-- 目的：区分用户注册方式，供后台管理正确显示注册来源
-- =============================================

DROP PROCEDURE IF EXISTS add_register_type_column;

DELIMITER $$
CREATE PROCEDURE add_register_type_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND COLUMN_NAME  = 'register_type'
    ) THEN
        ALTER TABLE `user`
            ADD COLUMN `register_type` tinyint NOT NULL DEFAULT 0
            COMMENT '0=微信注册 1=手机号注册 2=短信验证码登录注册'
            AFTER `login_password`;
    END IF;
END$$
DELIMITER ;

CALL add_register_type_column();
DROP PROCEDURE IF EXISTS add_register_type_column;

-- =============================================
-- 变更 2：回填已有用户的 register_type
-- 有 login_password 的用户标记为手机号注册(1)，其余为微信注册(0)
-- =============================================

UPDATE `user`
SET `register_type` = 1
WHERE `login_password` IS NOT NULL
  AND `login_password` != ''
  AND `register_type` = 0;

-- =============================================
-- 验证：查看变更结果
-- =============================================
-- SELECT id, nick_name, phone, register_type, login_password IS NOT NULL AS has_pwd FROM `user`;

-- =============================================
-- 回滚语句（请勿在本脚本中执行，仅供参考）
-- ALTER TABLE `user` DROP COLUMN `register_type`;
-- =============================================
