-- =============================================
-- 迁移脚本：V11
-- 版本：migration_v11
-- 变更日期：2026-04-16
-- 变更说明：
--   1. 新增用户行为埋点表 user_behavior
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v11.sql
-- =============================================

USE `kinetic_sports`;

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

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT 0, '推荐分析', '/recommend', NULL, 'chart', 9, 0, NULL, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `path` = '/recommend' AND `is_deleted` = 0
);

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT parent.id, '推荐统计', '/recommend/stats', 'recommend/stats', 'chart', 1, 1, 'recommend:stats', 1
FROM (SELECT id FROM `sys_menu` WHERE `path` = '/recommend' AND `is_deleted` = 0 LIMIT 1) parent
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `path` = '/recommend/stats' AND `is_deleted` = 0
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.id
FROM `sys_menu` menu
WHERE menu.`path` IN ('/recommend', '/recommend/stats')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = menu.`id`
  );
