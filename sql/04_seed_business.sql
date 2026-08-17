-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 目标数据库: postgis_36_sample
-- 目标架构: business（业务服务）
-- 前置: 已执行 sql/00_init.sql、sql/02_business_db.sql、sql/03_seed_auth.sql
--
-- 本脚本为种子数据（示例购物中心，3 个楼层）:
--   * 1 个商场 + 3 个楼层(B2/B1/1F) + 7 个分区
--   * 32 个车位（B2 24 个 + B1 8 个，含无障碍/充电等类型，含占用演示）
--   * 6 个商铺分类 + 13 个商铺（1F 零售 10 个 + B1 餐饮 3 个）
--   * 17 个 POI（电梯/扶梯/楼梯/卫生间/出入口/服务台/母婴室）
--   * 18 个蓝牙信标（每层 6 个网格布点）
--   * 导航图：90 个节点 + 88 条边 + 4 条跨层连接（电梯/楼梯/扶梯）
--   * 用户-商场绑定、车辆、停车记录、收藏、导航历史、定位会话、操作日志
--
-- 空间坐标约定: 楼层本地平面坐标系, 单位米, SRID=0
--   平面: 100m x 60m; 主通道 y=27.5; 南通道 y=15; 北通道 y=42
--
-- ID 约定（固定 ID 便于跨脚本引用）:
--   商场/楼层/分区: 1 / 101..103 / 201..402
--   车位: 30001..30024(B2) 31001..31008(B1)
--   分类/商铺: 501..506 / 601..613
--   POI/信标: 701..717 / 801..826
--   导航节点: 9xxx(通道) 91xxx(B2车位入口) 92xxx(B1车位入口)
--             94xxx(B1商铺入口) 97xxx(1F商铺入口)
--   导航边: 94xxx(B2通道) 942xx(B2车位) 943xx(B1通道) 944xx(B1车位/商铺)
--           945xx(1F通道) 946xx(1F商铺) 98xx(跨层)
--   业务数据: 99001 起
-- 重复执行: 全部 ON CONFLICT 幂等, 可安全重复执行
-- ============================================================

SET search_path TO business, public;

-- ============================================================
-- 1. 商场 / 楼层 / 分区
-- ============================================================
INSERT INTO mall (id, mall_code, mall_name, province, city, district, address, lng, lat, status, remark)
VALUES (1, 'MALL001', '示例购物中心', '浙江省', '杭州市', '滨江区', '江汉路 1515 号', 120.327627, 30.310719, 1, '种子示例数据商场')
ON CONFLICT (id) DO NOTHING;

INSERT INTO mall_floor (id, mall_id, floor_code, floor_name, sort_order, status, width_m, height_m, remark)
VALUES
    (101, 1, 'B2', '地下二层（停车场）', 1, 1, 100, 60, '车库层：24 个车位'),
    (102, 1, 'B1', '地下一层（餐饮+停车场）', 2, 1, 100, 60, '餐饮 + 8 个车位'),
    (103, 1, '1F', '一层（零售）', 3, 1, 100, 60, '零售商铺层')
ON CONFLICT (id) DO NOTHING;

INSERT INTO mall_zone (id, mall_id, floor_id, zone_code, zone_name, geom, color, sort_order, remark)
VALUES
    (201, 1, 101, 'A区', 'B2-A区', ST_GeomFromText('POLYGON((5 15, 45 15, 45 45, 5 45, 5 15))', 0), '#4CAF50', 1, 'B2 层 A 区停车位'),
    (202, 1, 101, 'B区', 'B2-B区', ST_GeomFromText('POLYGON((55 15, 95 15, 95 45, 55 45, 55 15))', 0), '#2196F3', 2, 'B2 层 B 区停车位'),
    (203, 1, 101, '通道', 'B2-中庭通道', ST_GeomFromText('POLYGON((45 15, 55 15, 55 45, 45 45, 45 15))', 0), '#9E9E9E', 3, 'B2 层中庭通道'),
    (301, 1, 102, 'A区', 'B1-A区', ST_GeomFromText('POLYGON((5 15, 45 15, 45 45, 5 45, 5 15))', 0), '#4CAF50', 1, 'B1 层 A 区停车位'),
    (302, 1, 102, 'B区', 'B1-B区', ST_GeomFromText('POLYGON((55 15, 95 15, 95 45, 55 45, 55 15))', 0), '#2196F3', 2, 'B1 层 B 区停车位'),
    (401, 1, 103, 'A区', '1F-A区', ST_GeomFromText('POLYGON((5 15, 45 15, 45 45, 5 45, 5 15))', 0), '#FF9800', 1, '1F 层 A 区（零售）'),
    (402, 1, 103, 'B区', '1F-B区', ST_GeomFromText('POLYGON((55 15, 95 15, 95 45, 55 45, 55 15))', 0), '#E91E63', 2, '1F 层 B 区（餐饮）')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 2. 车位（B2 24 个 + B1 8 个）
--    布局: A区 x∈[6,21], B区 x∈[60,75]; 行1 y∈[20,25] 开口朝南 y=20,
--          行2 y∈[30,35] 开口朝南 y=30; 每个车位 2.5m x 5m
-- ============================================================
WITH b2_spaces AS (
    SELECT s,
           CASE
               WHEN s BETWEEN 1 AND 6   THEN 6 + (s - 1) * 2.5
               WHEN s BETWEEN 7 AND 12  THEN 6 + (s - 7) * 2.5
               WHEN s BETWEEN 13 AND 18 THEN 60 + (s - 13) * 2.5
               ELSE 60 + (s - 19) * 2.5
           END AS x0,
           CASE WHEN s <= 6 OR (s BETWEEN 13 AND 18) THEN 20 ELSE 30 END AS y1
    FROM generate_series(1, 24) s
)
INSERT INTO parking_space (id, mall_id, floor_id, zone_id, space_no, space_type, status, occupy_source,
                           geom, center_point, entrance_point, sort_order, remark)
SELECT
    30000 + s, 1, 101,
    CASE WHEN s <= 12 THEN 201 ELSE 202 END,
    'B2-' || CASE WHEN s <= 12 THEN 'A' ELSE 'B' END
              || lpad((s - CASE WHEN s <= 12 THEN 0 ELSE 12 END)::text, 2, '0'),
    CASE WHEN s = 1 THEN 'DISABLED' WHEN s = 2 THEN 'MOTHER_CHILD' ELSE 'NORMAL' END,
    CASE WHEN s IN (5, 8, 17) THEN 'OCCUPIED' ELSE 'FREE' END,
    CASE WHEN s IN (5, 8, 17) THEN 'APP' END,
    ST_GeomFromText('POLYGON((' || x0 || ' ' || y1 || ', ' || (x0 + 2.5) || ' ' || y1 || ', '
                    || (x0 + 2.5) || ' ' || (y1 + 5) || ', ' || x0 || ' ' || (y1 + 5) || ', '
                    || x0 || ' ' || y1 || '))', 0),
    ST_MakePoint(x0 + 1.25, y1 + 2.5),
    ST_MakePoint(x0 + 1.25, y1),
    s,
    'B2 层种子示例车位'
FROM b2_spaces
ON CONFLICT (id) DO NOTHING;

WITH b1_spaces AS (
    SELECT s,
           CASE WHEN s <= 4 THEN 6 + (s - 1) * 2.5 ELSE 60 + (s - 5) * 2.5 END AS x0,
           20 AS y1
    FROM generate_series(1, 8) s
)
INSERT INTO parking_space (id, mall_id, floor_id, zone_id, space_no, space_type, status, occupy_source,
                           geom, center_point, entrance_point, sort_order, remark)
SELECT
    31000 + s, 1, 102,
    CASE WHEN s <= 4 THEN 301 ELSE 302 END,
    'B1-' || CASE WHEN s <= 4 THEN 'A' ELSE 'B' END
              || lpad((s - CASE WHEN s <= 4 THEN 0 ELSE 4 END)::text, 2, '0'),
    CASE WHEN s IN (3, 4, 7) THEN 'CHARGING' ELSE 'NORMAL' END,
    CASE WHEN s = 1 THEN 'OCCUPIED' ELSE 'FREE' END,
    CASE WHEN s = 1 THEN 'CAMERA' END,
    ST_GeomFromText('POLYGON((' || x0 || ' ' || y1 || ', ' || (x0 + 2.5) || ' ' || y1 || ', '
                    || (x0 + 2.5) || ' ' || (y1 + 5) || ', ' || x0 || ' ' || (y1 + 5) || ', '
                    || x0 || ' ' || y1 || '))', 0),
    ST_MakePoint(x0 + 1.25, y1 + 2.5),
    ST_MakePoint(x0 + 1.25, y1),
    s,
    'B1 层种子示例车位'
FROM b1_spaces
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 3. 商铺分类 / 商铺
--    北侧商铺: 矩形 y∈[35,41], 开口 (x0+4, 35) 朝主通道
--    南侧商铺: 矩形 y∈[13,19], 开口 (x0+4, 19) 朝主通道
-- ============================================================
INSERT INTO shop_category (id, mall_id, parent_id, cat_name, sort_order, status)
VALUES
    (501, 1, 0, '餐饮',   1, 1),
    (502, 1, 0, '服装',   2, 1),
    (503, 1, 0, '数码',   3, 1),
    (504, 1, 0, '美妆',   4, 1),
    (505, 1, 0, '生活服务', 5, 1),
    (506, 1, 0, '珠宝',   6, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO shop (id, mall_id, floor_id, zone_id, shop_no, shop_name, short_name, category_id, brand,
                  phone, description, keywords,
                  geom, center_point, entrance_point, status, sort_order)
VALUES
    -- 1F 北侧（A区, 开口 y=35）: x0 = 8/16/24/32/40/48
    (601, 1, 103, 401, '1F-101', '星巴克', '星巴克', 501, 'Starbucks', '021-10000001',
     '全球连锁咖啡品牌，供应现磨咖啡与轻食', '咖啡,拿铁,星冰乐,早餐',
     ST_GeomFromText('POLYGON((8 35, 16 35, 16 41, 8 41, 8 35))', 0), ST_MakePoint(12, 38), ST_MakePoint(12, 35), 'OPEN', 1),
    (602, 1, 103, 401, '1F-102', '优衣库', '优衣库', 502, 'UNIQLO', '021-10000002',
     '日系快时尚服饰品牌', '服装,快时尚,T恤,羽绒服',
     ST_GeomFromText('POLYGON((16 35, 24 35, 24 41, 16 41, 16 35))', 0), ST_MakePoint(20, 38), ST_MakePoint(20, 35), 'OPEN', 2),
    (603, 1, 103, 401, '1F-103', '苹果授权店', '苹果授权店', 503, 'Apple', '021-10000003',
     'Apple 授权经销商，销售 iPhone/Mac/iPad', '手机,电脑,iphone,mac,ipad',
     ST_GeomFromText('POLYGON((24 35, 32 35, 32 41, 24 41, 24 35))', 0), ST_MakePoint(28, 38), ST_MakePoint(28, 35), 'OPEN', 3),
    (604, 1, 103, 401, '1F-104', '名创优品', '名创优品', 505, 'MINISO', '021-10000004',
     '生活好物集合店', '日用,饰品,生活用品',
     ST_GeomFromText('POLYGON((32 35, 40 35, 40 41, 32 41, 32 35))', 0), ST_MakePoint(36, 38), ST_MakePoint(36, 35), 'OPEN', 4),
    (605, 1, 103, 401, '1F-105', '屈臣氏', '屈臣氏', 504, 'Watsons', '021-10000005',
     '个人护理与美妆连锁', '美妆,护肤,洗护,日化',
     ST_GeomFromText('POLYGON((40 35, 48 35, 48 41, 40 41, 40 35))', 0), ST_MakePoint(44, 38), ST_MakePoint(44, 35), 'OPEN', 5),
    (606, 1, 103, 401, '1F-106', '周大福', '周大福', 506, 'Chow Tai Fook', '021-10000006',
     '知名黄金珠宝品牌', '黄金,珠宝,首饰,婚戒',
     ST_GeomFromText('POLYGON((48 35, 56 35, 56 41, 48 41, 48 35))', 0), ST_MakePoint(52, 38), ST_MakePoint(52, 35), 'OPEN', 6),
    -- 1F 南侧（B区, 开口 y=19）: x0 = 56/64/72/80
    (607, 1, 103, 402, '1F-107', '小米之家', '小米之家', 503, 'Xiaomi', '021-10000007',
     '小米全品类产品体验店', '手机,智能家居,小米,米家',
     ST_GeomFromText('POLYGON((56 13, 64 13, 64 19, 56 19, 56 13))', 0), ST_MakePoint(60, 16), ST_MakePoint(60, 19), 'OPEN', 7),
    (608, 1, 103, 402, '1F-108', '必胜客', '必胜客', 501, 'Pizza Hut', '021-10000008',
     '西式休闲餐饮连锁', '披萨,意面,西餐',
     ST_GeomFromText('POLYGON((64 13, 72 13, 72 19, 64 19, 64 13))', 0), ST_MakePoint(68, 16), ST_MakePoint(68, 19), 'OPEN', 8),
    (609, 1, 103, 402, '1F-109', '肯德基', '肯德基', 501, 'KFC', '021-10000009',
     '全球连锁快餐品牌', '炸鸡,汉堡,快餐',
     ST_GeomFromText('POLYGON((72 13, 80 13, 80 19, 72 19, 72 13))', 0), ST_MakePoint(76, 16), ST_MakePoint(76, 19), 'OPEN', 9),
    (610, 1, 103, 402, '1F-110', '无印良品', '无印良品', 505, 'MUJI', '021-10000010',
     '日系生活杂货与家居品牌', '家居,文具,服饰,生活杂货',
     ST_GeomFromText('POLYGON((80 13, 88 13, 88 19, 80 19, 80 13))', 0), ST_MakePoint(84, 16), ST_MakePoint(84, 19), 'OPEN', 10),
    -- B1 北侧（餐饮, 开口 y=35）: x0 = 8/16/24
    (611, 1, 102, 301, 'B1-101', '海底捞火锅', '海底捞', 501, 'Haidilao', '021-20000001',
     '知名川味火锅连锁', '火锅,川菜,海底捞,毛肚',
     ST_GeomFromText('POLYGON((8 35, 16 35, 16 41, 8 41, 8 35))', 0), ST_MakePoint(12, 38), ST_MakePoint(12, 35), 'OPEN', 1),
    (612, 1, 102, 301, 'B1-102', '面包新语', '面包新语', 501, 'BreadTalk', '021-20000002',
     '面包烘焙连锁', '面包,蛋糕,烘焙,早餐',
     ST_GeomFromText('POLYGON((16 35, 24 35, 24 41, 16 41, 16 35))', 0), ST_MakePoint(20, 38), ST_MakePoint(20, 35), 'OPEN', 2),
    (613, 1, 102, 302, 'B1-103', '盒马鲜生', '盒马鲜生', 505, 'Hema', '021-20000003',
     '生鲜超市与新零售体验店', '生鲜,超市,海鲜,盒马',
     ST_GeomFromText('POLYGON((24 35, 32 35, 32 41, 24 41, 24 35))', 0), ST_MakePoint(28, 38), ST_MakePoint(28, 35), 'OPEN', 3)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 4. POI 基础设施/兴趣点
-- ============================================================
INSERT INTO poi (id, mall_id, floor_id, poi_type, poi_name, geom, center_point, entrance_point, status, remark)
VALUES
    (701, 1, 101, 'ELEVATOR', '客梯1', ST_MakePoint(50, 55), ST_MakePoint(50, 55), ST_MakePoint(50, 53), 1, 'B2 层电梯'),
    (702, 1, 102, 'ELEVATOR', '客梯1', ST_MakePoint(50, 55), ST_MakePoint(50, 55), ST_MakePoint(50, 53), 1, 'B1 层电梯'),
    (703, 1, 103, 'ELEVATOR', '客梯1', ST_MakePoint(50, 55), ST_MakePoint(50, 55), ST_MakePoint(50, 53), 1, '1F 层电梯'),
    (704, 1, 103, 'ELEVATOR', '客梯2', ST_MakePoint(10, 55), ST_MakePoint(10, 55), ST_MakePoint(10, 53), 1, '1F 层西侧电梯'),
    (705, 1, 102, 'ESCALATOR', '扶梯1', ST_MakePoint(30, 55), ST_MakePoint(30, 55), ST_MakePoint(30, 53), 1, 'B1 层扶梯'),
    (706, 1, 103, 'ESCALATOR', '扶梯1', ST_MakePoint(30, 55), ST_MakePoint(30, 55), ST_MakePoint(30, 53), 1, '1F 层扶梯'),
    (707, 1, 101, 'STAIR', '楼梯1', ST_MakePoint(50, 5), ST_MakePoint(50, 5), ST_MakePoint(50, 7), 1, 'B2 层楼梯'),
    (708, 1, 102, 'STAIR', '楼梯1', ST_MakePoint(50, 5), ST_MakePoint(50, 5), ST_MakePoint(50, 7), 1, 'B1 层楼梯'),
    (709, 1, 103, 'TOILET', '卫生间A', ST_GeomFromText('POLYGON((12 8, 18 8, 18 12, 12 12, 12 8))', 0), ST_MakePoint(15, 10), ST_MakePoint(15, 12), 1, '1F 层西南卫生间'),
    (710, 1, 103, 'TOILET', '卫生间B', ST_GeomFromText('POLYGON((82 8, 88 8, 88 12, 82 12, 82 8))', 0), ST_MakePoint(85, 10), ST_MakePoint(85, 12), 1, '1F 层东南卫生间'),
    (711, 1, 102, 'TOILET', '卫生间', ST_GeomFromText('POLYGON((82 48, 88 48, 88 52, 82 52, 82 48))', 0), ST_MakePoint(85, 50), ST_MakePoint(85, 48), 1, 'B1 层卫生间'),
    (712, 1, 103, 'SERVICE_DESK', '服务台', ST_GeomFromText('POLYGON((47 25, 53 25, 53 30, 47 30, 47 25))', 0), ST_MakePoint(50, 27.5), ST_MakePoint(50, 30), 1, '1F 层中庭服务台'),
    (713, 1, 103, 'ENTRANCE', '商场入口A', ST_MakePoint(2, 27.5), ST_MakePoint(2, 27.5), ST_MakePoint(4, 27.5), 1, '1F 层西侧入口'),
    (714, 1, 103, 'ENTRANCE', '商场入口B', ST_MakePoint(98, 27.5), ST_MakePoint(98, 27.5), ST_MakePoint(96, 27.5), 1, '1F 层东侧入口'),
    (715, 1, 101, 'ENTRANCE', '车库入口', ST_MakePoint(2, 27.5), ST_MakePoint(2, 27.5), ST_MakePoint(4, 27.5), 1, 'B2 层车库入口'),
    (716, 1, 101, 'EXIT', '车库出口', ST_MakePoint(98, 27.5), ST_MakePoint(98, 27.5), ST_MakePoint(96, 27.5), 1, 'B2 层车库出口'),
    (717, 1, 103, 'NURSING_ROOM', '母婴室', ST_GeomFromText('POLYGON((82 48, 88 48, 88 52, 82 52, 82 48))', 0), ST_MakePoint(85, 50), ST_MakePoint(85, 48), 1, '1F 层母婴室')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 5. 蓝牙信标（每层 6 个, 网格布点）
-- ============================================================
INSERT INTO beacon (id, mall_id, floor_id, uuid, major, minor, mac, beacon_type, position_geom,
                    tx_power, battery, status, remark)
VALUES
    (801, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 201, 'AA:BB:CC:00:01:01', 'IBEACON', ST_MakePoint(10, 20), -59, 90, 'ACTIVE', 'B2 网格信标'),
    (802, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 202, 'AA:BB:CC:00:01:02', 'IBEACON', ST_MakePoint(30, 20), -59, 88, 'ACTIVE', 'B2 网格信标'),
    (803, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 203, 'AA:BB:CC:00:01:03', 'IBEACON', ST_MakePoint(70, 20), -59, 75, 'ACTIVE', 'B2 网格信标'),
    (804, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 204, 'AA:BB:CC:00:01:04', 'IBEACON', ST_MakePoint(90, 20), -59, 92, 'ACTIVE', 'B2 网格信标'),
    (805, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 205, 'AA:BB:CC:00:01:05', 'IBEACON', ST_MakePoint(50, 40), -59, 80, 'ACTIVE', 'B2 网格信标'),
    (806, 1, 101, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 206, 'AA:BB:CC:00:01:06', 'IBEACON', ST_MakePoint(10, 40), -59, 85, 'ACTIVE', 'B2 网格信标'),
    (811, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 301, 'AA:BB:CC:00:02:01', 'IBEACON', ST_MakePoint(10, 20), -59, 91, 'ACTIVE', 'B1 网格信标'),
    (812, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 302, 'AA:BB:CC:00:02:02', 'IBEACON', ST_MakePoint(30, 20), -59, 87, 'ACTIVE', 'B1 网格信标'),
    (813, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 303, 'AA:BB:CC:00:02:03', 'IBEACON', ST_MakePoint(70, 20), -59, 93, 'ACTIVE', 'B1 网格信标'),
    (814, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 304, 'AA:BB:CC:00:02:04', 'IBEACON', ST_MakePoint(90, 20), -59, 78, 'ACTIVE', 'B1 网格信标'),
    (815, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 305, 'AA:BB:CC:00:02:05', 'IBEACON', ST_MakePoint(50, 40), -59, 82, 'ACTIVE', 'B1 网格信标'),
    (816, 1, 102, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 306, 'AA:BB:CC:00:02:06', 'IBEACON', ST_MakePoint(10, 40), -59, 86, 'ACTIVE', 'B1 网格信标'),
    (821, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 401, 'AA:BB:CC:00:03:01', 'IBEACON', ST_MakePoint(10, 20), -59, 89, 'ACTIVE', '1F 网格信标'),
    (822, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 402, 'AA:BB:CC:00:03:02', 'IBEACON', ST_MakePoint(30, 20), -59, 84, 'ACTIVE', '1F 网格信标'),
    (823, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 403, 'AA:BB:CC:00:03:03', 'IBEACON', ST_MakePoint(70, 20), -59, 95, 'ACTIVE', '1F 网格信标'),
    (824, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 404, 'AA:BB:CC:00:03:04', 'IBEACON', ST_MakePoint(90, 20), -59, 77, 'ACTIVE', '1F 网格信标'),
    (825, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 405, 'AA:BB:CC:00:03:05', 'IBEACON', ST_MakePoint(50, 40), -59, 81, 'ACTIVE', '1F 网格信标'),
    (826, 1, 103, 'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0', 100, 406, 'AA:BB:CC:00:03:06', 'IBEACON', ST_MakePoint(10, 40), -59, 83, 'ACTIVE', '1F 网格信标')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 6. 导航图: 节点（nav_node）
--    B2: 9001-9007 主通道, 9011-9018 南北通道, 9091 电梯口, 9092 楼梯口
--    B1: 9301-9307 主通道, 9311-9318 南北通道, 9391 电梯口, 9392 楼梯口, 9393 扶梯口
--    1F: 9601-9607 主通道, 9611-9618 南北通道, 9691 电梯口, 9692 扶梯口, 9693 楼梯口
-- ============================================================
INSERT INTO nav_node (id, mall_id, floor_id, node_type, name, geom, is_accessible, sort_order, remark)
VALUES
    -- B2 通道节点
    (9001, 1, 101, 'WAYPOINT',  '车库入口',     ST_MakePoint(2, 27.5), TRUE, 1, 'B2 西侧车库入口'),
    (9002, 1, 101, 'JUNCTION',  '主通道西口',   ST_MakePoint(10, 27.5), TRUE, 2, 'B2 主通道与西通道交汇'),
    (9003, 1, 101, 'WAYPOINT',  'A区通道口',    ST_MakePoint(30, 27.5), TRUE, 3, 'B2 A 区主通道口'),
    (9004, 1, 101, 'JUNCTION',  '中庭通道',     ST_MakePoint(50, 27.5), TRUE, 4, 'B2 中庭电梯/楼梯区域'),
    (9005, 1, 101, 'WAYPOINT',  'B区通道口',    ST_MakePoint(70, 27.5), TRUE, 5, 'B2 B 区主通道口'),
    (9006, 1, 101, 'JUNCTION',  '主通道东口',   ST_MakePoint(90, 27.5), TRUE, 6, 'B2 主通道与东通道交汇'),
    (9007, 1, 101, 'WAYPOINT',  '车库出口',     ST_MakePoint(98, 27.5), TRUE, 7, 'B2 东侧车库出口'),
    (9011, 1, 101, 'JUNCTION',  '南通道西端',   ST_MakePoint(10, 15), TRUE, 11, 'B2 南通道西端'),
    (9012, 1, 101, 'JUNCTION',  '北通道西端',   ST_MakePoint(10, 42), TRUE, 12, 'B2 北通道西端'),
    (9013, 1, 101, 'JUNCTION',  '南通道东端',   ST_MakePoint(90, 15), TRUE, 13, 'B2 南通道东端'),
    (9014, 1, 101, 'JUNCTION',  '北通道东端',   ST_MakePoint(90, 42), TRUE, 14, 'B2 北通道东端'),
    (9015, 1, 101, 'WAYPOINT',  '南通道A区',    ST_MakePoint(30, 15), TRUE, 15, 'B2 南通道 A 区段'),
    (9016, 1, 101, 'WAYPOINT',  '南通道B区',    ST_MakePoint(70, 15), TRUE, 16, 'B2 南通道 B 区段'),
    (9017, 1, 101, 'WAYPOINT',  '北通道A区',    ST_MakePoint(30, 42), TRUE, 17, 'B2 北通道 A 区段'),
    (9018, 1, 101, 'WAYPOINT',  '北通道B区',    ST_MakePoint(70, 42), TRUE, 18, 'B2 北通道 B 区段'),
    (9091, 1, 101, 'ELEVATOR',  '电梯口',       ST_MakePoint(50, 55), TRUE, 91, 'B2 客梯1 口'),
    (9092, 1, 101, 'STAIR',     '楼梯口',       ST_MakePoint(50, 5), TRUE, 92, 'B2 楼梯1 口'),
    -- B1 通道节点
    (9301, 1, 102, 'WAYPOINT',  '西入口',       ST_MakePoint(2, 27.5), TRUE, 1, 'B1 西侧入口'),
    (9302, 1, 102, 'JUNCTION',  '主通道西口',   ST_MakePoint(10, 27.5), TRUE, 2, 'B1 主通道与西通道交汇'),
    (9303, 1, 102, 'WAYPOINT',  'A区通道口',    ST_MakePoint(30, 27.5), TRUE, 3, 'B1 A 区主通道口'),
    (9304, 1, 102, 'JUNCTION',  '中庭通道',     ST_MakePoint(50, 27.5), TRUE, 4, 'B1 中庭电梯/楼梯区域'),
    (9305, 1, 102, 'WAYPOINT',  'B区通道口',    ST_MakePoint(70, 27.5), TRUE, 5, 'B1 B 区主通道口'),
    (9306, 1, 102, 'JUNCTION',  '主通道东口',   ST_MakePoint(90, 27.5), TRUE, 6, 'B1 主通道与东通道交汇'),
    (9307, 1, 102, 'WAYPOINT',  '东出口',       ST_MakePoint(98, 27.5), TRUE, 7, 'B1 东侧出口'),
    (9311, 1, 102, 'JUNCTION',  '南通道西端',   ST_MakePoint(10, 15), TRUE, 11, 'B1 南通道西端'),
    (9312, 1, 102, 'JUNCTION',  '北通道西端',   ST_MakePoint(10, 42), TRUE, 12, 'B1 北通道西端'),
    (9313, 1, 102, 'JUNCTION',  '南通道东端',   ST_MakePoint(90, 15), TRUE, 13, 'B1 南通道东端'),
    (9314, 1, 102, 'JUNCTION',  '北通道东端',   ST_MakePoint(90, 42), TRUE, 14, 'B1 北通道东端'),
    (9315, 1, 102, 'WAYPOINT',  '南通道A区',    ST_MakePoint(30, 15), TRUE, 15, 'B1 南通道 A 区段'),
    (9316, 1, 102, 'WAYPOINT',  '南通道B区',    ST_MakePoint(70, 15), TRUE, 16, 'B1 南通道 B 区段'),
    (9317, 1, 102, 'WAYPOINT',  '北通道A区',    ST_MakePoint(30, 42), TRUE, 17, 'B1 北通道 A 区段'),
    (9318, 1, 102, 'WAYPOINT',  '北通道B区',    ST_MakePoint(70, 42), TRUE, 18, 'B1 北通道 B 区段'),
    (9391, 1, 102, 'ELEVATOR',  '电梯口',       ST_MakePoint(50, 55), TRUE, 91, 'B1 客梯1 口'),
    (9392, 1, 102, 'STAIR',     '楼梯口',       ST_MakePoint(50, 5), TRUE, 92, 'B1 楼梯1 口'),
    (9393, 1, 102, 'ESCALATOR', '扶梯口',       ST_MakePoint(30, 55), TRUE, 93, 'B1 扶梯1 口'),
    -- 1F 通道节点
    (9601, 1, 103, 'WAYPOINT',  '商场入口A',    ST_MakePoint(2, 27.5), TRUE, 1, '1F 西侧入口'),
    (9602, 1, 103, 'JUNCTION',  '主通道西口',   ST_MakePoint(10, 27.5), TRUE, 2, '1F 主通道与西通道交汇'),
    (9603, 1, 103, 'WAYPOINT',  'A区通道口',    ST_MakePoint(30, 27.5), TRUE, 3, '1F A 区主通道口'),
    (9604, 1, 103, 'JUNCTION',  '中庭/服务台',  ST_MakePoint(50, 27.5), TRUE, 4, '1F 中庭服务台区域'),
    (9605, 1, 103, 'WAYPOINT',  'B区通道口',    ST_MakePoint(70, 27.5), TRUE, 5, '1F B 区主通道口'),
    (9606, 1, 103, 'JUNCTION',  '主通道东口',   ST_MakePoint(90, 27.5), TRUE, 6, '1F 主通道与东通道交汇'),
    (9607, 1, 103, 'WAYPOINT',  '商场入口B',    ST_MakePoint(98, 27.5), TRUE, 7, '1F 东侧入口'),
    (9611, 1, 103, 'JUNCTION',  '南通道西端',   ST_MakePoint(10, 15), TRUE, 11, '1F 南通道西端'),
    (9612, 1, 103, 'JUNCTION',  '北通道西端',   ST_MakePoint(10, 42), TRUE, 12, '1F 北通道西端'),
    (9613, 1, 103, 'JUNCTION',  '南通道东端',   ST_MakePoint(90, 15), TRUE, 13, '1F 南通道东端'),
    (9614, 1, 103, 'JUNCTION',  '北通道东端',   ST_MakePoint(90, 42), TRUE, 14, '1F 北通道东端'),
    (9615, 1, 103, 'WAYPOINT',  '南通道A区',    ST_MakePoint(30, 15), TRUE, 15, '1F 南通道 A 区段'),
    (9616, 1, 103, 'WAYPOINT',  '南通道B区',    ST_MakePoint(70, 15), TRUE, 16, '1F 南通道 B 区段'),
    (9617, 1, 103, 'WAYPOINT',  '北通道A区',    ST_MakePoint(30, 42), TRUE, 17, '1F 北通道 A 区段'),
    (9618, 1, 103, 'WAYPOINT',  '北通道B区',    ST_MakePoint(70, 42), TRUE, 18, '1F 北通道 B 区段'),
    (9691, 1, 103, 'ELEVATOR',  '电梯口',       ST_MakePoint(50, 55), TRUE, 91, '1F 客梯1 口'),
    (9692, 1, 103, 'ESCALATOR', '扶梯口',       ST_MakePoint(30, 55), TRUE, 92, '1F 扶梯1 口'),
    (9693, 1, 103, 'STAIR',     '楼梯口',       ST_MakePoint(50, 5), TRUE, 93, '1F 楼梯1 口')
ON CONFLICT (id) DO NOTHING;

-- 车位入口节点（坐标取自车位 entrance_point）
INSERT INTO nav_node (id, mall_id, floor_id, node_type, name, geom, is_accessible, sort_order, remark)
SELECT 91000 + (ps.id - 30000), 1, 101, 'SPACE_ENTRY', 'B2 车位入口 ' || ps.space_no,
       ps.entrance_point, TRUE, 0, '由种子脚本生成'
FROM parking_space ps
WHERE ps.mall_id = 1 AND ps.floor_id = 101
ON CONFLICT (id) DO NOTHING;

INSERT INTO nav_node (id, mall_id, floor_id, node_type, name, geom, is_accessible, sort_order, remark)
SELECT 92000 + (ps.id - 31000), 1, 102, 'SPACE_ENTRY', 'B1 车位入口 ' || ps.space_no,
       ps.entrance_point, TRUE, 0, '由种子脚本生成'
FROM parking_space ps
WHERE ps.mall_id = 1 AND ps.floor_id = 102
ON CONFLICT (id) DO NOTHING;

-- 商铺入口节点
INSERT INTO nav_node (id, mall_id, floor_id, node_type, name, geom, is_accessible, sort_order, remark)
VALUES
    (94001, 1, 102, 'SHOP_ENTRY', '海底捞入口',  ST_MakePoint(12, 35), TRUE, 1, 'B1 海底捞入口'),
    (94002, 1, 102, 'SHOP_ENTRY', '面包新语入口', ST_MakePoint(20, 35), TRUE, 2, 'B1 面包新语入口'),
    (94003, 1, 102, 'SHOP_ENTRY', '盒马鲜生入口', ST_MakePoint(28, 35), TRUE, 3, 'B1 盒马鲜生入口'),
    (97001, 1, 103, 'SHOP_ENTRY', '星巴克入口',  ST_MakePoint(12, 35), TRUE, 1, '1F 星巴克入口'),
    (97002, 1, 103, 'SHOP_ENTRY', '优衣库入口',  ST_MakePoint(20, 35), TRUE, 2, '1F 优衣库入口'),
    (97003, 1, 103, 'SHOP_ENTRY', '苹果授权店入口', ST_MakePoint(28, 35), TRUE, 3, '1F 苹果授权店入口'),
    (97004, 1, 103, 'SHOP_ENTRY', '名创优品入口', ST_MakePoint(36, 35), TRUE, 4, '1F 名创优品入口'),
    (97005, 1, 103, 'SHOP_ENTRY', '屈臣氏入口',  ST_MakePoint(44, 35), TRUE, 5, '1F 屈臣氏入口'),
    (97006, 1, 103, 'SHOP_ENTRY', '周大福入口',  ST_MakePoint(52, 35), TRUE, 6, '1F 周大福入口'),
    (97007, 1, 103, 'SHOP_ENTRY', '小米之家入口', ST_MakePoint(60, 19), TRUE, 7, '1F 小米之家入口'),
    (97008, 1, 103, 'SHOP_ENTRY', '必胜客入口',  ST_MakePoint(68, 19), TRUE, 8, '1F 必胜客入口'),
    (97009, 1, 103, 'SHOP_ENTRY', '肯德基入口',  ST_MakePoint(76, 19), TRUE, 9, '1F 肯德基入口'),
    (97010, 1, 103, 'SHOP_ENTRY', '无印良品入口', ST_MakePoint(84, 19), TRUE, 10, '1F 无印良品入口')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 7. 导航图: 边（nav_edge）
-- ============================================================
INSERT INTO nav_edge (id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status)
VALUES
    -- B2 主通道
    (94001, 1, 9001, 9002, 'WALK', 8, 1, TRUE, 1),
    (94002, 1, 9002, 9003, 'WALK', 20, 1, TRUE, 1),
    (94003, 1, 9003, 9004, 'WALK', 20, 1, TRUE, 1),
    (94004, 1, 9004, 9005, 'WALK', 20, 1, TRUE, 1),
    (94005, 1, 9005, 9006, 'WALK', 20, 1, TRUE, 1),
    (94006, 1, 9006, 9007, 'WALK', 8, 1, TRUE, 1),
    -- B2 南通道 / 北通道
    (94007, 1, 9011, 9015, 'WALK', 20, 1, TRUE, 1),
    (94008, 1, 9015, 9016, 'WALK', 40, 1, TRUE, 1),
    (94009, 1, 9016, 9013, 'WALK', 20, 1, TRUE, 1),
    (94010, 1, 9012, 9017, 'WALK', 20, 1, TRUE, 1),
    (94011, 1, 9017, 9018, 'WALK', 40, 1, TRUE, 1),
    (94012, 1, 9018, 9014, 'WALK', 20, 1, TRUE, 1),
    -- B2 主通道与南北通道连接
    (94013, 1, 9002, 9011, 'WALK', 12.5, 1, TRUE, 1),
    (94014, 1, 9002, 9012, 'WALK', 14.5, 1, TRUE, 1),
    (94015, 1, 9003, 9015, 'WALK', 12.5, 1, TRUE, 1),
    (94016, 1, 9003, 9017, 'WALK', 14.5, 1, TRUE, 1),
    (94017, 1, 9005, 9016, 'WALK', 12.5, 1, TRUE, 1),
    (94018, 1, 9005, 9018, 'WALK', 14.5, 1, TRUE, 1),
    (94019, 1, 9006, 9013, 'WALK', 12.5, 1, TRUE, 1),
    (94020, 1, 9006, 9014, 'WALK', 14.5, 1, TRUE, 1),
    -- B2 电梯口 / 楼梯口
    (94021, 1, 9091, 9004, 'WALK', 27.5, 1, TRUE, 1),
    (94022, 1, 9092, 9004, 'WALK', 22.5, 1, TRUE, 1),
    -- B1 主通道
    (94301, 1, 9301, 9302, 'WALK', 8, 1, TRUE, 1),
    (94302, 1, 9302, 9303, 'WALK', 20, 1, TRUE, 1),
    (94303, 1, 9303, 9304, 'WALK', 20, 1, TRUE, 1),
    (94304, 1, 9304, 9305, 'WALK', 20, 1, TRUE, 1),
    (94305, 1, 9305, 9306, 'WALK', 20, 1, TRUE, 1),
    (94306, 1, 9306, 9307, 'WALK', 8, 1, TRUE, 1),
    -- B1 南通道 / 北通道
    (94307, 1, 9311, 9315, 'WALK', 20, 1, TRUE, 1),
    (94308, 1, 9315, 9316, 'WALK', 40, 1, TRUE, 1),
    (94309, 1, 9316, 9313, 'WALK', 20, 1, TRUE, 1),
    (94310, 1, 9312, 9317, 'WALK', 20, 1, TRUE, 1),
    (94311, 1, 9317, 9318, 'WALK', 40, 1, TRUE, 1),
    (94312, 1, 9318, 9314, 'WALK', 20, 1, TRUE, 1),
    -- B1 主通道与南北通道连接
    (94313, 1, 9302, 9311, 'WALK', 12.5, 1, TRUE, 1),
    (94314, 1, 9302, 9312, 'WALK', 14.5, 1, TRUE, 1),
    (94315, 1, 9303, 9315, 'WALK', 12.5, 1, TRUE, 1),
    (94316, 1, 9303, 9317, 'WALK', 14.5, 1, TRUE, 1),
    (94317, 1, 9305, 9316, 'WALK', 12.5, 1, TRUE, 1),
    (94318, 1, 9305, 9318, 'WALK', 14.5, 1, TRUE, 1),
    (94319, 1, 9306, 9313, 'WALK', 12.5, 1, TRUE, 1),
    (94320, 1, 9306, 9314, 'WALK', 14.5, 1, TRUE, 1),
    -- B1 电梯口 / 楼梯口 / 扶梯口
    (94321, 1, 9391, 9304, 'WALK', 27.5, 1, TRUE, 1),
    (94322, 1, 9392, 9304, 'WALK', 22.5, 1, TRUE, 1),
    (94323, 1, 9393, 9303, 'WALK', 27.5, 1, TRUE, 1),
    -- 1F 主通道
    (94501, 1, 9601, 9602, 'WALK', 8, 1, TRUE, 1),
    (94502, 1, 9602, 9603, 'WALK', 20, 1, TRUE, 1),
    (94503, 1, 9603, 9604, 'WALK', 20, 1, TRUE, 1),
    (94504, 1, 9604, 9605, 'WALK', 20, 1, TRUE, 1),
    (94505, 1, 9605, 9606, 'WALK', 20, 1, TRUE, 1),
    (94506, 1, 9606, 9607, 'WALK', 8, 1, TRUE, 1),
    -- 1F 南通道 / 北通道
    (94507, 1, 9611, 9615, 'WALK', 20, 1, TRUE, 1),
    (94508, 1, 9615, 9616, 'WALK', 40, 1, TRUE, 1),
    (94509, 1, 9616, 9613, 'WALK', 20, 1, TRUE, 1),
    (94510, 1, 9612, 9617, 'WALK', 20, 1, TRUE, 1),
    (94511, 1, 9617, 9618, 'WALK', 40, 1, TRUE, 1),
    (94512, 1, 9618, 9614, 'WALK', 20, 1, TRUE, 1),
    -- 1F 主通道与南北通道连接
    (94513, 1, 9602, 9611, 'WALK', 12.5, 1, TRUE, 1),
    (94514, 1, 9602, 9612, 'WALK', 14.5, 1, TRUE, 1),
    (94515, 1, 9603, 9615, 'WALK', 12.5, 1, TRUE, 1),
    (94516, 1, 9603, 9617, 'WALK', 14.5, 1, TRUE, 1),
    (94517, 1, 9605, 9616, 'WALK', 12.5, 1, TRUE, 1),
    (94518, 1, 9605, 9618, 'WALK', 14.5, 1, TRUE, 1),
    (94519, 1, 9606, 9613, 'WALK', 12.5, 1, TRUE, 1),
    (94520, 1, 9606, 9614, 'WALK', 14.5, 1, TRUE, 1),
    -- 1F 电梯口 / 扶梯口 / 楼梯口
    (94521, 1, 9691, 9604, 'WALK', 27.5, 1, TRUE, 1),
    (94522, 1, 9692, 9603, 'WALK', 27.5, 1, TRUE, 1),
    (94523, 1, 9693, 9604, 'WALK', 22.5, 1, TRUE, 1)
ON CONFLICT (id) DO NOTHING;

-- B2 车位入口边（连接到最近通道节点, 距离由几何实时计算）
WITH b2_space_links AS (
    SELECT ps.id AS space_id,
           CASE
               WHEN ps.zone_id = 201 AND ST_Y(ps.entrance_point) = 20 AND ST_X(ps.entrance_point) <= 20 THEN 9011
               WHEN ps.zone_id = 201 AND ST_Y(ps.entrance_point) = 20 THEN 9015
               WHEN ps.zone_id = 201 AND ST_X(ps.entrance_point) <= 20 THEN 9002
               WHEN ps.zone_id = 201 THEN 9003
               WHEN ps.zone_id = 202 AND ST_Y(ps.entrance_point) = 20 AND ST_X(ps.entrance_point) <= 80 THEN 9016
               WHEN ps.zone_id = 202 AND ST_Y(ps.entrance_point) = 20 THEN 9013
               WHEN ps.zone_id = 202 AND ST_X(ps.entrance_point) <= 80 THEN 9005
               ELSE 9006
           END AS target_node_id
    FROM parking_space ps
    WHERE ps.mall_id = 1 AND ps.floor_id = 101
)
INSERT INTO nav_edge (id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status)
SELECT 94200 + (sl.space_id - 30000), 1, 91000 + (sl.space_id - 30000), sl.target_node_id, 'WALK',
       ROUND(ST_Distance(nd.geom, ps.entrance_point)::numeric, 2), 1, TRUE, 1
FROM b2_space_links sl
JOIN parking_space ps ON ps.id = sl.space_id
JOIN nav_node nd ON nd.id = sl.target_node_id
ON CONFLICT (id) DO NOTHING;

-- B1 车位入口边
WITH b1_space_links AS (
    SELECT ps.id AS space_id,
           CASE WHEN ps.zone_id = 301 AND ST_X(ps.entrance_point) <= 20 THEN 9311
                WHEN ps.zone_id = 301 THEN 9315
                WHEN ps.zone_id = 302 AND ST_X(ps.entrance_point) <= 80 THEN 9316
                ELSE 9313
           END AS target_node_id
    FROM parking_space ps
    WHERE ps.mall_id = 1 AND ps.floor_id = 102
)
INSERT INTO nav_edge (id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status)
SELECT 94400 + (sl.space_id - 31000), 1, 92000 + (sl.space_id - 31000), sl.target_node_id, 'WALK',
       ROUND(ST_Distance(nd.geom, ps.entrance_point)::numeric, 2), 1, TRUE, 1
FROM b1_space_links sl
JOIN parking_space ps ON ps.id = sl.space_id
JOIN nav_node nd ON nd.id = sl.target_node_id
ON CONFLICT (id) DO NOTHING;

-- B1 商铺入口边
INSERT INTO nav_edge (id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status)
VALUES
    (94411, 1, 94001, 9312, 'WALK', 7.28, 1, TRUE, 1),
    (94412, 1, 94002, 9317, 'WALK', 12.21, 1, TRUE, 1),
    (94413, 1, 94003, 9317, 'WALK', 7.28, 1, TRUE, 1)
ON CONFLICT (id) DO NOTHING;

-- 1F 商铺入口边
INSERT INTO nav_edge (id, mall_id, from_node_id, to_node_id, edge_type, distance_m, weight, bidirectional, status)
VALUES
    (94601, 1, 97001, 9612, 'WALK', 7.28, 1, TRUE, 1),
    (94602, 1, 97002, 9617, 'WALK', 12.21, 1, TRUE, 1),
    (94603, 1, 97003, 9617, 'WALK', 7.28, 1, TRUE, 1),
    (94604, 1, 97004, 9617, 'WALK', 8.60, 1, TRUE, 1),
    (94605, 1, 97005, 9604, 'WALK', 9.60, 1, TRUE, 1),
    (94606, 1, 97006, 9604, 'WALK', 7.57, 1, TRUE, 1),
    (94607, 1, 97007, 9616, 'WALK', 12.81, 1, TRUE, 1),
    (94608, 1, 97008, 9616, 'WALK', 4.47, 1, TRUE, 1),
    (94609, 1, 97009, 9616, 'WALK', 7.21, 1, TRUE, 1),
    (94610, 1, 97010, 9613, 'WALK', 7.21, 1, TRUE, 1)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 8. 跨楼层连接（floor_connect）
--    电梯: B2(9091) <-> B1(9391) <-> 1F(9691)
--    楼梯: B2(9092) <-> B1(9392)
--    扶梯: B1(9393) <-> 1F(9692)
-- ============================================================
INSERT INTO floor_connect (id, mall_id, from_floor_id, to_floor_id, connect_type, from_node_id, to_node_id,
                           cost_m, status)
VALUES
    (9801, 1, 101, 102, 'ELEVATOR', 9091, 9391, 20, 1),
    (9802, 1, 102, 103, 'ELEVATOR', 9391, 9691, 20, 1),
    (9803, 1, 101, 102, 'STAIR',    9092, 9392, 15, 1),
    (9804, 1, 102, 103, 'ESCALATOR', 9393, 9692, 10, 1)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 9. 用户-商场绑定 / 车辆 / 停车记录 / 收藏 / 历史 / 会话 / 日志
--    用户 ID 引用 auth 架构种子: 1001=admin 1002=operator 1003=demo
-- ============================================================
INSERT INTO mall_user (id, mall_id, user_id, role_code, status)
VALUES
    (99001, 1, 1001, 'MALL_ADMIN', 1),
    (99002, 1, 1002, 'OPERATOR',   1),
    (99003, 1, 1003, 'USER',       1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_vehicle (id, user_id, plate_no, is_default, status)
VALUES
    (99101, 1003, '沪A·D12345', TRUE, 1),
    (99102, 1003, '沪B·E67890', FALSE, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO parking_record (id, user_id, mall_id, space_id, plate_no, entry_time, exit_time, status, source, remark)
VALUES
    (99201, 1003, 1, 30005, '沪A·D12345', now() - interval '2 hours', NULL, 'PARKING', 'APP', '演示进行中的停车记录'),
    (99202, 1003, 1, 30010, '沪A·D12345', now() - interval '1 day', now() - interval '1 day' + interval '3 hours', 'ENDED', 'APP', '演示已结束的停车记录')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_destination (id, user_id, mall_id, target_type, target_id, alias_name, sort_order)
VALUES
    (99601, 1003, 1, 'SPACE', 30008, '常去车位', 1),
    (99602, 1003, 1, 'SHOP',  601,   '星巴克',   2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO nav_history (id, user_id, mall_id, from_floor_id, to_floor_id, from_type, from_target_id,
                         to_type, to_target_id, path_geom, distance_m, duration_s, status, start_time, end_time)
VALUES
    (99301, 1003, 1, 101, 101, 'CURRENT_POSITION', NULL, 'SPACE', 30005,
     ST_GeomFromText('LINESTRING(2 27.5, 10 27.5, 10 20)', 0), 17.5, 60, 'FINISHED',
     now() - interval '2 hours', now() - interval '2 hours' + interval '1 minute'),
    (99302, 1003, 1, 101, 103, 'SPACE', 30005, 'SHOP', 601,
     ST_GeomFromText('LINESTRING(10 20, 10 27.5, 10 42, 12 35)', 0), 24.78, 90, 'FINISHED',
     now() - interval '1 hour', now() - interval '1 hour' + interval '2 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO positioning_session (id, user_id, mall_id, device_id, floor_id, last_pos, last_accuracy_m,
                                 start_time, status)
VALUES
    (99401, 1003, 1, 'demo-iphone-15', 101, ST_MakePoint(5.5, 28.5), 2.5, now() - interval '30 minutes', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO op_log (id, user_id, mall_id, module, action, target_type, target_id, detail, ip)
VALUES
    (99501, 1001, 1, '车位', '新增', 'parking_space', 30001,
     '{"action":"CREATE","entity":"parking_space","spaceNo":"B2-A01"}'::jsonb, '127.0.0.1'),
    (99502, 1002, 1, '商铺', '更新', 'shop', 611,
     '{"action":"UPDATE","entity":"shop","shopName":"海底捞火锅","status":"OPEN"}'::jsonb, '127.0.0.1')
ON CONFLICT (id) DO NOTHING;
