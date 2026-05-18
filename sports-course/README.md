# KINETIC 体育培训课程服务平台

本科毕业设计项目，面向体育培训业务场景，提供 `管理后台 + 小程序端 + 后端服务` 的一体化课程服务平台。项目支持课程展示、排课预约、订单支付、签到销课、优惠券、教练管理、AI 客服等核心能力。

## 项目亮点

- 基于 `AI 协同开发` 完成从需求分析、功能拆解、方案设计、编码实现到测试验证的完整流程。
- 覆盖课程管理、团课排课、订单流转、退款处理、签到销课等真实业务链路。
- 支持 AI 客服会话、知识库检索、转人工和用户反馈闭环。
- 前后端分离，便于部署、调试和二次扩展。

## 技术栈

- 后端：Spring Boot、MyBatis Plus、Sa-Token、Redisson、RabbitMQ
- 前端管理后台：Vue 3、Vite、Element Plus、Pinia
- 用户端：UniApp、小程序
- 数据库：MySQL

## 核心功能

- 课程管理：私教课、团课、课程分类、课程详情
- 排课预约：团课场次管理、预约下单、余位控制
- 订单中心：支付、取消、退款、订单详情
- 用户中心：个人资料、课包、优惠券、签到记录
- 教练与营销：教练管理、轮播图、优惠券
- AI 客服：智能问答、会话管理、转人工、反馈统计

## 系统架构

- `shop-back-end`
  - `kinetic-sports-api`：小程序接口
  - `kinetic-sports-admin`：管理后台接口
  - `kinetic-sports-service`：核心业务逻辑
  - `kinetic-sports-bean`：实体、DTO、VO
  - `kinetic-sports-common`：通用配置与工具类
  - `kinetic-sports-security`：权限与认证
- `sports-admin-web`：后台管理前端
- `sports-custom-mini`：小程序前端

## 快速启动

1. 创建数据库 `kinetic_sports_course`
2. 导入 `shop-back-end/db/kinetic_sports_course.sql`
3. 启动后端 API 与管理端服务
4. 启动 `sports-admin-web`
5. 启动 `sports-custom-mini`

> 具体端口与初始化说明可参考 `COURSE_VERSION_SETUP.md`


