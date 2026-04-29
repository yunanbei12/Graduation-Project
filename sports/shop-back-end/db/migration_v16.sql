-- =============================================
-- 迁移脚本：V16
-- 版本：migration_v16
-- 变更日期：2026-04-25
-- 变更说明：
--   1. 为游客会话增加 guest_token，收紧匿名会话访问
-- 执行方式：mysql -u root -p123456 kinetic_sports < migration_v16.sql
-- =============================================

USE `kinetic_sports`;

ALTER TABLE `ai_session`
  ADD COLUMN `guest_token` varchar(64) DEFAULT NULL COMMENT '游客会话令牌' AFTER `user_id`;

ALTER TABLE `ai_session`
  ADD KEY `idx_guest_token` (`guest_token`);
