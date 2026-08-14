-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 目标数据库: postgis_36_sample
-- 目标架构: business（业务服务）
-- 前置: 先执行 sql/00_init.sql 完成建库与扩展安装
--       （postgis / pg_trgm 为数据库级扩展, 已由 00_init 安装）
--
-- 空间坐标约定(重要):
--   * 所有室内几何使用"楼层本地平面坐标系", 单位: 米
--   * SRID 统一为 0; 距离计算直接用 ST_Distance 得到米, 无投影畸变
--   * 要素表同时保存 geom(面/线)、center_point(中心)、entrance_point(入口)
--
-- 约定:
--   * 主键使用应用层雪花ID (MyBatis-Plus IdType.ASSIGN_ID)
--   * 逻辑删除 deleted: 0=正常 1=已删除
--   * 不建物理外键, 跨表关联全部走索引 + 应用层校验
--   * 每个字段均带中文注释
-- ============================================================

-- 创建业务架构（若已存在则跳过）
CREATE SCHEMA IF NOT EXISTS business;

COMMENT ON SCHEMA business IS '业务服务架构：商场空间数据、导航图、停车与导航业务';

-- 以下未加架构前缀的对象统一创建/查询于 business 架构
-- （保留 public 在搜索路径中, 用于解析 PostGIS/pg_trgm 等扩展函数）
SET search_path TO business, public;

-- ---------- 商场 ----------
CREATE TABLE mall (
    id         BIGINT       NOT NULL,
    mall_code  VARCHAR(32)  NOT NULL,
    mall_name  VARCHAR(128) NOT NULL,
    province   VARCHAR(64),
    city       VARCHAR(64),
    district   VARCHAR(64),
    address    VARCHAR(255),
    lng        DOUBLE PRECISION,
    lat        DOUBLE PRECISION,
    status     SMALLINT     NOT NULL DEFAULT 1,
    remark     VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_mall PRIMARY KEY (id)
);

COMMENT ON TABLE mall IS '商场主数据表';
COMMENT ON COLUMN mall.id IS '商场ID（雪花ID，应用层生成）';
COMMENT ON COLUMN mall.mall_code IS '商场编码（全局唯一）';
COMMENT ON COLUMN mall.mall_name IS '商场名称';
COMMENT ON COLUMN mall.province IS '所在省份';
COMMENT ON COLUMN mall.city IS '所在城市';
COMMENT ON COLUMN mall.district IS '所在区县';
COMMENT ON COLUMN mall.address IS '详细地址';
COMMENT ON COLUMN mall.lng IS '商场经度（WGS84）';
COMMENT ON COLUMN mall.lat IS '商场纬度（WGS84）';
COMMENT ON COLUMN mall.status IS '商场状态：1=营业 0=停用';
COMMENT ON COLUMN mall.remark IS '备注';
COMMENT ON COLUMN mall.created_at IS '创建时间';
COMMENT ON COLUMN mall.updated_at IS '更新时间';
COMMENT ON COLUMN mall.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_mall_code ON mall (mall_code) WHERE deleted = 0;

-- ---------- 楼层 ----------
CREATE TABLE mall_floor (
    id         BIGINT        NOT NULL,
    mall_id    BIGINT        NOT NULL,
    floor_code VARCHAR(16)   NOT NULL,
    floor_name VARCHAR(64),
    sort_order INT           NOT NULL DEFAULT 0,
    status     SMALLINT      NOT NULL DEFAULT 1,
    width_m    DOUBLE PRECISION,
    height_m   DOUBLE PRECISION,
    remark     VARCHAR(255),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted    SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_mall_floor PRIMARY KEY (id)
);

COMMENT ON TABLE mall_floor IS '商场楼层表';
COMMENT ON COLUMN mall_floor.id IS '楼层ID（雪花ID，应用层生成）';
COMMENT ON COLUMN mall_floor.mall_id IS '所属商场ID';
COMMENT ON COLUMN mall_floor.floor_code IS '楼层编码，如 B3/B2/B1/1F/2F';
COMMENT ON COLUMN mall_floor.floor_name IS '楼层名称';
COMMENT ON COLUMN mall_floor.sort_order IS '楼层排序号（地下到地上递增）';
COMMENT ON COLUMN mall_floor.status IS '楼层状态：1=开放 0=关闭';
COMMENT ON COLUMN mall_floor.width_m IS '楼层平面图宽度（米）';
COMMENT ON COLUMN mall_floor.height_m IS '楼层平面图高度（米）';
COMMENT ON COLUMN mall_floor.remark IS '备注';
COMMENT ON COLUMN mall_floor.created_at IS '创建时间';
COMMENT ON COLUMN mall_floor.updated_at IS '更新时间';
COMMENT ON COLUMN mall_floor.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_floor ON mall_floor (mall_id, floor_code) WHERE deleted = 0;
CREATE INDEX idx_floor_mall ON mall_floor (mall_id) WHERE deleted = 0;

-- ---------- 分区 ----------
CREATE TABLE mall_zone (
    id         BIGINT       NOT NULL,
    mall_id    BIGINT       NOT NULL,
    floor_id   BIGINT       NOT NULL,
    zone_code  VARCHAR(32)  NOT NULL,
    zone_name  VARCHAR(64),
    geom       geometry(Geometry, 0),
    color      VARCHAR(16),
    sort_order INT          NOT NULL DEFAULT 0,
    remark     VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_mall_zone PRIMARY KEY (id)
);

COMMENT ON TABLE mall_zone IS '分区表：楼层内的B区/A区等区域';
COMMENT ON COLUMN mall_zone.id IS '分区ID（雪花ID，应用层生成）';
COMMENT ON COLUMN mall_zone.mall_id IS '所属商场ID';
COMMENT ON COLUMN mall_zone.floor_id IS '所属楼层ID';
COMMENT ON COLUMN mall_zone.zone_code IS '分区编码，如 B区/A区';
COMMENT ON COLUMN mall_zone.zone_name IS '分区名称';
COMMENT ON COLUMN mall_zone.geom IS '分区轮廓（楼层本地米制坐标），支持多边形/多多边形';
COMMENT ON COLUMN mall_zone.color IS '分区在地图上的展示颜色';
COMMENT ON COLUMN mall_zone.sort_order IS '排序号';
COMMENT ON COLUMN mall_zone.remark IS '备注';
COMMENT ON COLUMN mall_zone.created_at IS '创建时间';
COMMENT ON COLUMN mall_zone.updated_at IS '更新时间';
COMMENT ON COLUMN mall_zone.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_zone ON mall_zone (mall_id, floor_id, zone_code) WHERE deleted = 0;
CREATE INDEX idx_zone_floor ON mall_zone (floor_id);
CREATE INDEX idx_zone_geom  ON mall_zone USING GIST (geom);

-- ---------- 车位 ----------
CREATE TABLE parking_space (
    id             BIGINT       NOT NULL,
    mall_id        BIGINT       NOT NULL,
    floor_id       BIGINT       NOT NULL,
    zone_id        BIGINT,
    space_no       VARCHAR(32)  NOT NULL,
    space_type     VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    status         VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    occupy_source  VARCHAR(20),
    geom           geometry(Geometry, 0),
    center_point   geometry(Point, 0),
    entrance_point geometry(Point, 0),
    sort_order     INT          NOT NULL DEFAULT 0,
    remark         VARCHAR(255),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_parking_space PRIMARY KEY (id)
);

COMMENT ON TABLE parking_space IS '车位表';
COMMENT ON COLUMN parking_space.id IS '车位ID（雪花ID，应用层生成）';
COMMENT ON COLUMN parking_space.mall_id IS '所属商场ID';
COMMENT ON COLUMN parking_space.floor_id IS '所属楼层ID';
COMMENT ON COLUMN parking_space.zone_id IS '所属分区ID';
COMMENT ON COLUMN parking_space.space_no IS '车位编号，如 B3-012';
COMMENT ON COLUMN parking_space.space_type IS '车位类型：NORMAL=普通 DISABLED=无障碍 CHARGING=充电 COMPACT=微型 MECHANICAL=机械 MOTHER_CHILD=母婴';
COMMENT ON COLUMN parking_space.status IS '车位状态：FREE=空闲 OCCUPIED=占用 LOCKED=锁定 FAULT=故障';
COMMENT ON COLUMN parking_space.occupy_source IS '占用信息来源：APP=用户上报 CAMERA=摄像头 MAGNET=地磁 GATE=道闸 MANUAL=人工';
COMMENT ON COLUMN parking_space.geom IS '车位轮廓（楼层本地米制坐标）';
COMMENT ON COLUMN parking_space.center_point IS '车位中心点（用于就近检索）';
COMMENT ON COLUMN parking_space.entrance_point IS '车位开口点（作为导航终点）';
COMMENT ON COLUMN parking_space.sort_order IS '排序号';
COMMENT ON COLUMN parking_space.remark IS '备注';
COMMENT ON COLUMN parking_space.created_at IS '创建时间';
COMMENT ON COLUMN parking_space.updated_at IS '更新时间';
COMMENT ON COLUMN parking_space.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_space_no ON parking_space (mall_id, space_no) WHERE deleted = 0;
CREATE INDEX idx_space_mall_floor  ON parking_space (mall_id, floor_id);
CREATE INDEX idx_space_status      ON parking_space (mall_id, status) WHERE deleted = 0;
CREATE INDEX idx_space_center_gist ON parking_space USING GIST (center_point);
CREATE INDEX idx_space_geom_gist   ON parking_space USING GIST (geom);
CREATE INDEX idx_space_no_trgm     ON parking_space USING GIN (space_no gin_trgm_ops) WHERE deleted = 0;

-- ---------- 商铺分类 ----------
CREATE TABLE shop_category (
    id         BIGINT       NOT NULL,
    mall_id    BIGINT,
    parent_id  BIGINT       NOT NULL DEFAULT 0,
    cat_name   VARCHAR(64)  NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_shop_category PRIMARY KEY (id)
);

COMMENT ON TABLE shop_category IS '商铺分类表';
COMMENT ON COLUMN shop_category.id IS '分类ID（雪花ID，应用层生成）';
COMMENT ON COLUMN shop_category.mall_id IS '所属商场ID（空=平台通用分类）';
COMMENT ON COLUMN shop_category.parent_id IS '父分类ID，0=根分类';
COMMENT ON COLUMN shop_category.cat_name IS '分类名称';
COMMENT ON COLUMN shop_category.sort_order IS '排序号';
COMMENT ON COLUMN shop_category.status IS '分类状态：1=启用 0=停用';
COMMENT ON COLUMN shop_category.created_at IS '创建时间';
COMMENT ON COLUMN shop_category.updated_at IS '更新时间';
COMMENT ON COLUMN shop_category.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_shop_category ON shop_category (COALESCE(mall_id, 0), cat_name) WHERE deleted = 0;

-- ---------- 商铺 ----------
CREATE TABLE shop (
    id             BIGINT        NOT NULL,
    mall_id        BIGINT        NOT NULL,
    floor_id       BIGINT        NOT NULL,
    zone_id        BIGINT,
    shop_no        VARCHAR(32),
    shop_name      VARCHAR(128)  NOT NULL,
    short_name     VARCHAR(64),
    category_id    BIGINT,
    brand          VARCHAR(64),
    phone          VARCHAR(20),
    logo_url       VARCHAR(512),
    description    TEXT,
    keywords       TEXT,
    geom           geometry(Geometry, 0),
    center_point   geometry(Point, 0),
    entrance_point geometry(Point, 0),
    status         VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    sort_order     INT           NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted        SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_shop PRIMARY KEY (id)
);

COMMENT ON TABLE shop IS '商铺表';
COMMENT ON COLUMN shop.id IS '商铺ID（雪花ID，应用层生成）';
COMMENT ON COLUMN shop.mall_id IS '所属商场ID';
COMMENT ON COLUMN shop.floor_id IS '所属楼层ID';
COMMENT ON COLUMN shop.zone_id IS '所属分区ID';
COMMENT ON COLUMN shop.shop_no IS '商铺编号，如 1F-101';
COMMENT ON COLUMN shop.shop_name IS '商铺名称（搜索主字段）';
COMMENT ON COLUMN shop.short_name IS '商铺简称';
COMMENT ON COLUMN shop.category_id IS '商铺分类ID';
COMMENT ON COLUMN shop.brand IS '品牌名称';
COMMENT ON COLUMN shop.phone IS '联系电话';
COMMENT ON COLUMN shop.logo_url IS '商铺Logo图片地址';
COMMENT ON COLUMN shop.description IS '商铺简介';
COMMENT ON COLUMN shop.keywords IS '搜索关键词，逗号分隔，如 火锅,川菜,海底捞';
COMMENT ON COLUMN shop.geom IS '商铺轮廓（楼层本地米制坐标）';
COMMENT ON COLUMN shop.center_point IS '商铺中心点（用于就近检索）';
COMMENT ON COLUMN shop.entrance_point IS '商铺入口点（作为导航终点）';
COMMENT ON COLUMN shop.status IS '商铺状态：OPEN=营业 DECORATING=装修 CLOSED=关闭';
COMMENT ON COLUMN shop.sort_order IS '排序号';
COMMENT ON COLUMN shop.created_at IS '创建时间';
COMMENT ON COLUMN shop.updated_at IS '更新时间';
COMMENT ON COLUMN shop.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_shop_no ON shop (mall_id, shop_no) WHERE deleted = 0 AND shop_no <> '';
CREATE INDEX idx_shop_mall_floor ON shop (mall_id, floor_id);
CREATE INDEX idx_shop_category    ON shop (category_id);
CREATE INDEX idx_shop_name_trgm   ON shop USING GIN (shop_name gin_trgm_ops);
CREATE INDEX idx_shop_key_trgm    ON shop USING GIN (keywords gin_trgm_ops);
CREATE INDEX idx_shop_center_gist ON shop USING GIST (center_point);

-- ---------- 基础设施/兴趣点 ----------
CREATE TABLE poi (
    id             BIGINT       NOT NULL,
    mall_id        BIGINT       NOT NULL,
    floor_id       BIGINT       NOT NULL,
    poi_type       VARCHAR(32)  NOT NULL,
    poi_name       VARCHAR(64),
    geom           geometry(Geometry, 0),
    center_point   geometry(Point, 0),
    entrance_point geometry(Point, 0),
    status         SMALLINT     NOT NULL DEFAULT 1,
    remark         VARCHAR(255),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_poi PRIMARY KEY (id)
);

COMMENT ON TABLE poi IS '基础设施/兴趣点表：电梯、扶梯、楼梯、卫生间、出入口、服务台等';
COMMENT ON COLUMN poi.id IS '设施ID（雪花ID，应用层生成）';
COMMENT ON COLUMN poi.mall_id IS '所属商场ID';
COMMENT ON COLUMN poi.floor_id IS '所属楼层ID';
COMMENT ON COLUMN poi.poi_type IS '设施类型：ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯 TOILET=卫生间 ENTRANCE=商场出入口 EXIT=车库出口 SERVICE_DESK=服务台 NURSING_ROOM=母婴室 ATM=取款机';
COMMENT ON COLUMN poi.poi_name IS '设施名称';
COMMENT ON COLUMN poi.geom IS '设施轮廓（楼层本地米制坐标）';
COMMENT ON COLUMN poi.center_point IS '设施中心点（用于就近检索）';
COMMENT ON COLUMN poi.entrance_point IS '设施入口点（作为导航终点）';
COMMENT ON COLUMN poi.status IS '设施状态：1=可用 0=不可用';
COMMENT ON COLUMN poi.remark IS '备注';
COMMENT ON COLUMN poi.created_at IS '创建时间';
COMMENT ON COLUMN poi.updated_at IS '更新时间';
COMMENT ON COLUMN poi.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE INDEX idx_poi_mall_floor  ON poi (mall_id, floor_id);
CREATE INDEX idx_poi_type        ON poi (mall_id, floor_id, poi_type);
CREATE INDEX idx_poi_center_gist ON poi USING GIST (center_point);

-- ---------- 蓝牙信标 ----------
CREATE TABLE beacon (
    id             BIGINT       NOT NULL,
    mall_id        BIGINT       NOT NULL,
    floor_id       BIGINT       NOT NULL,
    uuid           VARCHAR(36)  NOT NULL,
    major          INT          NOT NULL,
    minor          INT          NOT NULL,
    mac            VARCHAR(20),
    beacon_type    VARCHAR(20)  NOT NULL DEFAULT 'IBEACON',
    position_geom  geometry(Point, 0),
    tx_power       SMALLINT     NOT NULL DEFAULT -59,
    battery        SMALLINT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_report_at TIMESTAMPTZ,
    remark         VARCHAR(255),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_beacon PRIMARY KEY (id)
);

COMMENT ON TABLE beacon IS '蓝牙信标表：室内定位的物理设备点位';
COMMENT ON COLUMN beacon.id IS '信标ID（雪花ID，应用层生成）';
COMMENT ON COLUMN beacon.mall_id IS '所属商场ID';
COMMENT ON COLUMN beacon.floor_id IS '所属楼层ID';
COMMENT ON COLUMN beacon.uuid IS 'iBeacon广播UUID';
COMMENT ON COLUMN beacon.major IS 'iBeacon major编号（建议按商场编码）';
COMMENT ON COLUMN beacon.minor IS 'iBeacon minor编号（建议按楼层+点位编码）';
COMMENT ON COLUMN beacon.mac IS '信标MAC地址';
COMMENT ON COLUMN beacon.beacon_type IS '信标协议类型：IBEACON/EDDYSTONE';
COMMENT ON COLUMN beacon.position_geom IS '信标布点坐标（楼层本地米制坐标）';
COMMENT ON COLUMN beacon.tx_power IS '1米处RSSI参考值（定位校准用）';
COMMENT ON COLUMN beacon.battery IS '信标电量百分比';
COMMENT ON COLUMN beacon.status IS '信标状态：ACTIVE=正常 INACTIVE=停用 FAULT=故障';
COMMENT ON COLUMN beacon.last_report_at IS '最近一次心跳上报时间';
COMMENT ON COLUMN beacon.remark IS '备注';
COMMENT ON COLUMN beacon.created_at IS '创建时间';
COMMENT ON COLUMN beacon.updated_at IS '更新时间';
COMMENT ON COLUMN beacon.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_beacon_identity ON beacon (mall_id, uuid, major, minor) WHERE deleted = 0;
CREATE INDEX idx_beacon_floor ON beacon (mall_id, floor_id);
CREATE INDEX idx_beacon_gist  ON beacon USING GIST (position_geom);

-- ---------- 导航图: 节点 ----------
CREATE TABLE nav_node (
    id             BIGINT        NOT NULL,
    mall_id        BIGINT        NOT NULL,
    floor_id       BIGINT        NOT NULL,
    node_type      VARCHAR(20)   NOT NULL DEFAULT 'WAYPOINT',
    name           VARCHAR(64),
    geom           geometry(Point, 0) NOT NULL,
    is_accessible  BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order     INT           NOT NULL DEFAULT 0,
    remark         VARCHAR(255),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted        SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_nav_node PRIMARY KEY (id)
);

COMMENT ON TABLE nav_node IS '导航节点表：通道点、岔路口、门、电梯口、车位入口、商铺入口等';
COMMENT ON COLUMN nav_node.id IS '节点ID（雪花ID，应用层生成）';
COMMENT ON COLUMN nav_node.mall_id IS '所属商场ID';
COMMENT ON COLUMN nav_node.floor_id IS '所属楼层ID';
COMMENT ON COLUMN nav_node.node_type IS '节点类型：WAYPOINT=通道点 JUNCTION=岔路口 DOOR=门 ELEVATOR=电梯口 ESCALATOR=扶梯口 STAIR=楼梯口 SPACE_ENTRY=车位入口 SHOP_ENTRY=商铺入口 POI_ENTRY=设施入口';
COMMENT ON COLUMN nav_node.name IS '节点名称';
COMMENT ON COLUMN nav_node.geom IS '节点坐标（楼层本地米制坐标）';
COMMENT ON COLUMN nav_node.is_accessible IS '是否可通行：true=可通行 false=通道封闭';
COMMENT ON COLUMN nav_node.sort_order IS '排序号';
COMMENT ON COLUMN nav_node.remark IS '备注';
COMMENT ON COLUMN nav_node.created_at IS '创建时间';
COMMENT ON COLUMN nav_node.updated_at IS '更新时间';
COMMENT ON COLUMN nav_node.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE INDEX idx_nav_node_floor ON nav_node (mall_id, floor_id);
CREATE INDEX idx_nav_node_gist  ON nav_node USING GIST (geom);

-- ---------- 导航图: 边 ----------
CREATE TABLE nav_edge (
    id            BIGINT        NOT NULL,
    mall_id       BIGINT        NOT NULL,
    from_node_id  BIGINT        NOT NULL,
    to_node_id    BIGINT        NOT NULL,
    edge_type     VARCHAR(20)   NOT NULL DEFAULT 'WALK',
    distance_m    DOUBLE PRECISION NOT NULL,
    weight        DOUBLE PRECISION NOT NULL DEFAULT 1,
    geom          geometry(LineString, 0),
    bidirectional BOOLEAN       NOT NULL DEFAULT TRUE,
    status        SMALLINT      NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_nav_edge PRIMARY KEY (id)
);

COMMENT ON TABLE nav_edge IS '导航边表：连通导航节点的通行线段';
COMMENT ON COLUMN nav_edge.id IS '边ID（雪花ID，应用层生成）';
COMMENT ON COLUMN nav_edge.mall_id IS '所属商场ID';
COMMENT ON COLUMN nav_edge.from_node_id IS '起始节点ID';
COMMENT ON COLUMN nav_edge.to_node_id IS '终点节点ID';
COMMENT ON COLUMN nav_edge.edge_type IS '边类型：WALK=步行 ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯';
COMMENT ON COLUMN nav_edge.distance_m IS '边长（米），路径规划直接使用';
COMMENT ON COLUMN nav_edge.weight IS '通行成本系数（上下楼/绕行加权，默认1）';
COMMENT ON COLUMN nav_edge.geom IS '边几何（曲线通道存实际线型，直线通道可为空）';
COMMENT ON COLUMN nav_edge.bidirectional IS '是否双向通行：true=双向 false=单向';
COMMENT ON COLUMN nav_edge.status IS '边状态：1=可用 0=不可用';
COMMENT ON COLUMN nav_edge.created_at IS '创建时间';
COMMENT ON COLUMN nav_edge.updated_at IS '更新时间';
COMMENT ON COLUMN nav_edge.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_nav_edge ON nav_edge (from_node_id, to_node_id) WHERE deleted = 0;
CREATE INDEX idx_nav_edge_to   ON nav_edge (to_node_id);
CREATE INDEX idx_nav_edge_mall ON nav_edge (mall_id);
CREATE INDEX idx_nav_edge_gist ON nav_edge USING GIST (geom);

-- ---------- 跨楼层连接 ----------
CREATE TABLE floor_connect (
    id            BIGINT        NOT NULL,
    mall_id       BIGINT        NOT NULL,
    from_floor_id BIGINT        NOT NULL,
    to_floor_id   BIGINT        NOT NULL,
    connect_type  VARCHAR(20)   NOT NULL,
    from_node_id  BIGINT        NOT NULL,
    to_node_id    BIGINT        NOT NULL,
    cost_m        DOUBLE PRECISION NOT NULL DEFAULT 20,
    geom          geometry(LineString, 0),
    status        SMALLINT      NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_floor_connect PRIMARY KEY (id)
);

COMMENT ON TABLE floor_connect IS '跨楼层连接表：电梯/扶梯/楼梯的上下层节点绑定';
COMMENT ON COLUMN floor_connect.id IS '跨层连接ID（雪花ID，应用层生成）';
COMMENT ON COLUMN floor_connect.mall_id IS '所属商场ID';
COMMENT ON COLUMN floor_connect.from_floor_id IS '起始楼层ID';
COMMENT ON COLUMN floor_connect.to_floor_id IS '目标楼层ID';
COMMENT ON COLUMN floor_connect.connect_type IS '连接方式：ELEVATOR=电梯 ESCALATOR=扶梯 STAIR=楼梯';
COMMENT ON COLUMN floor_connect.from_node_id IS '起始层导航节点ID';
COMMENT ON COLUMN floor_connect.to_node_id IS '目标层导航节点ID';
COMMENT ON COLUMN floor_connect.cost_m IS '跨层折算成本（米），电梯等待/绕行按此加权';
COMMENT ON COLUMN floor_connect.geom IS '跨层连接线几何（可为空）';
COMMENT ON COLUMN floor_connect.status IS '连接状态：1=可用 0=不可用';
COMMENT ON COLUMN floor_connect.created_at IS '创建时间';
COMMENT ON COLUMN floor_connect.updated_at IS '更新时间';
COMMENT ON COLUMN floor_connect.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_floor_connect ON floor_connect (from_node_id, to_node_id) WHERE deleted = 0;
CREATE INDEX idx_floor_connect_floor ON floor_connect (from_floor_id, to_floor_id);

-- ---------- 用户-商场绑定 ----------
CREATE TABLE mall_user (
    id         BIGINT       NOT NULL,
    mall_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    role_code  VARCHAR(32)  NOT NULL DEFAULT 'USER',
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_mall_user PRIMARY KEY (id)
);

COMMENT ON TABLE mall_user IS '用户-商场绑定表：用户在具体商场内的角色';
COMMENT ON COLUMN mall_user.id IS '绑定ID（雪花ID，应用层生成）';
COMMENT ON COLUMN mall_user.mall_id IS '商场ID';
COMMENT ON COLUMN mall_user.user_id IS '用户ID（跨库引用auth_db）';
COMMENT ON COLUMN mall_user.role_code IS '商场内角色：USER=普通用户 MALL_ADMIN=商场管理员 OPERATOR=运营人员 MERCHANT=商户';
COMMENT ON COLUMN mall_user.status IS '绑定状态：1=生效 0=失效';
COMMENT ON COLUMN mall_user.created_at IS '创建时间';
COMMENT ON COLUMN mall_user.updated_at IS '更新时间';
COMMENT ON COLUMN mall_user.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_mall_user ON mall_user (mall_id, user_id) WHERE deleted = 0;
CREATE INDEX idx_mall_user_user ON mall_user (user_id);

-- ---------- 用户车辆 ----------
CREATE TABLE user_vehicle (
    id         BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    plate_no   VARCHAR(16)  NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_vehicle PRIMARY KEY (id)
);

COMMENT ON TABLE user_vehicle IS '用户车辆表：维护用户常用车牌';
COMMENT ON COLUMN user_vehicle.id IS '车辆ID（雪花ID，应用层生成）';
COMMENT ON COLUMN user_vehicle.user_id IS '所属用户ID';
COMMENT ON COLUMN user_vehicle.plate_no IS '车牌号';
COMMENT ON COLUMN user_vehicle.is_default IS '是否默认车辆：true=是 false=否';
COMMENT ON COLUMN user_vehicle.status IS '车辆状态：1=正常 0=失效';
COMMENT ON COLUMN user_vehicle.created_at IS '创建时间';
COMMENT ON COLUMN user_vehicle.updated_at IS '更新时间';
COMMENT ON COLUMN user_vehicle.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_user_vehicle ON user_vehicle (user_id, plate_no) WHERE deleted = 0;
CREATE INDEX idx_vehicle_user ON user_vehicle (user_id);

-- ---------- 停车记录 ----------
CREATE TABLE parking_record (
    id         BIGINT       NOT NULL,
    user_id    BIGINT,
    mall_id    BIGINT       NOT NULL,
    space_id   BIGINT,
    plate_no   VARCHAR(16),
    entry_time TIMESTAMPTZ,
    exit_time  TIMESTAMPTZ,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PARKING',
    source     VARCHAR(20)  NOT NULL DEFAULT 'APP',
    remark     VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_parking_record PRIMARY KEY (id)
);

COMMENT ON TABLE parking_record IS '停车记录表：用户停车入场/离场记录';
COMMENT ON COLUMN parking_record.id IS '记录ID（雪花ID，应用层生成）';
COMMENT ON COLUMN parking_record.user_id IS '用户ID（可为空，支持未登录录入）';
COMMENT ON COLUMN parking_record.mall_id IS '所属商场ID';
COMMENT ON COLUMN parking_record.space_id IS '车位ID';
COMMENT ON COLUMN parking_record.plate_no IS '车牌号';
COMMENT ON COLUMN parking_record.entry_time IS '入场时间';
COMMENT ON COLUMN parking_record.exit_time IS '离场时间';
COMMENT ON COLUMN parking_record.status IS '记录状态：PARKING=停车中 ENDED=已结束 CANCELLED=已取消';
COMMENT ON COLUMN parking_record.source IS '录入来源：APP=小程序 PLATE=车牌识别 STAFF=人工';
COMMENT ON COLUMN parking_record.remark IS '备注';
COMMENT ON COLUMN parking_record.created_at IS '创建时间';
COMMENT ON COLUMN parking_record.updated_at IS '更新时间';
COMMENT ON COLUMN parking_record.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE INDEX idx_parking_user_time ON parking_record (user_id, entry_time DESC);
CREATE INDEX idx_parking_mall_time ON parking_record (mall_id, entry_time DESC);
CREATE INDEX idx_parking_space     ON parking_record (space_id, status);
CREATE INDEX idx_parking_plate     ON parking_record (plate_no);

-- ---------- 用户收藏 ----------
CREATE TABLE user_destination (
    id          BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    mall_id     BIGINT       NOT NULL,
    target_type VARCHAR(20)  NOT NULL,
    target_id   BIGINT       NOT NULL,
    alias_name  VARCHAR(64),
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_destination PRIMARY KEY (id)
);

COMMENT ON TABLE user_destination IS '用户收藏表：常去车位、商铺、设施';
COMMENT ON COLUMN user_destination.id IS '收藏ID（雪花ID，应用层生成）';
COMMENT ON COLUMN user_destination.user_id IS '用户ID';
COMMENT ON COLUMN user_destination.mall_id IS '所属商场ID';
COMMENT ON COLUMN user_destination.target_type IS '目标类型：SPACE=车位 SHOP=商铺 POI=设施';
COMMENT ON COLUMN user_destination.target_id IS '目标ID（对应目标类型的主键）';
COMMENT ON COLUMN user_destination.alias_name IS '收藏别名';
COMMENT ON COLUMN user_destination.sort_order IS '排序号';
COMMENT ON COLUMN user_destination.created_at IS '创建时间';
COMMENT ON COLUMN user_destination.updated_at IS '更新时间';
COMMENT ON COLUMN user_destination.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_user_dest ON user_destination (user_id, mall_id, target_type, target_id) WHERE deleted = 0;
CREATE INDEX idx_user_dest_user ON user_destination (user_id);

-- ---------- 导航历史 ----------
CREATE TABLE nav_history (
    id             BIGINT        NOT NULL,
    user_id        BIGINT,
    mall_id        BIGINT        NOT NULL,
    from_floor_id  BIGINT,
    to_floor_id    BIGINT,
    from_type      VARCHAR(20),
    from_target_id BIGINT,
    to_type        VARCHAR(20)   NOT NULL,
    to_target_id   BIGINT        NOT NULL,
    path_geom      geometry(LineString, 0),
    distance_m     DOUBLE PRECISION,
    duration_s     INT,
    status         VARCHAR(20)   NOT NULL DEFAULT 'FINISHED',
    start_time     TIMESTAMPTZ,
    end_time       TIMESTAMPTZ,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted        SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_nav_history PRIMARY KEY (id)
);

COMMENT ON TABLE nav_history IS '导航历史表：记录用户每次导航的起终点、路径与耗时';
COMMENT ON COLUMN nav_history.id IS '导航记录ID（雪花ID，应用层生成）';
COMMENT ON COLUMN nav_history.user_id IS '用户ID（可为空）';
COMMENT ON COLUMN nav_history.mall_id IS '所属商场ID';
COMMENT ON COLUMN nav_history.from_floor_id IS '起始楼层ID';
COMMENT ON COLUMN nav_history.to_floor_id IS '目标楼层ID';
COMMENT ON COLUMN nav_history.from_type IS '起点类型：CURRENT_POSITION=当前位置 SPACE=车位 SHOP=商铺 POI=设施';
COMMENT ON COLUMN nav_history.from_target_id IS '起点目标ID';
COMMENT ON COLUMN nav_history.to_type IS '终点类型：SPACE=车位 SHOP=商铺 POI=设施';
COMMENT ON COLUMN nav_history.to_target_id IS '终点目标ID';
COMMENT ON COLUMN nav_history.path_geom IS '整条路径线（用于历史回放与可视化）';
COMMENT ON COLUMN nav_history.distance_m IS '路径总距离（米）';
COMMENT ON COLUMN nav_history.duration_s IS '导航耗时（秒）';
COMMENT ON COLUMN nav_history.status IS '导航状态：FINISHED=完成 ABORTED=中途放弃';
COMMENT ON COLUMN nav_history.start_time IS '导航开始时间';
COMMENT ON COLUMN nav_history.end_time IS '导航结束时间';
COMMENT ON COLUMN nav_history.created_at IS '创建时间';
COMMENT ON COLUMN nav_history.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE INDEX idx_nav_history_user_time ON nav_history (user_id, start_time DESC);
CREATE INDEX idx_nav_history_mall_time ON nav_history (mall_id, start_time);
CREATE INDEX idx_nav_history_path_gist ON nav_history USING GIST (path_geom);

-- ---------- 定位会话 ----------
CREATE TABLE positioning_session (
    id              BIGINT       NOT NULL,
    user_id         BIGINT,
    mall_id         BIGINT       NOT NULL,
    device_id       VARCHAR(64),
    floor_id        BIGINT,
    last_pos        geometry(Point, 0),
    last_accuracy_m DOUBLE PRECISION,
    start_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    end_time        TIMESTAMPTZ,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_positioning_session PRIMARY KEY (id)
);

COMMENT ON TABLE positioning_session IS '定位会话表：记录用户一次进入商场的定位过程';
COMMENT ON COLUMN positioning_session.id IS '会话ID（雪花ID，应用层生成）';
COMMENT ON COLUMN positioning_session.user_id IS '用户ID（可为空）';
COMMENT ON COLUMN positioning_session.mall_id IS '所属商场ID';
COMMENT ON COLUMN positioning_session.device_id IS '客户端设备标识';
COMMENT ON COLUMN positioning_session.floor_id IS '当前楼层ID';
COMMENT ON COLUMN positioning_session.last_pos IS '最近一次定位坐标（楼层本地米制坐标）';
COMMENT ON COLUMN positioning_session.last_accuracy_m IS '最近一次定位精度（米）';
COMMENT ON COLUMN positioning_session.start_time IS '会话开始时间';
COMMENT ON COLUMN positioning_session.end_time IS '会话结束时间';
COMMENT ON COLUMN positioning_session.status IS '会话状态：ACTIVE=进行中 ENDED=已结束';
COMMENT ON COLUMN positioning_session.created_at IS '创建时间';
COMMENT ON COLUMN positioning_session.updated_at IS '更新时间';
COMMENT ON COLUMN positioning_session.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE INDEX idx_pos_session_user ON positioning_session (user_id, start_time DESC);
CREATE INDEX idx_pos_session_mall ON positioning_session (mall_id, start_time);
CREATE INDEX idx_pos_session_gist ON positioning_session USING GIST (last_pos);

-- ---------- 操作日志 ----------
CREATE TABLE op_log (
    id          BIGINT       NOT NULL,
    user_id     BIGINT,
    mall_id     BIGINT,
    module      VARCHAR(64),
    action      VARCHAR(64),
    target_type VARCHAR(64),
    target_id   BIGINT,
    detail      JSONB,
    ip          VARCHAR(64),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_op_log PRIMARY KEY (id)
);

COMMENT ON TABLE op_log IS '操作日志表：记录运营人员对空间/车位/商铺数据的操作';
COMMENT ON COLUMN op_log.id IS '日志ID（雪花ID，应用层生成）';
COMMENT ON COLUMN op_log.user_id IS '操作人用户ID';
COMMENT ON COLUMN op_log.mall_id IS '所属商场ID';
COMMENT ON COLUMN op_log.module IS '操作模块（如车位、商铺、导航图）';
COMMENT ON COLUMN op_log.action IS '操作动作（如新增、修改、删除）';
COMMENT ON COLUMN op_log.target_type IS '操作对象类型';
COMMENT ON COLUMN op_log.target_id IS '操作对象ID';
COMMENT ON COLUMN op_log.detail IS '操作详情（JSON格式）';
COMMENT ON COLUMN op_log.ip IS '操作人IP地址';
COMMENT ON COLUMN op_log.created_at IS '创建时间';

CREATE INDEX idx_op_log_user ON op_log (user_id, created_at DESC);
CREATE INDEX idx_op_log_mall ON op_log (mall_id, created_at);
