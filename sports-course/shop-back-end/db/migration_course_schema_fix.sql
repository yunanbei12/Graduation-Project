USE `kinetic_sports_course`;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'start_hour') = 0,
    'ALTER TABLE `course` ADD COLUMN `start_hour` varchar(10) DEFAULT NULL AFTER `min_group_size`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'end_hour') = 0,
    'ALTER TABLE `course` ADD COLUMN `end_hour` varchar(10) DEFAULT NULL AFTER `start_hour`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'location') = 0,
    'ALTER TABLE `course` ADD COLUMN `location` varchar(200) DEFAULT NULL AFTER `end_hour`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'location_image') = 0,
    'ALTER TABLE `course` ADD COLUMN `location_image` varchar(500) DEFAULT NULL AFTER `location`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'location_id') = 0,
    'ALTER TABLE `course` ADD COLUMN `location_id` bigint DEFAULT NULL AFTER `location_image`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course_schedule' AND COLUMN_NAME = 'schedule_date') = 0,
    'ALTER TABLE `course_schedule` ADD COLUMN `schedule_date` date DEFAULT NULL AFTER `coach_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_session' AND COLUMN_NAME = 'guest_token') = 0,
    'ALTER TABLE `ai_session` ADD COLUMN `guest_token` varchar(64) DEFAULT NULL AFTER `user_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

UPDATE `course_schedule`
SET `schedule_date` = DATE(`start_time`)
WHERE `schedule_date` IS NULL AND `start_time` IS NOT NULL;

UPDATE `sys_user`
SET `role_id` = 1
WHERE (`username` = 'admin' OR `id` = 1)
  AND (`role_id` IS NULL OR `role_id` <> 1);

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
