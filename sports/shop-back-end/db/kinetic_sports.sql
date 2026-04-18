CREATE DATABASE IF NOT EXISTS `kinetic_sports` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `kinetic_sports`;

-- 用户表
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

-- 教练表
CREATE TABLE IF NOT EXISTS `coach` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `english_name` varchar(50) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像(列表卡片展示)',
  `pic` varchar(500) DEFAULT NULL COMMENT '详情图片(详情页大图)',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话(后台展示)',
  `years` int DEFAULT 0 COMMENT '教龄',
  `rating` decimal(2,1) DEFAULT 5.0 COMMENT '评分',
  `bio` text COMMENT '简介',
  `certs` varchar(500) DEFAULT NULL COMMENT '认证，逗号分隔',
  `skills` varchar(500) DEFAULT NULL COMMENT '擅长技能，逗号分隔',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 课程分类
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

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` tinyint NOT NULL COMMENT '1=私教课 2=团课',
  `category_id` bigint DEFAULT NULL,
  `coach_id` bigint DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `pic` varchar(500) DEFAULT NULL COMMENT '主图',
  `pics` varchar(2000) DEFAULT NULL COMMENT '图片列表JSON',
  `description` varchar(500) DEFAULT NULL COMMENT '简短描述',
  `detail` text COMMENT '详细介绍(富文本)',
  `lesson_count` int DEFAULT NULL COMMENT '课程节数(私教课包)',
  `features` varchar(1000) DEFAULT NULL COMMENT '特色标签JSON',
  `is_door_service` tinyint DEFAULT 0 COMMENT '0=否 1=上门授课',
  `validity_days` int DEFAULT 90 COMMENT '课包有效期天数(私教课)',
  `min_group_size` int DEFAULT NULL COMMENT '成团人数(团课)',
  `settle_ratio` decimal(5,4) DEFAULT 0.5000 COMMENT '教练分成比例(0.5=50%)',
  `sales` int DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=下架 1=上架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_coach` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 团课排课
CREATE TABLE IF NOT EXISTS `course_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `coach_id` bigint DEFAULT NULL,
  `start_time` datetime NOT NULL COMMENT '上课开始时间',
  `end_time` datetime NOT NULL COMMENT '上课结束时间',
  `location` varchar(200) DEFAULT NULL COMMENT '上课地点',
  `total_seats` int NOT NULL DEFAULT 0 COMMENT '总名额',
  `enrolled_seats` int NOT NULL DEFAULT 0 COMMENT '已报名人数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已结束 3=已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品分类
CREATE TABLE IF NOT EXISTS `prod_category` (
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

-- 商品表
CREATE TABLE IF NOT EXISTS `prod` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `pic` varchar(500) DEFAULT NULL,
  `pics` varchar(2000) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `detail` text,
  `sales` int DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=下架 1=上架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SKU表
CREATE TABLE IF NOT EXISTS `sku` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prod_id` bigint NOT NULL,
  `properties` varchar(200) DEFAULT NULL COMMENT '规格属性，如: 颜色:黑色,尺码:42',
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `stocks` int NOT NULL DEFAULT 0,
  `pic` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_prod` (`prod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 购物车
CREATE TABLE IF NOT EXISTS `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `prod_id` bigint NOT NULL,
  `sku_id` bigint DEFAULT NULL,
  `qty` int NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `order_type` tinyint NOT NULL COMMENT '1=课程订单 2=商品订单',
  `total_amount` decimal(10,2) NOT NULL,
  `actual_amount` decimal(10,2) NOT NULL,
  `coupon_id` bigint DEFAULT NULL COMMENT '使用的优惠券ID',
  `coupon_amount` decimal(10,2) DEFAULT 0 COMMENT '优惠券抵扣金额',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式(wechat/alipay/cash)',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=待支付 2=已支付 3=待排课/待发货 4=已完成 5=已取消 6=退款中 7=已退款 8=退款驳回',
  `remark` varchar(500) DEFAULT NULL,
  `schedule_id` bigint DEFAULT NULL COMMENT '关联排课ID(团课)',
  `refund_reason` varchar(500) DEFAULT NULL,
  `before_refund_status` tinyint DEFAULT NULL COMMENT '退款申请前的原状态',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单项
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `item_type` tinyint NOT NULL COMMENT '1=课程 2=商品',
  `item_id` bigint NOT NULL COMMENT '课程ID或商品ID',
  `item_name` varchar(200) NOT NULL COMMENT '快照名称',
  `item_pic` varchar(500) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL COMMENT '快照价格',
  `qty` int NOT NULL DEFAULT 1,
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `sku_properties` varchar(200) DEFAULT NULL COMMENT 'SKU规格快照',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 管理员
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

-- 角色
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

-- 菜单
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

-- 角色菜单关联
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

-- 用户课包(私教课购买后生成)
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

-- 消课核销记录
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

-- 教练结算记录
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

-- 优惠券
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

-- 用户优惠券
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

-- 短信验证码日志
CREATE TABLE IF NOT EXISTS `sms_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(20) NOT NULL COMMENT '手机号',
  `mobile_code` varchar(10) NOT NULL COMMENT '验证码',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '0=注册验证 1=绑定手机验证',
  `content` varchar(500) DEFAULT NULL COMMENT '短信内容',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=有效 0=已失效',
  `rec_date` datetime NOT NULL COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_phone_type` (`user_phone`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 客服会话
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

-- AI 客服消息
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

-- AI 知识库
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

-- AI 转人工记录
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

-- AI 评价反馈
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

-- 用户行为埋点
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `item_type` tinyint NOT NULL COMMENT '1=课程 2=商品',
  `item_id` bigint NOT NULL COMMENT '课程ID或商品ID',
  `behavior_type` varchar(32) NOT NULL COMMENT 'view_detail/recommend_click',
  `source_page` varchar(64) DEFAULT NULL COMMENT '来源页面',
  `source_section` varchar(64) DEFAULT NULL COMMENT '来源推荐区块',
  `source_item_type` tinyint DEFAULT NULL COMMENT '来源对象类型',
  `source_item_id` bigint DEFAULT NULL COMMENT '来源对象ID',
  `extra_info` varchar(500) DEFAULT NULL COMMENT '额外信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_create` (`user_id`, `create_time`),
  KEY `idx_item_behavior` (`item_type`, `item_id`, `behavior_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为埋点表';

-- 初始数据
INSERT INTO `sys_user` (`username`, `password`, `nick_name`, `status`) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 1);
INSERT INTO `sys_role` (`name`, `remark`, `status`) VALUES ('超级管理员', '拥有所有权限', 1);

-- 课程分类初始数据
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('篮球', '🏀', 1, 1);
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('足球', '⚽', 2, 1);
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('羽毛球', '🏸', 3, 1);
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('体能', '🏋️', 4, 1);
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('网球', '🎾', 5, 1);
INSERT INTO `course_category` (`name`, `icon`, `sort`, `status`) VALUES ('瑜伽', '🧘', 6, 1);

-- 商品分类初始数据
INSERT INTO `prod_category` (`name`, `icon`, `sort`, `status`) VALUES ('服装', '👕', 1, 1);
INSERT INTO `prod_category` (`name`, `icon`, `sort`, `status`) VALUES ('器械', '🏋️', 2, 1);
INSERT INTO `prod_category` (`name`, `icon`, `sort`, `status`) VALUES ('护具', '🦺', 3, 1);
INSERT INTO `prod_category` (`name`, `icon`, `sort`, `status`) VALUES ('补剂', '💊', 4, 1);

-- AI 客服知识库初始数据
INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`) VALUES ('退款说明', 'refund', '退款,退费,售后', '课程订单在已支付或待排课阶段支持提交退款申请，商品订单也可在订单页填写退款原因后提交，后台会统一审核处理。', 10, 1);
INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`) VALUES ('课程咨询', 'course', '课程,团课,私教,课包', '系统提供私教课包和团课两类课程，私教更适合长期训练规划，团课更适合灵活预约和短期体验。', 9, 1);
INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`) VALUES ('优惠券规则', 'coupon', '优惠券,优惠,折扣', '优惠券分为全场券、课程券和商品券三类，是否可用取决于有效期、使用门槛和订单类型。', 8, 1);
INSERT INTO `ai_knowledge` (`title`, `category`, `keywords`, `content`, `priority`, `status`) VALUES ('账号帮助', 'account', '手机号,绑定,密码,登录', '为了顺利下单和处理售后，建议用户先绑定手机号；资料修改、密码设置和登录相关操作可在个人中心完成。', 7, 1);

-- 菜单初始数据
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '课程管理', '/course', NULL, 'education', 1, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (1, '课程列表', '/course/list', 'course/list', 'list', 1, 1, 'course:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (1, '课程分类', '/course/category', 'course/category', 'category', 2, 1, 'course:category', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '教练管理', '/coach', NULL, 'user', 2, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (4, '教练列表', '/coach/list', 'coach/list', 'list', 1, 1, 'coach:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '排课管理', '/schedule', NULL, 'calendar', 3, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (6, '排课列表', '/schedule/list', 'schedule/list', 'list', 1, 1, 'schedule:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '商城管理', '/prod', NULL, 'shopping', 4, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (8, '商品列表', '/prod/list', 'prod/list', 'list', 1, 1, 'prod:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (8, '商品分类', '/prod/category', 'prod/category', 'category', 2, 1, 'prod:category', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '订单管理', '/order', NULL, 'document', 5, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (11, '课程订单', '/order/course', 'order/course', 'list', 1, 1, 'order:course', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (11, '商品订单', '/order/product', 'order/product', 'list', 2, 1, 'order:product', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '优惠券管理', '/coupon', NULL, 'ticket', 6, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (14, '优惠券列表', '/coupon/list', 'coupon/list', 'list', 1, 1, 'coupon:list', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '财务管理', '/finance', NULL, 'money', 7, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (16, '收入统计', '/finance/income', 'finance/income', 'chart', 1, 1, 'finance:income', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (16, '教练结算', '/finance/settlement', 'finance/settlement', 'wallet', 2, 1, 'finance:settlement', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '系统管理', '/sys', NULL, 'setting', 8, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (0, '推荐分析', '/recommend', NULL, 'chart', 9, 0, NULL, 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES ((SELECT id FROM (SELECT id FROM `sys_menu` WHERE path='/recommend') t), '推荐统计', '/recommend/stats', 'recommend/stats', 'chart', 1, 1, 'recommend:stats', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (19, '管理员管理', '/sys/admin', 'sys/admin', 'user', 1, 1, 'sys:admin', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (19, '角色管理', '/sys/role', 'sys/role', 'peoples', 2, 1, 'sys:role', 1);
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`) VALUES (19, '菜单管理', '/sys/menu', 'sys/menu', 'tree', 3, 1, 'sys:menu', 1);
