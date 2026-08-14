-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 目标数据库: postgis_36_sample
-- 目标架构: auth（用户中心 / 认证服务）
-- 前置: 先执行 sql/00_init.sql 完成建库与扩展安装
--
-- 约定:
--   * 主键使用应用层雪花ID (MyBatis-Plus IdType.ASSIGN_ID)
--   * 逻辑删除字段 deleted: 0=正常 1=已删除
--   * 时间统一 TIMESTAMPTZ, 由应用层 MetaObjectHandler 填充
--   * 跨架构表(如 user_id) 不建物理外键, 只建索引, 由应用保证一致性
--   * 每个字段均带中文注释
-- ============================================================

-- 创建用户中心架构（若已存在则跳过）
CREATE SCHEMA IF NOT EXISTS auth;

COMMENT ON SCHEMA auth IS '用户中心/认证服务架构：用户、角色、权限、登录日志';

-- 以下未加架构前缀的对象统一创建/查询于 auth 架构
-- （保留 public 在搜索路径中, 用于解析 PostGIS/pg_trgm 等扩展函数）
SET search_path TO auth, public;

-- ---------- 用户 ----------
CREATE TABLE sys_user (
    id             BIGINT       NOT NULL,
    username       VARCHAR(64)  NOT NULL,
    password_hash  VARCHAR(128) NOT NULL,
    nickname       VARCHAR(64),
    real_name      VARCHAR(64),
    phone          VARCHAR(20),
    email          VARCHAR(128),
    avatar_url     VARCHAR(512),
    user_type      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status         SMALLINT     NOT NULL DEFAULT 1,
    last_login_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted        SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_user PRIMARY KEY (id)
);

COMMENT ON TABLE sys_user IS '用户表：平台车主、商场运营、管理员、商户的账号';
COMMENT ON COLUMN sys_user.id IS '用户ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_user.username IS '登录用户名';
COMMENT ON COLUMN sys_user.password_hash IS '登录密码（BCrypt加密哈希）';
COMMENT ON COLUMN sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.avatar_url IS '头像图片地址';
COMMENT ON COLUMN sys_user.user_type IS '用户类型：USER=普通车主用户 STAFF=商场运营人员 ADMIN=平台管理员 MERCHANT=商铺商户';
COMMENT ON COLUMN sys_user.status IS '账号状态：1=启用 0=禁用';
COMMENT ON COLUMN sys_user.last_login_at IS '最后登录时间';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_sys_user_username ON sys_user (username) WHERE deleted = 0;
CREATE UNIQUE INDEX uk_sys_user_phone    ON sys_user (phone)    WHERE deleted = 0 AND phone <> '';
CREATE INDEX idx_sys_user_created        ON sys_user (created_at);

-- ---------- 角色 ----------
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL,
    role_code   VARCHAR(32)  NOT NULL,
    role_name   VARCHAR(64)  NOT NULL,
    description VARCHAR(255),
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_role PRIMARY KEY (id)
);

COMMENT ON TABLE sys_role IS '角色表：定义平台级角色';
COMMENT ON COLUMN sys_role.id IS '角色ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_role.role_code IS '角色编码：ADMIN=平台管理员 MALL_OPERATOR=商场运营 MERCHANT=商铺商户 USER=普通用户';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.description IS '角色描述';
COMMENT ON COLUMN sys_role.status IS '角色状态：1=启用 0=禁用';
COMMENT ON COLUMN sys_role.created_at IS '创建时间';
COMMENT ON COLUMN sys_role.updated_at IS '更新时间';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_sys_role_code ON sys_role (role_code) WHERE deleted = 0;

-- ---------- 权限点 ----------
CREATE TABLE sys_permission (
    id         BIGINT       NOT NULL,
    parent_id  BIGINT       NOT NULL DEFAULT 0,
    perm_code  VARCHAR(64)  NOT NULL,
    perm_name  VARCHAR(64)  NOT NULL,
    perm_type  VARCHAR(20)  NOT NULL DEFAULT 'API',
    path       VARCHAR(255),
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_permission PRIMARY KEY (id)
);

COMMENT ON TABLE sys_permission IS '权限表：菜单/按钮/接口三级权限点';
COMMENT ON COLUMN sys_permission.id IS '权限ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_permission.parent_id IS '父权限ID，0=根权限';
COMMENT ON COLUMN sys_permission.perm_code IS '权限编码';
COMMENT ON COLUMN sys_permission.perm_name IS '权限名称';
COMMENT ON COLUMN sys_permission.perm_type IS '权限类型：MENU=菜单 BUTTON=按钮 API=接口';
COMMENT ON COLUMN sys_permission.path IS '资源路径';
COMMENT ON COLUMN sys_permission.sort_order IS '排序号';
COMMENT ON COLUMN sys_permission.created_at IS '创建时间';
COMMENT ON COLUMN sys_permission.deleted IS '逻辑删除标记：0=正常 1=已删除';

CREATE UNIQUE INDEX uk_sys_perm_code ON sys_permission (perm_code) WHERE deleted = 0;

-- ---------- 用户-角色关联 ----------
CREATE TABLE sys_user_role (
    id         BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    role_id    BIGINT       NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_sys_user_role PRIMARY KEY (id)
);

COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '关联ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_user_role.created_at IS '创建时间';

CREATE UNIQUE INDEX uk_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX idx_user_role_role ON sys_user_role (role_id);

-- ---------- 角色-权限关联 ----------
CREATE TABLE sys_role_permission (
    id            BIGINT       NOT NULL,
    role_id       BIGINT       NOT NULL,
    permission_id BIGINT       NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_sys_role_permission PRIMARY KEY (id)
);

COMMENT ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '关联ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';
COMMENT ON COLUMN sys_role_permission.created_at IS '创建时间';

CREATE UNIQUE INDEX uk_role_perm ON sys_role_permission (role_id, permission_id);
CREATE INDEX idx_role_perm_perm ON sys_role_permission (permission_id);

-- ---------- 登录日志 ----------
CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL,
    user_id     BIGINT,
    username    VARCHAR(64),
    ip          VARCHAR(64),
    user_agent  VARCHAR(255),
    success     SMALLINT     NOT NULL DEFAULT 1,
    fail_reason VARCHAR(255),
    login_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_sys_login_log PRIMARY KEY (id)
);

COMMENT ON TABLE sys_login_log IS '登录日志表：记录每次登录成功/失败';
COMMENT ON COLUMN sys_login_log.id IS '日志ID（雪花ID，应用层生成）';
COMMENT ON COLUMN sys_login_log.user_id IS '用户ID（未登录成功时为空）';
COMMENT ON COLUMN sys_login_log.username IS '登录用户名';
COMMENT ON COLUMN sys_login_log.ip IS '登录IP地址';
COMMENT ON COLUMN sys_login_log.user_agent IS '浏览器/客户端User-Agent';
COMMENT ON COLUMN sys_login_log.success IS '登录结果：1=成功 0=失败';
COMMENT ON COLUMN sys_login_log.fail_reason IS '登录失败原因';
COMMENT ON COLUMN sys_login_log.login_at IS '登录时间';

CREATE INDEX idx_login_log_user ON sys_login_log (user_id, login_at DESC);
CREATE INDEX idx_login_log_time ON sys_login_log (login_at);

-- ============================================================
-- 初始化内置角色
-- ============================================================
INSERT INTO sys_role (id, role_code, role_name, description)
VALUES
    (1, 'ADMIN',         '平台管理员', '平台级管理权限'),
    (2, 'MALL_OPERATOR', '商场运营',   '管理单个商场的楼层/车位/商铺/导航数据'),
    (3, 'MERCHANT',      '商铺商户',   '查看所属商铺与访客数据'),
    (4, 'USER',          '普通用户',   '车位/商铺导航、收藏、停车记录');
