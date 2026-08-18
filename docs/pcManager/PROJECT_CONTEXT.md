# 项目经理PC管理后台 — 前端交接包（PROJECT_CONTEXT）

> 本文件是**前端工作的唯一入口**。方案 B（前后端独立仓库、独立工作区/对话），
> 本交接包解决"新前端对话如何拿到全部背景而不手敲"。
> **每次开前端对话，第一句引用此文件即可**：
>   "读 docs/PROJECT_CONTEXT.md，并按 API_CONTRACT 对接，开始实现 X"
>
> 备份说明：本文件由后端仓库打包生成，前端仓库应复制本 `docs/pcManager` 整个文件夹作为交接基础。
> 刷新契约：当后端接口变更后，在后端仓库重跑导出，更新 `API_CONTRACT/openapi-*.json`。

---

## 一、这是什么

**商场可视化智能车位/商铺导航系统**。两个前端 + 一套后端 API：
- **小程序端**（C 端，面向普通用户）：车位/商铺导航、附近车位、停车记录
- **本项目：PC 管理后台**（B 端，面向商场运营/平台管理员/商户）：空间数据维护 + 导航图编辑器

后端为 Spring Cloud 微服务，全部接口 POST + JSON。

## 二、后端地址与登录

| 项 | 值 |
| --- | --- |
| 网关（对外统一入口） | `http://127.0.0.1:8081` |
| 认证服务 Swagger | `http://127.0.0.1:8101/swagger-ui.html` |
| 业务服务 Swagger | `http://127.0.0.1:8102/swagger-ui.html` |
| OpenAPI JSON | `API_CONTRACT/openapi-auth.json`、`API_CONTRACT/openapi-business.json` |
| 后端仓库 | `C:\Users\54316\Desktop\projects\positioning-backend`（分支 0.1） |

**测试账号**（密码均 `123456`）：
- `admin` → 平台管理员（全部权限）
- `operator` → 商场运营（业务维护权限）
- `demo` → 普通用户（不应登录管理端做管理）

**登录**：`POST /auth/login` 传 `{"account":"admin","password":"123456"}` → 返回 `data.token`（JWT，即 satoken）。

## 三、接口通用约定（全后端统一）

1. **全部 POST + JSON**：请求体与响应体均为 JSON。
2. **统一响应结构**：`{"code":200,"message":"操作成功","data":...}`
   - `200` 成功；失败 `400/401/403/404/500/503`；`message` 为中文提示。
3. **请求头**：需登录的接口携带 `satoken: <JWT>`（除 `/auth/login`、`/auth/register`、`/uploads/**`）。
4. **通用入参**：
   - 分页：`{"pageNum":1,"pageSize":10}`
   - 按 ID：`{"id":1}`
5. **分页响应**：`data: {total: 数量, records: [各行]}`。

## 四、技术选型与目录结构（前端）

| 项 | 选型 |
| --- | --- |
| 框架 | Vue 3 + TypeScript + Vite |
| UI | Element Plus |
| 状态 | Pinia |
| 地图 | MapLibre GL JS（导航图编辑器核心） |
| HTTP | Axios（统一拦截器解包 code/message/data、注 satoken、401 跳登录） |

推荐前端目录结构（`src/`）：
```
api/        # 按模块封装（auth/mall/space/shop/nav/parking/...）
types/      # 对齐后端实体/DTO/Result/PageResult 的 TS 类型
router/     # 路由 + 守卫（登录态 + RBAC 角色过滤）
stores/     # auth（token/角色/权限码）、editor（导航图编辑器状态）
views/      # login/layout/mall/space/shop/poi/beacon/nav-editor/parking/...
components/ # GeoPreview/GeoDraw/FloorImageUpload 等
utils/request.ts
```
> 更完整的页面结构与导航图编辑器方案见同目录 `管理后台前端规划.md` 第 4、5 节。

## 五、前端开发联调配置

- **Vite 代理**：`vite.config.ts` 中把 `/api` 代理到 `http://127.0.0.1:8081`，请求路径形如 `POST /api/business/space/query`。
- 或直接配 `VITE_API_BASE=http://127.0.0.1:8081`。
- 后端网关已全放开 CORS，跨域无碍。
- 开发期可直连 Swagger UI 验证接口（带 satoken 调试）。

## 六、当前后端接口基线

- 后端最新提交：`82fd83f`（补齐 nav-node/update-geometry、文件上传、mall_floor.image_url，修复 PostGIS 兼容）。
- 接口总数：认证服务 5 个 + 业务服务 123 个（含新增 nav-node/update-geometry、file/upload）。
- 接口完整定义见 `API_CONTRACT/openapi-*.json`；也可随时用 Swagger UI 在线查看最新。

## 七、页面 → 后端接口快速索引（管理端常用）

| 页面 | 主要接口 |
| --- | --- |
| 登录 | `POST /auth/login`、`POST /auth/logout`、`POST /auth/me` |
| 商场/楼层/分区 | `mall/page|get|create|update|delete`、`floor/by-mall|page|create|update|delete`、`zone/page|create|update|delete` |
| 车位 | `space/query|search|page|get|create|update|delete|get-geometry|update-geometry|occupy|release` |
| 商铺+分类 | `shop/query|search|page|get|create|update|delete|get-geometry|update-geometry`、`shop-category/*` |
| 设施/信标 | `poi/page|get|create|update|delete`、`beacon/query|by-floor|page|...` |
| 导航图编辑器 | `nav-node/query|with-geometry|update-geometry|update|delete`、`nav-edge/query|create|update|delete`、`floor-connect/*`、`nav/route` |
| 文件上传(平面图) | `POST /business/file/upload`（multipart 字段 `file`，返回 `{url:"/uploads/xxx"}`） |
| 停车/用户/日志 | `parking-record/page|get`、`mall-user/*`、`op-log/page|get` |

> 全部接口路径与方法、字段约束（必填、枚举、几何 GeoJSON 结构）**以 `API_CONTRACT/openapi-*.json` 为准**。

## 八、登录与权限（RBAC）

- 角色来源：`/auth/me` 返回 `userType`（ADMIN/STAFF/MERCHANT/USER）；业务侧经 `/business/mall-user/user-info` 查 `mall_user.role_code`。
- 管理端主要角色：`ADMIN`（平台管理员）、`STAFF`/`MALL_OPERATOR`（商场运营）、`MERCHANT`（商户只读）。
- 按钮/菜单权限码来自后端 `sys_permission.perm_code`（如 `space:edit`、`nav:edit`）。
