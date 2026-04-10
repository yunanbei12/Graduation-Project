# backend

体育教培机构管理系统后端工程。

当前已完成第 1 周基础骨架：

1. Spring Boot 3 + Java 17 + Maven
2. MySQL / Redis / RabbitMQ 基础配置与本地 `docker-compose`
3. 统一返回结构、全局异常处理、统一安全异常返回
4. JWT 服务骨架
5. 请求链路 `requestId` 与基础访问日志
6. MyBatis-Plus 分页拦截器
7. 健康检查接口
8. 按业务域划分的包结构

当前已完成第 2 周第一批后端接口：

1. 后台登录、当前登录信息、退出登录
2. 小程序 mock 微信登录、当前用户信息、用户资料更新
3. 后台课程列表、新增、详情、编辑、上下架
4. 后台教练列表、新增、编辑、工作量统计
5. 后台排期列表、新增、取消
6. 小程序课程列表、课程详情、课程排期

本地启动：

```bash
mvn spring-boot:run
```

建议先启动依赖服务：

```bash
docker compose up -d
```

默认会自动导入仓库中的建表脚本：

```text
../文档/mysql建表脚本.sql
```

本地开发配置：

1. 默认使用 `dev` 环境
2. 数据库：`sport_edu_mgmt`
3. MySQL：`root / 123456`
4. Redis：`localhost:6379`
5. RabbitMQ：`guest / guest`，控制台 `http://localhost:15672`

健康检查：

```bash
GET /api/open/v1/health
GET /actuator/health
```

默认测试账号：

1. 后台管理员：`admin`
2. 默认密码：`123456`

小程序登录说明：

1. 当前开发阶段未接真实微信登录
2. `POST /api/app/v1/auth/wx-login` 中传入的 `code` 会按 `mock_{code}` 生成 `openid`
3. 首次登录会自动创建用户与默认画像记录
