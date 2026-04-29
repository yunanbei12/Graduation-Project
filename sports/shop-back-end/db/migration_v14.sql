-- 教练排课看板菜单
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT parent.id, '教练排课看板', '/schedule/board', 'course/coach-board', 'calendar', 5, 1, 'schedule:board', 1
FROM (SELECT id FROM `sys_menu` WHERE `path` = '/course' AND `is_deleted` = 0 LIMIT 1) parent
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `path` = '/schedule/board' AND `is_deleted` = 0
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.id
FROM `sys_menu` menu
WHERE menu.`path` = '/schedule/board'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = menu.`id`
  );
