# 商场可视化智能车位/商铺导航系统

> 项目总览、架构设计与运行说明（主文档）。

基于 Spring Boot 3.5 + Spring Cloud 2025 + Nacos 的微服务后端，覆盖登录鉴权、商场空间数据、车位/商铺检索、室内路径规划与停车记录等接口。

**定位**：用户进入商场地下车库后，输入车位号或目标商铺名称，系统通过**蓝牙室内定位 + 路径可视化**，指引用户"往左走20米，右转进入B区"。

**核心技术壁垒**：空间数据的组织与可视化呈现（而非高并发）。

**核心场景**：蓝牙信标扫描上报 RSSI 定位用户所在楼层坐标；输入车位号/商铺名模糊搜索；路径规划并返回带几何的路径与分段指令；停车记录、收藏、导航历史、运营方空间数据维护。

## 数据库设计

- **数据库**：PostgreSQL 16 + PostGIS；单库 `postgis_36_sample`，两个架构：`auth`（用户中心）、`business`（业务）。
- **共 24 张表**：auth 6 张 + business 18 张，**所有字段均有中文注释**（`COMMENT ON`）。
- **主键**：应用层雪花 ID（BIGINT）；**不建物理外键**，跨表/跨架构关联只建索引，由应用保证一致性。
- **逻辑删除**：`deleted`（0/1），部分唯一索引 `WHERE deleted = 0`。

### 坐标与空间建模

- 室内几何统一"楼层本地平面坐标系"，单位米，SRID=0；距离用 `ST_Distance` 直接得米，无投影畸变。
- 分层：`mall → mall_floor → mall_zone → 空间要素`。
- 空间要素三几何列：`geom`（轮廓，落区判断）、`center_point`（中心，KNN 就近检索）、`entrance_point`（入口，导航终点）。
- 导航图：`nav_node`（节点+`is_accessible` 封路）、`nav_edge`（边长冗余 `distance_m`、成本权重 `weight`、`bidirectional`）、`floor_connect`（跨楼层绑定+折算成本 `cost_m`）。
- 文本搜索：`pg_trgm` GIN 索引（shop_name/keywords/space_no）。

### 表清单

**auth 架构**：sys_user、sys_role、sys_permission、sys_user_role、sys_role_permission、sys_login_log。

**business 架构**：mall、mall_floor、mall_zone、parking_space、shop_category、shop、poi、beacon、nav_node、nav_edge、floor_connect、mall_user、user_vehicle、parking_record、user_destination、nav_history、positioning_session、op_log。

### SQL 脚本

| 脚本 | 职责 | 备注 |
| --- | --- | --- |
| sql/00_init.sql | 安装 postgis、pg_trgm 扩展 | **以用户最新版本为准，不得改动** |
| sql/01_auth_db.sql | 建 auth schema 与 6 张表 | 含内置角色种子数据 |
| sql/02_business_db.sql | 建 business schema 与 18 张表 | |
| sql/03_seed_auth.sql | 种子账号（admin/operator/demo，密码均 123456）+ RBAC 权限点 | |
| sql/04_seed_business.sql | 示例商场种子数据（3 楼层/32 车位/13 商铺/导航图等） | |

## 技术栈与版本

| 组件 | 版本 |
| --- | --- |
| JDK | 21 |
| Spring Boot | 3.5.16 |
| Spring Cloud | 2025.0.3 |
| Spring Cloud Alibaba | 2025.0.0.0（Nacos 客户端 3.0.x） |
| 鉴权 | Sa-Token 1.45.0（JWT 无状态模式） |
| ORM | MyBatis-Plus 3.5.12（+ mybatis-plus-jsqlparser 分页模块） |
| 数据库 | PostgreSQL 16 + PostGIS（`postgis_36_sample` 库，`auth` / `business` 两个架构） |
| 缓存/限流 | Caffeine（网关限流） |
| 熔断 | Resilience4j（spring-cloud-starter-circuitbreaker-reactor-resilience4j） |
| 构建 | Maven 3.9+ |

## 模块结构（Maven 聚合）

```text
positioning-backend
├── pom.xml                              # 父工程：版本管理 + dependencyManagement
├── positioning-common                   # 公共模块（不依赖 Web 容器）
│   └── com.positioning.common
│       ├── api/Result.java              # 统一响应 {"code","message","data"}
│       ├── api/PageResult.java          # 统一分页
│       ├── dto/PageQuery.java           # 分页入参 {"pageNum","pageSize"}
│       ├── dto/IdRequest.java           # 按ID入参 {"id"}
│       └── exception/BizException.java  # 业务异常
├── positioning-gateway                  # 网关（WebFlux，端口 8081）
│   ├── GatewayApplication.java
│   ├── filter/RateLimitFilter.java      # Caffeine 令牌桶按IP限流
│   └── controller/FallbackController.java
├── positioning-auth
│   ├── positioning-auth-api             # Feign 契约 + DTO
│   │   ├── dto/UserDTO.java
│   │   └── client/UserFeignClient.java  # POST /internal/user/get
│   └── positioning-auth-biz             # 端口 8101，JDBC currentSchema=auth
│       ├── AuthApplication.java
│       ├── config/（Password/SaToken/StpInterface/MybatisPlus/OpenApi/MybatisTypeHandler）
│       ├── entity/（6 张 auth 表实体）
│       ├── mapper/（6 个 Mapper，含角色/权限联查）
│       ├── service/（AuthService + impl）
│       ├── controller/（AuthController、internal/UserFeignController）
│       └── exception/GlobalExceptionHandler.java
└── positioning-business
    ├── positioning-business-api         # Feign 契约（预留）
    │   ├── dto/（MallDTO/SpaceDTO/ShopDTO）
    │   └── client/（Mall/Space/ShopFeignClient，POST /internal/.../get）
    └── positioning-business-biz         # 端口 8102，JDBC currentSchema=business
        ├── BusinessApplication.java     # @EnableFeignClients(UserFeignClient)
        ├── config/（SaToken/StpInterface/MybatisPlus/OpenApi/WebConfig）
        ├── entity/（18 张业务表实体）
        ├── mapper/（18 个 Mapper，含 PostGIS @Select/@Update）
        ├── service/（NavService 路径规划、ParkingRecordService 停车联动）
        ├── dto/（查询/请求/响应 DTO）
        ├── controller/
        │   ├── BaseCrudController.java  # 通用 POST CRUD 基类
        │   ├── 业务控制器（Mall/Space/Shop/Poi/Beacon/NavNode/NavEdge/...）
        │   ├── FileController.java      # 文件上传（楼层平面图）
        │   └── internal/（InternalMall/Space/ShopController，实现预留 Feign 契约）
        └── exception/GlobalExceptionHandler.java
```

## 服务注册与配置

| 服务 | 注册名 | 端口 | 数据库架构 |
| --- | --- | --- | --- |
| positioning-gateway | positioning-gateway | 8081 | - |
| positioning-auth-biz | positioning-auth | 8101 | auth |
| positioning-business-biz | positioning-business | 8102 | business |

### 环境变量

`DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD/NACOS_ADDR/NACOS_USERNAME/NACOS_PASSWORD/SA_TOKEN_SECRET`（数据库默认账号 `postgres/123456`）。

## 网关设计

- **路由**：`/auth/**`、`/internal/**` → `lb://positioning-auth`；`/business/**` → `lb://positioning-business`；`/uploads/**` → `lb://positioning-business`（静态文件）。
- **限流**：`RateLimitFilter`（GlobalFilter，order=-100），Caffeine 令牌桶按客户端 IP，默认 100 次/分钟（`positioning.gateway.rate-limit.capacity/refill-per-minute` 可调），超限返回 429 JSON。
- **熔断**：每条路由挂 `CircuitBreaker` 过滤器 + `forward:/fallback/auth|business` 降级（Resilience4j 默认配置在 application.yml）。
- **跨域**：globalcors 全放开（开发用）。
- 网关不校验 token（各服务用 Sa-Token 自校验，如需统一收敛可后续增强）。

## 认证设计（Sa-Token + JWT）

- `POST /auth/register`：用户名/手机号唯一校验，BCrypt 加密，默认 `user_type=USER`、`status=1`。
- `POST /auth/login`：支持用户名或手机号；校验密码与状态；`StpUtil.login(userId)` 签发 JWT；写 `sys_login_log` 成功/失败日志；更新 `last_login_at`。
- `POST /auth/logout`、`POST /auth/me`：基于 `StpUtil`。
- **路由拦截**：除 `/auth/login`、`/auth/register`、`/internal/**`、`/actuator/**`、`/error`、Swagger 相关外全部要求登录。
- **RBAC**：`StpInterfaceImpl` 从 `sys_role/sys_user_role/sys_role_permission/sys_permission` 联查角色码与权限码。
- **Feign 内部接口**：`POST /internal/user/get`（JSON `{"id":...}`）返回 UserDTO，供业务服务调用。

## 业务设计

- 通用 CRUD：`BaseCrudController` 提供 `POST /page|get|create|update|delete`；专用控制器继承并叠加领域接口。
- **车位**：`/query`（mallId/floorId/status 分页）、`/search`（space_no 模糊）、`/nearby`（PostGIS KNN 空闲车位）、`/get-geometry`、`/update-geometry`、`/occupy`、`/release`。
- **商铺**：`/query`（多条件+关键词分页）、`/search`（pg_trgm ILIKE）、`/get-geometry`、`/update-geometry`。
- **信标**：`/query`、`/by-floor`（ACTIVE 列表，供定位扫描）。
- **导航**：节点 `POST /with-geometry`、`POST /update-geometry`（拖拽移动节点）；路径规划 `POST /business/nav/route`（Dijkstra，跨层连接按双向折算成本参与寻路）。
- **停车**：`/park`（入场，车位置 OCCUPIED）、`/end`（离场，车位置 FREE）、`/my`。
- **用户侧**：收藏 `/my`、导航历史 `/record|/my`、定位会话 `/start|/end|/active`，均自动绑定当前登录用户。
- **用户-商场绑定**：`POST /business/mall-user/create` 先经 Feign 调认证服务校验用户；`/user-info` 跨服务聚合用户信息。
- **文件上传**：`POST /business/file/upload`（楼层平面图，保存 `./uploads`，返回 `/uploads/xxx`）。
- **内部接口**（预留契约实现）：`POST /internal/mall|space|shop/get`，已放行登录拦截。

## 环境准备

1. JDK 21（可用 `C:\Users\54316\.jdks\jbr-21.0.11` 等任意 JDK 21）。
2. Maven 3.9+（本机可用阿里云镜像 `~/.m2/settings.xml`，首次构建需联网下载依赖）。
3. Nacos（2.x/3.x），默认 `127.0.0.1:8848`。
4. PostgreSQL 16 + PostGIS，按 `sql/` 目录脚本初始化（见上文"SQL 脚本"表）。

## 启动

```bash
# 1. 编译（JDK21）：建议先设置 JAVA_HOME
$env:JAVA_HOME='C:\Users\54316\.jdks\jbr-21.0.11'
mvn clean package -DskipTests

# 2. 启动顺序：先 Nacos、PostgreSQL → 网关 → 认证 → 业务
java -jar positioning-gateway/target/positioning-gateway-1.0.0-SNAPSHOT.jar
java -jar positioning-auth/positioning-auth-biz/target/positioning-auth-biz-1.0.0-SNAPSHOT.jar
java -jar positioning-business/positioning-business-biz/target/positioning-business-biz-1.0.0-SNAPSHOT.jar
```

## 接口约定

- 所有业务接口统一使用 `POST`，请求体与响应体均为 JSON（统一结构 `{"code":200,"message":"操作成功","data":...}`）。
- 分页、按 ID 查询/删除等操作使用统一请求体：`{"pageNum":1,"pageSize":10}`、`{"id":1}`。
- 需登录接口携带请求头 `satoken: <JWT>`。
- **错误码**：200 成功 / 400 参数或业务错误 / 401 未登录 / 403 无权限 / 404 不存在 / 429 限流 / 500 系统异常 / 503 服务不可用。

## 典型调用

```bash
# 注册
curl -X POST http://127.0.0.1:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456","nickname":"演示用户"}'

# 登录（返回 satoken JWT）
curl -X POST http://127.0.0.1:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"demo","password":"123456"}'

# 携带 token 访问业务接口（分页）
curl -X POST http://127.0.0.1:8081/business/mall/page \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"pageNum":1,"pageSize":10}'

# 附近空闲车位（楼层本地米制坐标 x,y）
curl -X POST http://127.0.0.1:8081/business/space/nearby \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"mallId":1,"x":10.5,"y":20.3,"limit":5}'

# 路径规划
curl -X POST http://127.0.0.1:8081/business/nav/route \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"mallId":1,"fromNodeId":101,"toNodeId":205}'
```

## 日志（Logback）

全局日志配置位于 `positioning-common/src/main/resources/logback-spring.xml`，三个服务统一生效：

| 输出 | 文件 | 说明 |
| --- | --- | --- |
| 全部日志 | `logs/{服务名}/{服务名}-all.log` | 所有级别，按日期+大小滚动，保留 30 天 |
| 异常日志 | `logs/{服务名}/{服务名}-error.log` | 仅 ERROR（含异常堆栈），按日期+大小滚动，保留 30 天 |
| 控制台 | stdout | 全部级别（开发调试） |

- 滚动文件名带日期：`xxx-all.2026-08-17.0.log`（单文件超 100MB 时 `.0/.1/.2` 递增）
- 可用环境变量覆盖：`LOG_PATH`（日志根目录，默认 `./logs`）、`LOG_LEVEL`（根级别，默认 INFO）

## 说明与约定

- 主键由应用层雪花 ID 生成；跨库/跨架构不做物理外键，由应用保证一致性。
- Sa-Token 使用 **JWT 无状态模式（`token-style: jwt`，StpLogicJwtForStateless）**，认证服务与业务服务共享 `SA_TOKEN_SECRET`；网关不做鉴权，业务接口由各服务统一校验。
  - 注意：不要使用 `jwt-simple`（token 虽是 JWT，但 loginId 映射仍存本地 Dao，多服务场景下业务服务会报 401）。
- PostGIS 几何列（`geom`/`center_point`/`entrance_point` 等）在通用 CRUD 中不读写，车位/商铺几何通过 `GET/PUT .../{id}/geometry` 接口以 GeoJSON 读写；导航节点创建必须走 `POST /business/nav-node/with-geometry`。
- JDBC 连接通过 `options=-csearch_path=<schema>,public` 保留 public 搜索路径（PostGIS/pg_trgm 扩展函数所在架构）。
- 跨服务调用示例：`business-biz` 通过 OpenFeign 调用 `positioning-auth` 的 `/internal/user` 查询用户（见 `MallUserController`）。
- 业务服务的 `api` 子模块为预留 Feign 契约，内部接口 `/internal/mall|space|shop` 已实现，供后续服务直接声明使用。

## 关键实现细节

- **雪花 ID**：`@TableId(type = IdType.ASSIGN_ID)`；节点等手工构造用 `IdWorker.getId()`。
- **审计字段**：`MyMetaObjectHandler` 自动填充 `createdAt/updatedAt`（`strictInsertFill/strictUpdateFill`，无该字段的表自动跳过）。
- **逻辑删除**：`@TableLogic` + 全局配置 `logic-delete-field: deleted`。
- **分页**：`PaginationInnerInterceptor(DbType.POSTGRE_SQL)`；MyBatis-Plus 3.5.9+ 需依赖 `mybatis-plus-jsqlparser`。
- **几何列**：实体中 String 类型几何字段统一 `@TableField(select=false, insertStrategy=NEVER, updateStrategy=NEVER)`；空间读写走专用 SQL（`ST_AsGeoJSON` / `ST_SetSRID(ST_GeomFromGeoJSON(...),0)` / `ST_Centroid` / `<->` KNN）。
  - **注意**：`ST_GeomFromGeoJSON` 两参重载（`(geo,srid)`）在部分 PostGIS 版本不存在，统一用 `ST_SetSRID(ST_GeomFromGeoJSON(geo),0)`。
- **TIMESTAMPTZ 兼容**：实体用 `LocalDateTime`、DB 用 `TIMESTAMPTZ`，通过全局 TypeHandler `PostgresTimestamptzTypeHandler` 转换。
- **Sa-Token JWT**：认证与业务服务 `token-name: satoken`、`token-style: jwt`（StpLogicJwtForStateless）、共享 `jwt-secret-key`；业务服务经 `StpUtil.getLoginIdAsLong()` 取用户。
- **Feign**：business-biz 经 `@EnableFeignClients(clients = UserFeignClient.class)` 调用 auth；auth 侧 `/internal/**` 放行拦截。
- **网关包名坑**：Caffeine 的 Maven 坐标是 `com.github.ben-manes:caffeine`，但 Java 包名是 `com.github.benmanes.caffeine`（无连字符）。

## 已知注意事项

- `sql/00_init.sql` 只装扩展不建库（用户已按连接后直接执行扩展安装调整），**不要改回**。
- 通用 CRUD 不写几何列：车位/商铺需先 `create` 再用几何接口更新；导航节点必须用 `with-geometry` 创建、`update-geometry` 移动（`geom` NOT NULL）。
- 网关路由把 `/internal/**` 指向 auth；business 的 `/internal/**` 仅服务间 Feign 直接调用，不走网关。
- 业务服务 Sa-Token 拦截已放行 `/internal/**`、`/uploads/**` 与 Swagger 相关路径。

## 建议后续任务

1. **单元/集成测试**：AuthService、NavService、ParkingRecordService、网关限流；PostGIS 查询用 Testcontainers。
2. **Docker Compose**：Nacos + PostgreSQL(PostGIS) 一键环境。
3. **定位算法**：beacon RSSI 指纹/三角定位与 positioning_session 联动。
4. **转向指令**：基于节点坐标/`ST_Azimuth` 生成"直行/左转/右转/上楼"指令并扩展 RouteVO。
5. 可选：网关 Sa-Token 统一鉴权、车位状态 Redis 缓存、MyBatis-Plus 代码生成器。

> 注：种子数据、接口文档（Springdoc OpenAPI）、Logback 日志已实现；PC 管理后台前端交接包见 `docs/pcManager/`。

## 文件索引

| 文件 | 说明 |
| --- | --- |
| `README.md` | 本文件：项目总览、架构设计与运行说明（主文档） |
| `pom.xml` | 父工程版本管理 |
| `docs/01-database-design.md` | 数据库设计文档 |
| `docs/管理后台前端规划.md` | PC 管理后台前端规划（技术选型/接口清单/页面结构/导航图编辑器方案） |
| `docs/pcManager/` | PC 管理后台前端交接包（PROJECT_CONTEXT / API_CONTRACT / HANDOFF_TEMPLATE） |
| `docs/DeepSeek-Harness-交接提示词.md` | 可直接粘贴给新执行环境的主提示词 |
| `docs/初始代码版本提示词.txt` | 用户历史提示词（勿动） |
| `docs/4.实体关系图.png` | 用户提供的 ER 图（勿动） |
| `sql/00_init.sql` ~ `sql/04_seed_business.sql` | 数据库脚本 |
