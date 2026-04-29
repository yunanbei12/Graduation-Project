-- 版本：migration_v13
-- 说明：团课模板增加上课地点图片字段
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v13.sql

ALTER TABLE `course`
ADD COLUMN `location_image` varchar(255) DEFAULT NULL COMMENT '团课上课地点图片' AFTER `location`;
