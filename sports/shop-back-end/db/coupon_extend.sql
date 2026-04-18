-- 优惠券表扩展字段
ALTER TABLE `coupon` ADD COLUMN `register_gift` tinyint DEFAULT 0 COMMENT '是否注册赠送 0=否 1=是' AFTER `status`;
ALTER TABLE `coupon` ADD COLUMN `activity_trigger` tinyint DEFAULT NULL COMMENT '活动触发类型 null=无 1=消费满额' AFTER `register_gift`;
ALTER TABLE `coupon` ADD COLUMN `activity_amount` decimal(10,2) DEFAULT NULL COMMENT '活动触发金额' AFTER `activity_trigger`;
