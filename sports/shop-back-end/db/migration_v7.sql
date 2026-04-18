-- =============================================
-- 迁移脚本：V7
-- 版本：migration_v7
-- 变更日期：2026-04-15
-- 变更说明：
--   1. 订单表新增退款补偿所需字段
--   2. 订单项新增 sku_id，支持商品库存回滚
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v7.sql
-- =============================================

USE `kinetic_sports`;

SET @db_name = DATABASE();

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name
          AND TABLE_NAME = 'order'
          AND COLUMN_NAME = 'before_refund_status'
    ),
    'SELECT ''skip order.before_refund_status''',
    'ALTER TABLE `order` ADD COLUMN `before_refund_status` tinyint DEFAULT NULL COMMENT ''退款申请前的原状态'' AFTER `refund_reason`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name
          AND TABLE_NAME = 'order'
          AND COLUMN_NAME = 'refund_amount'
    ),
    'SELECT ''skip order.refund_amount''',
    'ALTER TABLE `order` ADD COLUMN `refund_amount` decimal(10,2) DEFAULT NULL COMMENT ''退款金额'' AFTER `before_refund_status`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name
          AND TABLE_NAME = 'order'
          AND COLUMN_NAME = 'close_time'
    ),
    'SELECT ''skip order.close_time''',
    'ALTER TABLE `order` ADD COLUMN `close_time` datetime DEFAULT NULL COMMENT ''关闭时间'' AFTER `refund_amount`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name
          AND TABLE_NAME = 'order'
          AND COLUMN_NAME = 'finish_time'
    ),
    'SELECT ''skip order.finish_time''',
    'ALTER TABLE `order` ADD COLUMN `finish_time` datetime DEFAULT NULL COMMENT ''完成时间'' AFTER `close_time`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name
          AND TABLE_NAME = 'order_item'
          AND COLUMN_NAME = 'sku_id'
    ),
    'SELECT ''skip order_item.sku_id''',
    'ALTER TABLE `order_item` ADD COLUMN `sku_id` bigint DEFAULT NULL COMMENT ''SKU ID'' AFTER `qty`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
