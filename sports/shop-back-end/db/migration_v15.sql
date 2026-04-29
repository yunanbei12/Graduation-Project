CREATE TABLE IF NOT EXISTS `course_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '地点名称',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '地点主图',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `description` varchar(1000) DEFAULT NULL COMMENT '地点介绍',
  `sort` int DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_location_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团课地点表';

ALTER TABLE `course`
  ADD COLUMN `location_id` bigint DEFAULT NULL COMMENT '团课地点ID' AFTER `location_image`;

ALTER TABLE `course`
  ADD KEY `idx_location_id` (`location_id`);

INSERT INTO `course_location` (`name`, `cover_image`, `status`, `sort`)
SELECT source.loc, MAX(source.cover_image), 1, 0
FROM (
  SELECT `location` AS loc, `location_image` AS cover_image
  FROM `course`
  WHERE `type` = 2 AND `location` IS NOT NULL AND `location` <> ''
  UNION ALL
  SELECT `location` AS loc, NULL AS cover_image
  FROM `course_schedule`
  WHERE `location` IS NOT NULL AND `location` <> ''
) source
WHERE source.loc IS NOT NULL AND source.loc <> ''
  AND NOT EXISTS (
    SELECT 1 FROM `course_location` cl WHERE cl.`name` = source.loc
  )
GROUP BY source.loc;

UPDATE `course` c
LEFT JOIN `course_location` cl ON cl.`name` = c.`location`
SET c.`location_id` = cl.`id`
WHERE c.`type` = 2 AND c.`location_id` IS NULL AND c.`location` IS NOT NULL AND c.`location` <> '';

UPDATE `course` c
LEFT JOIN (
  SELECT cs.`course_id`, MIN(cs.`id`) AS schedule_id
  FROM `course_schedule` cs
  WHERE cs.`location` IS NOT NULL AND cs.`location` <> ''
  GROUP BY cs.`course_id`
) first_schedule ON first_schedule.`course_id` = c.`id`
LEFT JOIN `course_schedule` cs ON cs.`id` = first_schedule.`schedule_id`
LEFT JOIN `course_location` cl ON cl.`name` = cs.`location`
SET c.`location_id` = cl.`id`,
    c.`location` = IFNULL(c.`location`, cs.`location`)
WHERE c.`type` = 2 AND c.`location_id` IS NULL AND cl.`id` IS NOT NULL;

INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT parent.id, '上课地点', '/course/location', 'course/location', 'category', 3, 1, 'course:location', 1
FROM (SELECT id FROM `sys_menu` WHERE `path` = '/course' AND `is_deleted` = 0 LIMIT 1) parent
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `path` = '/course/location' AND `is_deleted` = 0
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.id
FROM `sys_menu` menu
WHERE menu.`path` = '/course/location'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = menu.`id`
  );
