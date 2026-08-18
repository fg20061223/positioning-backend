# 商场可视化智能车位/商铺导航系统（后端）

基于 Spring Boot 3.5 + Spring Cloud 2025 + Nacos 的微服务后端，覆盖登录鉴权、商场空间数据、车位/商铺检索、室内路径规划与停车记录等接口。

## 技术栈与版本

| 组件 | 版本 |
| --- | --- |
| JDK | 21 |
| Spring Boot | 3.5.16 |
| Spring Cloud | 2025.0.3 |
| Spring Cloud Alibaba | 2025.0.0.0（Nacos 客户端 3.0.x） |
| 鉴权 | Sa-Token 1.45.0（JWT 无状态模式） |
| ORM | MyBatis-Plus 3.5.12 |
| 数据库 | PostgreSQL 16 + PostGIS（`postgis_36_sample` 库，`auth` / `business` 两个架构） |
| 构建 | Maven 3.9+ |

## 模块结构

```
positioning-backend
├── positioning-common                 # 公共模块: 统一返回/分页/业务异常
├── positioning-gateway                # 网关: 路由、限流(Caffeine)、熔断(Resilience4j)
├── positioning-auth
│   ├── positioning-auth-api           # 认证服务 Feign 契约 + 共享 DTO
│   └── positioning-auth-biz           # 认证服务实现: Sa-Token + auth 架构表
└── positioning-business
    ├── positioning-business-api       # 业务服务 Feign 契约（预留）
    └── positioning-business-biz       # 业务服务实现: business 架构表
```

| 服务 | 注册名 | 端口 | 数据库架构 |
| --- | --- | --- | --- |
| positioning-gateway | positioning-gateway | 8081 | - |
| positioning-auth-biz | positioning-auth | 8101 | auth |
| positioning-business-biz | positioning-business | 8102 | business |

## 环境准备

1. JDK 21（可用 `C:\Users\54316\.jdks\jbr-21.0.11` 等任意 JDK 21）。
2. Maven 3.9+。
3. Nacos（2.x/3.x），默认 `127.0.0.1:8848`。
4. PostgreSQL 16 + PostGIS，按 `sql/` 目录脚本初始化：
   - `sql/00_init.sql`：安装 `postgis`、`pg_trgm` 扩展（以你的最新版本为准）；
   - `sql/01_auth_db.sql`：建 `auth` 架构及用户中心表；
   - `sql/02_business_db.sql`：建 `business` 架构及业务表；
   - `sql/03_seed_auth.sql`：种子账号（admin/operator/demo，密码均 `123456`）+ RBAC 权限点；
   - `sql/04_seed_business.sql`：示例商场种子数据（3 楼层/32 车位/13 商铺/导航图等）。

## 启动

```bash
# 1. 编译（JDK21）
mvn clean package -DskipTests

# 2. 分别启动（或 IDE 中直接运行各 Application）
java -jar positioning-gateway/target/positioning-gateway-1.0.0-SNAPSHOT.jar
java -jar positioning-auth/positioning-auth-biz/target/positioning-auth-biz-1.0.0-SNAPSHOT.jar
java -jar positioning-business/positioning-business-biz/target/positioning-business-biz-1.0.0-SNAPSHOT.jar
```

默认数据库账号 `postgres/123456`，可通过环境变量覆盖：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`NACOS_ADDR`、`SA_TOKEN_SECRET`。

## 网关路由

| 路由 | 转发目标 |
| --- | --- |
| `/auth/**`、`/internal/**` | `lb://positioning-auth` |
| `/business/**` | `lb://positioning-business` |

网关提供全局限流（按 IP 令牌桶，默认 100 次/分钟，可在 `application.yml` 调整）与 Resilience4j 熔断降级。

## 接口约定

- 所有业务接口统一使用 `POST`，请求体与响应体均为 JSON（统一结构 `{"code":200,"message":"操作成功","data":...}`）。
- 分页、按 ID 查询/删除等操作使用统一请求体：`{"pageNum":1,"pageSize":10}`、`{"id":1}`。

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
