-- =============================================
-- KINETIC 测试数据清理脚本
-- =============================================

USE `kinetic_sports`;

-- 1. 删除 SKU 数据 (这些数据依赖于 prod_id)
-- 我们通过匹配 prod 表中的商品名称前缀来精准删除，防止误删其他数据
DELETE FROM `sku` 
WHERE `prod_id` IN (
    SELECT `prod_id` FROM `prod` WHERE `name` LIKE 'KINETIC %'
);

-- 2. 删除商品数据 (prod)
-- 删除所有名称以 'KINETIC ' 开头的商品
DELETE FROM `prod` WHERE `name` LIKE 'KINETIC %';

-- 3. 重置商品分类图标 (可选)
-- 如果你想把之前 UPDATE 的图标改回空或者默认值，可以执行以下语句
UPDATE `prod_category` SET `icon` = NULL WHERE `name` IN ('服装', '器械', '护具', '补剂');

-- 4. 重置自增 ID (可选)
-- 如果你希望删除后，下次插入数据的 ID 继续从之前的序号开始（且表已清空或处于开发阶段）
-- 注意：如果表中有其他正式数据，请慎用以下语句
-- ALTER TABLE `prod` AUTO_INCREMENT = 1;
-- ALTER TABLE `sku` AUTO_INCREMENT = 1;

-- =============================================
-- 清理完成
-- =============================================