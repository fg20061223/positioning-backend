# 前后端对话交接模板（HANDOFF_TEMPLATE）

> 方案 B：前后端独立工作区/对话。每次开新对话用"一句话引用"获得全量上下文，无需手敲背景。

---

## 一、开新前端对话时的第一条指令（直接复制用）

只发这一句即可启动（把 `X` 换成本次任务）：

```text
读 docs/PROJECT_CONTEXT.md，并依据 docs/API_CONTRACT 下的 openapi-*.json
对接后端接口，开始实现任务 X：<一句话描述本次要做的页面/功能>。
```

前端对话会依次读取：
1. `PROJECT_CONTEXT.md` → 全部项目背景、接口约定、测试账号、技术选型
2. `API_CONTRACT/openapi-*.json` → 全部接口定义（生成 TS 类型用）
3. `管理后台前端规划.md` → 页面结构、导航图编辑器方案、里程碑（如需）

---

## 二、后端变更后如何刷新契约（关键，唯一活契约来源）

后端改了接口后，在前端侧重新导出（或由后端对话代劳），保持契约同步：

```powershell
curl.exe -s -o docs/API_CONTRACT/openapi-auth.json http://127.0.0.1:8101/v3/api-docs
curl.exe -s -o docs/API_CONTRACT/openapi-business.json http://127.0.0.1:8102/v3/api-docs
```

> 前提：后端三个服务已启动（网关 8081 / 认证 8101 / 业务 8102）。
> 若导出为空或 401，先确认服务已就绪、用 Swagger UI 在线兜底。

---

## 三、每个前端对话应遵守的约定（写给前端对话/AI）

1. **以 openapi 快照为唯一接口真源**，字段/枚举/必填/几何结构以它为准，不要凭记忆。
2. **接口调用统一**：走 `utils/request.ts`（自动带 `satoken`、解包 code/message/data、401 跳登录）。
3. **联调地址**：`VITE_API_BASE=http://127.0.0.1:8081`（网关）或 vite 代理 `/api -> 8081`。
4. **测试账号**：admin / operator（密码 123456）登录管理端；demo 不应有管理权限。
5. **页面与接口归属**见 `PROJECT_CONTEXT.md` 第七节；完整页面结构见 `管理后台前端规划.md`。
6. 隐藏但需知道的坑：
   - 几何字段走专用接口（`*-geometry` / `with-geometry`），**不要用通用 update 传几何**。
   - `nav-node/update-geometry` 用于拖拽移动节点（前端算米制坐标 Point GeoJSON）。
   - 删除导航节点前，先删引用它的边（后端无物理外键，前端保证一致性）。
   - 楼层平面图上传后把返回的 `{url}` 写入 `floor/update` 的 `imageUrl` 字段。

---

## 四、协商变更接口（反向：前端需要后端配合时）

前端对话若发现缺接口/字段，**禁止自行改后端契约**（后端独立工作区）。应：
1. 记录需求：缺什么接口/字段、用于哪页面、期望出入参。
2. 用一段话发给"后端对话"：例子：

```text
后端：管理后台需要 X 能力（页面 Y）。建议新增/修改接口 <路径/方法>，
入参示意 <JSON>，出参示意 <JSON>。请确认并实现，完成后刷新 openapi 契约。
```

---

## 五、版本与提交约定

- 前后端各自独立 git 仓库、独立提交。
- API 基线以"后端某 commit"为准，前端交接包记录该基线（见 PROJECT_CONTEXT 第六节）。
- 破坏性接口变更（改字段名/删接口/改枚举）必须走"四"协商并更新 openapi 快照，避免前端静默失效。
