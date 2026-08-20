-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 目标数据库: postgis_36_sample
-- 目标架构: business（业务服务）
-- 前置: 已执行 sql/00~04 脚本
--
-- 本脚本为字典表设计与枚举种子数据:
--   * 记录原表结构设计中各字段的枚举取值(如 车位类型/状态、商铺状态、设施类型、节点类型等)
--   * 供前端下拉框/展示文案使用, 避免前端硬编码枚举
--
-- 约定:
--   * 主键雪花ID(种子用固定 ID 段 60001 起)
--   * 逻辑删除 deleted, 唯一索引 (dict_type, dict_code) WHERE deleted=0
--   * 重复执行幂等(ON CONFLICT)
-- ============================================================

SET search_path TO business, public;

-- ---------- 字典表 ----------
CREATE TABLE IF NOT EXISTS sys_dict (
    id         BIGINT       NOT NULL,
    dict_type  VARCHAR(64)  NOT NULL,
    dict_code  VARCHAR(64)  NOT NULL,
    dict_label VARCHAR(64)  NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    status     SMALLINT     NOT NULL DEFAULT 1,
    remark     VARCHAR(255),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted    SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_dict PRIMARY KEY (id)
);

COMMENT ON TABLE sys_dict IS '系统字典表：记录原表结构设计的枚举取值（下拉框/文案用）';
COMMENT ON COLUMN sys_dict.id IS '字典ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_dict.dict_type IS '字典类型编码，如 space_type/space_status/shop_status';
COMMENT ON COLUMN sys_dict.dict_code IS '字典项编码（对应字段存储值）';
COMMENT ON COLUMN sys_dict.dict_label IS '字典项名称（中文展示文案）';
COMMENT ON COLUMN sys_dict.sort_order IS '排序号（类型内升序）';
COMMENT ON COLUMN sys_dict.status IS '状态：1=启用 0=停用';
COMMENT ON COLUMN sys_dict.remark IS '备注（可标注来源表/字段）';
COMMENT ON COLUMN sys_dict.created_at IS '创建时间';
COMMENT ON COLUMN sys_dict.updated_at IS '更新时间';
COMMENT ON COLUMN sys_dict.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX IF NOT EXISTS uk_dict ON sys_dict (dict_type, dict_code) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_dict_type ON sys_dict (dict_type) WHERE deleted = 0;

-- ============================================================
-- 枚举种子数据（来源: sql/01_auth_db.sql、sql/02_business_db.sql 字段注释）
-- ============================================================
INSERT INTO sys_dict (id, dict_type, dict_code, dict_label, sort_order, status, remark) VALUES
    -- parking_space.space_type 车位类型
    (60001, 'space_type',     'NORMAL',        '普通',     1, 1, 'parking_space.space_type'),
    (60002, 'space_type',     'DISABLED',      '无障碍',   2, 1, 'parking_space.space_type'),
    (60003, 'space_type',     'CHARGING',      '充电',     3, 1, 'parking_space.space_type'),
    (60004, 'space_type',     'COMPACT',       '微型',     4, 1, 'parking_space.space_type'),
    (60005, 'space_type',     'MECHANICAL',    '机械',     5, 1, 'parking_space.space_type'),
    (60006, 'space_type',     'MOTHER_CHILD',  '母婴',     6, 1, 'parking_space.space_type'),
    -- parking_space.status 车位状态
    (60011, 'space_status',   'FREE',          '空闲',     1, 1, 'parking_space.status'),
    (60012, 'space_status',   'OCCUPIED',      '占用',     2, 1, 'parking_space.status'),
    (60013, 'space_status',   'LOCKED',        '锁定',     3, 1, 'parking_space.status'),
    (60014, 'space_status',   'FAULT',         '故障',     4, 1, 'parking_space.status'),
    -- parking_space.occupy_source 占用来源
    (60021, 'occupy_source',  'APP',           '用户上报', 1, 1, 'parking_space.occupy_source'),
    (60022, 'occupy_source',  'CAMERA',        '摄像头',   2, 1, 'parking_space.occupy_source'),
    (60023, 'occupy_source',  'MAGNET',        '地磁',     3, 1, 'parking_space.occupy_source'),
    (60024, 'occupy_source',  'GATE',          '道闸',     4, 1, 'parking_space.occupy_source'),
    (60025, 'occupy_source',  'MANUAL',        '人工',     5, 1, 'parking_space.occupy_source'),
    -- shop.status 商铺状态
    (60031, 'shop_status',    'OPEN',          '营业',     1, 1, 'shop.status'),
    (60032, 'shop_status',    'DECORATING',    '装修',     2, 1, 'shop.status'),
    (60033, 'shop_status',    'CLOSED',        '关闭',     3, 1, 'shop.status'),
    -- poi.poi_type 设施类型
    (60041, 'poi_type',       'ELEVATOR',      '电梯',     1, 1, 'poi.poi_type'),
    (60042, 'poi_type',       'ESCALATOR',     '扶梯',     2, 1, 'poi.poi_type'),
    (60043, 'poi_type',       'STAIR',         '楼梯',     3, 1, 'poi.poi_type'),
    (60044, 'poi_type',       'TOILET',        '卫生间',   4, 1, 'poi.poi_type'),
    (60045, 'poi_type',       'ENTRANCE',      '商场出入口', 5, 1, 'poi.poi_type'),
    (60046, 'poi_type',       'EXIT',          '车库出口', 6, 1, 'poi.poi_type'),
    (60047, 'poi_type',       'SERVICE_DESK',  '服务台',   7, 1, 'poi.poi_type'),
    (60048, 'poi_type',       'NURSING_ROOM',  '母婴室',   8, 1, 'poi.poi_type'),
    (60049, 'poi_type',       'ATM',           '取款机',   9, 1, 'poi.poi_type'),
    -- beacon.beacon_type 信标协议
    (60051, 'beacon_type',    'IBEACON',       'iBeacon',  1, 1, 'beacon.beacon_type'),
    (60052, 'beacon_type',    'EDDYSTONE',     'Eddystone', 2, 1, 'beacon.beacon_type'),
    -- beacon.status 信标状态
    (60061, 'beacon_status',  'ACTIVE',        '正常',     1, 1, 'beacon.status'),
    (60062, 'beacon_status',  'INACTIVE',      '停用',     2, 1, 'beacon.status'),
    (60063, 'beacon_status',  'FAULT',         '故障',     3, 1, 'beacon.status'),
    -- nav_node.node_type 导航节点类型
    (60071, 'nav_node_type',  'WAYPOINT',      '通道点',   1, 1, 'nav_node.node_type'),
    (60072, 'nav_node_type',  'JUNCTION',      '岔路口',   2, 1, 'nav_node.node_type'),
    (60073, 'nav_node_type',  'DOOR',          '门',       3, 1, 'nav_node.node_type'),
    (60074, 'nav_node_type',  'ELEVATOR',      '电梯口',   4, 1, 'nav_node.node_type'),
    (60075, 'nav_node_type',  'ESCALATOR',     '扶梯口',   5, 1, 'nav_node.node_type'),
    (60076, 'nav_node_type',  'STAIR',         '楼梯口',   6, 1, 'nav_node.node_type'),
    (60077, 'nav_node_type',  'SPACE_ENTRY',   '车位入口', 7, 1, 'nav_node.node_type'),
    (60078, 'nav_node_type',  'SHOP_ENTRY',    '商铺入口', 8, 1, 'nav_node.node_type'),
    (60079, 'nav_node_type',  'POI_ENTRY',     '设施入口', 9, 1, 'nav_node.node_type'),
    -- nav_edge.edge_type 导航边类型
    (60081, 'nav_edge_type',  'WALK',          '步行',     1, 1, 'nav_edge.edge_type'),
    (60082, 'nav_edge_type',  'ELEVATOR',      '电梯',     2, 1, 'nav_edge.edge_type'),
    (60083, 'nav_edge_type',  'ESCALATOR',     '扶梯',     3, 1, 'nav_edge.edge_type'),
    (60084, 'nav_edge_type',  'STAIR',         '楼梯',     4, 1, 'nav_edge.edge_type'),
    -- floor_connect.connect_type 跨层连接方式
    (60091, 'connect_type',   'ELEVATOR',      '电梯',     1, 1, 'floor_connect.connect_type'),
    (60092, 'connect_type',   'ESCALATOR',     '扶梯',     2, 1, 'floor_connect.connect_type'),
    (60093, 'connect_type',   'STAIR',         '楼梯',     3, 1, 'floor_connect.connect_type'),
    -- mall_user.role_code 商场内角色
    (60101, 'mall_user_role', 'USER',          '普通用户', 1, 1, 'mall_user.role_code'),
    (60102, 'mall_user_role', 'MALL_ADMIN',    '商场管理员', 2, 1, 'mall_user.role_code'),
    (60103, 'mall_user_role', 'OPERATOR',      '运营人员', 3, 1, 'mall_user.role_code'),
    (60104, 'mall_user_role', 'MERCHANT',      '商户',     4, 1, 'mall_user.role_code'),
    -- parking_record.status 停车记录状态
    (60111, 'parking_status', 'PARKING',       '停车中',   1, 1, 'parking_record.status'),
    (60112, 'parking_status', 'ENDED',         '已结束',   2, 1, 'parking_record.status'),
    (60113, 'parking_status', 'CANCELLED',     '已取消',   3, 1, 'parking_record.status'),
    -- parking_record.source 录入来源
    (60121, 'parking_source', 'APP',           '小程序',   1, 1, 'parking_record.source'),
    (60122, 'parking_source', 'PLATE',         '车牌识别', 2, 1, 'parking_record.source'),
    (60123, 'parking_source', 'STAFF',         '人工',     3, 1, 'parking_record.source'),
    -- user_destination.target_type 收藏目标类型
    (60131, 'target_type',    'SPACE',         '车位',     1, 1, 'user_destination.target_type'),
    (60132, 'target_type',    'SHOP',          '商铺',     2, 1, 'user_destination.target_type'),
    (60133, 'target_type',    'POI',           '设施',     3, 1, 'user_destination.target_type'),
    -- nav_history 起终点类型
    (60141, 'nav_from_type',  'CURRENT_POSITION', '当前位置', 1, 1, 'nav_history.from_type'),
    (60142, 'nav_from_type',  'SPACE',         '车位',     2, 1, 'nav_history.from_type'),
    (60143, 'nav_from_type',  'SHOP',          '商铺',     3, 1, 'nav_history.from_type'),
    (60144, 'nav_from_type',  'POI',           '设施',     4, 1, 'nav_history.from_type'),
    (60151, 'nav_to_type',    'SPACE',         '车位',     1, 1, 'nav_history.to_type'),
    (60152, 'nav_to_type',    'SHOP',          '商铺',     2, 1, 'nav_history.to_type'),
    (60153, 'nav_to_type',    'POI',           '设施',     3, 1, 'nav_history.to_type'),
    -- positioning_session.status 定位会话状态
    (60161, 'session_status', 'ACTIVE',        '进行中',   1, 1, 'positioning_session.status'),
    (60162, 'session_status', 'ENDED',         '已结束',   2, 1, 'positioning_session.status'),
    -- mall.status / mall_floor.status 数值状态
    (60171, 'mall_status',    '1',             '营业',     1, 1, 'mall.status'),
    (60172, 'mall_status',    '0',             '停用',     2, 1, 'mall.status'),
    (60181, 'floor_status',   '1',             '开放',     1, 1, 'mall_floor.status'),
    (60182, 'floor_status',   '0',             '关闭',     2, 1, 'mall_floor.status'),
    -- auth 架构枚举（跨架构记录, 供管理后台下拉）
    (60191, 'user_type',      'USER',          '普通车主用户', 1, 1, 'auth.sys_user.user_type'),
    (60192, 'user_type',      'STAFF',         '商场运营人员', 2, 1, 'auth.sys_user.user_type'),
    (60193, 'user_type',      'ADMIN',         '平台管理员', 3, 1, 'auth.sys_user.user_type'),
    (60194, 'user_type',      'MERCHANT',      '商铺商户',  4, 1, 'auth.sys_user.user_type'),
    (60201, 'role_code',      'ADMIN',         '平台管理员', 1, 1, 'auth.sys_role.role_code'),
    (60202, 'role_code',      'MALL_OPERATOR', '商场运营',  2, 1, 'auth.sys_role.role_code'),
    (60203, 'role_code',      'MERCHANT',      '商铺商户',  3, 1, 'auth.sys_role.role_code'),
    (60204, 'role_code',      'USER',          '普通用户',  4, 1, 'auth.sys_role.role_code')
ON CONFLICT (id) DO NOTHING;
