# 电子商城后端接口开发文档（前端对接用）

> 后端服务：Spring Boot，端口 `9191`（生产环境通过 Nginx 反向代理，前端统一请求 `/api/...` 即可）。
> 全部接口返回 JSON，结构统一为 `Result`。

---

## 1. 通用约定

### 1.1 统一响应结构

```json
{
  "code": "200",
  "msg": null,
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | string | 状态码（字符串），见下表 |
| `msg`  | string | 提示信息，成功通常为 `null`，失败时为错误描述 |
| `data` | any | 业务数据，可为对象 / 数组 / 基础类型 / `null` |

**状态码定义**

| code | 含义 |
|------|------|
| `200` | 成功 |
| `401` | 未登录 / token 失效（前端应跳转登录页） |
| `403` | 已登录但无权限（仅管理员可访问） |
| `500` | 服务异常 |
| `510` | 未找到结果 |

### 1.2 鉴权机制

- 登录成功后，后端返回 `token`，**前端后续所有需登录的请求必须在 HTTP Header 中携带**：
  ```
  token: <登录返回的 token>
  ```
- token 同时作为 Redis 会话标识，有效期默认 1440 分钟（24 小时），每次请求会自动续期。
- token 失效时统一返回 `code = 401`。

### 1.3 接口权限分级

通过 `@Authority` 注解控制，分三级：

| 级别 | 含义 |
|------|------|
| `noRequire` | 公开访问，无需登录 |
| `requireLogin` | 需登录 |
| `requireAuthority` | 需当前用户 `role == "admin"` |

> 在每个接口说明中已标注「权限」。

### 1.4 分页约定

`GET` 分页接口统一使用以下查询参数：

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `pageNum` | int | 1 | 页码，从 1 开始（部分搜索接口从 0 开始，已单独标注） |
| `pageSize` | int | 10 | 每页条数 |

返回结构（MyBatis-Plus 标准）：

```json
{
  "records": [],
  "total": 0,
  "size": 10,
  "current": 1,
  "pages": 0
}
```

### 1.5 跨域

后端开启了 CORS，本地开发允许跨域；生产环境请走 Nginx 同源代理。

---

## 2. 用户与权限

### 2.1 登录

`POST /login` · 公开

**Body**
```json
{ "username": "admin", "password": "123456" }
```

**Response.data**（`UserDTO`）
```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "avatarUrl": "/avatar/xxx.png",
  "token": "eyJhbGciOi...",
  "role": "admin"
}
```

### 2.2 注册

`POST /register` · 公开

**Body**：同登录表单。

**Response.data**：`User` 对象（不含 token，注册成功后需调用登录）。

### 2.3 获取当前用户角色

`POST /role` · 需登录

返回 `data` 为角色字符串（`admin` / `user`）。

### 2.4 获取当前用户 ID

`GET /userid` · 需登录

返回 `data` 为 `long` 类型用户 ID。

### 2.5 根据用户名查用户信息

`GET /userinfo/{username}` · 需登录

### 2.6 用户分页查询（管理端）

`GET /user/page` · 需登录

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | |
| pageSize | int | 是 | |
| id | string | 否 | 模糊匹配 |
| username | string | 否 | 模糊匹配 |
| nickname | string | 否 | 模糊匹配 |

### 2.7 用户新增 / 编辑

`POST /user` · 需登录

**Body**（`User`）
```json
{
  "id": 1,
  "username": "tom",
  "nickname": "汤姆",
  "email": "tom@a.com",
  "phone": "13800000000",
  "address": "杭州",
  "avatarUrl": "/avatar/x.png",
  "role": "user",
  "newPassword": "明文新密码"
}
```

> 修改密码时传 `newPassword`；`password` 字段服务端不返回。

### 2.8 删除用户

- `DELETE /user/{id}` · 管理员
- `POST /user/del/batch`，Body：`[1,2,3]` · 管理员

### 2.9 重置密码

`GET /user/resetPassword?id={userId}&newPassword={pwd}` · 需登录

---

## 3. 收货地址

> 整个 `/api/address` 模块需登录。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/address/{userId}` | 查询某用户的所有地址 |
| GET | `/api/address` | 查询全部地址（管理端用） |
| POST | `/api/address` | 新增 / 更新（有 id 即更新） |
| PUT | `/api/address` | 根据 id 更新 |
| DELETE | `/api/address/{id}` | 删除 |

**Address 实体**
```json
{
  "id": 1,
  "linkUser": "张三",
  "linkAddress": "杭州市西湖区...",
  "linkPhone": "13800000000",
  "userId": 1
}
```

---

## 4. 商品

### 4.1 商品实体 `Good`

```json
{
  "id": 1,
  "name": "iPhone 16",
  "description": "...",
  "discount": 0.9,
  "sales": 120,
  "saleMoney": 120000.00,
  "categoryId": 2,
  "imgs": "/file/a.png|/file/b.png",
  "createTime": "2026-08-08 12:00:00",
  "recommend": true,
  "isDelete": false,
  "price": 7999.00
}
```

> `imgs` 多图使用 `|` 分隔。`price` 字段不入库（`@TableField(exist=false)`），是给前端展示用的「最低价」聚合值。

### 4.2 接口列表

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/good` | 公开 | 推荐商品列表（`recommend=1`） |
| GET | `/api/good/{id}` | 公开 | 商品详情 |
| GET | `/api/good/standard/{id}` | 公开 | 商品规格列表（`id` 为商品 id） |
| GET | `/api/good/rank?num=10` | 公开 | 销量榜 |
| GET | `/api/good/page` | 公开 | 分页：`pageNum`、`pageSize`、`searchText`、`categoryId` |
| GET | `/api/good/search` | 公开 | 搜索（带 `searchText` 时走 Elasticsearch） |
| GET | `/api/good/fullPage` | 公开 | 后台分页（含已下架） |
| GET | `/api/good/recommend?id=&isRecommend=` | 管理员 | 设置推荐 |
| POST | `/api/good` | 管理员 | 新增 / 修改商品 |
| PUT | `/api/good` | 管理员 | 修改商品 |
| DELETE | `/api/good/{id}` | 管理员 | 删除商品 |
| POST | `/api/good/standard?goodId=10` | 管理员 | 批量保存规格，Body：`Standard[]` |
| DELETE | `/api/good/standard` | 管理员 | 删除某条规格，Body：`Standard` |

**Standard 规格**
```json
{
  "goodId": 10,
  "value": "256G / 黑色",
  "price": 7999.00,
  "store": 50
}
```

---

## 5. 分类

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/category/{id}` | 公开 | 查单个分类 |
| GET | `/api/category` | 公开 | 全部分类 |
| POST | `/api/category` | 需登录 | 新增 / 更新 |
| POST | `/api/category/add` | 需登录 | 新增下级分类并建立图标关联 |
| PUT | `/api/category` | 管理员 | 修改 |
| GET | `/api/category/delete?id=` | 管理员 | 删除 |

**Category**
```json
{ "id": 1, "name": "数码", "iconId": 3 }
```

---

## 6. 分类图标

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/icon` | 公开 | 图标 + 关联分类列表 |
| GET | `/api/icon/{id}` | 公开 | 单个图标 |
| POST | `/api/icon` | 管理员 | 新增/更新 |
| PUT | `/api/icon` | 管理员 | 更新 |
| GET | `/api/icon/delete?id=` | 管理员 | 删除（返回 `{code,msg,data}`） |

**Icon**
```json
{ "id": 1, "value": "eletronic", "categories": [ { "id": 1, "name": "数码" } ] }
```

---

## 7. 轮播图

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/carousel` | 公开 | 全部轮播（按 `showOrder` 排序） |
| GET | `/api/carousel/{id}` | 公开 | 单条 |
| POST | `/api/carousel` | 管理员 | 新增/更新（需先校验 `goodId` 存在） |
| PUT | `/api/carousel` | 管理员 | 更新 |
| DELETE | `/api/carousel/{id}` | 管理员 | 删除 |

**Carousel**
```json
{
  "id": 1,
  "goodId": 10,
  "showOrder": 1,
  "goodName": "iPhone 16",
  "img": "/file/x.png"
}
```

---

## 8. 购物车

> 整个 `/api/cart` 模块需登录。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart/{id}` | 按 id 查购物车项 |
| GET | `/api/cart` | 全部购物车 |
| GET | `/api/cart/userid/{userId}` | 当前用户的购物车 |
| POST | `/api/cart` | 加入购物车（自动写入 `createTime`） |
| PUT | `/api/cart` | 更新数量 / 规格 |
| DELETE | `/api/cart/{id}` | 删除 |

**Cart**
```json
{
  "id": 1,
  "count": 2,
  "createTime": "2026-08-08 12:00:00",
  "goodId": 10,
  "standard": "256G / 黑色",
  "userId": 1
}
```

---

## 9. 订单

> 整个 `/api/order` 模块需登录。

### 9.1 订单实体

```json
{
  "id": 1,
  "orderNo": "202608081234567890",
  "totalPrice": 15998.00,
  "userId": 1,
  "linkUser": "张三",
  "linkPhone": "13800000000",
  "linkAddress": "杭州",
  "state": "待付款",
  "createTime": "2026-08-08 12:00:00",
  "goods": "[{...OrderItem}]",
  "cartId": 5
}
```

**state 流转**

```
待付款 → 待发货 → 待收货 → 已收货
```

**OrderItem**（前端下单时把购物车选中项打包成 JSON 字符串放到 `goods` 字段）

```json
{ "id": 10, "standard": "256G", "num": 2 }
```

### 9.2 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/order/userid/{userid}` | 需登录 | 查询某用户订单 |
| GET | `/api/order/orderNo/{orderNo}` | 需登录 | 按订单号查 |
| GET | `/api/order` | 需登录 | 全部订单 |
| GET | `/api/order/page` | 需登录 | 分页（后台），过滤掉「待付款」单 |
| POST | `/api/order` | 需登录 | 创建订单，**返回 `data` 为订单编号 orderNo** |
| GET | `/api/order/paid/{orderNo}` | 需登录 | 用户支付 |
| GET | `/api/order/received/{orderNo}` | 需登录 | 用户确认收货 |
| GET | `/api/order/delivery/{orderNo}` | 管理员 | 后台发货 |
| PUT | `/api/order` | 需登录 | 更新订单 |
| DELETE | `/api/order/{id}` | 需登录 | 删除订单 |

**`/api/order/page` 参数**：`pageNum`、`pageSize`、`orderNo`（模糊）、`state`（精确）。

---

## 10. 秒杀

### 10.1 秒杀券 `SeckillVoucher`

```json
{
  "id": 1,
  "voucherAmount": 50.00,
  "minAmount": 300.00,
  "stock": 100,
  "beginTime": "2026-08-08T20:00:00",
  "endTime": "2026-08-08T22:00:00",
  "createTime": "...",
  "updateTime": "...",
  "status": 1
}
```

`status`：`0` 未开始 / `1` 进行中 / `2` 已结束 / `3` 已停用

### 10.2 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/seckill/list?pageNum=&pageSize=` | 公开 | 用户端秒杀券列表（含未开始/进行中） |
| GET | `/api/seckill/active` | 公开 | 进行中的秒杀券 |
| POST | `/api/seckill/purchase/{voucherId}` | 需登录 | **抢券**（高并发接口，建议前端节流） |
| GET | `/api/seckill/my-vouchers` | 需登录 | 我的优惠券列表 |
| GET | `/api/seckill/admin/page?pageNum=&pageSize=` | 需登录 | 后台分页 |
| POST | `/api/seckill/admin` | 管理员 | 创建秒杀券 |
| PUT | `/api/seckill/admin` | 管理员 | 修改秒杀券 |
| DELETE | `/api/seckill/admin/{id}` | 管理员 | 删除秒杀券 |

### 10.3 我的优惠券 `SeckillOrderDTO`

```json
{
  "id": 100,
  "userId": 1,
  "voucherId": 1,
  "payType": 1,
  "status": 2,
  "createTime": "...",
  "payTime": "...",
  "voucherAmount": 50.00,
  "minAmount": 300.00,
  "voucherStatus": "未使用"
}
```

---

## 11. 文件 / 头像

### 11.1 头像 `/avatar`（部分公开）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/avatar` | 公开 | 上传，`multipart/form-data`，字段名 `file`。返回 `data` 为访问 URL |
| GET | `/avatar/{fileName}` | 公开 | 直接返回图片字节流（`<img src>` 直接用） |
| GET | `/avatar/page?pageNum=&pageSize=` | 公开 | 分页 |
| DELETE | `/avatar/{id}` | 管理员 | 删除 |

返回结构示例：
```json
{ "code": "200", "msg": null, "data": "/avatar/1691486400-xxx.png" }
```

### 11.2 通用文件 `/file`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/file/upload` | 公开 | 上传，`multipart/form-data`，字段名 `file` |
| GET | `/file/{fileName}` | 公开 | 下载 / 直接展示 |
| GET | `/file/page?pageNum=&pageSize=&fileName=` | 公开 | 分页 |
| DELETE | `/file/{id}` | 管理员 | 假删（`enable=0`） |
| POST | `/file/del/batch`，Body：`[1,2]` | 管理员 | 批量假删 |
| GET | `/file/enable?id=&enable=` | 管理员 | 启用/禁用文件 |

---

## 12. 搜索（Elasticsearch）

> 后端通过 `app.es.enabled` 控制 ES 是否启用。
> **未启用时所有接口自动回退到 MySQL LIKE，响应结构完全一致**，前端无需感知差异。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search/goods?keyword=&page=&size=` | 关键词搜索（page 从 0 开始） |
| GET | `/search/goods/name?name=&page=&size=` | 按名称 |
| GET | `/search/goods/category?categoryId=&page=&size=` | 按分类 |
| GET | `/search/goods/recommended?page=&size=` | 推荐商品分页 |
| GET | `/search/goods/recommended/all` | 全部推荐 |
| GET | `/search/goods/category/all?categoryId=` | 分类下全部商品 |
| GET | `/search/stats` | ES 索引统计信息 |

返回的 `GoodDocument` 与 `GoodDTO` 字段一致：`id`、`name`、`imgs`、`price`。

---

## 13. 数据同步（管理端，仅 ES 启用时有效）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/sync/goods/all` | 全量同步 |
| POST | `/sync/goods/{goodId}` | 同步单个商品 |
| PUT | `/sync/goods/{goodId}` | 更新单个 |
| DELETE | `/sync/goods/{goodId}` | 从 ES 删除 |
| GET | `/sync/status` | 检查 ES 连接状态 |
| POST | `/sync/index/create` | 创建索引 |
| DELETE | `/sync/index` | 删除索引 |

> ES 未启用时统一返回 `data = "Elasticsearch 未启用，数据同步已跳过"`。

---

## 14. 收入统计

> 整个 `/api/income` 模块需管理员权限。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/income/chart` | 图表数据 |
| GET | `/api/income/week` | 周收入 |
| GET | `/api/income/month` | 月收入 |

---

## 15. 前端对接 Checklist

1. **统一在请求头携带 token**：登录后所有 axios 请求默认加上 `token: ${userDTO.token}`。
2. **统一拦截 401**：弹出「请重新登录」并清空本地用户状态、跳转登录页。
3. **图片 URL 直接拼接**：后端返回的图片路径形如 `/file/xxx.png` / `/avatar/xxx.png`，前端拼上后端域名即可（生产 Nginx 已代理）。
4. **分页参数从 1 还是 0 开始**：业务接口（`/api/.../page`）统一从 `1` 开始；`/search/...` 系列从 `0` 开始。
5. **下单流程**：`POST /api/order` 成功后返回的是**订单编号字符串**，不是 id；之后调用 `/api/order/paid/{orderNo}` 完成支付。
6. **秒杀接口节流**：`POST /api/seckill/purchase/{voucherId}` 后端已加 Redis 限流和 Lua 原子扣减，前端按钮务必做防抖避免重复点击。
7. **管理员专用接口**：普通用户访问会返回 `403`，前端需要根据 `userDTO.role` 控制菜单与按钮可见性。

---

## 16. 接口快速索引

| 模块 | Base |
|------|------|
| 用户/登录 | `/login` `/register` `/role` `/userid` `/userinfo` `/user/*` |
| 收货地址 | `/api/address` |
| 商品 | `/api/good` |
| 分类 | `/api/category` |
| 图标 | `/api/icon` |
| 轮播图 | `/api/carousel` |
| 购物车 | `/api/cart` |
| 订单 | `/api/order` |
| 秒杀 | `/api/seckill` |
| 文件上传 | `/file` `/avatar` |
| 搜索 | `/search` |
| 数据同步 | `/sync` |
| 收入统计 | `/api/income` |
