-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 目标数据库: postgis_36_sample
-- 目标架构: auth（用户中心 / 认证服务）
-- 前置: 已执行 sql/00_init.sql、sql/01_auth_db.sql
--
-- 本脚本为种子数据:
--   * 初始化平台管理员 / 商场运营 / 演示用户三个账号（密码均为 123456, BCrypt）
--   * 初始化接口权限点（sys_permission）
--   * 建立 用户-角色、角色-权限 绑定（RBAC）
--
-- ID 约定: 种子数据使用固定 ID 便于跨脚本引用
--   sys_user:       1001=平台管理员 admin / 1002=商场运营 operator / 1003=演示用户 demo
--   sys_permission: 2001 起
--   sys_user_role:  3001 起
--   sys_role_permission: 40001/41001/42001/43001 分段
-- 重复执行: 脚本整体幂等(ON CONFLICT 跳过), 可安全重复执行
-- ============================================================

SET search_path TO auth, public;

-- ---------- 用户 ----------
-- 说明: 若此前通过注册接口创建过同名账号(username 唯一), 请先清理旧账号再执行本脚本
--   DELETE FROM auth.sys_user WHERE username IN ('admin','operator','demo') AND deleted = 0;
INSERT INTO sys_user (id, username, password_hash, nickname, real_name, phone, email, user_type, status)
SELECT v.id, v.username, v.password_hash, v.nickname, v.real_name, v.phone, v.email, v.user_type, v.status
FROM (VALUES
    (1001, 'admin',    '$2a$10$DrN94Pcbj/wi0sxDnVuJv.L0JF9xV3L9KlaFs93J.Rc0GLJNqY9PW', '平台管理员', '管理员', '13800000001', 'admin@example.com', 'ADMIN', 1),
    (1002, 'operator', '$2a$10$DrN94Pcbj/wi0sxDnVuJv.L0JF9xV3L9KlaFs93J.Rc0GLJNqY9PW', '商场运营',   '运营小张', '13800000002', 'operator@example.com', 'STAFF', 1),
    (1003, 'demo',     '$2a$10$DrN94Pcbj/wi0sxDnVuJv.L0JF9xV3L9KlaFs93J.Rc0GLJNqY9PW', '演示用户',   '演示小李', '13800000003', 'demo@example.com', 'USER', 1)
) AS v(id, username, password_hash, nickname, real_name, phone, email, user_type, status)
WHERE NOT EXISTS (SELECT 1 FROM sys_user u
                  WHERE u.username = v.username AND u.deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.id = v.id);

-- ---------- 权限点（接口级） ----------
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, sort_order)
VALUES
    (2001, 0, 'mall:list',        '商场查看',     'API', '/business/mall/**',                1),
    (2002, 0, 'mall:edit',        '商场维护',     'API', '/business/mall/**',                2),
    (2003, 0, 'floor:list',       '楼层查看',     'API', '/business/floor/**',               3),
    (2004, 0, 'floor:edit',       '楼层维护',     'API', '/business/floor/**',               4),
    (2005, 0, 'zone:list',        '分区查看',     'API', '/business/zone/**',                5),
    (2006, 0, 'zone:edit',        '分区维护',     'API', '/business/zone/**',                6),
    (2007, 0, 'space:list',       '车位查看',     'API', '/business/space/**',               7),
    (2008, 0, 'space:edit',       '车位维护',     'API', '/business/space/**',               8),
    (2009, 0, 'space:operate',    '车位占用释放', 'API', '/business/space/occupy',           9),
    (2010, 0, 'shop:list',        '商铺查看',     'API', '/business/shop/**',                10),
    (2011, 0, 'shop:edit',        '商铺维护',     'API', '/business/shop/**',                11),
    (2012, 0, 'category:list',    '分类查看',     'API', '/business/shop-category/**',       12),
    (2013, 0, 'category:edit',    '分类维护',     'API', '/business/shop-category/**',       13),
    (2014, 0, 'poi:list',         '设施查看',     'API', '/business/poi/**',                 14),
    (2015, 0, 'poi:edit',         '设施维护',     'API', '/business/poi/**',                 15),
    (2016, 0, 'beacon:list',      '信标查看',     'API', '/business/beacon/**',              16),
    (2017, 0, 'beacon:edit',      '信标维护',     'API', '/business/beacon/**',              17),
    (2018, 0, 'nav:list',         '导航图查看',   'API', '/business/nav-node/**',            18),
    (2019, 0, 'nav:edit',         '导航图维护',   'API', '/business/nav-node/**',            19),
    (2020, 0, 'nav:route',        '路径规划',     'API', '/business/nav/route',              20),
    (2021, 0, 'parking:list',     '停车记录查看', 'API', '/business/parking-record/**',      21),
    (2022, 0, 'parking:operate',  '停车入场离场', 'API', '/business/parking-record/**',      22),
    (2023, 0, 'destination:manage','收藏管理',    'API', '/business/destination/**',         23),
    (2024, 0, 'history:list',     '导航历史',     'API', '/business/nav-history/**',         24),
    (2025, 0, 'session:manage',   '定位会话',     'API', '/business/positioning-session/**', 25),
    (2026, 0, 'user:list',        '用户查看',     'API', '/business/mall-user/**',           26),
    (2027, 0, 'user:edit',        '用户绑定维护', 'API', '/business/mall-user/**',           27),
    (2028, 0, 'log:list',         '操作日志',     'API', '/business/op-log/**',              28)
ON CONFLICT (id) DO NOTHING;

-- ---------- 用户-角色绑定 ----------
-- 角色: 1=ADMIN 2=MALL_OPERATOR 3=MERCHANT 4=USER（由 01_auth_db.sql 初始化）
INSERT INTO sys_user_role (id, user_id, role_id)
VALUES
    (3001, 1001, 1),
    (3002, 1002, 2),
    (3003, 1003, 4)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ---------- 角色-权限绑定 ----------
-- ADMIN(角色1): 全部权限
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 40000 + row_number() OVER (ORDER BY id), 1, id FROM sys_permission
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MALL_OPERATOR(角色2): 业务维护类权限（不含用户管理/个人业务）
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 41000 + row_number() OVER (ORDER BY id), 2, id FROM sys_permission
WHERE perm_code NOT IN ('user:list', 'user:edit', 'destination:manage', 'history:list', 'session:manage')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MERCHANT(角色3): 商铺/车位查看 + 路径规划
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 42000 + row_number() OVER (ORDER BY id), 3, id FROM sys_permission
WHERE perm_code IN ('space:list', 'shop:list', 'nav:route')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- USER(角色4): 普通用户能力（检索/导航/停车/收藏/历史/会话）
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT 43000 + row_number() OVER (ORDER BY id), 4, id FROM sys_permission
WHERE perm_code IN ('space:list', 'shop:list', 'nav:route', 'parking:list',
                    'parking:operate', 'destination:manage', 'history:list', 'session:manage')
ON CONFLICT (role_id, permission_id) DO NOTHING;
