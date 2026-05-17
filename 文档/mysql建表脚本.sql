SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `sport_edu_mgmt`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `sport_edu_mgmt`;

DROP TABLE IF EXISTS `chat_logs`;
DROP TABLE IF EXISTS `faq_docs`;
DROP TABLE IF EXISTS `user_badges`;
DROP TABLE IF EXISTS `badges`;
DROP TABLE IF EXISTS `growth_timeline`;
DROP TABLE IF EXISTS `student_coach_comments`;
DROP TABLE IF EXISTS `student_skill_records`;
DROP TABLE IF EXISTS `physical_test_records`;
DROP TABLE IF EXISTS `student_profiles`;
DROP TABLE IF EXISTS `moment_comments`;
DROP TABLE IF EXISTS `moment_likes`;
DROP TABLE IF EXISTS `moments`;
DROP TABLE IF EXISTS `course_product_relations`;
DROP TABLE IF EXISTS `package_item_relations`;
DROP TABLE IF EXISTS `product_reviews`;
DROP TABLE IF EXISTS `logistics`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `product_skus`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `product_categories`;
DROP TABLE IF EXISTS `finance_records`;
DROP TABLE IF EXISTS `refund_items`;
DROP TABLE IF EXISTS `refunds`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `recommendation_logs`;
DROP TABLE IF EXISTS `user_coupons`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `group_order_members`;
DROP TABLE IF EXISTS `group_orders`;
DROP TABLE IF EXISTS `lesson_records`;
DROP TABLE IF EXISTS `lessons`;
DROP TABLE IF EXISTS `course_schedules`;
DROP TABLE IF EXISTS `coaches`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `user_profiles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `sys_admin_roles`;
DROP TABLE IF EXISTS `sys_roles`;
DROP TABLE IF EXISTS `sys_admins`;

CREATE TABLE `sys_admins` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `real_name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Admin users';

CREATE TABLE `sys_roles` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `role_code` VARCHAR(50) NOT NULL,
  `role_name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Admin roles';

CREATE TABLE `sys_admin_roles` (
  `id` BIGINT NOT NULL,
  `admin_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Admin role relation';

CREATE TABLE `users` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `openid` VARCHAR(64) NOT NULL,
  `unionid` VARCHAR(64) DEFAULT NULL,
  `nickname` VARCHAR(100) DEFAULT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `real_name` VARCHAR(50) DEFAULT NULL,
  `gender` TINYINT DEFAULT 0,
  `birthday` DATE DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `parent_name` VARCHAR(50) DEFAULT NULL,
  `register_source` TINYINT NOT NULL DEFAULT 1,
  `status` TINYINT NOT NULL DEFAULT 1,
  `last_login_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Mini program users';

CREATE TABLE `user_profiles` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `preferred_type` TINYINT DEFAULT NULL,
  `price_range_min` DECIMAL(10,2) DEFAULT NULL,
  `price_range_max` DECIMAL(10,2) DEFAULT NULL,
  `preferred_location` VARCHAR(100) DEFAULT NULL,
  `sport_preferences` JSON DEFAULT NULL,
  `time_preference` VARCHAR(50) DEFAULT NULL,
  `price_sensitive_level` TINYINT DEFAULT NULL,
  `purchase_count` INT NOT NULL DEFAULT 0,
  `profile_source` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_preferred_type` (`preferred_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User recommendation profiles';

CREATE TABLE `courses` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `course_name` VARCHAR(100) NOT NULL,
  `course_code` VARCHAR(50) DEFAULT NULL,
  `course_type` TINYINT NOT NULL,
  `sport_type` VARCHAR(50) NOT NULL,
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `description` TEXT,
  `detail_images` JSON DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `lesson_count` INT DEFAULT NULL,
  `validity_days` INT DEFAULT NULL,
  `is_door_to_door` TINYINT NOT NULL DEFAULT 0,
  `service_areas` JSON DEFAULT NULL,
  `fixed_schedule_desc` VARCHAR(100) DEFAULT NULL,
  `fixed_location` VARCHAR(100) DEFAULT NULL,
  `max_participants` INT DEFAULT NULL,
  `group_success_count` INT DEFAULT NULL,
  `operation_weight` INT NOT NULL DEFAULT 1,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_type_status` (`course_type`, `status`),
  KEY `idx_sport_type` (`sport_type`),
  KEY `idx_operation_weight` (`operation_weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Course master';

CREATE TABLE `coaches` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `coach_name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `id_card_no` VARCHAR(64) DEFAULT NULL,
  `gender` TINYINT DEFAULT 0,
  `sport_items` JSON DEFAULT NULL,
  `introduction` TEXT,
  `hourly_rate` DECIMAL(10,2) DEFAULT NULL,
  `available_times` JSON DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Coach profiles';

CREATE TABLE `course_schedules` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `course_id` BIGINT NOT NULL,
  `coach_id` BIGINT NOT NULL,
  `schedule_date` DATE NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `location` VARCHAR(100) NOT NULL,
  `capacity` INT NOT NULL,
  `min_group_count` INT NOT NULL,
  `enrolled_count` INT NOT NULL DEFAULT 0,
  `waitlist_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0,
  `cancel_reason` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_date` (`course_id`, `schedule_date`),
  KEY `idx_coach_time` (`coach_id`, `start_time`),
  KEY `idx_status_start_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Course schedules';

CREATE TABLE `lessons` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `course_id` BIGINT NOT NULL,
  `source_order_id` BIGINT NOT NULL,
  `lesson_type` TINYINT NOT NULL,
  `total_count` INT NOT NULL,
  `used_count` INT NOT NULL DEFAULT 0,
  `remaining_count` INT NOT NULL DEFAULT 0,
  `frozen_count` INT NOT NULL DEFAULT 0,
  `purchase_amount` DECIMAL(10,2) NOT NULL,
  `original_amount` DECIMAL(10,2) DEFAULT NULL,
  `effective_time` DATETIME NOT NULL,
  `expire_time` DATETIME DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Lesson accounts';

CREATE TABLE `lesson_records` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `lesson_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `course_id` BIGINT NOT NULL,
  `schedule_id` BIGINT DEFAULT NULL,
  `coach_id` BIGINT DEFAULT NULL,
  `consume_count` INT NOT NULL,
  `remaining_count` INT NOT NULL,
  `consume_type` TINYINT NOT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `operator_id` BIGINT NOT NULL,
  `notify_status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_lesson_id` (`lesson_id`),
  KEY `idx_user_created_at` (`user_id`, `created_at`),
  KEY `idx_schedule_id` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Lesson consume records';

CREATE TABLE `group_orders` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `group_no` VARCHAR(50) NOT NULL,
  `group_type` TINYINT NOT NULL,
  `course_id` BIGINT DEFAULT NULL,
  `schedule_id` BIGINT DEFAULT NULL,
  `initiator_user_id` BIGINT NOT NULL,
  `target_count` INT NOT NULL,
  `current_count` INT NOT NULL DEFAULT 0,
  `group_price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `expire_time` DATETIME NOT NULL,
  `success_time` DATETIME DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_no` (`group_no`),
  KEY `idx_status_expire_time` (`status`, `expire_time`),
  KEY `idx_initiator_user_id` (`initiator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Group orders';

CREATE TABLE `group_order_members` (
  `id` BIGINT NOT NULL,
  `group_order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `join_time` DATETIME NOT NULL,
  `member_role` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_order_id`, `user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Group order members';

CREATE TABLE `coupons` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `coupon_name` VARCHAR(100) NOT NULL,
  `coupon_type` TINYINT NOT NULL,
  `coupon_scope` TINYINT NOT NULL,
  `discount_amount` DECIMAL(10,2) DEFAULT NULL,
  `discount_rate` DECIMAL(5,2) DEFAULT NULL,
  `threshold_amount` DECIMAL(10,2) DEFAULT NULL,
  `total_count` INT NOT NULL DEFAULT 0,
  `remain_count` INT NOT NULL DEFAULT 0,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `stackable` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scope_status` (`coupon_scope`, `status`),
  KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Coupon templates';

CREATE TABLE `user_coupons` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `coupon_id` BIGINT NOT NULL,
  `acquire_type` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `used_order_id` BIGINT DEFAULT NULL,
  `used_time` DATETIME DEFAULT NULL,
  `expire_time` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User coupons';

CREATE TABLE `recommendation_logs` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `scene_code` VARCHAR(50) NOT NULL,
  `item_type` TINYINT NOT NULL,
  `item_id` BIGINT NOT NULL,
  `recommend_score` DECIMAL(8,4) NOT NULL,
  `recommend_reason` VARCHAR(255) DEFAULT NULL,
  `clicked` TINYINT NOT NULL DEFAULT 0,
  `purchased` TINYINT NOT NULL DEFAULT 0,
  `exposed_time` DATETIME NOT NULL,
  `clicked_time` DATETIME DEFAULT NULL,
  `purchased_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_scene` (`user_id`, `scene_code`),
  KEY `idx_item_type_item_id` (`item_type`, `item_id`),
  KEY `idx_exposed_time` (`exposed_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Recommendation logs';

CREATE TABLE `orders` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `order_no` VARCHAR(50) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `order_type` TINYINT NOT NULL,
  `group_order_id` BIGINT DEFAULT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `coupon_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `coupon_id` BIGINT DEFAULT NULL,
  `consignee_name` VARCHAR(50) DEFAULT NULL,
  `consignee_phone` VARCHAR(20) DEFAULT NULL,
  `consignee_address` VARCHAR(255) DEFAULT NULL,
  `pay_status` TINYINT NOT NULL DEFAULT 0,
  `order_status` TINYINT NOT NULL DEFAULT 0,
  `close_time` DATETIME DEFAULT NULL,
  `pay_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_order_status` (`user_id`, `order_status`),
  KEY `idx_pay_status_pay_time` (`pay_status`, `pay_time`),
  KEY `idx_group_order_id` (`group_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Order master';

CREATE TABLE `order_items` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `order_id` BIGINT NOT NULL,
  `item_type` TINYINT NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `sku_id` BIGINT DEFAULT NULL,
  `item_name` VARCHAR(100) NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `fulfillment_status` TINYINT NOT NULL DEFAULT 0,
  `ext_json` JSON DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_item_type_biz_id` (`item_type`, `biz_id`),
  KEY `idx_fulfillment_status` (`fulfillment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Order items';

CREATE TABLE `payments` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `order_id` BIGINT NOT NULL,
  `pay_no` VARCHAR(50) NOT NULL,
  `pay_channel` TINYINT NOT NULL DEFAULT 1,
  `out_trade_no` VARCHAR(64) NOT NULL,
  `third_trade_no` VARCHAR(64) DEFAULT NULL,
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` TINYINT NOT NULL DEFAULT 0,
  `pay_time` DATETIME DEFAULT NULL,
  `callback_time` DATETIME DEFAULT NULL,
  `callback_content` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_third_trade_no` (`third_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Payment records';

CREATE TABLE `refunds` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `refund_no` VARCHAR(50) NOT NULL,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `refund_type` TINYINT NOT NULL,
  `refund_reason_type` TINYINT DEFAULT NULL,
  `refund_reason` VARCHAR(255) DEFAULT NULL,
  `apply_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `approved_amount` DECIMAL(10,2) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `audit_admin_id` BIGINT DEFAULT NULL,
  `audit_time` DATETIME DEFAULT NULL,
  `refund_time` DATETIME DEFAULT NULL,
  `third_refund_no` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_refund_time` (`refund_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refund records';

CREATE TABLE `refund_items` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `refund_id` BIGINT NOT NULL,
  `order_item_id` BIGINT NOT NULL,
  `item_type` TINYINT NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `calc_type` TINYINT NOT NULL,
  `original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `deduct_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `calc_desc` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_refund_id` (`refund_id`),
  KEY `idx_order_item_id` (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refund item details';

CREATE TABLE `finance_records` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `record_type` TINYINT NOT NULL,
  `category` TINYINT NOT NULL,
  `order_id` BIGINT DEFAULT NULL,
  `refund_id` BIGINT DEFAULT NULL,
  `coach_id` BIGINT DEFAULT NULL,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `record_date` DATE NOT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_record_type_date` (`record_type`, `record_date`),
  KEY `idx_category_date` (`category`, `record_date`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Finance records';

CREATE TABLE `product_categories` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `category_name` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product categories';

CREATE TABLE `products` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `product_name` VARCHAR(100) NOT NULL,
  `product_code` VARCHAR(50) DEFAULT NULL,
  `product_type` TINYINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `image_urls` JSON DEFAULT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `sales_count` INT NOT NULL DEFAULT 0,
  `weight` DECIMAL(10,2) DEFAULT NULL,
  `stock_warning` INT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_product_type_status` (`product_type`, `status`),
  KEY `idx_sales_count` (`sales_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Products';

CREATE TABLE `product_skus` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `product_id` BIGINT NOT NULL,
  `sku_no` VARCHAR(50) NOT NULL,
  `spec_name` VARCHAR(50) NOT NULL,
  `spec_value` VARCHAR(100) NOT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `stock` INT NOT NULL DEFAULT 0,
  `sales_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_no` (`sku_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product skus';

CREATE TABLE `cart_items` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `item_type` TINYINT NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `sku_id` BIGINT DEFAULT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `checked` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_type`, `biz_id`, `sku_id`),
  KEY `idx_user_checked` (`user_id`, `checked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shopping cart';

CREATE TABLE `logistics` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `order_id` BIGINT NOT NULL,
  `company_name` VARCHAR(50) NOT NULL,
  `tracking_no` VARCHAR(64) NOT NULL,
  `ship_time` DATETIME DEFAULT NULL,
  `receive_time` DATETIME DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `tracking_snapshot` JSON DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tracking_no` (`tracking_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Logistics records';

CREATE TABLE `product_reviews` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `product_id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `rating` TINYINT NOT NULL,
  `content` VARCHAR(500) DEFAULT NULL,
  `images` JSON DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product reviews';

CREATE TABLE `package_item_relations` (
  `id` BIGINT NOT NULL,
  `package_product_id` BIGINT NOT NULL,
  `item_type` TINYINT NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `sku_id` BIGINT DEFAULT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_package_product_id` (`package_product_id`),
  KEY `idx_item_type_biz_id` (`item_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Package item relations';

CREATE TABLE `course_product_relations` (
  `id` BIGINT NOT NULL,
  `course_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `relation_type` TINYINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_product_type` (`course_id`, `product_id`, `relation_type`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Course product relations';

CREATE TABLE `moments` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `media_type` TINYINT NOT NULL,
  `media_urls` JSON NOT NULL,
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `coach_id` BIGINT DEFAULT NULL,
  `course_id` BIGINT DEFAULT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `tags` JSON DEFAULT NULL,
  `publisher_id` BIGINT NOT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `is_top` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status_is_top_created_at` (`status`, `is_top`, `created_at`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_coach_id` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Moments content';

CREATE TABLE `moment_likes` (
  `id` BIGINT NOT NULL,
  `moment_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_moment_user` (`moment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Moment likes';

CREATE TABLE `moment_comments` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `moment_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `reply_user_id` BIGINT DEFAULT NULL,
  `content` VARCHAR(500) NOT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_moment_parent` (`moment_id`, `parent_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Moment comments';

CREATE TABLE `student_profiles` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `join_date` DATE DEFAULT NULL,
  `current_skill_level` TINYINT DEFAULT NULL,
  `total_class_count` INT NOT NULL DEFAULT 0,
  `total_lesson_hours` INT NOT NULL DEFAULT 0,
  `favorite_course_type` TINYINT DEFAULT NULL,
  `privacy_level` TINYINT NOT NULL DEFAULT 0,
  `report_last_generated_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_current_skill_level` (`current_skill_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Student profiles';

CREATE TABLE `physical_test_records` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `test_date` DATE NOT NULL,
  `height_cm` DECIMAL(5,2) DEFAULT NULL,
  `weight_kg` DECIMAL(5,2) DEFAULT NULL,
  `bmi` DECIMAL(5,2) DEFAULT NULL,
  `body_fat_rate` DECIMAL(5,2) DEFAULT NULL,
  `vital_capacity` INT DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_test_date` (`user_id`, `test_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Physical test records';

CREATE TABLE `student_skill_records` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `sport_type` VARCHAR(50) NOT NULL,
  `skill_level` TINYINT DEFAULT NULL,
  `skill_scores` JSON NOT NULL,
  `evaluate_date` DATE NOT NULL,
  `evaluator_id` BIGINT DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_evaluate_date` (`user_id`, `evaluate_date`),
  KEY `idx_sport_type` (`sport_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Student skill records';

CREATE TABLE `student_coach_comments` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `coach_id` BIGINT DEFAULT NULL,
  `schedule_id` BIGINT DEFAULT NULL,
  `comment_type` TINYINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `comment_date` DATE NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_comment_date` (`user_id`, `comment_date`),
  KEY `idx_coach_id` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Student coach comments';

CREATE TABLE `growth_timeline` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `event_type` TINYINT NOT NULL,
  `event_title` VARCHAR(100) NOT NULL,
  `event_date` DATE NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `media_urls` JSON DEFAULT NULL,
  `source_type` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_event_date` (`user_id`, `event_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Growth timeline';

CREATE TABLE `badges` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `badge_name` VARCHAR(50) NOT NULL,
  `icon_url` VARCHAR(255) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `condition_desc` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Badge definitions';

CREATE TABLE `user_badges` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `badge_id` BIGINT NOT NULL,
  `earned_date` DATE NOT NULL,
  `source_desc` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`),
  KEY `idx_badge_id` (`badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User badges';

CREATE TABLE `faq_docs` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `question` VARCHAR(255) NOT NULL,
  `answer` TEXT NOT NULL,
  `similar_questions` JSON DEFAULT NULL,
  `category` VARCHAR(50) NOT NULL,
  `tags` JSON DEFAULT NULL,
  `related_course_id` BIGINT DEFAULT NULL,
  `priority` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category`, `status`),
  KEY `idx_related_course_id` (`related_course_id`),
  FULLTEXT KEY `fulltext_idx_question_answer` (`question`, `answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='FAQ knowledge base';

CREATE TABLE `chat_logs` (
  `id` BIGINT NOT NULL,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  `institution_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT NOT NULL,
  `session_id` VARCHAR(64) NOT NULL,
  `question` VARCHAR(500) NOT NULL,
  `intent_type` VARCHAR(50) DEFAULT NULL,
  `matched_faq_ids` JSON DEFAULT NULL,
  `answer` TEXT,
  `model_name` VARCHAR(50) DEFAULT NULL,
  `response_time_ms` INT DEFAULT NULL,
  `satisfaction` TINYINT DEFAULT NULL,
  `transfer_to_manual` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT NOT NULL DEFAULT 0,
  `updated_by` BIGINT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_session` (`user_id`, `session_id`),
  KEY `idx_intent_type` (`intent_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI chat logs';

SET FOREIGN_KEY_CHECKS = 1;
