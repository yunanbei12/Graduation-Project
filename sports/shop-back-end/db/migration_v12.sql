-- 迁移脚本 v12: 课包完成状态支持
-- 说明：私教课包全部消课完成后，课包状态设为3（已完成），对应订单状态也更新为4（已完成）

-- 更新 user_course_package 表的状态字段注释
ALTER TABLE `user_course_package` 
MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '0=已过期 1=正常 2=已退费 3=已完成';

-- 将已用完的课包（used_lessons >= total_lessons）状态从 0 更新为 3
UPDATE `user_course_package` 
SET `status` = 3 
WHERE `status` = 0 
  AND `used_lessons` >= `total_lessons` 
  AND `is_deleted` = 0;

-- 将已完成课包对应的订单状态更新为已完成（如果当前是待排课状态）
UPDATE `order` o
INNER JOIN `user_course_package` p ON o.id = p.order_id
SET o.status = 4, o.finish_time = NOW()
WHERE p.status = 3 
  AND o.status = 3 
  AND o.order_type = 1
  AND o.is_deleted = 0
  AND p.is_deleted = 0;
