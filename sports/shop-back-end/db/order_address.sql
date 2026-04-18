-- 为订单表添加地址快照字段
ALTER TABLE `order` 
ADD COLUMN `address_snapshot` TEXT COMMENT '收货地址快照(JSON)' AFTER `finish_time`;
