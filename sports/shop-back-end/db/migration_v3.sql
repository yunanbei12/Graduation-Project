-- =============================================
-- 迁移脚本：V3
-- 版本：migration_v3
-- 变更日期：2026-04-12
-- 变更说明：
--   1. user 表新增 login_password 字段（支持账号密码注册登录）
--   2. 新增 login_password 字段索引（通过 nick_name 查询优化）
-- 执行方式：mysql -u root -p kinetic_sports < migration_v3.sql
-- 回滚方式：执行 migration_v3_rollback.sql
-- =============================================

USE `kinetic_sports`;

-- =============================================
-- 变更 1：user 表新增账号密码字段
-- 目的：支持小程序端使用用户名+密码方式注册和登录，
--       不影响已有微信登录用户（该字段允许为 NULL）
-- =============================================

-- 先检查字段是否已存在，避免重复执行报错
-- 若字段已存在则跳过（MySQL 8.0+ 可通过存储过程实现）
DROP PROCEDURE IF EXISTS add_login_password_column;

DELIMITER $$
CREATE PROCEDURE add_login_password_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND COLUMN_NAME  = 'login_password'
    ) THEN
        ALTER TABLE `user`
            ADD COLUMN `login_password` varchar(100) DEFAULT NULL
                COMMENT 'BCrypt 加密密码（账号密码注册登录使用，微信登录用户为 NULL）'
            AFTER `open_id`;
    END IF;
END$$
DELIMITER ;

CALL add_login_password_column();
DROP PROCEDURE IF EXISTS add_login_password_column;

-- =============================================
-- 变更 2：为 user.nick_name 添加唯一索引
-- 目的：账号密码登录时通过 nick_name 查找用户，
--       同时保证用户名唯一性约束
-- 注意：若已存在同名重复数据则需先清理再添加
-- =============================================

DROP PROCEDURE IF EXISTS add_nick_name_unique_index;

DELIMITER $$
CREATE PROCEDURE add_nick_name_unique_index()
BEGIN
    -- 检查是否已存在该索引
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'user'
          AND INDEX_NAME   = 'uk_nick_name'
    ) THEN
        -- 检查是否有重复 nick_name（有则不加唯一索引，只加普通索引）
        IF (SELECT COUNT(*) FROM (
                SELECT nick_name FROM `user`
                WHERE nick_name IS NOT NULL AND is_deleted = 0
                GROUP BY nick_name HAVING COUNT(*) > 1
            ) t) = 0 THEN
            ALTER TABLE `user` ADD UNIQUE KEY `uk_nick_name` (`nick_name`);
        ELSE
            -- 存在重复数据时降级为普通索引
            ALTER TABLE `user` ADD KEY `idx_nick_name` (`nick_name`);
        END IF;
    END IF;
END$$
DELIMITER ;

CALL add_nick_name_unique_index();
DROP PROCEDURE IF EXISTS add_nick_name_unique_index;

-- =============================================
-- 验证：查看 user 表当前结构
-- =============================================
-- 可手动执行以下语句确认变更是否生效：
-- SHOW COLUMNS FROM `user`;
-- SHOW INDEX FROM `user`;

-- =============================================
-- 回滚语句（请勿在本脚本中执行，仅供参考）
-- ALTER TABLE `user` DROP COLUMN `login_password`;
-- ALTER TABLE `user` DROP INDEX `uk_nick_name`;
-- ALTER TABLE `user` DROP INDEX `idx_nick_name`;
-- =============================================
