# DeepSeek Harness 交接提示词

> 用途：将本会话已完成的项目在 DeepSeek Harness 上继续执行。把本文件内容作为首轮提示词整体粘贴，并把「相关文件」一节的目录一并提供即可。

---

你是继续接手本项目的高级后端工程师。仓库位于 `C:\Users\54316\Desktop\projects\positioning-backend`，是一个**商场可视化智能车位/商铺导航系统**的后端，已完成第一版可编译代码并提交（git HEAD `0e16a4a`，交接时工作区仅有少量后续改动）。

## 0. 相关文件（先全部阅读再动手）

- `README.md`：项目总览、版本矩阵、启动方式、接口约定
- `docs/项目设计架构总结.md`：本项目最完整的设计与代码总结（本提示词的详细版）
- `docs/01-database-design.md`：数据库设计文档
- `sql/00_init.sql`、`sql/01_auth_db.sql`、`sql/02_business_db.sql`：数据库脚本
- 根目录 `pom.xml`：版本管理（Spring Boot 3.5.16 / Spring Cloud 2025.0.3 / Spring Cloud Alibaba 2025.0.0.0 / MyBatis-Plus 3.5.12 / Sa-Token 1.45.0）

## 1. 项目说明

- 用户进入商场地下车库后，输入车位号或商铺名，系统基于**蓝牙室内定位 + 路径可视化**给出"往左走20米，右转进入B区"式的导航指引。
- 技术壁垒是**空间数据的组织与可视化呈现**，不是高并发。
- 数据库为 PostgreSQL 16 + PostGIS，单库 `postgis_36_sample`，按 `auth`、`business` 两个架构隔离微服务数据。

## 2. 项目设计（数据库）摘要

- 共 24 张表：auth 架构 6 张（用户/角色/权限/关联/登录日志），business 架构 18 张（商场/楼层/分区/车位/商铺/分类/POI/信标/导航节点/导航边/跨层连接/用户绑定/车辆/停车记录/收藏/导航历史/定位会话/操作日志），**每个字段均有中文注释**。
- 坐标约定：室内几何统一用"楼层本地平面坐标（米）"，SRID=0；空间要素同时保存 `geom`（轮廓）、`center_point`（中心）、`entrance_point`（入口）。
- 导航图：`nav_node` + `nav_edge`（边长冗余、权重、双向标记）+ `floor_connect`（跨楼层电梯/扶梯/楼梯绑定）。
- 主键：应用层雪花 ID（MyBatis-Plus `IdType.ASSIGN_ID`）；**不建物理外键**，跨表/跨架构关联只建索引。逻辑删除字段 `deleted`。
- SQL 脚本职责：`00_init.sql` 安装 `postgis`、`pg_trgm` 扩展（**以用户最新版本为准，不得改回或修改**）；`01/02` 分别建 schema 与表。

## 3. 架构设计

**版本矩阵**：JDK 21、Maven、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0（Nacos 3.0.3）、MyBatis-Plus 3.5.12（分页需额外依赖 `mybatis-plus-jsqlparser`）、Sa-Token 1.45.0（JWT 无状态模式）。

**模块结构**（Maven 聚合工程，9 个子模块，128 个 Java 文件）：

```text
positioning-backend
├── positioning-common        # 统一返回 Result/PageResult、业务异常 BizException、公共 DTO（PageQuery/IdRequest）
├── positioning-gateway       # Spring Cloud Gateway：路由(lb://)、Caffeine 限流、Resilience4j 熔断降级
├── positioning-auth
│   ├── positioning-auth-api  # Feign 契约 + 共享 DTO（UserFeignClient）
│   └── positioning-auth-biz  # Sa-Token 鉴权、RBAC、登录/注册/登出/me、登录日志（auth 架构表）
└── positioning-business
    ├── positioning-business-api  # Feign 契约（预留）：Mall/Space/Shop 三个 Client
    └── positioning-business-biz  # 18 张业务表实体+Mapper+控制器、路径规划、停车、收藏等（business 架构表）
```

**服务注册与端口**（全部注册到 Nacos `127.0.0.1:8848`，可环境变量覆盖）：

| 服务 | 注册名 | 端口 | JDBC currentSchema |
| --- | --- | --- | --- |
| 网关 | positioning-gateway | 8080 | - |
| 认证服务 | positioning-auth | 8101 | auth |
| 业务服务 | positioning-business | 8102 | business |

**关键设计**：

- 网关路由：`/auth/**`、`/internal/**` → `lb://positioning-auth`；`/business/**` → `lb://positioning-business`；网关不做 token 校验（鉴权在各服务内完成）。
- Sa-Token JWT：认证与业务服务共享 `SA_TOKEN_SECRET`，`token-style: jwt-simple`；业务服务用同一套密钥校验 token 并取 `StpUtil.getLoginIdAsLong()`。
- RBAC：auth 服务从 `sys_role/sys_permission` 关联实时加载角色与权限码（`StpInterfaceImpl`）；业务服务角色取自 `mall_user.role_code`。
- OpenFeign：business-biz 调用 auth 服务的 `POST /internal/user/get`（JSON `{"id":...}`）校验/聚合用户；business-api 为预留契约，内部实现已存在（`/internal/mall|space|shop/get`）。
- **接口约定：所有接口一律 POST，请求体与响应体均为 JSON**（响应统一 `{"code":200,"message":"...","data":...}`；分页入参 `{"pageNum":1,"pageSize":10}`，按 ID 操作 `{"id":1}`）。
- PostGIS 几何列（`geom/center_point/entrance_point/position_geom/path_geom/last_pos`）在通用 CRUD 中不读写（`@TableField(select=false, insert/update NEVER)`），车位/商铺几何走专用 `POST /get-geometry`、`POST /update-geometry`（GeoJSON），导航节点创建走 `POST /business/nav-node/with-geometry`。

## 4. 当前代码状态（已实现并编译通过）

- 认证：注册、登录（用户名/手机号 + BCrypt）、登出、当前用户；登录日志落库；Sa-Token 路由拦截（放行 `/auth/login`、`/auth/register`、`/internal/**`、`/actuator/**`）。
- 业务：18 张表通用 CRUD（`BaseCrudController` 基类）；车位条件分页/车位号搜索/附近空闲车位 KNN/几何读写/占用释放；商铺条件分页/trgm 模糊搜索/几何读写；信标按楼层查询；导航节点带几何创建；Dijkstra 路径规划（`POST /business/nav/route`）；停车入场/离场联动车位状态；收藏、导航历史、定位会话（绑定当前登录用户）；用户-商场绑定走 Feign 校验。
- 网关：路由 + 按 IP 令牌桶限流（默认 100 次/分，`positioning.gateway.rate-limit.*` 可调）+ Resilience4j 熔断降级（`/fallback/auth`、`/fallback/business`）+ CORS。
- 编译验证：`mvn -B -DskipTests compile` 9/9 模块 BUILD SUCCESS（JDK 21，本机可用 `C:\Users\54316\.jdks\jbr-21.0.11`）。

## 5. 运行方式

```bash
# 构建（需要 JDK 21）
mvn clean package -DskipTests

# 分别启动（或 IDE 运行各 Application）：
# positioning-gateway / positioning-auth-biz / positioning-business-biz
```

前提：Nacos 已启动；PostgreSQL 16 + PostGIS 已建库并执行 `sql/00/01/02` 脚本。数据库默认账号 `postgres/123456，可通过 `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD/NACOS_ADDR/SA_TOKEN_SECRET` 覆盖。

```bash
# 注册
curl -X POST http://127.0.0.1:8080/auth/register -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456","nickname":"演示用户"}'
# 登录（响应含 satoken，后续请求头携带 satoken: <token>）
curl -X POST http://127.0.0.1:8080/auth/login -H "Content-Type: application/json" \
  -d '{"account":"demo","password":"123456"}'
# 分页
curl -X POST http://127.0.0.1:8080/business/mall/page -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"pageNum":1,"pageSize":10}'
```

## 6. 硬性约束（必须遵守）

1. `sql/00_init.sql` 以用户最新修改为准，**不要修改回去、不要改动内容**。
2. 不要删除已有代码注释，除非注释已过时或错误。
3. 主键（应用层雪花 ID）与外键（不建物理外键，仅索引+应用保证）规则不变。
4. 所有业务接口保持 POST + JSON 风格，不要回退成 GET/路径参数。
5. 网关模块禁止引入 `spring-boot-starter-web`（WebFlux 冲突）。
6. 用户自有文件 `docs/初始代码版本提示词.txt`、`docs/4.实体关系图.png` 不要修改或删除。
7. 新增功能优先复用现有模式：实体+Mapper+`BaseCrudController`、POST 请求 DTO、`Result` 统一返回。

## 7. 建议的后续任务（按优先级）

1. **种子数据**：为 business 架构编写示例数据 SQL（商场/楼层/分区/车位/商铺/POI/信标/导航图，含 PostGIS 几何），并初始化一个管理员账号与 RBAC 权限。
2. **接口文档**：在两个 biz 模块集成 Springdoc OpenAPI（注意网关是 WebFlux，不要放网关），输出可交互的 `/v3/api-docs`。
3. **一键环境**：补充 Docker Compose（Nacos + PostgreSQL + PostGIS），便于 DeepSeek Harness 环境直接启动联调。
4. **测试**：为核心服务（AuthService、NavService、ParkingRecordService、网关限流）补单元测试，关键查询用 Testcontainers-PostGIS 集成测试。
5. **定位算法**：基于 beacon + positioning_session 实现 RSSI 指纹/三角定位接口。
6. **转向指令**：在 RouteVO 中增加基于节点坐标/`ST_Azimuth` 的"直行/左转/右转/上楼"指令生成。
7. **网关统一鉴权**（可选增强）：引入 Sa-Token 网关插件，把鉴权收敛到网关。

## 8. 验收标准

- 在干净环境（JDK21 + Maven + Nacos + PostgreSQL/PostGIS）能按 README 一键构建并启动三个服务，且均注册到 Nacos。
- 数据库按 00/01/02 脚本初始化后，登录、车位搜索/附近查询、路径规划、停车记录等主链路接口可用。
- 新增代码保持：POST + JSON、中文注释、雪花 ID、逻辑删除、统一 Result、不破坏既有 SQL 与注释。

---

开始工作前，请先阅读「0. 相关文件」列出的全部文件，然后按「5. 运行方式」在本机或 Harness 环境中启动项目做冒烟验证，再继续「7. 建议的后续任务」。
