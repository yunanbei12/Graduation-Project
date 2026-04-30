CREATE DATABASE IF NOT EXISTS `kinetic_sports_course` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `kinetic_sports_course`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `open_id` varchar(100) DEFAULT NULL,
  `login_password` varchar(100) DEFAULT NULL COMMENT 'BCrypt加密密码（账号密码登录）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `coach` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `english_name` varchar(50) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `pic` varchar(500) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `years` int DEFAULT 0,
  `rating` decimal(2,1) DEFAULT 5.0,
  `bio` text,
  `certs` varchar(500) DEFAULT NULL,
  `skills` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `icon` varchar(500) DEFAULT NULL,
  `sort` int DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` tinyint NOT NULL COMMENT '1=私教课 2=团课',
  `category_id` bigint DEFAULT NULL,
  `coach_id` bigint DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `pic` varchar(500) DEFAULT NULL,
  `pics` varchar(2000) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `detail` text,
  `lesson_count` int DEFAULT NULL,
  `features` varchar(1000) DEFAULT NULL,
  `is_door_service` tinyint DEFAULT 0,
  `validity_days` int DEFAULT 90,
  `min_group_size` int DEFAULT NULL,
  `start_hour` varchar(10) DEFAULT NULL,
  `end_hour` varchar(10) DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `location_image` varchar(500) DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  `settle_ratio` decimal(5,4) DEFAULT 0.5000,
  `sales` int DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_coach` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `coach_id` bigint DEFAULT NULL,
  `schedule_date` date DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `location` varchar(200) DEFAULT NULL,
  `total_seats` int NOT NULL DEFAULT 0,
  `enrolled_seats` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已结束 3=已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `cover_image` varchar(500) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT NULL,
  `image_url` varchar(500) NOT NULL,
  `link_url` varchar(500) DEFAULT NULL,
  `link_type` tinyint NOT NULL DEFAULT 1 COMMENT '1=页面 2=小程序 3=H5',
  `sort` int NOT NULL DEFAULT 0,
  `position` tinyint NOT NULL DEFAULT 1 COMMENT '1=首页 2=课程页',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_position_status_sort` (`position`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL,
  `order_type` tinyint NOT NULL COMMENT '课程版固定使用 1=课程订单',
  `total_amount` decimal(10,2) NOT NULL,
  `actual_amount` decimal(10,2) NOT NULL,
  `coupon_id` bigint DEFAULT NULL,
  `coupon_amount` decimal(10,2) DEFAULT 0,
  `payment_method` varchar(20) DEFAULT NULL,
  `payment_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=待支付 2=已支付 3=待排课 4=已完成 5=已取消 6=退款中 7=已退款 8=退款驳回',
  `remark` varchar(500) DEFAULT NULL,
  `schedule_id` bigint DEFAULT NULL,
  `refund_reason` varchar(500) DEFAULT NULL,
  `before_refund_status` tinyint DEFAULT NULL,
  `refund_amount` decimal(10,2) DEFAULT NULL,
  `close_time` datetime DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `item_type` tinyint NOT NULL COMMENT '课程版固定使用 1=课程',
  `item_id` bigint NOT NULL,
  `item_name` varchar(200) NOT NULL,
  `item_pic` varchar(500) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `qty` int NOT NULL DEFAULT 1,
  `sku_id` bigint DEFAULT NULL,
  `sku_properties` varchar(200) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `nick_name` varchar(50) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT 0,
  `name` varchar(50) NOT NULL,
  `path` varchar(200) DEFAULT NULL,
  `component` varchar(200) DEFAULT NULL,
  `icon` varchar(50) DEFAULT NULL,
  `sort` int DEFAULT 0,
  `type` tinyint NOT NULL COMMENT '0=目录 1=菜单 2=按钮',
  `permission` varchar(100) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_course_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `total_lessons` int NOT NULL,
  `used_lessons` int NOT NULL DEFAULT 0,
  `expire_time` datetime NOT NULL,
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
  `package_id` bigint DEFAULT NULL,
  `schedule_id` bigint DEFAULT NULL,
  `coach_id` bigint DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `checkin_time` datetime NOT NULL,
  `checkin_type` tinyint NOT NULL COMMENT '1=私教课消课 2=团课签到',
  `status` tinyint NOT NULL DEFAULT 1,
  `lesson_price` decimal(10,2) DEFAULT NULL,
  `coach_ratio` decimal(5,4) DEFAULT NULL,
  `coach_amount` decimal(10,2) DEFAULT NULL,
  `settle_status` tinyint NOT NULL DEFAULT 0,
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
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `total_lessons` int NOT NULL DEFAULT 0,
  `total_amount` decimal(10,2) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `settle_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_coach` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `type` tinyint NOT NULL,
  `discount` decimal(10,2) DEFAULT NULL,
  `min_amount` decimal(10,2) DEFAULT 0,
  `discount_ratio` decimal(3,2) DEFAULT NULL,
  `scope` tinyint NOT NULL COMMENT '课程版仅使用 1=通用 2=仅课程',
  `total_count` int NOT NULL DEFAULT 0,
  `used_count` int NOT NULL DEFAULT 0,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
  `use_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sms_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(20) NOT NULL,
  `mobile_code` varchar(10) NOT NULL,
  `type` tinyint NOT NULL DEFAULT 0,
  `content` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `rec_date` datetime NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`user_phone`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `guest_token` varchar(64) DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `last_question` varchar(500) DEFAULT NULL,
  `last_reply` text,
  `last_intent` varchar(50) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `need_handover` tinyint NOT NULL DEFAULT 0,
  `last_message_time` datetime DEFAULT NULL,
  `resolved_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `content` text,
  `intent` varchar(50) DEFAULT NULL,
  `confidence` decimal(4,2) DEFAULT NULL,
  `reply_text` text,
  `cards_json` text,
  `actions_json` text,
  `source_type` varchar(20) DEFAULT NULL,
  `hit_rule` tinyint NOT NULL DEFAULT 0,
  `need_handover` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `category` varchar(50) DEFAULT 'faq',
  `keywords` varchar(500) DEFAULT NULL,
  `content` text NOT NULL,
  `priority` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_handover` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `latest_question` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `admin_remark` varchar(500) DEFAULT NULL,
  `handled_by` varchar(50) DEFAULT NULL,
  `handled_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `message_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `rating` tinyint NOT NULL DEFAULT 1,
  `comment` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `item_type` tinyint NOT NULL COMMENT '课程版固定使用 1=课程',
  `item_id` bigint NOT NULL,
  `behavior_type` varchar(32) NOT NULL,
  `source_page` varchar(64) DEFAULT NULL,
  `source_section` varchar(64) DEFAULT NULL,
  `source_item_type` tinyint DEFAULT NULL,
  `source_item_id` bigint DEFAULT NULL,
  `extra_info` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_create` (`user_id`, `create_time`),
  KEY `idx_item_behavior` (`item_type`, `item_id`, `behavior_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `nick_name`, `role_id`, `status`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 1, 1);

INSERT IGNORE INTO `sys_role` (`id`, `name`, `remark`, `status`) VALUES
(1, '超级管理员', '拥有所有权限', 1);

INSERT IGNORE INTO `course_category` (`id`, `name`, `icon`, `sort`, `status`) VALUES
(1, '篮球', '🏀', 1, 1),
(2, '足球', '⚽', 2, 1),
(3, '羽毛球', '🏸', 3, 1),
(4, '体能', '🏋️', 4, 1),
(5, '网球', '🎾', 5, 1),
(6, '瑜伽', '🧘', 6, 1);

INSERT IGNORE INTO `ai_knowledge` (`id`, `title`, `category`, `keywords`, `content`, `priority`, `status`) VALUES
(1, '退款说明', 'refund', '退款,退费,售后', '课程订单在已支付或待排课阶段支持提交退款申请，由后台统一审核处理。', 10, 1),
(2, '课程咨询', 'course', '课程,团课,私教,课包', '系统提供私教课包和团课两类课程，私教更适合长期训练规划，团课更适合灵活预约和短期体验。', 9, 1),
(3, '优惠券规则', 'coupon', '优惠券,优惠,折扣', '课程版支持通用券和课程券两类优惠券，是否可用取决于有效期、使用门槛和订单类型。', 8, 1),
(4, '账号帮助', 'account', '手机号,绑定,密码,登录', '为了顺利预约课程和处理售后，建议用户先绑定手机号；资料修改、密码设置和登录相关操作可在个人中心完成。', 7, 1);

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES
(30, 0, '仪表盘', '/dashboard', 'dashboard/index', 'odometer', 0, 1, 'dashboard:view', 1),
(1, 0, '课程管理', '/course', NULL, 'education', 1, 0, NULL, 1),
(2, 1, '课程列表', '/course', 'course/index', 'list', 1, 1, 'course:list', 1),
(3, 1, '课程分类', '/course/category', 'course/category', 'category', 2, 1, 'course:category', 1),
(4, 0, '教练管理', '/coach', NULL, 'user', 2, 0, NULL, 1),
(5, 4, '教练列表', '/coach', 'coach/index', 'list', 1, 1, 'coach:list', 1),
(6, 0, '排课管理', '/schedule', NULL, 'calendar', 3, 0, NULL, 1),
(7, 6, '排课列表', '/schedule', 'course/schedule', 'list', 1, 1, 'schedule:list', 1),
(8, 0, '订单管理', '/order', NULL, 'document', 4, 0, NULL, 1),
(9, 8, '课程订单', '/order', 'order/index', 'list', 1, 1, 'order:course', 1),
(10, 0, '营销管理', '/marketing', NULL, 'ticket', 5, 0, NULL, 1),
(11, 10, '轮播图管理', '/marketing/banner', 'marketing/banner', 'list', 1, 1, 'marketing:banner', 1),
(12, 0, '财务管理', '/finance', NULL, 'money', 6, 0, NULL, 1),
(13, 12, '收入统计', '/finance/income', 'finance/income', 'chart', 1, 1, 'finance:income', 1),
(14, 12, '教练结算', '/finance/settlement', 'finance/settlement', 'wallet', 2, 1, 'finance:settlement', 1),
(15, 0, '系统管理', '/system', NULL, 'setting', 9, 0, NULL, 1),
(16, 0, '推荐分析', '/recommend', NULL, 'chart', 8, 0, NULL, 1),
(17, 16, '推荐统计', '/recommend/stats', 'recommend/stats', 'chart', 1, 1, 'recommend:stats', 1),
(18, 15, '管理员管理', '/system/user', 'system/user', 'user', 1, 1, 'sys:user', 1),
(19, 15, '角色管理', '/system/role', 'system/role', 'peoples', 2, 1, 'sys:role', 1),
(20, 15, '菜单管理', '/system/menu', 'system/menu', 'tree', 3, 1, 'sys:menu', 1),
(21, 1, '上课地点', '/course/location', 'course/location', 'category', 3, 1, 'course:location', 1),
(22, 6, '教练排课看板', '/schedule/board', 'course/coach-board', 'calendar', 2, 1, 'schedule:board', 1),
(23, 1, '销课管理', '/course/checkin', 'course/checkin', 'list', 4, 1, 'course:checkin', 1),
(24, 10, '优惠券管理', '/marketing/coupon', 'marketing/coupon', 'ticket', 2, 1, 'marketing:coupon', 1),
(25, 15, '用户管理', '/system/members', 'system/members', 'user', 4, 1, 'sys:members', 1),
(26, 0, 'AI管理', '/ai', NULL, 'chat', 7, 0, NULL, 1),
(27, 26, 'AI会话管理', '/ai/session', 'ai/session', 'list', 1, 1, 'ai:session', 1),
(28, 26, 'AI知识库', '/ai/knowledge', 'ai/knowledge', 'document', 2, 1, 'ai:knowledge', 1),
(29, 26, 'AI客服统计', '/ai/stats', 'ai/stats', 'chart', 3, 1, 'ai:stats', 1)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`path` = VALUES(`path`),
`component` = VALUES(`component`),
`icon` = VALUES(`icon`),
`sort` = VALUES(`sort`),
`type` = VALUES(`type`),
`permission` = VALUES(`permission`),
`status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 30),
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
(1, 26), (1, 27), (1, 28), (1, 29);
