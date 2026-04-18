-- =============================================
-- 增量迁移脚本：从旧版升级到新版
-- 已有数据库执行此脚本即可，不会丢失任何数据
-- =============================================

USE `kinetic_sports`;

-- 1. course 表新增3个字段
ALTER TABLE `course` ADD COLUMN `validity_days` int DEFAULT 90 COMMENT '课包有效期天数(私教课)' AFTER `is_door_service`;
ALTER TABLE `course` ADD COLUMN `min_group_size` int DEFAULT NULL COMMENT '成团人数(团课)' AFTER `validity_days`;
ALTER TABLE `course` ADD COLUMN `settle_ratio` decimal(5,4) DEFAULT 0.5000 COMMENT '教练分成比例(0.5=50%)' AFTER `min_group_size`;

-- 2. order 表新增5个字段
ALTER TABLE `order` ADD COLUMN `course_id` bigint DEFAULT NULL COMMENT '关联课程ID' AFTER `user_id`;
ALTER TABLE `order` ADD COLUMN `coupon_id` bigint DEFAULT NULL COMMENT '使用的优惠券ID' AFTER `actual_amount`;
ALTER TABLE `order` ADD COLUMN `coupon_amount` decimal(10,2) DEFAULT 0 COMMENT '优惠券抵扣金额' AFTER `coupon_id`;
ALTER TABLE `order` ADD COLUMN `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式(wechat/alipay/cash)' AFTER `coupon_amount`;
ALTER TABLE `order` ADD COLUMN `payment_time` datetime DEFAULT NULL COMMENT '支付时间' AFTER `payment_method`;

-- 3. 新建5张表
CREATE TABLE IF NOT EXISTS `user_course_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL COMMENT '关联课程',
  `order_id` bigint NOT NULL COMMENT '关联订单',
  `total_lessons` int NOT NULL COMMENT '总节数',
  `used_lessons` int NOT NULL DEFAULT 0 COMMENT '已用节数',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=已过期 1=正常 2=已退费',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `package_id` bigint DEFAULT NULL COMMENT '课包ID(私教课)',
  `schedule_id` bigint DEFAULT NULL COMMENT '排课ID(团课)',
  `coach_id` bigint DEFAULT NULL COMMENT '教练ID',
  `location` varchar(200) DEFAULT NULL COMMENT '上课地点',
  `checkin_time` datetime NOT NULL COMMENT '上课时间',
  `checkin_type` tinyint NOT NULL COMMENT '1=私教课消课 2=团课签到',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=缺勤 1=正常出勤',
  `lesson_price` decimal(10,2) DEFAULT NULL COMMENT '单节课价格快照',
  `coach_ratio` decimal(5,4) DEFAULT NULL COMMENT '教练分成比例快照(0.5=50%)',
  `coach_amount` decimal(10,2) DEFAULT NULL COMMENT '教练分成金额(自动计算)',
  `settle_status` tinyint NOT NULL DEFAULT 0 COMMENT '0=未结算 1=已结算',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_package` (`package_id`),
  KEY `idx_schedule` (`schedule_id`),
  KEY `idx_coach_settle` (`coach_id`, `settle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `coach_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coach_id` bigint NOT NULL,
  `period_start` date NOT NULL COMMENT '结算周期起始',
  `period_end` date NOT NULL COMMENT '结算周期截止',
  `total_lessons` int NOT NULL DEFAULT 0 COMMENT '结算课次',
  `total_amount` decimal(10,2) NOT NULL COMMENT '教练结算总金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=待结算 1=已结算',
  `settle_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_coach` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '优惠券名称',
  `type` tinyint NOT NULL COMMENT '1=满减 2=折扣 3=无门槛',
  `discount` decimal(10,2) DEFAULT NULL COMMENT '满减金额/无门槛金额',
  `min_amount` decimal(10,2) DEFAULT 0 COMMENT '使用门槛金额',
  `discount_ratio` decimal(3,2) DEFAULT NULL COMMENT '折扣率(0.8=8折)',
  `scope` tinyint NOT NULL COMMENT '1=全场 2=仅课程 3=仅商品',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '发放总量(0=不限)',
  `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `start_time` datetime NOT NULL COMMENT '生效时间',
  `end_time` datetime NOT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL COMMENT '使用时关联订单',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 新增菜单数据
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '优惠券管理', '/coupon', NULL, 'ticket', 6, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES ((SELECT id FROM (SELECT id FROM sys_menu WHERE name='优惠券管理') t), '优惠券列表', '/coupon/list', 'coupon/list', 'list', 1, 1, 'coupon:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '财务管理', '/finance', NULL, 'money', 7, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES ((SELECT id FROM (SELECT id FROM sys_menu WHERE name='财务管理') t), '收入统计', '/finance/income', 'finance/income', 'chart', 1, 1, 'finance:income', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES ((SELECT id FROM (SELECT id FROM sys_menu WHERE name='财务管理') t), '教练结算', '/finance/settlement', 'finance/settlement', 'wallet', 2, 1, 'finance:settlement', 1);
