-- =============================================
-- 迁移脚本：V6
-- 版本：migration_v6
-- 变更日期：2026-04-13
-- 变更说明：
--   1. 新增 AI 客服相关表：ai_session / ai_message / ai_knowledge / ai_handover / ai_feedback
--   2. 初始化 AI 客服知识库基础数据
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v6.sql
-- =============================================

USE `kinetic_sports`;

CREATE TABLE IF NOT EXISTS `ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `last_question` varchar(500) DEFAULT NULL,
  `last_reply` text,
  `last_intent` varchar(50) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=处理中 1=已解决 2=待人工 3=本轮已结束',
  `need_handover` tinyint NOT NULL DEFAULT 0 COMMENT '0=否 1=是',
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
  `role` varchar(20) NOT NULL COMMENT 'user/assistant',
  `content` text,
  `intent` varchar(50) DEFAULT NULL,
  `confidence` decimal(4,2) DEFAULT NULL,
  `reply_text` text,
  `cards_json` text,
  `actions_json` text,
  `source_type` varchar(20) DEFAULT NULL COMMENT 'rule/model/fallback',
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
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
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
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=待处理 1=已处理',
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
  `rating` tinyint NOT NULL DEFAULT 1 COMMENT '1=有帮助 0=无帮助',
  `comment` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`)
SELECT '退款说明', 'refund', '退款,退费,售后', '课程订单在已支付或待排课阶段支持提交退款申请，商品订单也可在订单页填写退款原因后提交，后台会统一审核处理。', 10, 1
WHERE NOT EXISTS (SELECT 1 FROM `ai_knowledge` WHERE `title` = '退款说明');

INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`)
SELECT '课程咨询', 'course', '课程,团课,私教,课包', '系统提供私教课包和团课两类课程，私教更适合长期训练规划，团课更适合灵活预约和短期体验。', 9, 1
WHERE NOT EXISTS (SELECT 1 FROM `ai_knowledge` WHERE `title` = '课程咨询');

INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`)
SELECT '优惠券规则', 'coupon', '优惠券,优惠,折扣', '优惠券分为全场券、课程券和商品券三类，是否可用取决于有效期、使用门槛和订单类型。', 8, 1
WHERE NOT EXISTS (SELECT 1 FROM `ai_knowledge` WHERE `title` = '优惠券规则');

INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`)
SELECT '账号帮助', 'account', '手机号,绑定,密码,登录', '为了顺利下单和处理售后，建议用户先绑定手机号；资料修改、密码设置和登录相关操作可在个人中心完成。', 7, 1
WHERE NOT EXISTS (SELECT 1 FROM `ai_knowledge` WHERE `title` = '账号帮助');
