# 社区互助平台 - API接口规范文档

## 一、接口设计规范

### 1.1 URL规范

- 使用小写字母和连字符
- 使用名词复数表示资源集合
- 使用版本号前缀 `/api/v1/`
- RESTful风格设计

```
GET    /api/v1/users          # 获取用户列表
GET    /api/v1/users/{id}     # 获取单个用户
POST   /api/v1/users          # 创建用户
PUT    /api/v1/users/{id}     # 更新用户
DELETE /api/v1/users/{id}     # 删除用户
```

### 1.2 请求/响应格式

- 统一使用JSON格式
- 请求头必须包含 `Content-Type: application/json`
- 认证请求头 `Authorization: Bearer {token}`

### 1.3 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1715000000000
}
```

### 1.4 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 10,
    "totalPages": 10
  },
  "timestamp": 1715000000000
}
```

---

## 二、接口清单

### 2.1 用户服务 (user-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/v1/user/register | 用户注册 | 公开 |
| POST | /api/v1/user/login | 用户登录 | 公开 |
| POST | /api/v1/user/logout | 用户登出 | 已登录 |
| GET | /api/v1/user/info | 获取用户信息 | 已登录 |
| PUT | /api/v1/user/info | 更新用户信息 | 已登录 |
| PUT | /api/v1/user/password | 修改密码 | 已登录 |
| PUT | /api/v1/user/avatar | 更新头像 | 已登录 |
| GET | /api/v1/user/search | 搜索用户 | 已登录 |
| GET | /api/v1/user/addresses | 获取地址列表 | 已登录 |
| POST | /api/v1/user/addresses | 新增地址 | 已登录 |
| PUT | /api/v1/user/addresses/{id} | 更新地址 | 已登录 |
| DELETE | /api/v1/user/addresses/{id} | 删除地址 | 已登录 |
| POST | /api/v1/user/verify/idcard | 身份证认证 | 已登录 |
| POST | /api/v1/user/verify/face | 人脸认证 | 已登录 |

### 2.2 订单服务 (order-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/v1/orders | 创建订单 | 已登录 |
| GET | /api/v1/orders | 获取订单列表 | 已登录 |
| GET | /api/v1/orders/{id} | 获取订单详情 | 已登录 |
| PUT | /api/v1/orders/{id}/cancel | 取消订单 | 已登录 |
| PUT | /api/v1/orders/{id}/accept | 接单 | 服务提供者 |
| PUT | /api/v1/orders/{id}/complete | 完成订单 | 服务提供者 |
| POST | /api/v1/orders/{id}/rate | 评价订单 | 已登录 |
| GET | /api/v1/orders/{id}/progress | 获取服务进度 | 已登录 |
| POST | /api/v1/orders/{id}/progress | 更新服务进度 | 服务提供者 |
| GET | /api/v1/wishes | 获取心愿列表 | 公开 |
| POST | /api/v1/wishes | 发布心愿 | 已登录 |
| GET | /api/v1/wishes/{id} | 获取心愿详情 | 公开 |
| PUT | /api/v1/wishes/{id} | 更新心愿 | 已登录 |
| DELETE | /api/v1/wishes/{id} | 删除心愿 | 已登录 |
| GET | /api/v1/wishes/search | 搜索心愿 | 公开 |

### 2.3 支付服务 (payment-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/v1/payment/create | 创建支付 | 已登录 |
| POST | /api/v1/payment/callback | 支付回调 | 公开 |
| GET | /api/v1/payment/status/{orderId} | 查询支付状态 | 已登录 |
| POST | /api/v1/payment/refund | 申请退款 | 已登录 |
| GET | /api/v1/payment/transactions | 获取交易记录 | 已登录 |
| GET | /api/v1/payment/transactions/{id} | 获取交易详情 | 已登录 |

### 2.4 消息服务 (message-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/v1/messages | 获取消息列表 | 已登录 |
| GET | /api/v1/messages/unread | 获取未读消息 | 已登录 |
| GET | /api/v1/messages/unread-count | 获取未读数量 | 已登录 |
| PUT | /api/v1/messages/{id}/read | 标记已读 | 已登录 |
| PUT | /api/v1/messages/read-all | 全部已读 | 已登录 |
| DELETE | /api/v1/messages/{id} | 删除消息 | 已登录 |
| POST | /api/v1/friends/apply | 申请好友 | 已登录 |
| GET | /api/v1/friends/applications | 获取好友申请 | 已登录 |
| PUT | /api/v1/friends/{id}/accept | 同意好友 | 已登录 |
| PUT | /api/v1/friends/{id}/reject | 拒绝好友 | 已登录 |
| GET | /api/v1/friends | 获取好友列表 | 已登录 |
| DELETE | /api/v1/friends/{id} | 删除好友 | 已登录 |
| PUT | /api/v1/friends/{id}/remark | 修改备注 | 已登录 |
| POST | /api/v1/groups | 创建群聊 | 已登录 |
| GET | /api/v1/groups | 获取我的群聊 | 已登录 |
| GET | /api/v1/groups/{id} | 获取群聊详情 | 已登录 |
| PUT | /api/v1/groups/{id} | 更新群聊 | 群主/管理员 |
| POST | /api/v1/groups/{id}/members | 添加成员 | 群主/管理员 |
| DELETE | /api/v1/groups/{id}/members/{userId} | 移除成员 | 群主/管理员 |
| POST | /api/v1/groups/{id}/quit | 退出群聊 | 已登录 |

### 2.5 积分服务 (credit-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/v1/credits/balance | 获取积分余额 | 已登录 |
| GET | /api/v1/credits/logs | 获取积分记录 | 已登录 |
| POST | /api/v1/credits/checkin | 签到 | 已登录 |
| POST | /api/v1/credits/transfer | 积分转账 | 已登录 |

### 2.6 活动服务 (activity-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/v1/activities | 创建活动 | 已登录 |
| GET | /api/v1/activities | 获取活动列表 | 公开 |
| GET | /api/v1/activities/{id} | 获取活动详情 | 公开 |
| PUT | /api/v1/activities/{id} | 更新活动 | 组织者 |
| DELETE | /api/v1/activities/{id} | 删除活动 | 组织者/管理员 |
| POST | /api/v1/activities/{id}/signup | 报名活动 | 已登录 |
| PUT | /api/v1/activities/{id}/signup/cancel | 取消报名 | 已登录 |
| GET | /api/v1/activities/{id}/signups | 获取报名列表 | 组织者 |
| POST | /api/v1/activities/{id}/checkin | 签到 | 组织者 |

### 2.7 管理后台 (admin-service)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/v1/admin/login | 管理员登录 | 公开 |
| GET | /api/v1/admin/statistics | 获取统计数据 | 管理员 |
| GET | /api/v1/admin/users | 获取用户列表 | 管理员 |
| PUT | /api/v1/admin/users/{id}/status | 更新用户状态 | 管理员 |
| GET | /api/v1/admin/orders | 获取订单列表 | 管理员 |
| GET | /api/v1/admin/activities | 获取活动列表 | 管理员 |
| POST | /api/v1/admin/activities | 创建活动 | 管理员 |

---

## 三、权限控制策略

### 3.1 认证机制

系统采用JWT Token进行无状态认证：

```
请求流程:
客户端 -> 携带Token -> API网关 -> 验证Token -> 转发请求 -> 目标服务
```

### 3.2 权限级别

| 级别 | 描述 | 示例 |
|------|------|------|
| PUBLIC | 公开接口，无需登录 | 登录、注册、公开列表 |
| AUTHENTICATED | 已登录用户 | 个人信息、发布内容 |
| OWNER | 资源所有者 | 修改自己的订单 |
| PROVIDER | 服务提供者 | 接单、完成订单 |
| ADMIN | 管理员 | 用户管理、内容审核 |
| SUPER_ADMIN | 超级管理员 | 系统配置、管理员管理 |

### 3.3 网关鉴权配置

```yaml
# 白名单配置 (无需认证)
auth.white-list:
  - /api/v1/user/login
  - /api/v1/user/register
  - /api/v1/admin/login
  - /api/v1/wishes
  - /api/v1/activities
  - /api/v1/payment/callback

# 权限映射
auth.permission-map:
  /api/v1/user/**: AUTHENTICATED
  /api/v1/orders/**: AUTHENTICATED
  /api/v1/payment/**: AUTHENTICATED
  /api/v1/admin/**: ADMIN
```

### 3.4 接口限流策略

| 接口类型 | 限流规则 | 说明 |
|----------|----------|------|
| 登录接口 | 10次/分钟 | 防止暴力破解 |
| 短信验证码 | 5次/小时 | 防止短信轰炸 |
| 普通接口 | 100次/分钟 | 防止恶意请求 |
| 支付接口 | 20次/分钟 | 保护支付安全 |

---

## 四、错误码规范

### 4.1 HTTP状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 4.2 业务错误码

| 错误码 | 含义 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | 账号已被禁用 |
| 1003 | 验证码错误 |
| 1004 | 验证码已过期 |
| 2001 | 订单不存在 |
| 2002 | 订单状态不允许此操作 |
| 2003 | 订单已超时 |
| 3001 | 余额不足 |
| 3002 | 支付密码错误 |
| 3003 | 支付已超时 |
| 4001 | 好友申请已存在 |
| 4002 | 已是好友 |
| 4003 | 群聊人数已满 |
| 5001 | 积分不足 |
| 5002 | 签到已重复 |

---

## 五、数据字典

### 5.1 用户类型 (user_type)

| 值 | 含义 |
|----|------|
| 1 | 普通用户 |
| 2 | 服务提供者 |
| 3 | 管理员 |

### 5.2 订单状态 (order_status)

| 值 | 含义 |
|----|------|
| 0 | 待接单 |
| 1 | 进行中 |
| 2 | 已完成 |
| 3 | 已取消 |
| 4 | 已退款 |

### 5.3 支付状态 (payment_status)

| 值 | 含义 |
|----|------|
| 0 | 待支付 |
| 1 | 支付中 |
| 2 | 成功 |
| 3 | 失败 |
| 4 | 已退款 |

### 5.4 消息类型 (message_type)

| 值 | 含义 |
|----|------|
| 1 | 系统消息 |
| 2 | 订单消息 |
| 3 | 支付消息 |
| 4 | 积分消息 |
| 5 | 好友消息 |

### 5.5 服务类型 (service_type)

| 值 | 含义 |
|----|------|
| errand | 跑腿代办 |
| companion | 陪伴服务 |
| shopping | 代购代买 |
| care | 照护服务 |
| tech | 技术支持 |
| other | 其他 |
