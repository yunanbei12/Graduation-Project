-- =============================================
-- Migration V9: 添加订单地址快照字段
-- =============================================

USE `kinetic_sports`;

-- 为订单表添加收货地址快照字段
ALTER TABLE `order` ADD COLUMN `address_snapshot` TEXT NULL COMMENT '收货地址快照(JSON)' AFTER `finish_time`;
