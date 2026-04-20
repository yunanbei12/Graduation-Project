# 体育培训商城系统设计与实现

## 摘要

随着全民健身意识增强，体育培训行业规模持续扩大，传统健身房和培训机构在课程管理、订单处理、用户运营等方面面临效率瓶颈。本文设计并实现了一套面向体育培训场景的多端商城系统，涵盖管理后台、用户小程序和后端服务三个子系统，支持私教课包、团课排期、商品销售、优惠券营销等核心业务，并引入个性化推荐与AI客服模块提升用户体验。

系统后端采用Spring Boot框架，结合MyBatis-Plus实现数据持久化，使用Sa-Token完成认证鉴权，通过RabbitMQ处理订单延迟关单、团课成团检查等异步事件。推荐模块融合用户订单、购物车、签到、浏览行为等多源信号，构建用户画像并计算综合得分，实现课程与商品的个性化排序。AI客服模块采用规则识别与业务数据组装相结合的方式，支持课程推荐、订单查询、课包查询、优惠券咨询等意图识别，可返回业务卡片与操作动作，复杂问题支持转人工处理，后台提供客服工作台实现会话接管与历史追溯。

测试结果表明，系统功能完整，订单状态流转正确，推荐结果与用户偏好匹配度较高，AI客服能够处理常见业务咨询并平滑转接人工。系统采用模块化单体架构，结构清晰，适合中小型体育培训机构的业务场景。

**关键词：** 体育培训；商城系统；个性化推荐；AI客服；Spring Boot

---

## Abstract

With the growing awareness of fitness, the sports training industry continues to expand. Traditional gyms and training institutions face efficiency bottlenecks in course management, order processing, and user operations. This thesis designs and implements a multi-platform commerce system for sports training scenarios, covering three subsystems: admin dashboard, user mini-program, and backend services. The system supports core businesses including private lesson packages, group class scheduling, product sales, and coupon marketing, with personalized recommendation and AI customer service modules to enhance user experience.

The backend adopts Spring Boot framework with MyBatis-Plus for data persistence, Sa-Token for authentication, and RabbitMQ for asynchronous events such as delayed order cancellation and group class formation checks. The recommendation module integrates multi-source signals including user orders, shopping carts, check-ins, and browsing behaviors to build user profiles and calculate comprehensive scores for personalized ranking of courses and products. The AI customer service module combines rule recognition with business data assembly, supporting intent recognition for course recommendation, order inquiry, package query, and coupon consultation. It returns business cards and action buttons, with complex issues supporting handover to human agents. The admin dashboard provides a customer service workbench for session takeover and history tracing.

Test results show that the system has complete functionality, correct order state transitions, high matching between recommendation results and user preferences, and the AI customer service can handle common business inquiries and smoothly transfer to human agents. The system adopts a modular monolithic architecture with clear structure, suitable for small and medium-sized sports training institutions.

**Keywords:** Sports Training; Commerce System; Personalized Recommendation; AI Customer Service; Spring Boot

---

## 第1章 引言

### 1.1 背景及意义

近年来，国家层面持续出台全民健身相关政策，居民健康意识显著提升，体育培训市场规模稳步增长。根据相关行业报告，2023年中国体育培训市场规模已突破2000亿元，涵盖健身、瑜伽、游泳、球类等多个细分领域。与此同时，传统体育培训机构在数字化运营方面仍存在明显短板：课程排期依赖人工协调，学员课包管理缺乏系统支撑，订单与财务数据分散，用户复购与留存难以有效追踪。

从技术视角看，体育培训业务具有其特殊性。一方面，课程产品分为私教课包与团课两种形态，私教课包涉及课时消耗、有效期管理、教练结算，团课则依赖排课、名额控制、成团逻辑；另一方面，用户决策周期较长，需要多次浏览、试课、比价，对个性化推荐和及时响应有较高要求。这些特点决定了通用的电商系统难以直接复用，需要针对业务场景进行定制设计。

在此背景下，设计一套面向体育培训场景的商城系统，将课程管理、订单处理、用户运营、智能推荐、在线客服等功能整合，对提升培训机构运营效率、改善用户体验具有实际价值。本文所述系统以KINETIC体育培训商城为原型，覆盖从用户浏览、下单、消课到售后的完整业务闭环，并探索推荐算法与AI客服在垂直领域的应用。

### 1.2 国内外研究现状

在电商与O2O领域，推荐系统已成为提升用户转化率的关键技术。早期研究集中于协同过滤算法，包括基于用户的协同过滤（User-CF）和基于物品的协同过滤（Item-CF），通过分析用户-物品交互矩阵预测用户偏好。随着深度学习发展，神经网络协同过滤（NCF）、深度兴趣网络（DIN）等方法被提出，能够捕捉更复杂的非线性特征。然而，这些方法通常依赖大规模用户行为数据，对于中小型垂直平台而言，冷启动问题与数据稀疏性仍是挑战。

在智能客服领域，早期方案以规则引擎和关键词匹配为主，能够处理标准化问题但泛化能力有限。近年来，基于预训练语言模型的对话系统取得突破，如GPT系列、BERT等，在开放域问答中表现优异。但在垂直业务场景中，单纯依赖大模型存在知识边界模糊、业务数据无法实时获取、回复可控性差等问题。因此，"规则+检索+生成"的混合架构成为主流选择，即先识别意图，再查询业务数据或知识库，最后由模型润色输出。

在国内，体育培训行业的数字化程度参差不齐。大型连锁品牌多采用自研或定制系统，中小型机构则依赖Excel、微信群等传统方式管理业务。市场上已有部分SaaS产品提供课程管理、会员系统等功能，但在个性化推荐、智能客服等方面仍处于起步阶段。本文所述系统尝试在这一领域进行探索，将推荐算法与AI客服落地到体育培训场景，为同类项目提供参考。

### 1.3 研究内容与论文结构

本文主要研究内容包括：

一是体育培训商城系统的需求分析与架构设计。通过梳理业务流程，明确课程管理、订单处理、用户运营等功能需求，设计多端协同的系统架构。

二是个性化推荐模块的设计与实现。针对课程与商品两类推荐对象，融合订单、购物车、签到、浏览行为等多源信号，构建用户画像并设计综合评分公式。

三是AI客服模块的设计与实现。采用意图识别、知识库匹配、业务数据组装相结合的方式，支持常见业务咨询，并提供转人工与会话管理机制。

四是系统测试与效果评估。对核心功能模块进行测试，验证订单状态流转、推荐效果、客服回复准确性等。

论文结构安排如下：第2章介绍相关理论与技术支持；第3章进行系统需求分析，包括功能需求、用例模型、类模型与数据库设计；第4章阐述系统设计与实现细节；第5章进行系统测试与结果分析；最后总结全文并展望后续工作。

---

## 第2章 相关理论与技术支持

### 2.1 个性化推荐算法

#### 2.1.1 协同过滤算法

协同过滤是推荐系统中应用最广泛的方法之一，其核心思想是利用用户的历史行为数据预测用户对未知物品的偏好。基于用户的协同过滤（User-CF）通过寻找与目标用户兴趣相似的邻居用户，将邻居用户喜欢而目标用户未接触的物品推荐给目标用户。基于物品的协同过滤（Item-CF）则通过计算物品之间的相似度，向用户推荐与其历史偏好物品相似的其他物品。

协同过滤的优势在于无需物品内容信息，仅依赖用户行为即可建模。但其局限性也很明显：新用户或新物品缺乏行为数据时难以推荐（冷启动问题）；用户行为矩阵通常非常稀疏，影响推荐质量；当用户兴趣变化时，模型更新存在滞后。

#### 2.1.2 基于内容的推荐

基于内容的推荐通过分析物品的属性特征，寻找与用户历史偏好相似的物品进行推荐。例如，在课程推荐场景中，可根据课程类别、教练、价格区间等属性计算相似度。该方法不依赖其他用户的行为数据，能够较好地处理冷启动问题，但推荐结果容易局限于用户已知的兴趣范围，缺乏多样性。

#### 2.1.3 混合推荐策略

实际系统中，单一推荐方法往往难以满足需求。混合推荐策略将多种方法结合，例如先用协同过滤生成候选集，再用基于内容的方法进行精排，或根据用户活跃度动态选择推荐策略。本文所述系统采用混合策略，将行为相似度、内容相似度、热门度、可约性等多个维度融合，通过加权公式计算综合得分。

### 2.2 意图识别与对话系统

#### 2.2.1 意图识别

意图识别是对话系统的核心组件，其任务是将用户输入的自然语言映射到预定义的意图类别。传统方法包括基于规则的关键词匹配、基于机器学习的分类模型（如SVM、随机森林）。深度学习方法则利用循环神经网络（RNN）、卷积神经网络（CNN）或预训练语言模型（如BERT）进行文本表示，再通过分类层输出意图概率。

在垂直领域对话系统中，意图类别通常与业务场景紧密相关。例如，体育培训商城的意图包括课程推荐、订单查询、课包查询、优惠券咨询、退款帮助、转人工等。由于类别数量有限且边界相对清晰，基于规则与关键词匹配的方法仍具有实用价值，且可解释性强、调试成本低。

#### 2.2.2 槽位填充与对话管理

槽位填充是从用户输入中提取关键参数的过程，例如"我想查上周的订单"中提取时间范围。对话管理则负责根据当前意图和槽位状态决定系统回复或执行动作。在任务型对话系统中，通常采用状态机或帧结构管理对话流程。

本文所述AI客服模块采用简化的对话管理策略：识别意图后直接查询业务数据并组装回复，不维护复杂的对话状态。对于需要进一步澄清的场景，通过追问或提供选项引导用户。

### 2.3 系统开发技术

#### 2.3.1 前端相关技术

管理后台前端采用Vue 3框架，配合Element Plus组件库构建用户界面。Vue 3引入Composition API，支持更灵活的代码组织方式，便于逻辑复用。Element Plus提供了丰富的企业级UI组件，如表格、表单、对话框、标签页等，能够快速搭建后台管理界面。状态管理使用Pinia，相比Vuex具有更简洁的API和更好的TypeScript支持。

小程序端采用uni-app框架开发，基于Vue 3语法编写页面。uni-app支持一次开发编译到多个平台，包括微信小程序、支付宝小程序、H5等。项目中主要针对微信小程序进行适配，使用uni.request封装网络请求，使用uni.setStorageSync管理本地缓存。

#### 2.3.2 后端相关技术

后端采用Java 17与Spring Boot 4.0框架。Spring Boot通过自动配置和起步依赖简化了项目搭建，内嵌Tomcat服务器便于快速启动。数据持久化使用MyBatis-Plus，在MyBatis基础上提供了通用CRUD操作、条件构造器、分页插件等功能，减少样板代码。

认证鉴权使用Sa-Token框架，相比Spring Security具有更简洁的API和更低的接入成本。Sa-Token支持多种登录模式、权限验证、会话管理等功能，通过拦截器统一处理token校验。

消息队列使用RabbitMQ，用于处理订单延迟关单、团课成团检查等异步事件。通过延迟队列实现订单创建后30分钟自动取消未支付订单，通过定时任务检查团课是否达到成团人数。

缓存使用Redis，存储用户会话信息、推荐结果缓存、分布式锁等。在订单创建时使用Redisson分布式锁保护库存与排课名额，防止超卖。

### 2.4 本章小结

本章介绍了个性化推荐算法、意图识别与对话系统的基本原理，以及系统开发所采用的前后端技术栈。这些理论与技术为后续章节的系统设计与实现提供了基础支撑。

---

## 第3章 体育培训商城系统需求分析

### 3.1 需求概述

体育培训商城系统面向三类用户角色：学员用户、运营管理人员、客服人员。学员用户通过小程序端浏览课程与商品、下单购买、查看订单与课包、咨询客服；运营管理人员通过后台管理课程、商品、排课、订单、营销活动等；客服人员通过后台工作台处理用户咨询、回复消息、管理会话状态。

系统需支持两类课程产品：私教课包与团课。私教课包是学员购买后获得一定数量的课时，可在有效期内预约教练上课，系统需记录课时消耗、支持消课与标记缺勤。团课是固定时间、固定地点的集体课程，学员报名后占用名额，系统需管理排课、名额控制、成团逻辑。

商城模块需支持商品展示、分类浏览、购物车、订单创建与支付、收货地址管理。订单模块需处理课程订单与商品订单两种类型，支持优惠券使用、退款申请与审核。

推荐模块需根据用户行为数据，在首页、课程页、商品页提供个性化推荐，并给出推荐理由。AI客服模块需识别用户意图，返回业务数据卡片与操作动作，支持转人工与会话管理。

### 3.2 系统功能需求分析

#### 3.2.1 课程管理功能

课程管理功能包括课程信息维护、分类管理、排课管理、消课管理。

课程信息维护支持创建、编辑、上下架课程，设置课程名称、类型（私教/团课）、分类、教练、价格、原价、图片、描述、特色标签等。私教课包需设置课时数、有效期天数；团课需设置成团人数、上课地点、每日时间范围。

分类管理支持多级分类，每个课程关联一个分类，便于用户按分类筛选浏览。

排课管理针对团课，支持创建排课记录，设置上课日期、时间、总名额、已报名人数。排课状态包括未开始、进行中、已结束、已取消。用户报名团课时需检查排课状态与剩余名额。

消课管理针对私教课包，教练或管理员可为学员消课，记录消课时间、教练、出勤状态。支持标记缺勤并退还课时。当课包所有课时消耗完毕时，自动更新课包状态为已完成，并更新对应订单状态。

#### 3.2.2 订单管理功能

订单管理功能包括订单创建、支付、取消、退款、状态流转。

订单创建时需校验课程或商品的合法性、库存或名额、用户手机号绑定状态。计算订单金额与优惠券抵扣，锁定优惠券，生成订单编号，发布延迟关单事件。

支付成功后，根据订单类型执行不同逻辑：课程订单生成课包或占用团课名额，商品订单扣减SKU库存。核销优惠券，更新销量，发布支付成功事件。

订单取消时释放优惠券，恢复库存或名额。退款申请需记录退款原因，更新订单状态为退款中，管理员审核通过后执行退款并更新状态为已退款，审核驳回则恢复原状态。

订单状态包括：待支付、已支付、待排课/待发货、已完成、已取消、退款中、已退款、退款驳回。状态流转需保证原子性与一致性，使用分布式锁防止并发问题。

#### 3.2.3 个性化推荐功能

个性化推荐功能包括首页推荐、课程推荐、商品推荐、相关课程推荐、相关商品推荐。

推荐模块需融合多源用户行为数据：已支付订单、购物车记录、上课签到、浏览行为、推荐点击行为。根据行为类型与时间衰减计算权重，构建用户画像，包括偏好课程类别、偏好课程类型、偏好教练、平均价格区间等。

推荐得分由行为相似度、内容相似度、热门度、可约性（团课）四个维度加权计算。行为相似度衡量候选物品与用户历史偏好物品的关联程度；内容相似度衡量候选物品与当前浏览物品的属性相似度；热门度基于销量归一化计算；可约性针对团课，根据剩余名额与最近场次时间计算。

推荐结果需附带推荐理由，如"根据你近期课程偏好推荐"、"与你当前查看的同教练课程"、"近期可预约的热门团课"等。

#### 3.2.4 AI客服功能

AI客服功能包括意图识别、业务数据查询、回复生成、转人工、会话管理。

意图识别需支持以下类别：课程推荐、排课咨询、商品推荐、订单查询、退款帮助、课包查询、优惠券咨询、签到咨询、账号帮助、转人工、通用问答。通过关键词匹配与规则判断识别意图，并估计置信度。

业务数据查询根据意图查询对应数据：课程推荐查询热门课程，订单查询获取用户订单列表，课包查询获取用户课包信息，优惠券咨询获取用户可用优惠券等。

回复生成包括文本回复与卡片回复。卡片支持课程卡片、商品卡片、订单卡片、课包卡片、优惠券卡片等，包含图片、标题、副标题、价格、元信息等字段。动作支持跳转导航、转人工等。

转人工功能允许用户申请人工介入，系统创建转人工记录，更新会话状态为待人工。后台客服工作台显示待处理会话，客服可接入并发送人工回复。

会话管理支持会话创建、状态更新、历史查询。会话状态包括：AI处理中、已解决、待人工、本轮已结束。用户再次发送消息时自动开启新会话，历史会话保留供查询。

### 3.3 用例模型

#### 3.3.1 用例图

系统主要用例包括：

**学员用户用例：**
- 浏览课程列表与详情
- 浏览商品列表与详情
- 购买私教课包
- 报名团课
- 管理购物车
- 创建商品订单
- 查看订单列表与详情
- 申请退款
- 查看课包列表与详情
- 查看优惠券
- 咨询AI客服
- 申请转人工
- 管理收货地址
- 管理个人信息

**运营管理人员用例：**
- 管理课程信息
- 管理课程分类
- 管理排课
- 管理商品信息
- 管理商品分类
- 管理订单
- 处理退款
- 管理优惠券
- 管理轮播图
- 查看数据看板
- 管理教练信息
- 查看财务报表

**客服人员用例：**
- 查看会话列表
- 查看会话详情与消息历史
- 接入人工
- 发送人工回复
- 使用快捷回复
- 标记已解决
- 结束本轮咨询
- 查看转人工记录
- 查看用户反馈

#### 3.3.2 用例规约

以"购买私教课包"用例为例：

| 项目 | 内容 |
|------|------|
| 用例名称 | 购买私教课包 |
| 参与者 | 学员用户 |
| 前置条件 | 用户已登录并绑定手机号 |
| 主事件流 | 1. 用户浏览课程详情页<br>2. 用户点击"立即购买"<br>3. 系统跳转订单确认页，显示课程信息、价格、可用优惠券<br>4. 用户选择优惠券（可选）<br>5. 用户点击"确认支付"<br>6. 系统创建订单，锁定优惠券，发布延迟关单事件<br>7. 用户完成支付<br>8. 系统更新订单状态，生成课包，核销优惠券 |
| 备选事件流 | 4a. 用户未绑定手机号：系统提示先绑定手机号<br>6a. 用户取消订单：系统释放优惠券，更新订单状态为已取消<br>7a. 支付超时：延迟任务自动取消订单 |
| 后置条件 | 订单状态为已支付，课包已创建，优惠券已核销 |

以"咨询AI客服"用例为例：

| 项目 | 内容 |
|------|------|
| 用例名称 | 咨询AI客服 |
| 参与者 | 学员用户 |
| 前置条件 | 无 |
| 主事件流 | 1. 用户进入AI客服页面<br>2. 系统显示欢迎消息与快捷问题<br>3. 用户输入问题或点击快捷问题<br>4. 系统识别意图，查询业务数据，生成回复<br>5. 系统返回文本回复与卡片（如有）<br>6. 用户查看回复，可继续提问或点击卡片跳转 |
| 备选事件流 | 4a. 意图识别为"转人工"：系统创建转人工记录，更新会话状态<br>5a. 用户未登录且查询需登录数据：系统提示登录后查看 |
| 后置条件 | 会话已创建，消息已保存 |

### 3.4 基于类的建模

系统核心领域类包括：

**用户域：**
- User：用户实体，包含昵称、头像、手机号、OpenId、登录密码、注册类型等属性
- UserAddress：用户收货地址
- UserCoupon：用户优惠券
- UserCoursePackage：用户课包
- UserBehavior：用户行为记录

**课程域：**
- Course：课程实体，包含名称、类型、分类、教练、价格、课时数、有效期、成团人数等属性
- CourseCategory：课程分类
- CourseSchedule：团课排课
- CourseCheckin：消课记录
- Coach：教练信息

**商城域：**
- Prod：商品实体
- ProdCategory：商品分类
- Sku：商品规格
- Cart：购物车

**订单域：**
- Order：订单实体，包含订单编号、用户ID、订单类型、课程ID、总金额、实付金额、优惠券ID、优惠券抵扣、支付方式、状态等属性
- OrderItem：订单明细

**AI客服域：**
- AiSession：AI会话
- AiMessage：AI消息
- AiKnowledge：AI知识库
- AiHandover：转人工记录
- AiFeedback：用户反馈

### 3.5 类模型

以订单类为例，其属性与方法设计如下：

```java
@TableName("`order`")
public class Order extends BaseEntity {
    private String orderNumber;      // 订单编号
    private Long userId;             // 用户ID
    private Long courseId;           // 关联课程ID
    private Integer orderType;       // 订单类型：1=课程 2=商品
    private BigDecimal totalAmount;  // 订单总金额
    private BigDecimal actualAmount; // 实付金额
    private Long couponId;           // 使用的优惠券ID
    private BigDecimal couponAmount; // 优惠券抵扣金额
    private String paymentMethod;    // 支付方式
    private LocalDateTime paymentTime;   // 支付时间
    private Integer status;          // 订单状态
    private String remark;           // 备注
    private Long scheduleId;         // 团课排课ID
    private String refundReason;     // 退款原因
    private Integer beforeRefundStatus;  // 退款前状态
    private BigDecimal refundAmount; // 退款金额
    private LocalDateTime closeTime; // 关闭时间
    private LocalDateTime finishTime;    // 完成时间
    private String addressSnapshot;  // 收货地址快照
}
```

订单服务接口定义核心方法：

```java
public interface OrderService {
    Order createCourseOrder(Long userId, Order params);
    Order createProdOrder(Long userId, Order params);
    Order payOrder(Long userId, Long orderId, String paymentMethod);
    void cancelOrder(Long userId, Long orderId);
    void requestRefund(Long userId, Long orderId, String reason);
    void approveRefund(Long orderId, boolean approve, String remark);
    Order getOrderDetail(Long userId, Long orderId);
    Page<Order> listUserOrders(Long userId, Integer orderType, Integer status, int page, int size);
}
```

### 3.6 系统E-R图

系统主要实体关系如下：

- User与Order是一对多关系，一个用户可有多个订单
- User与UserCoursePackage是一对多关系，一个用户可有多个课包
- User与UserCoupon是一对多关系，一个用户可有多张优惠券
- User与UserAddress是一对多关系，一个用户可有多个收货地址
- Course与CourseCategory是多对一关系，多个课程属于一个分类
- Course与Coach是多对一关系，多个课程由一个教练负责
- Course与CourseSchedule是一对多关系，一个团课可有多个排课场次
- Order与OrderItem是一对多关系，一个商品订单可有多个明细
- Prod与ProdCategory是多对一关系
- Prod与Sku是一对多关系
- AiSession与AiMessage是一对多关系
- AiSession与AiHandover是一对多关系

### 3.7 本章小结

本章对体育培训商城系统进行了需求分析，明确了课程管理、订单管理、个性化推荐、AI客服等功能需求，通过用例模型描述了用户与系统的交互过程，通过类模型与E-R图展示了系统的静态结构。这些分析为后续章节的设计与实现奠定了基础。

---

## 第4章 系统设计与实现

### 4.1 总体架构设计

系统采用前后端分离架构，包含三个子系统：管理后台（sports-admin-web）、用户小程序（sports-custom-mini）、后端服务（shop-back-end）。

后端服务采用模块化单体架构，划分为以下模块：

- kinetic-sports-admin：后台管理应用入口，端口8085，提供管理端API
- kinetic-sports-api：用户API应用入口，端口8086，提供小程序端API
- kinetic-sports-service：核心业务服务层，包含Service接口与实现、Mapper、MQ消费者、推荐与AI逻辑
- kinetic-sports-bean：实体、DTO、VO定义
- kinetic-sports-common：公共配置、响应封装、异常处理、MyBatis配置、RabbitMQ配置
- kinetic-sports-security：Sa-Token安全模块，分离admin与api的安全配置

系统架构图如下：

```
┌─────────────────┐     ┌─────────────────┐
│  管理后台 Vue3   │     │  小程序 uni-app │
└────────┬────────┘     └────────┬────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  admin:8085     │     │   api:8086      │
└────────┬────────┘     └────────┬────────┘
         │                       │
         └───────────┬───────────┘
                     ▼
         ┌───────────────────────┐
         │   service 业务服务层   │
         └───────────┬───────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    ┌─────────┐ ┌─────────┐ ┌─────────┐
    │  MySQL  │ │  Redis  │ │RabbitMQ │
    └─────────┘ └─────────┘ └─────────┘
```

### 4.2 功能模块设计

#### 4.2.1 课程模块

课程模块负责课程信息管理、分类管理、排课管理、消课管理。

课程控制器（CourseController）提供课程CRUD接口，支持按分类、类型、状态筛选，支持上下架操作。课程详情接口返回课程基本信息、教练信息、排课列表（团课）。

排课控制器（ScheduleController）提供排课CRUD接口，支持按课程ID、日期范围查询。创建排课时校验时间冲突，更新排课时检查是否已有用户报名。

消课控制器（CheckinController）提供消课接口，教练或管理员可为学员消课。消课时校验课包有效性、剩余课时，更新课包已用课时数。当已用课时等于总课时时，更新课包状态为已完成，更新订单状态为已完成。标记缺勤时退还课时，若课包从已完成状态恢复，则同步恢复订单状态。

#### 4.2.2 订单模块

订单模块负责订单创建、支付、取消、退款等核心交易逻辑。

订单创建流程：
1. 校验用户手机号绑定状态
2. 校验课程/商品合法性（状态、库存/名额）
3. 计算订单金额与优惠券抵扣
4. 使用分布式锁保护库存/名额
5. 创建订单记录与订单明细
6. 锁定优惠券
7. 发布延迟关单事件
8. 返回订单信息

支付流程：
1. 校验订单状态为待支付
2. 更新订单状态为已支付，记录支付时间与支付方式
3. 根据订单类型执行：课程订单生成课包或占用团课名额，商品订单扣减SKU库存
4. 核销优惠券
5. 更新销量
6. 发布支付成功事件

退款流程：
1. 用户提交退款申请，记录退款原因
2. 更新订单状态为退款中，记录退款前状态
3. 管理员审核：通过则执行退款，更新状态为已退款，释放优惠券，恢复库存/名额；驳回则恢复原状态

#### 4.2.3 推荐模块

推荐模块负责课程与商品的个性化推荐，核心逻辑在RecommendServiceImpl中实现。

用户画像构建（buildUserProfile方法）：
1. 查询用户已支付订单，按时间衰减计算权重，记录偏好课程ID、分类、类型、教练、价格
2. 查询用户购物车记录，按时间衰减计算权重，记录偏好商品ID、分类、价格
3. 查询用户签到记录，按时间衰减计算权重，记录偏好课程ID、分类、类型、教练
4. 查询用户浏览与点击行为，按行为类型与时间衰减计算权重，记录偏好物品ID、分类

推荐得分计算：
```java
finalScore = 0.45 * behaviorScore 
           + 0.25 * contentScore 
           + 0.20 * popularityScore 
           + 0.10 * availabilityScore;
```

其中：
- behaviorScore：行为相似度得分，衡量候选物品与用户历史偏好物品的关联程度
- contentScore：内容相似度得分，衡量候选物品与当前浏览物品的属性相似度
- popularityScore：热门度得分，基于销量归一化
- availabilityScore：可约性得分，针对团课根据剩余名额与最近场次计算

推荐理由生成根据匹配规则返回对应文案，如"根据你近期课程偏好推荐"、"与你当前查看的同教练课程"等。

#### 4.2.4 AI客服模块

AI客服模块负责用户咨询的意图识别、业务数据查询、回复生成、转人工处理，核心逻辑在AiCustomerServiceImpl中实现。

意图识别（detectIntent方法）通过关键词匹配判断用户意图：
- 包含"推荐"、"适合"等词：课程推荐或商品推荐
- 包含"订单"、"买了"等词：订单查询
- 包含"课包"、"剩几节"等词：课包查询
- 包含"优惠券"、"券"等词：优惠券咨询
- 包含"退款"、"退钱"等词：退款帮助
- 包含"人工"、"客服"等词：转人工

回复生成（buildReply方法）根据意图调用对应方法：
- 课程推荐：查询热门课程，生成课程卡片，可选调用大模型润色回复文本
- 订单查询：查询用户订单列表，生成订单卡片
- 课包查询：查询用户课包信息，生成课包卡片
- 优惠券咨询：查询用户可用优惠券，生成优惠券卡片
- 转人工：创建转人工记录，更新会话状态

会话管理：
- 会话状态：0=AI处理中，1=已解决，2=待人工，3=本轮已结束
- 用户发送消息时，若当前会话已解决或已结束，则创建新会话
- 后台客服可接入人工、发送回复、标记解决、结束咨询

### 4.3 系统数据库设计

系统主要数据表设计如下：

**课程表（course）：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(100) | 课程名称 |
| type | int | 类型：1=私教 2=团课 |
| category_id | bigint | 分类ID |
| coach_id | bigint | 教练ID |
| price | decimal(10,2) | 价格 |
| original_price | decimal(10,2) | 原价 |
| lesson_count | int | 课时数（私教） |
| validity_days | int | 有效期天数（私教） |
| min_group_size | int | 成团人数（团课） |
| location | varchar(200) | 上课地点（团课） |
| sales | int | 销量 |
| status | int | 状态：0=下架 1=上架 |

**订单表（order）：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| order_number | varchar(32) | 订单编号 |
| user_id | bigint | 用户ID |
| order_type | int | 类型：1=课程 2=商品 |
| course_id | bigint | 课程ID |
| total_amount | decimal(10,2) | 总金额 |
| actual_amount | decimal(10,2) | 实付金额 |
| coupon_id | bigint | 优惠券ID |
| coupon_amount | decimal(10,2) | 优惠券抵扣 |
| status | int | 状态 |
| payment_method | varchar(20) | 支付方式 |
| payment_time | datetime | 支付时间 |
| finish_time | datetime | 完成时间 |

**AI会话表（ai_session）：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| title | varchar(100) | 会话标题 |
| last_question | text | 最后问题 |
| last_reply | text | 最后回复 |
| last_intent | varchar(50) | 最后意图 |
| status | int | 状态 |
| need_handover | int | 是否需人工 |
| last_message_time | datetime | 最后消息时间 |

### 4.4 关键功能模块实现

#### 4.4.1 个性化推荐模块实现

推荐模块的核心实现在RecommendServiceImpl类中，以下展示关键代码片段：

用户画像构建：
```java
private UserProfile buildUserProfile(Long userId) {
    UserProfile profile = new UserProfile();
    if (userId == null) return profile;
    
    // 查询已支付订单
    List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
        .eq(Order::getUserId, userId)
        .in(Order::getStatus, Set.of(2, 3, 4, 6))
        .orderByDesc(Order::getCreateTime));
    
    for (Order order : orders) {
        Course course = courseMap.get(order.getCourseId());
        double weight = recencyWeight(order.getCreateTime(), 5D);
        profile.addCourseItemWeight(course.getId(), weight);
        profile.addCourseCategoryWeight(course.getCategoryId(), weight * 0.9D);
        profile.addCourseTypeWeight(course.getType(), weight * 0.6D);
        profile.addCourseCoachWeight(course.getCoachId(), weight * 0.5D);
        profile.addCoursePrice(course.getPrice(), weight);
    }
    // ... 购物车、签到、浏览行为类似处理
    return profile;
}
```

时间衰减权重计算：
```java
private double recencyWeight(LocalDateTime time, double baseWeight) {
    if (time == null) return baseWeight * 0.6D;
    long days = Math.max(0L, Duration.between(time, LocalDateTime.now()).toDays());
    double decay = Math.exp(-days / 30D);
    return baseWeight * Math.max(0.3D, decay);
}
```

推荐得分计算与排序：
```java
for (CandidateScore<Course> candidate : scored) {
    double behaviorScore = maxBehavior <= 0 ? 0D : candidate.behaviorRaw / maxBehavior;
    candidate.finalScore = 0.45D * behaviorScore
            + 0.25D * candidate.contentScore
            + 0.20D * candidate.popularityScore
            + 0.10D * candidate.availabilityScore;
}

scored.sort(Comparator
    .comparingDouble((CandidateScore<Course> it) -> it.finalScore).reversed()
    .thenComparing(it -> it.item.getSales(), Comparator.reverseOrder()));
```

#### 4.4.2 AI客服模块实现

AI客服模块的核心实现在AiCustomerServiceImpl类中，以下展示关键代码片段：

意图识别：
```java
private String detectIntent(String message) {
    if (containsAny(message, "推荐", "适合", "想练", "想学")) {
        return message.contains("团课") || message.contains("团") ? "course_recommend" : "course_recommend";
    }
    if (containsAny(message, "订单", "买了", "下单")) return "order_query";
    if (contains{}
Any(message, "课包", "剩几节", "还有几节")) return "package_query";
    if (containsAny(message, "优惠券", "券", "折扣")) return "coupon_query";
    if (containsAny(message, "退款", "退钱", "退课")) return "refund_help";
    if (containsAny(message, "人工", "客服", "转人工")) return "manual_service";
    return "general_faq";
}
```

课程推荐回复生成：
```java
private ChatDraft buildCourseRecommendReply(String message, Long userId) {
    Integer type = null;
    if (message.contains("团课")) type = 2;
    else if (message.contains("私教") || message.contains("课包")) type = 1;
    
    List<Course> courses = courseService.list(new LambdaQueryWrapper<Course>()
        .eq(Course::getStatus, 1)
        .eq(type != null, Course::getType, type)
        .orderByDesc(Course::getSales)
        .last("limit 3"));
    
    ChatDraft draft = new ChatDraft();
    draft.cards = courses.stream().map(this::toCourseCard).collect(Collectors.toList());
    draft.actions.add(action("navigate", "查看课程", null, COURSE_ROUTE));
    
    String fallback = courses.isEmpty()
        ? "当前可推荐的课程还在整理中，建议稍后再看课程页。"
        : "我先帮你挑了几门热度和口碑都不错的课程，适合先从上面的卡片里看看。";
    applyFinalReply(draft, "course_recommend", message, userId, knowledges, draft.cards, fallback);
    return draft;
}
```

会话状态管理：
```java
@Transactional
public AiChatResponse chat(AiChatRequest request, Long userId) {
    String message = request.getMessage().trim();
    AiSession session = prepareSession(request.getSessionId(), userId, message);
    aiMessageService.save(buildUserMessage(session.getId(), userId, message));
    
    String intent = detectIntent(message);
    ChatDraft draft = buildReply(intent, message, userId, session.getId());
    
    AiChatResponse response = new AiChatResponse();
    response.setSessionId(session.getId());
    response.setReplyText(draft.replyText);
    response.setIntent(intent);
    response.setCards(draft.cards);
    response.setActions(draft.actions);
    response.setNeedHandover(draft.needHandover);
    
    aiMessageService.save(buildAssistantMessage(session.getId(), userId, intent, confidence, draft));
    updateSessionSnapshot(session, message, response);
    return response;
}
```

#### 4.4.3 订单管理模块实现

订单模块的核心实现在OrderServiceImpl类中，以下展示关键代码片段：

订单创建：
```java
@Override
@Transactional
public Order createCourseOrder(Long userId, Order params) {
    User user = requireUser(userId);
    ensurePhoneBound(user);
    
    Course course = courseService.getById(params.getCourseId());
    if (course == null || course.getStatus() != 1) {
        throw new KineticSportsBindException("课程不存在或已下架");
    }
    
    BigDecimal totalAmount = course.getPrice();
    BigDecimal actualAmount = totalAmount;
    BigDecimal couponAmount = BigDecimal.ZERO;
    
    if (params.getCouponId() != null) {
        CouponPricing pricing = calculateCouponPricing(userId, params.getCouponId(), totalAmount, 1);
        couponAmount = pricing.couponAmount;
        actualAmount = pricing.actualAmount;
    }
    
    Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setUserId(userId);
    order.setOrderType(1);
    order.setCourseId(course.getId());
    order.setTotalAmount(totalAmount);
    order.setActualAmount(actualAmount);
    order.setCouponId(params.getCouponId());
    order.setCouponAmount(couponAmount);
    order.setStatus(1);
    save(order);
    
    if (params.getCouponId() != null) {
        lockCouponForOrder(params.getCouponId(), userId, order.getId());
    }
    
    // 发布延迟关单事件
    rabbitTemplate.convertAndSend("order.delay.exchange", "order.close", 
        toLifecycleMessage(order));
    
    return order;
}
```

支付处理：
```java
private void doPay(Order order) {
    order.setStatus(2);
    order.setPaymentTime(LocalDateTime.now());
    updateById(order);
    
    if (Objects.equals(order.getOrderType(), 1)) {
        handleCoursePay(order);
    } else {
        handleProdPay(order);
    }
    
    consumeLockedCoupon(order);
}

private void handleCoursePay(Order order) {
    Course course = courseService.getById(order.getCourseId());
    if (course.getType() == 1) {
        // 私教课包：生成课包
        UserCoursePackage pkg = new UserCoursePackage();
        pkg.setUserId(order.getUserId());
        pkg.setCourseId(course.getId());
        pkg.setOrderId(order.getId());
        pkg.setTotalLessons(course.getLessonCount());
        pkg.setUsedLessons(0);
        pkg.setStatus(1);
        pkg.setExpireTime(LocalDateTime.now().plusDays(course.getValidityDays()));
        userCoursePackageService.save(pkg);
        
        order.setStatus(3);
        updateById(order);
    } else {
        // 团课：占用名额
        CourseSchedule schedule = courseScheduleService.getById(order.getScheduleId());
        schedule.setEnrolledSeats(schedule.getEnrolledSeats() + 1);
        courseScheduleService.updateById(schedule);
        
        order.setStatus(3);
        updateById(order);
        
        publishGroupCheckEventIfAbsent(schedule.getId());
    }
}
```

#### 4.4.4 用户信息管理模块实现

用户信息管理包括用户注册、登录、信息修改、手机号绑定等功能。

用户登录支持三种方式：微信登录、手机号密码登录、短信验证码登录。微信登录通过code换取openId，若用户不存在则自动创建。手机号密码登录使用BCrypt校验密码。短信验证码登录验证通过后自动创建用户（若不存在）。

手机号绑定：
```java
@Override
public void bindPhone(Long userId, String phone, String code) {
    User user = getById(userId);
    if (user == null) {
        throw new KineticSportsBindException("用户不存在");
    }
    if (StringUtils.hasText(user.getPhone())) {
        throw new KineticSportsBindException("已绑定手机号");
    }
    
    // 验证短信验证码
    String cachedCode = redisTemplate.opsForValue().get("sms:" + phone);
    if (!Objects.equals(code, cachedCode)) {
        throw new KineticSportsBindException("验证码错误");
    }
    
    user.setPhone(phone);
    updateById(user);
    
    redisTemplate.delete("sms:" + phone);
}
```

### 4.5 本章小结

本章详细阐述了系统的总体架构、功能模块设计、数据库设计以及关键功能模块的实现。系统采用模块化单体架构，前后端分离，后端划分为admin、api、service、bean、common、security等模块。推荐模块融合多源用户行为数据，通过加权公式计算综合得分；AI客服模块采用意图识别与业务数据组装相结合的方式，支持常见业务咨询与转人工；订单模块处理课程订单与商品订单的创建、支付、退款等核心交易逻辑。

---

## 第5章 系统测试

### 5.1 系统功能测试

#### 5.1.1 个性化推荐模块测试

**测试用例1：首页推荐**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证首页推荐返回课程与商品列表 |
| 前置条件 | 用户已登录，存在课程与商品数据 |
| 测试步骤 | 1. 调用首页推荐接口<br>2. 检查返回的课程列表与商品列表 |
| 预期结果 | 返回课程列表与商品列表，每个物品包含id、名称、图片、价格、推荐理由 |
| 实际结果 | 返回6门课程与6件商品，推荐理由正确显示 |
| 测试结论 | 通过 |

**测试用例2：课程推荐（有用户行为）**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证推荐结果与用户历史偏好相关 |
| 前置条件 | 用户已购买瑜伽类课程，已签到私教课 |
| 测试步骤 | 1. 调用课程推荐接口<br>2. 检查推荐结果中瑜伽类课程排名 |
| 预期结果 | 瑜伽类课程排名靠前，推荐理由包含"根据你近期课程偏好推荐" |
| 实际结果 | 瑜伽类课程排名前3，推荐理由正确 |
| 测试结论 | 通过 |

**测试用例3：相关课程推荐**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证相关课程推荐与当前课程属性相似 |
| 前置条件 | 存在多门课程，部分课程属于同一分类或同一教练 |
| 测试步骤 | 1. 调用相关课程推荐接口，传入当前课程ID<br>2. 检查推荐结果 |
| 预期结果 | 返回与当前课程同分类或同教练的课程，推荐理由正确 |
| 实际结果 | 返回同分类课程，推荐理由为"与你当前查看的同类课程" |
| 测试结论 | 通过 |

#### 5.1.2 AI客服模块测试

**测试用例1：课程推荐意图**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证AI客服能识别课程推荐意图并返回课程卡片 |
| 前置条件 | 存在课程数据 |
| 测试步骤 | 1. 发送消息"推荐适合我的课程"<br>2. 检查返回的意图、回复文本、卡片 |
| 预期结果 | 意图为course_recommend，返回课程卡片，回复文本引导用户选择 |
| 实际结果 | 意图正确，返回3张课程卡片，回复文本自然流畅 |
| 测试结论 | 通过 |

**测试用例2：订单查询意图**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证AI客服能查询用户订单并返回订单卡片 |
| 前置条件 | 用户已登录，存在订单数据 |
| 测试步骤 | 1. 发送消息"我的订单怎么样了"<br>2. 检查返回的订单卡片 |
| 预期结果 | 意图为order_query，返回用户订单卡片 |
| 实际结果 | 返回用户最近3条订单，卡片信息正确 |
| 测试结论 | 通过 |

**测试用例3：转人工意图**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证转人工功能正常工作 |
| 前置条件 | 用户已登录，存在会话 |
| 测试步骤 | 1. 发送消息"转人工"<br>2. 检查会话状态与转人工记录 |
| 预期结果 | 会话状态更新为待人工，转人工记录创建成功 |
| 实际结果 | 会话状态为2（待人工），转人工记录存在 |
| 测试结论 | 通过 |

**测试用例4：人工回复同步**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证后台人工回复能同步到小程序端 |
| 前置条件 | 会话状态为待人工，后台客服已接入 |
| 测试步骤 | 1. 后台发送人工回复<br>2. 小程序端轮询获取消息 |
| 预期结果 | 小程序端显示人工回复，来源标识为"人工客服" |
| 实际结果 | 小程序端正确显示人工回复，标识正确 |
| 测试结论 | 通过 |

#### 5.1.3 订单管理模块测试

**测试用例1：私教课包订单创建**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证私教课包订单创建流程正确 |
| 前置条件 | 用户已登录并绑定手机号，课程状态为上架 |
| 测试步骤 | 1. 调用创建订单接口<br>2. 检查订单状态、优惠券锁定情况 |
| 预期结果 | 订单创建成功，状态为待支付，优惠券锁定 |
| 实际结果 | 订单创建成功，状态正确，优惠券状态为已锁定 |
| 测试结论 | 通过 |

**测试用例2：订单支付与课包生成**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证支付成功后课包正确生成 |
| 前置条件 | 存在待支付订单 |
| 测试步骤 | 1. 调用支付接口<br>2. 检查订单状态、课包创建情况、优惠券核销 |
| 预期结果 | 订单状态为已支付/待排课，课包创建成功，优惠券已核销 |
| 实际结果 | 订单状态为3，课包已创建，优惠券状态为已使用 |
| 测试结论 | 通过 |

**测试用例3：订单退款**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证退款流程正确 |
| 前置条件 | 存在已支付订单 |
| 测试步骤 | 1. 用户申请退款<br>2. 管理员审核通过<br>3. 检查订单状态、课包状态、优惠券状态 |
| 预期结果 | 订单状态为已退款，课包状态为已退费，优惠券已释放 |
| 实际结果 | 订单状态为7，课包状态为2，优惠券状态为未使用 |
| 测试结论 | 通过 |

#### 5.1.4 用户信息管理模块测试

**测试用例1：用户注册**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证用户注册流程正确 |
| 前置条件 | 手机号未注册 |
| 测试步骤 | 1. 调用注册接口，传入手机号、密码、验证码<br>2. 检查用户创建情况 |
| 预期结果 | 用户创建成功，密码已加密，注册类型为手机号注册 |
| 实际结果 | 用户创建成功，密码为BCrypt加密，registerType为1 |
| 测试结论 | 通过 |

**测试用例2：手机号绑定**

| 项目 | 内容 |
|------|------|
| 测试目的 | 验证手机号绑定流程正确 |
| 前置条件 | 用户已登录，未绑定手机号 |
| 测试步骤 | 1. 调用绑定手机号接口<br>2. 检查用户手机号字段 |
| 预期结果 | 用户手机号更新成功 |
| 实际结果 | 用户手机号已更新 |
| 测试结论 | 通过 |

### 5.2 测试结果总结

通过对个性化推荐、AI客服、订单管理、用户信息管理等模块的功能测试，验证了系统核心功能的正确性。测试结果表明：

一是个性化推荐模块能够根据用户历史行为数据生成推荐结果，推荐理由准确，推荐结果与用户偏好匹配度较高。

二是AI客服模块能够正确识别用户意图，返回业务数据卡片与操作动作，转人工功能正常工作，人工回复能实时同步到小程序端。

三是订单管理模块能够正确处理订单创建、支付、退款等核心交易流程，状态流转正确，优惠券锁定、核销、释放逻辑正确。

四是用户信息管理模块能够正确处理用户注册、登录、手机号绑定等功能，密码加密存储，验证码校验正确。

系统整体功能完整，满足体育培训商城的业务需求。

---

## 总结与展望

本文设计并实现了一套面向体育培训场景的商城系统，涵盖管理后台、用户小程序和后端服务三个子系统。系统支持私教课包与团课两种课程形态，实现了课程管理、订单处理、用户运营等核心业务功能，并引入个性化推荐与AI客服模块提升用户体验。

在推荐模块中，系统融合用户订单、购物车、签到、浏览行为等多源信号，构建用户画像，通过行为相似度、内容相似度、热门度、可约性四个维度加权计算综合得分，实现课程与商品的个性化排序。在AI客服模块中，系统采用意图识别与业务数据组装相结合的方式，支持课程推荐、订单查询、课包查询、优惠券咨询等常见业务咨询，并提供转人工与会话管理机制。

系统采用模块化单体架构，结构清晰，便于维护与扩展。通过功能测试验证了系统核心功能的正确性。

后续工作可从以下方面展开：

一是推荐算法优化。当前推荐模块采用加权公式计算得分，后续可引入机器学习模型，如深度神经网络，捕捉更复杂的用户偏好特征。同时可增加A/B测试框架，评估不同推荐策略的效果。

二是AI客服能力增强。当前意图识别基于关键词匹配，后续可引入预训练语言模型提升意图识别准确率。同时可增加多轮对话管理，支持更复杂的咨询场景。

三是系统性能优化。当前系统在N+1查询、缓存策略等方面仍有优化空间，后续可增加批量查询、缓存预热、异步处理等优化手段。

四是移动端扩展。当前小程序端仅支持微信平台，后续可扩展到支付宝小程序、抖音小程序等平台，覆盖更多用户群体。

---

## 致谢

时光飞逝，四年的大学生活即将画上句点。回首这段旅程，有太多人值得感谢。

感谢我的指导老师，从选题到开题，从设计到实现，每一个环节都给予我耐心的指导和细致的建议。老师严谨的治学态度和深厚的专业功底，让我受益匪浅。

感谢我的室友和同学们，四年来一起熬夜赶作业、一起讨论技术问题、一起吐槽bug，这些日子虽然辛苦，却也充实而温暖。特别感谢在我遇到技术难题时伸出援手的同学们，你们的帮助让我少走了很多弯路。

感谢我的家人，是你们的支持和理解，让我能够安心完成学业。虽然你们可能不太懂我在做什么，但每次电话里的关心和鼓励，都是我前进的动力。

最后，感谢那个在深夜里还在敲代码的自己。虽然这个系统还有很多不完美的地方，但它记录了我这段时间的努力和成长。希望未来能继续保持这份热情，在技术的道路上走得更远。

---

## 参考文献

[1] 项亮. 推荐系统实践[M]. 北京: 人民邮电出版社, 2012.

[2] Koren Y, Bell R, Volinsky C. Matrix factorization techniques for recommender systems[J]. Computer, 2009, 42(8): 30-37.

[3] Covington P, Adams J, Sargin E. Deep neural networks for YouTube recommendations[C]//Proceedings of the 10th ACM conference on recommender systems. 2016: 191-198.

[4] Devlin J, Chang M W, Lee K, et al. BERT: Pre-training of deep bidirectional transformers for language understanding[C]//Proceedings of NAACL-HLT. 2019: 4171-4186.

[5] 陈海波, 李东, 王伟. 基于协同过滤的个性化推荐算法研究综述[J]. 计算机应用研究, 2018, 35(6): 1601-1606.

[6] 刘知远, 林衍凯, 孙茂松. 知识表示学习研究进展[J]. 计算机研究与发展, 2016, 53(2): 247-261.

[7] Spring Boot官方文档[EB/OL]. https://spring.io/projects/spring-boot

[8] MyBatis-Plus官方文档[EB/OL]. https://baomidou.com/

[9] Sa-Token官方文档[EB/OL]. https://sa-token.cc/

[10] uni-app官方文档[EB/OL]. https://uniapp.dcloud.net.cn/

[11] Vue.js官方文档[EB/OL]. https://vuejs.org/

[12] Element Plus官方文档[EB/OL]. https://element-plus.org/

[13] RabbitMQ官方文档[EB/OL]. https://www.rabbitmq.com/

[14] Redis官方文档[EB/OL]. https://redis.io/

[15] 微信小程序开发文档[EB/OL]. https://developers.weixin.qq.com/miniprogram/dev/framework/
