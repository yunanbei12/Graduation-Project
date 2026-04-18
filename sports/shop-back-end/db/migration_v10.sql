-- 迁移脚本 v10：
-- 1. 修复菜单 path 与前端路由一致
-- 2. 补充缺失的菜单（营销管理、AI客服、用户管理）
-- 3. 给 admin 用户分配超级管理员角色
-- 4. 为超级管理员角色分配所有菜单权限

-- =============================================
-- 修复已有菜单的 path
-- =============================================
UPDATE `sys_menu` SET `path` = '/course'             WHERE `name` = '课程列表';
UPDATE `sys_menu` SET `path` = '/schedule'           WHERE `name` = '排课列表';
UPDATE `sys_menu` SET `path` = '/coach'              WHERE `name` = '教练列表';
UPDATE `sys_menu` SET `path` = '/prod'               WHERE `name` = '商品列表';
UPDATE `sys_menu` SET `path` = '/order'              WHERE `name` = '课程订单';
UPDATE `sys_menu` SET `path` = '/system/user'        WHERE `name` = '管理员管理';
UPDATE `sys_menu` SET `path` = '/system/role'        WHERE `name` = '角色管理';
UPDATE `sys_menu` SET `path` = '/system/menu'        WHERE `name` = '菜单管理';

-- 优惠券父菜单改为营销管理
UPDATE `sys_menu` SET `name` = '营销管理', `path` = '/marketing', `icon` = 'present' WHERE `name` = '优惠券管理' AND `type` = 0;
UPDATE `sys_menu` SET `path` = '/marketing/coupon'   WHERE `name` = '优惠券列表';

-- 排课管理并入课程管理目录下
UPDATE `sys_menu` SET
  `parent_id` = (SELECT id FROM (SELECT id FROM `sys_menu` WHERE `name` = '课程管理' AND `type` = 0 AND `is_deleted` = 0 LIMIT 1) t),
  `path` = '/schedule',
  `sort` = 3
WHERE `name` = '排课列表';

-- 删除空的排课管理目录（已无子菜单）
UPDATE `sys_menu` SET `is_deleted` = 1 WHERE `name` = '排课管理' AND `type` = 0;

-- =============================================
-- 补充缺失菜单
-- =============================================

-- 课程管理 > 销课管理
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '销课管理', '/course/checkin', 'course/checkin', 'edit-pen', 4, 1, 'course:checkin', 1
FROM `sys_menu` m
WHERE m.`name` = '课程管理' AND m.`type` = 0 AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/course/checkin' AND `is_deleted` = 0);

-- 营销管理 > 轮播图（如果不存在则插入）
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '轮播图管理', '/marketing/banner', 'marketing/banner', 'picture', 1, 1, 'marketing:banner', 1
FROM `sys_menu` m
WHERE m.`path` = '/marketing' AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/marketing/banner' AND `is_deleted` = 0);

-- 营销管理 > 优惠券 sort 调整
UPDATE `sys_menu` SET `sort` = 2 WHERE `path` = '/marketing/coupon';

-- AI客服（目录）
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT 0, 'AI客服', '/ai', NULL, 'chat-dot-round', 8, 0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/ai' AND `is_deleted` = 0);

-- AI客服 > 会话管理
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '会话管理', '/ai/session', 'ai/session', 'list', 1, 1, 'ai:session', 1
FROM `sys_menu` m
WHERE m.`path` = '/ai' AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/ai/session' AND `is_deleted` = 0);

-- AI客服 > 知识库管理
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '知识库管理', '/ai/knowledge', 'ai/knowledge', 'document', 2, 1, 'ai:knowledge', 1
FROM `sys_menu` m
WHERE m.`path` = '/ai' AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/ai/knowledge' AND `is_deleted` = 0);

-- AI客服 > 客服统计
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '客服统计', '/ai/stats', 'ai/stats', 'chart', 3, 1, 'ai:stats', 1
FROM `sys_menu` m
WHERE m.`path` = '/ai' AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/ai/stats' AND `is_deleted` = 0);

-- 系统管理 > 用户管理（小程序用户）
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`, `status`)
SELECT m.id, '用户管理', '/system/members', 'system/members', 'user-filled', 4, 1, 'sys:members', 1
FROM `sys_menu` m
WHERE m.`path` = '/sys' AND m.`is_deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/system/members' AND `is_deleted` = 0);

-- 系统管理目录 path 修正（/sys -> 保持不变，前端菜单用 id 作为 index）

-- =============================================
-- 给 admin 用户分配超级管理员角色
-- =============================================
UPDATE `sys_user` SET `role_id` = 1 WHERE `username` = 'admin';

-- =============================================
-- 为超级管理员角色分配所有菜单权限
-- =============================================
DELETE FROM `sys_role_menu` WHERE `role_id` = 1;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu` WHERE `is_deleted` = 0;
