-- =============================================
-- KINETIC 商品测试数据
-- =============================================

USE `kinetic_sports`;

-- =============================================
-- 商品分类 (已存在，这里补充图标)
-- =============================================
UPDATE `prod_category` SET `icon` = 'https://img.zcool.cn/community/01e8d95e3a6f95a801213f26c19c18.png' WHERE `name` = '服装';
UPDATE `prod_category` SET `icon` = 'https://img.zcool.cn/community/016c7a5e3a6f95a801213f269fb7c3.png' WHERE `name` = '器械';
UPDATE `prod_category` SET `icon` = 'https://img.zcool.cn/community/0178655e3a6f96a801213f2608e42d.png' WHERE `name` = '护具';
UPDATE `prod_category` SET `icon` = 'https://img.zcool.cn/community/01e68e5e3a6f96a801213f26cf6f42.png' WHERE `name` = '补剂';

-- =============================================
-- 商品数据
-- =============================================

-- 服装类 (category_id = 1)
INSERT INTO `prod` (`name`, `category_id`, `price`, `original_price`, `pic`, `pics`, `description`, `detail`, `sales`, `status`) VALUES
('KINETIC 专业运动T恤 男款', 1, 129.00, 199.00, 
 'https://gw.alicdn.com/imgextra/i3/656798993/O1CN01pB7vJM1Zs8pZJhZ7g_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i3/656798993/O1CN01pB7vJM1Zs8pZJhZ7g_!!656798993.jpg"]',
 '透气速干面料，适合各类运动场景，吸汗排湿，舒适贴身',
 '<p>【产品特点】</p><p>1. 采用高科技速干面料，快速排汗</p><p>2. 四面弹力设计，运动无束缚</p><p>3. 立体剪裁，贴合身形</p>', 
 328, 1),

('KINETIC 运动紧身裤 女款', 1, 159.00, 259.00,
 'https://gw.alicdn.com/imgextra/i1/656798993/O1CN01mN5bEL1Zs8pTQrX9F_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i1/656798993/O1CN01mN5bEL1Zs8pTQrX9F_!!656798993.jpg"]',
 '高腰提臀设计，塑形显瘦，专业运动瑜伽裤',
 '<p>【产品特点】</p><p>1. 高腰设计，收腹提臀</p><p>2. 蜂窝透气面料，舒适不闷热</p><p>3. 瑜伽、跑步、健身通用</p>',
 456, 1),

('KINETIC 篮球运动套装', 1, 299.00, 459.00,
 'https://gw.alicdn.com/imgextra/i4/656798993/O1CN01vCqXwK1Zs8pVvMhNr_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i4/656798993/O1CN01vCqXwK1Zs8pVvMhNr_!!656798993.jpg"]',
 '专业篮球比赛套装，透气网眼设计，宽松舒适',
 '<p>【产品特点】</p><p>1. 专业比赛级面料</p><p>2. 网眼透气设计</p><p>3. 宽松版型，运动自如</p>',
 189, 1),

('KINETIC 运动卫衣 连帽款', 1, 199.00, 329.00,
 'https://gw.alicdn.com/imgextra/i2/656798993/O1CN01wDxYzM1Zs8pXqKjLp_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i2/656798993/O1CN01wDxYzM1Zs8pXqKjLp_!!656798993.jpg"]',
 '加绒保暖，秋冬运动必备，时尚百搭',
 '<p>【产品特点】</p><p>1. 内里加绒，保暖舒适</p><p>2. 连帽设计，防风保暖</p><p>3. 袋鼠口袋，实用方便</p>',
 267, 1);

-- 器械类 (category_id = 2)
INSERT INTO `prod` (`name`, `category_id`, `price`, `original_price`, `pic`, `pics`, `description`, `detail`, `sales`, `status`) VALUES
('KINETIC 专业瑜伽垫 6mm', 2, 89.00, 139.00,
 'https://gw.alicdn.com/imgextra/i3/656798993/O1CN01yEzWnN1Zs8pZJiWqR_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i3/656798993/O1CN01yEzWnN1Zs8pZJiWqR_!!656798993.jpg"]',
 'NBR环保材质，防滑耐磨，加厚缓冲，瑜伽普拉提必备',
 '<p>【产品特点】</p><p>1. NBR环保材质，无异味</p><p>2. 6mm加厚设计，保护关节</p><p>3. 双面防滑纹理</p>',
 892, 1),

('KINETIC 可调节哑铃套装', 2, 399.00, 599.00,
 'https://gw.alicdn.com/imgextra/i1/656798993/O1CN01zFaXoO1Zs8pTQs0Tq_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i1/656798993/O1CN01zFaXoO1Zs8pTQs0Tq_!!656798993.jpg"]',
 '家用健身哑铃，2-20kg可调节，一对满足全家人需求',
 '<p>【产品特点】</p><p>1. 快速调节重量设计</p><p>2. 2-20kg多档位调节</p><p>3. 包胶设计，不伤地板</p>',
 534, 1),

('KINETIC 弹力带套装 5件套', 2, 59.00, 99.00,
 'https://gw.alicdn.com/imgextra/i2/656798993/O1CN01aGbYpP1Zs8pVvN1Ur_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i2/656798993/O1CN01aGbYpP1Zs8pVvN1Ur_!!656798993.jpg"]',
 '天然乳胶材质，5种阻力等级，全身塑形训练',
 '<p>【产品特点】</p><p>1. 天然乳胶，弹性持久</p><p>2. 5种颜色对应5种阻力</p><p>3. 轻便易携带</p>',
 1256, 1),

('KINETIC 跳绳 专业竞速款', 2, 49.00, 79.00,
 'https://gw.alicdn.com/imgextra/i3/656798993/O1CN01bHcZqQ1Zs8pXqL2Vs_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i3/656798993/O1CN01bHcZqQ1Zs8pXqL2Vs_!!656798993.jpg"]',
 '轴承设计，顺滑不缠绕，可调节长度，燃脂神器',
 '<p>【产品特点】</p><p>1. 高速轴承，顺滑耐用</p><p>2. 钢丝绳芯，不易断裂</p><p>3. 长度可调节</p>',
 2341, 1),

('KINETIC 筋膜枪 深层放松', 2, 299.00, 499.00,
 'https://gw.alicdn.com/imgextra/i4/656798993/O1CN01cIdArR1Zs8pZJj3Wt_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i4/656798993/O1CN01cIdArR1Zs8pZJj3Wt_!!656798993.jpg"]',
 '6档力度调节，4个按摩头，运动后放松必备',
 '<p>【产品特点】</p><p>1. 6档力度可调</p><p>2. 4种专业按摩头</p><p>3. 超长续航4小时</p>',
 678, 1);

-- 护具类 (category_id = 3)
INSERT INTO `prod` (`name`, `category_id`, `price`, `original_price`, `pic`, `pics`, `description`, `detail`, `sales`, `status`) VALUES
('KINETIC 护膝 运动防护', 3, 69.00, 99.00,
 'https://gw.alicdn.com/imgextra/i1/656798993/O1CN01dJeBsS1Zs8pTQt4Uu_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i1/656798993/O1CN01dJeBsS1Zs8pTQt4Uu_!!656798993.jpg"]',
 '加压稳定，硅胶垫圈保护髌骨，适合跑步篮球足球',
 '<p>【产品特点】</p><p>1. 硅胶垫圈，保护髌骨</p><p>2. 加压绑带，稳定支撑</p><p>3. 透气面料，不闷热</p>',
 1567, 1),

('KINETIC 护腕 专业加压', 3, 39.00, 59.00,
 'https://gw.alicdn.com/imgextra/i2/656798993/O1CN01eKfCtT1Zs8pVvO5Vv_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i2/656798993/O1CN01eKfCtT1Zs8pVvO5Vv_!!656798993.jpg"]',
 '高强度魔术贴，加压保护，健身举重必备',
 '<p>【产品特点】</p><p>1. 高强度魔术贴</p><p>2. 弹性加压设计</p><p>3. 拇指固定环</p>',
 892, 1),

('KINETIC 护踝 防扭伤', 3, 59.00, 89.00,
 'https://gw.alicdn.com/imgextra/i3/656798993/O1CN01fLgDuU1Zs8pXqM6Ww_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i3/656798993/O1CN01fLgDuU1Zs8pXqM6Ww_!!656798993.jpg"]',
 '8字绑带设计，稳定支撑，预防运动扭伤',
 '<p>【产品特点】</p><p>1. 8字绑带，稳定支撑</p><p>2. 透气网眼设计</p><p>3. 可调节松紧</p>',
 723, 1),

('KINETIC 运动护腰 举重腰带', 3, 89.00, 139.00,
 'https://gw.alicdn.com/imgextra/i4/656798993/O1CN01gMhEvV1Zs8pZJk7Xx_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i4/656798993/O1CN01gMhEvV1Zs8pZJk7Xx_!!656798993.jpg"]',
 '加厚海绵垫，保护腰椎，深蹲硬拉必备',
 '<p>【产品特点】</p><p>1. 加厚海绵垫</p><p>2. 双排扣设计</p><p>3. 保护腰椎</p>',
 456, 1);

-- 补剂类 (category_id = 4)
INSERT INTO `prod` (`name`, `category_id`, `price`, `original_price`, `pic`, `pics`, `description`, `detail`, `sales`, `status`) VALUES
('KINETIC 乳清蛋白粉 1kg', 4, 199.00, 299.00,
 'https://gw.alicdn.com/imgextra/i1/656798993/O1CN01hNiFwW1Zs8pTQu8Yy_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i1/656798993/O1CN01hNiFwW1Zs8pTQu8Yy_!!656798993.jpg"]',
 '进口乳清蛋白，高蛋白低脂肪，增肌塑形首选',
 '<p>【产品特点】</p><p>1. 进口乳清蛋白</p><p>2. 蛋白质含量>75%</p><p>3. 多种口味可选</p>',
 1234, 1),

('KINETIC 支链氨基酸 BCAA', 4, 129.00, 199.00,
 'https://gw.alicdn.com/imgextra/i2/656798993/O1CN01iOjGxX1Zs8pVvP9Zz_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i2/656798993/O1CN01iOjGxX1Zs8pVvP9Zz_!!656798993.jpg"]',
 '2:1:1黄金配比，促进肌肉恢复，减少运动疲劳',
 '<p>【产品特点】</p><p>1. 2:1:1黄金配比</p><p>2. 促进肌肉恢复</p><p>3. 减少运动疲劳</p>',
 567, 1),

('KINETIC 肌酸 一水肌酸', 4, 89.00, 139.00,
 'https://gw.alicdn.com/imgextra/i3/656798993/O1CN01jPkHyY1Zs8pXqN0A0_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i3/656798993/O1CN01jPkHyY1Zs8pXqN0A0_!!656798993.jpg"]',
 '纯度99%以上，提升爆发力，增加肌肉力量',
 '<p>【产品特点】</p><p>1. 纯度99%以上</p><p>2. 提升爆发力</p><p>3. 增加肌肉力量</p>',
 789, 1),

('KINETIC 运动电解质饮料粉', 4, 49.00, 79.00,
 'https://gw.alicdn.com/imgextra/i4/656798993/O1CN01kQlIzZ1Zs8pZJl1B1_!!656798993.jpg',
 '["https://gw.alicdn.com/imgextra/i4/656798993/O1CN01kQlIzZ1Zs8pZJl1B1_!!656798993.jpg"]',
 '快速补水，补充电解质，运动中随时补充能量',
 '<p>【产品特点】</p><p>1. 快速补水</p><p>2. 补充电解质</p><p>3. 低糖配方</p>',
 2345, 1);

-- =============================================
-- SKU 数据 (为部分商品添加规格)
-- =============================================

-- KINETIC 专业运动T恤 男款 (id=1) - 颜色尺码
INSERT INTO `sku` (`prod_id`, `properties`, `price`, `original_price`, `stocks`, `pic`) VALUES
(1, '颜色:黑色;尺码:M', 129.00, 199.00, 100, NULL),
(1, '颜色:黑色;尺码:L', 129.00, 199.00, 150, NULL),
(1, '颜色:黑色;尺码:XL', 129.00, 199.00, 120, NULL),
(1, '颜色:白色;尺码:M', 129.00, 199.00, 80, NULL),
(1, '颜色:白色;尺码:L', 129.00, 199.00, 100, NULL),
(1, '颜色:白色;尺码:XL', 129.00, 199.00, 60, NULL);

-- KINETIC 运动紧身裤 女款 (id=2) - 尺码
INSERT INTO `sku` (`prod_id`, `properties`, `price`, `original_price`, `stocks`, `pic`) VALUES
(2, '颜色:黑色;尺码:S', 159.00, 259.00, 80, NULL),
(2, '颜色:黑色;尺码:M', 159.00, 259.00, 120, NULL),
(2, '颜色:黑色;尺码:L', 159.00, 259.00, 100, NULL),
(2, '颜色:深灰;尺码:S', 159.00, 259.00, 60, NULL),
(2, '颜色:深灰;尺码:M', 159.00, 259.00, 80, NULL),
(2, '颜色:深灰;尺码:L', 159.00, 259.00, 50, NULL);

-- KINETIC 篮球运动套装 (id=3) - 尺码
INSERT INTO `sku` (`prod_id`, `properties`, `price`, `original_price`, `stocks`, `pic`) VALUES
(3, '颜色:红色;尺码:M', 299.00, 459.00, 50, NULL),
(3, '颜色:红色;尺码:L', 299.00, 459.00, 60, NULL),
(3, '颜色:红色;尺码:XL', 299.00, 459.00, 40, NULL),
(3, '颜色:蓝色;尺码:M', 299.00, 459.00, 50, NULL),
(3, '颜色:蓝色;尺码:L', 299.00, 459.00, 60, NULL),
(3, '颜色:蓝色;尺码:XL', 299.00, 459.00, 40, NULL);

-- KINETIC 可调节哑铃套装 (id=6) - 重量规格
INSERT INTO `sku` (`prod_id`, `properties`, `price`, `original_price`, `stocks`, `pic`) VALUES
(6, '规格:2-12kg单只', 399.00, 599.00, 50, NULL),
(6, '规格:2-20kg单只', 499.00, 699.00, 30, NULL),
(6, '规格:2-12kg一对', 699.00, 999.00, 40, NULL),
(6, '规格:2-20kg一对', 899.00, 1199.00, 25, NULL);
