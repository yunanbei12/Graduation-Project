# 体育教培机构管理系统 - API 接口设计文档

> 版本：v1.0  
> 日期：2026-04-10  
> 依据文档：《需求分析文档》《系统架构设计文档》《数据库详细设计文档》  
> 接口风格：RESTful API + JSON

---

## 一、文档目标

本文档用于定义体育教培机构管理系统的接口设计规范、接口分组、请求参数与响应结构，作为前后端联调、接口开发、测试验收和论文系统设计章节的统一依据。

目标如下：

1. 明确接口命名规范、鉴权方式、统一返回结构
2. 定义小程序端接口、后台管理接口、第三方回调接口
3. 覆盖 P0/P1 阶段核心业务链路
4. 为后续 Swagger/OpenAPI 文档生成提供基础蓝本

---

## 二、接口设计原则

### 2.1 统一前缀

接口统一采用版本化设计：

1. 小程序端：`/api/app/v1`
2. 后台端：`/api/admin/v1`
3. 第三方回调：`/api/open/v1`

### 2.2 统一数据格式

1. 请求体统一使用 `application/json`
2. 文件上传使用 `multipart/form-data`
3. 响应体统一使用 JSON

### 2.3 统一鉴权方式

1. 小程序端使用 `JWT`
2. 后台端使用 `JWT + RBAC`
3. 回调接口使用签名校验，不走 JWT

### 2.4 统一返回结构

成功返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败返回：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

### 2.5 分页结构

列表接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "pageNo": 1,
    "pageSize": 10,
    "total": 100
  }
}
```

---

## 三、通用规范

### 3.1 请求头规范

小程序端/后台端需要携带：

| Header | 说明 |
|--------|------|
| `Authorization` | `Bearer {token}` |
| `Content-Type` | `application/json` |
| `X-Request-Id` | 可选，请求链路 ID |

### 3.2 时间格式规范

1. 时间字段统一使用 `yyyy-MM-dd HH:mm:ss`
2. 日期字段统一使用 `yyyy-MM-dd`

### 3.3 金额格式规范

1. 接口返回金额单位统一为元
2. 保留两位小数

### 3.4 状态码约定

| code | 含义 |
|------|------|
| `200` | 成功 |
| `40001` | 参数错误 |
| `40002` | 业务校验失败 |
| `40101` | 未登录或 token 失效 |
| `40301` | 无权限 |
| `40401` | 资源不存在 |
| `40901` | 状态冲突，如重复支付、重复参团 |
| `50000` | 系统异常 |

---

## 四、认证与登录接口

### 4.1 小程序端认证

### 4.1.1 微信登录

- 接口：`POST /api/app/v1/auth/wx-login`
- 说明：使用微信 `code` 换取用户登录态
- 是否鉴权：否

请求参数：

```json
{
  "code": "wx-login-code",
  "nickname": "小明家长",
  "avatarUrl": "https://example.com/avatar.png"
}
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "userId": 10001,
    "openid": "oAbCd123",
    "nickname": "小明家长",
    "isNewUser": true
  }
}
```

### 4.1.2 获取当前用户信息

- 接口：`GET /api/app/v1/auth/me`
- 说明：获取当前登录用户资料
- 是否鉴权：是

返回字段：

1. `userId`
2. `nickname`
3. `avatarUrl`
4. `phone`
5. `parentName`
6. `status`

### 4.1.3 更新用户资料

- 接口：`PUT /api/app/v1/auth/profile`
- 是否鉴权：是

请求参数：

```json
{
  "realName": "张三",
  "phone": "13800000000",
  "parentName": "李女士",
  "birthday": "2015-05-01",
  "gender": 1
}
```

### 4.2 后台认证

### 4.2.1 后台登录

- 接口：`POST /api/admin/v1/auth/login`
- 是否鉴权：否

请求参数：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应字段：

1. `token`
2. `adminId`
3. `username`
4. `realName`
5. `roles`

### 4.2.2 获取后台当前登录信息

- 接口：`GET /api/admin/v1/auth/me`
- 是否鉴权：是

### 4.2.3 退出登录

- 接口：`POST /api/admin/v1/auth/logout`
- 是否鉴权：是

---

## 五、小程序端接口设计

### 5.1 首页与推荐模块

### 5.1.1 首页聚合数据

- 接口：`GET /api/app/v1/home/index`
- 说明：首页一次性返回 Banner、推荐课程、热门拼团、推荐商品、精彩瞬间
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `lat` | `decimal` | 否 | 用户纬度 |
| `lng` | `decimal` | 否 | 用户经度 |

响应字段：

1. `banners`
2. `recommendCourses`
3. `hotGroups`
4. `recommendProducts`
5. `moments`
6. `notices`

### 5.1.2 获取推荐课程列表

- 接口：`GET /api/app/v1/recommend/courses`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `scene` | `string` | 是 | 推荐场景，如 `home` |
| `size` | `int` | 否 | 默认 5 |

### 5.1.3 提交兴趣选座

- 接口：`POST /api/app/v1/recommend/preferences`
- 是否鉴权：是

请求参数：

```json
{
  "sportPreferences": ["篮球", "羽毛球"],
  "preferredLocation": "幸福小区",
  "preferredType": 3,
  "priceRangeMin": 1000,
  "priceRangeMax": 2000
}
```

### 5.2 课程模块

### 5.2.1 课程列表

- 接口：`GET /api/app/v1/courses`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNo` | `int` | 否 | 页码 |
| `pageSize` | `int` | 否 | 每页数量 |
| `courseType` | `int` | 否 | 1-私教，2-团课 |
| `sportType` | `string` | 否 | 运动项目 |
| `priceMin` | `decimal` | 否 | 最低价 |
| `priceMax` | `decimal` | 否 | 最高价 |
| `location` | `string` | 否 | 区域 |
| `sortType` | `string` | 否 | `recommend/sales/priceAsc/latest` |

列表项字段：

1. `courseId`
2. `courseName`
3. `courseType`
4. `sportType`
5. `coverUrl`
6. `price`
7. `originalPrice`
8. `lessonCount`
9. `tags`

### 5.2.2 课程详情

- 接口：`GET /api/app/v1/courses/{courseId}`
- 是否鉴权：否

响应字段：

1. 基础信息：`courseId`、`courseName`、`description`
2. 销售信息：`price`、`originalPrice`、`lessonCount`、`validityDays`
3. 服务信息：`isDoorToDoor`、`serviceAreas`
4. 团课信息：`fixedScheduleDesc`、`fixedLocation`、`maxParticipants`
5. 关联商品：`relatedProducts`
6. 推荐拼团：`groupOptions`

### 5.2.3 团课排期列表

- 接口：`GET /api/app/v1/courses/{courseId}/schedules`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dateFrom` | `string` | 否 | 起始日期 |
| `dateTo` | `string` | 否 | 结束日期 |

### 5.3 拼团模块

### 5.3.1 拼团列表

- 接口：`GET /api/app/v1/group-orders`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `groupType` | `int` | 否 | 1-私教课包拼团，2-团课拼团 |
| `status` | `int` | 否 | 0-进行中，1-成功，2-失败 |

### 5.3.2 拼团详情

- 接口：`GET /api/app/v1/group-orders/{groupOrderId}`
- 是否鉴权：否

响应字段：

1. `groupOrderId`
2. `groupNo`
3. `groupType`
4. `courseInfo`
5. `targetCount`
6. `currentCount`
7. `expireTime`
8. `status`
9. `members`
10. `shareInfo`

### 5.3.3 发起拼团

- 接口：`POST /api/app/v1/group-orders`
- 是否鉴权：是

请求参数：

```json
{
  "groupType": 1,
  "courseId": 20001,
  "scheduleId": null,
  "targetCount": 3,
  "couponId": 30001
}
```

响应字段：

1. `groupOrderId`
2. `orderId`
3. `orderNo`
4. `payAmount`

### 5.3.4 参与拼团

- 接口：`POST /api/app/v1/group-orders/{groupOrderId}/join`
- 是否鉴权：是

请求参数：

```json
{
  "couponId": 30001
}
```

### 5.4 订单与支付模块

### 5.4.1 预创建订单

- 接口：`POST /api/app/v1/orders/preview`
- 说明：下单前计算金额、优惠、运费和可用优惠券
- 是否鉴权：是

请求参数：

```json
{
  "items": [
    {
      "itemType": 1,
      "bizId": 20001,
      "skuId": null,
      "quantity": 1
    }
  ],
  "couponId": 30001
}
```

返回字段：

1. `totalAmount`
2. `discountAmount`
3. `payAmount`
4. `freightAmount`
5. `availableCoupons`

### 5.4.2 创建订单

- 接口：`POST /api/app/v1/orders`
- 是否鉴权：是

请求参数：

```json
{
  "orderType": 1,
  "items": [
    {
      "itemType": 1,
      "bizId": 20001,
      "quantity": 1
    }
  ],
  "couponId": 30001,
  "consigneeName": "张三",
  "consigneePhone": "13800000000",
  "consigneeAddress": "福建省厦门市集美区XX路"
}
```

响应字段：

1. `orderId`
2. `orderNo`
3. `payAmount`

### 5.4.3 获取微信支付参数

- 接口：`POST /api/app/v1/orders/{orderId}/pay`
- 是否鉴权：是

响应字段：

1. `timeStamp`
2. `nonceStr`
3. `packageValue`
4. `signType`
5. `paySign`

### 5.4.4 订单列表

- 接口：`GET /api/app/v1/orders`
- 是否鉴权：是

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNo` | `int` | 否 | 页码 |
| `pageSize` | `int` | 否 | 每页数量 |
| `orderStatus` | `int` | 否 | 订单状态 |
| `orderType` | `int` | 否 | 订单类型 |

### 5.4.5 订单详情

- 接口：`GET /api/app/v1/orders/{orderId}`
- 是否鉴权：是

### 5.4.6 取消订单

- 接口：`POST /api/app/v1/orders/{orderId}/cancel`
- 是否鉴权：是

请求参数：

```json
{
  "reason": "暂时不需要"
}
```

### 5.5 退款模块

### 5.5.1 退款预计算

- 接口：`GET /api/app/v1/refunds/preview`
- 是否鉴权：是

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | `long` | 是 | 订单 ID |

返回字段：

1. `refundType`
2. `applyAmount`
3. `calcDetails`
4. `rulesDescription`

### 5.5.2 发起退款申请

- 接口：`POST /api/app/v1/refunds`
- 是否鉴权：是

请求参数：

```json
{
  "orderId": 50001,
  "refundReasonType": 1,
  "refundReason": "时间安排冲突"
}
```

### 5.5.3 退款列表

- 接口：`GET /api/app/v1/refunds`
- 是否鉴权：是

### 5.5.4 退款详情

- 接口：`GET /api/app/v1/refunds/{refundId}`
- 是否鉴权：是

### 5.6 我的课程与销课记录

### 5.6.1 我的课包

- 接口：`GET /api/app/v1/lessons`
- 是否鉴权：是

### 5.6.2 课时详情

- 接口：`GET /api/app/v1/lessons/{lessonId}`
- 是否鉴权：是

### 5.6.3 销课记录列表

- 接口：`GET /api/app/v1/lesson-records`
- 是否鉴权：是

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `lessonId` | `long` | 否 | 课包 ID |
| `pageNo` | `int` | 否 | 页码 |
| `pageSize` | `int` | 否 | 每页数量 |

### 5.7 优惠券模块

### 5.7.1 我的优惠券

- 接口：`GET /api/app/v1/coupons/my`
- 是否鉴权：是

### 5.7.2 领取优惠券

- 接口：`POST /api/app/v1/coupons/{couponId}/receive`
- 是否鉴权：是

### 5.8 商品与电商模块

### 5.8.1 商品列表

- 接口：`GET /api/app/v1/products`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNo` | `int` | 否 | 页码 |
| `pageSize` | `int` | 否 | 每页数量 |
| `categoryId` | `long` | 否 | 分类 ID |
| `keyword` | `string` | 否 | 搜索关键词 |
| `sortType` | `string` | 否 | `sales/new/priceAsc/priceDesc` |

### 5.8.2 商品详情

- 接口：`GET /api/app/v1/products/{productId}`
- 是否鉴权：否

### 5.8.3 购物车列表

- 接口：`GET /api/app/v1/cart`
- 是否鉴权：是

### 5.8.4 加入购物车

- 接口：`POST /api/app/v1/cart/items`
- 是否鉴权：是

请求参数：

```json
{
  "itemType": 2,
  "bizId": 40001,
  "skuId": 41001,
  "quantity": 2
}
```

### 5.8.5 修改购物车数量

- 接口：`PUT /api/app/v1/cart/items/{cartItemId}`
- 是否鉴权：是

### 5.8.6 删除购物车项

- 接口：`DELETE /api/app/v1/cart/items/{cartItemId}`
- 是否鉴权：是

### 5.8.7 确认收货

- 接口：`POST /api/app/v1/orders/{orderId}/confirm-receipt`
- 是否鉴权：是

### 5.9 动态模块

### 5.9.1 动态列表

- 接口：`GET /api/app/v1/moments`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNo` | `int` | 否 | 页码 |
| `pageSize` | `int` | 否 | 每页数量 |

### 5.9.2 动态详情

- 接口：`GET /api/app/v1/moments/{momentId}`
- 是否鉴权：否

### 5.9.3 动态点赞

- 接口：`POST /api/app/v1/moments/{momentId}/like`
- 是否鉴权：是

### 5.9.4 取消点赞

- 接口：`DELETE /api/app/v1/moments/{momentId}/like`
- 是否鉴权：是

### 5.9.5 评论列表

- 接口：`GET /api/app/v1/moments/{momentId}/comments`
- 是否鉴权：否

### 5.9.6 发表评论

- 接口：`POST /api/app/v1/moments/{momentId}/comments`
- 是否鉴权：是

请求参数：

```json
{
  "parentId": 0,
  "replyUserId": null,
  "content": "这节课看起来很有意思"
}
```

### 5.10 AI 客服模块

### 5.10.1 发送问题

- 接口：`POST /api/app/v1/ai/chat`
- 是否鉴权：是

请求参数：

```json
{
  "sessionId": "session-001",
  "question": "私教课怎么收费？"
}
```

响应字段：

1. `sessionId`
2. `intentType`
3. `answer`
4. `matchedFaqs`
5. `transferToManual`

### 5.10.2 常见问题列表

- 接口：`GET /api/app/v1/ai/faqs`
- 是否鉴权：否

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `category` | `string` | 否 | FAQ 分类 |

### 5.10.3 会话记录

- 接口：`GET /api/app/v1/ai/chat-logs`
- 是否鉴权：是

### 5.11 成长档案模块

### 5.11.1 我的成长档案

- 接口：`GET /api/app/v1/growth/profile`
- 是否鉴权：是

响应字段：

1. `basicInfo`
2. `skillRadar`
3. `physicalTrends`
4. `badges`
5. `timeline`
6. `coachComments`
7. `moments`

### 5.11.2 成长时间轴列表

- 接口：`GET /api/app/v1/growth/timeline`
- 是否鉴权：是

### 5.11.3 我的徽章列表

- 接口：`GET /api/app/v1/growth/badges`
- 是否鉴权：是

---

## 六、后台管理接口设计

### 6.1 仪表盘与统计

### 6.1.1 仪表盘概览

- 接口：`GET /api/admin/v1/dashboard/overview`
- 是否鉴权：是

返回字段：

1. `todayIncome`
2. `todayOrderCount`
3. `monthIncome`
4. `monthOrderCount`
5. `activeStudentCount`
6. `groupSuccessRate`
7. `todayLessonConsumeCount`

### 6.1.2 收入趋势

- 接口：`GET /api/admin/v1/dashboard/income-trend`
- 是否鉴权：是

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `rangeType` | `string` | 是 | `7d/30d/month` |

### 6.2 课程管理

### 6.2.1 课程列表

- 接口：`GET /api/admin/v1/courses`
- 是否鉴权：是

### 6.2.2 新增课程

- 接口：`POST /api/admin/v1/courses`
- 是否鉴权：是

### 6.2.3 课程详情

- 接口：`GET /api/admin/v1/courses/{courseId}`
- 是否鉴权：是

### 6.2.4 编辑课程

- 接口：`PUT /api/admin/v1/courses/{courseId}`
- 是否鉴权：是

### 6.2.5 上下架课程

- 接口：`POST /api/admin/v1/courses/{courseId}/status`
- 是否鉴权：是

请求参数：

```json
{
  "status": 1
}
```

### 6.3 学员管理

### 6.3.1 学员列表

- 接口：`GET /api/admin/v1/students`
- 是否鉴权：是

### 6.3.2 学员详情

- 接口：`GET /api/admin/v1/students/{userId}`
- 是否鉴权：是

### 6.3.3 更新学员标签/备注

- 接口：`PUT /api/admin/v1/students/{userId}/profile`
- 是否鉴权：是

### 6.4 教练管理

### 6.4.1 教练列表

- 接口：`GET /api/admin/v1/coaches`
- 是否鉴权：是

### 6.4.2 新增教练

- 接口：`POST /api/admin/v1/coaches`
- 是否鉴权：是

### 6.4.3 编辑教练

- 接口：`PUT /api/admin/v1/coaches/{coachId}`
- 是否鉴权：是

### 6.4.4 教练课时统计

- 接口：`GET /api/admin/v1/coaches/{coachId}/workload`
- 是否鉴权：是

### 6.5 排课与销课管理

### 6.5.1 团课排期列表

- 接口：`GET /api/admin/v1/schedules`
- 是否鉴权：是

### 6.5.2 新增排期

- 接口：`POST /api/admin/v1/schedules`
- 是否鉴权：是

### 6.5.3 取消排期

- 接口：`POST /api/admin/v1/schedules/{scheduleId}/cancel`
- 是否鉴权：是

### 6.5.4 销课

- 接口：`POST /api/admin/v1/lessons/consume`
- 是否鉴权：是

请求参数：

```json
{
  "lessonId": 60001,
  "consumeCount": 1,
  "coachId": 70001,
  "scheduleId": null,
  "note": "篮球运球基础训练"
}
```

### 6.5.5 团课批量签到销课

- 接口：`POST /api/admin/v1/schedules/{scheduleId}/sign-in`
- 是否鉴权：是

请求参数：

```json
{
  "userIds": [10001, 10002, 10003]
}
```

### 6.6 拼团与优惠券管理

### 6.6.1 拼团列表

- 接口：`GET /api/admin/v1/group-orders`
- 是否鉴权：是

### 6.6.2 拼团详情

- 接口：`GET /api/admin/v1/group-orders/{groupOrderId}`
- 是否鉴权：是

### 6.6.3 手动取消团课拼团

- 接口：`POST /api/admin/v1/group-orders/{groupOrderId}/cancel`
- 是否鉴权：是

### 6.6.4 优惠券列表

- 接口：`GET /api/admin/v1/coupons`
- 是否鉴权：是

### 6.6.5 新建优惠券

- 接口：`POST /api/admin/v1/coupons`
- 是否鉴权：是

### 6.6.6 发放优惠券

- 接口：`POST /api/admin/v1/coupons/{couponId}/issue`
- 是否鉴权：是

请求参数：

```json
{
  "userIds": [10001, 10002]
}
```

### 6.7 订单、退款与物流管理

### 6.7.1 后台订单列表

- 接口：`GET /api/admin/v1/orders`
- 是否鉴权：是

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderType` | `int` | 否 | 订单类型 |
| `orderStatus` | `int` | 否 | 订单状态 |
| `payStatus` | `int` | 否 | 支付状态 |
| `dateFrom` | `string` | 否 | 起始时间 |
| `dateTo` | `string` | 否 | 结束时间 |

### 6.7.2 订单详情

- 接口：`GET /api/admin/v1/orders/{orderId}`
- 是否鉴权：是

### 6.7.3 退款列表

- 接口：`GET /api/admin/v1/refunds`
- 是否鉴权：是

### 6.7.4 退款详情

- 接口：`GET /api/admin/v1/refunds/{refundId}`
- 是否鉴权：是

### 6.7.5 审核退款

- 接口：`POST /api/admin/v1/refunds/{refundId}/audit`
- 是否鉴权：是

请求参数：

```json
{
  "approved": true,
  "approvedAmount": 1000.00,
  "remark": "审核通过"
}
```

### 6.7.6 发货

- 接口：`POST /api/admin/v1/orders/{orderId}/ship`
- 是否鉴权：是

请求参数：

```json
{
  "companyName": "顺丰",
  "trackingNo": "SF1234567890"
}
```

### 6.8 商品管理

### 6.8.1 商品列表

- 接口：`GET /api/admin/v1/products`
- 是否鉴权：是

### 6.8.2 新增商品

- 接口：`POST /api/admin/v1/products`
- 是否鉴权：是

### 6.8.3 编辑商品

- 接口：`PUT /api/admin/v1/products/{productId}`
- 是否鉴权：是

### 6.8.4 商品上下架

- 接口：`POST /api/admin/v1/products/{productId}/status`
- 是否鉴权：是

### 6.8.5 商品分类列表

- 接口：`GET /api/admin/v1/product-categories`
- 是否鉴权：是

### 6.9 动态管理

### 6.9.1 动态列表

- 接口：`GET /api/admin/v1/moments`
- 是否鉴权：是

### 6.9.2 发布动态

- 接口：`POST /api/admin/v1/moments`
- 是否鉴权：是

请求参数：

```json
{
  "title": "周六篮球团课精彩瞬间",
  "description": "本周课程氛围很好",
  "mediaType": 2,
  "mediaUrls": ["https://minio.example.com/a.mp4"],
  "coverUrl": "https://minio.example.com/a-cover.jpg",
  "courseId": 20001,
  "coachId": 70001,
  "location": "幸福小区篮球场",
  "tags": ["篮球", "团课"]
}
```

### 6.9.3 编辑动态

- 接口：`PUT /api/admin/v1/moments/{momentId}`
- 是否鉴权：是

### 6.9.4 上下架动态

- 接口：`POST /api/admin/v1/moments/{momentId}/status`
- 是否鉴权：是

### 6.10 成长档案管理

### 6.10.1 学员成长档案详情

- 接口：`GET /api/admin/v1/growth/students/{userId}`
- 是否鉴权：是

### 6.10.2 新增体测记录

- 接口：`POST /api/admin/v1/growth/students/{userId}/physical-tests`
- 是否鉴权：是

### 6.10.3 新增技能评分

- 接口：`POST /api/admin/v1/growth/students/{userId}/skills`
- 是否鉴权：是

### 6.10.4 新增教练评价

- 接口：`POST /api/admin/v1/growth/students/{userId}/coach-comments`
- 是否鉴权：是

### 6.10.5 新增成长节点

- 接口：`POST /api/admin/v1/growth/students/{userId}/timeline`
- 是否鉴权：是

### 6.10.6 发放徽章

- 接口：`POST /api/admin/v1/growth/students/{userId}/badges`
- 是否鉴权：是

### 6.11 AI 客服后台

### 6.11.1 FAQ 列表

- 接口：`GET /api/admin/v1/ai/faqs`
- 是否鉴权：是

### 6.11.2 新增 FAQ

- 接口：`POST /api/admin/v1/ai/faqs`
- 是否鉴权：是

### 6.11.3 编辑 FAQ

- 接口：`PUT /api/admin/v1/ai/faqs/{faqId}`
- 是否鉴权：是

### 6.11.4 问答日志列表

- 接口：`GET /api/admin/v1/ai/chat-logs`
- 是否鉴权：是

### 6.12 财务报表

### 6.12.1 收支概览

- 接口：`GET /api/admin/v1/finance/overview`
- 是否鉴权：是

### 6.12.2 收支趋势

- 接口：`GET /api/admin/v1/finance/trend`
- 是否鉴权：是

### 6.12.3 课程利润分析

- 接口：`GET /api/admin/v1/finance/course-profit`
- 是否鉴权：是

### 6.12.4 教练贡献度

- 接口：`GET /api/admin/v1/finance/coach-contribution`
- 是否鉴权：是

### 6.13 文件上传

### 6.13.1 通用文件上传

- 接口：`POST /api/admin/v1/files/upload`
- 是否鉴权：是
- `Content-Type`：`multipart/form-data`

返回字段：

1. `fileName`
2. `fileUrl`
3. `fileSize`
4. `contentType`

---

## 七、第三方回调接口

### 7.1 微信支付回调

### 7.1.1 支付结果通知

- 接口：`POST /api/open/v1/payments/wechat/notify`
- 是否鉴权：否，使用微信签名校验
- 说明：支付成功后更新订单、支付流水，并异步触发履约处理

### 7.2 微信订阅消息回调预留

### 7.2.1 消息发送状态回执

- 接口：`POST /api/open/v1/wechat/message/callback`
- 是否鉴权：否

---

## 八、典型业务链路接口编排

### 8.1 私教课包拼团购买链路

1. `GET /api/app/v1/courses`
2. `GET /api/app/v1/courses/{courseId}`
3. `POST /api/app/v1/group-orders`
4. `POST /api/app/v1/orders/{orderId}/pay`
5. `POST /api/open/v1/payments/wechat/notify`
6. `GET /api/app/v1/group-orders/{groupOrderId}`

### 8.2 团课报名链路

1. `GET /api/app/v1/courses/{courseId}/schedules`
2. `POST /api/app/v1/orders`
3. `POST /api/app/v1/orders/{orderId}/pay`
4. `POST /api/open/v1/payments/wechat/notify`

### 8.3 退款链路

1. `GET /api/app/v1/refunds/preview`
2. `POST /api/app/v1/refunds`
3. `GET /api/admin/v1/refunds`
4. `POST /api/admin/v1/refunds/{refundId}/audit`
5. `GET /api/app/v1/refunds/{refundId}`

### 8.4 销课链路

1. `GET /api/admin/v1/students/{userId}`
2. `POST /api/admin/v1/lessons/consume`
3. `GET /api/app/v1/lesson-records`

### 8.5 电商发货链路

1. `GET /api/admin/v1/orders`
2. `POST /api/admin/v1/orders/{orderId}/ship`
3. `GET /api/app/v1/orders/{orderId}`
4. `POST /api/app/v1/orders/{orderId}/confirm-receipt`

---

## 九、字段命名与 DTO 建议

### 9.1 请求 DTO 命名建议

1. 创建类：`CreateXxxRequest`
2. 更新类：`UpdateXxxRequest`
3. 查询类：`XxxQueryRequest`
4. 审核类：`AuditXxxRequest`

### 9.2 响应 DTO 命名建议

1. 列表项：`XxxListItemVO`
2. 详情项：`XxxDetailVO`
3. 聚合首页：`HomeIndexVO`
4. 分页结果：`PageResult<T>`

### 9.3 分层建议

1. Controller 只处理参数和响应
2. Service 负责业务编排
3. Domain Service 负责退款、拼团、推荐等核心规则

---

## 十、接口安全与校验建议

### 10.1 必做校验

1. 当前用户是否拥有订单访问权限
2. 当前订单状态是否允许支付、取消、退款
3. 拼团是否已满员、已过期、已失效
4. 团课是否超过取消截止时间
5. 商品库存是否足够
6. 销课时剩余课时是否充足

### 10.2 幂等建议

以下接口建议做幂等控制：

1. `POST /api/app/v1/orders`
2. `POST /api/app/v1/orders/{orderId}/pay`
3. `POST /api/open/v1/payments/wechat/notify`
4. `POST /api/admin/v1/refunds/{refundId}/audit`
5. `POST /api/admin/v1/lessons/consume`

---

## 十一、接口设计结论

本接口设计文档已覆盖以下核心接口域：

1. 登录认证
2. 首页推荐
3. 课程与排期
4. 拼团
5. 订单支付
6. 退款
7. 课时与销课
8. 优惠券
9. 电商与购物车
10. 动态
11. AI 客服
12. 成长档案
13. 后台管理
14. 第三方支付回调

该文档可直接作为：

1. Controller 拆分依据
2. 前后端联调依据
3. Swagger/OpenAPI 录入基础
4. 毕设论文“系统接口设计”章节依据

---

## 十二、下一步建议

建议基于本文档继续输出：

1. Swagger/OpenAPI 规范文件
2. 后端 Controller/DTO/VO 代码骨架
3. 前端接口调用封装清单
4. Postman 测试集合

---

**文档结束**
