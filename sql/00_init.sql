-- ============================================================
-- 商场可视化智能车位/商铺导航系统
-- 初始化脚本: 创建目标数据库并安装扩展
--
-- 执行方式（psql）:
--   psql -U <用户> -h <主机> -f sql/00_init.sql
-- 或手工执行:
--   1) 连接任意实例库(如 postgres), 执行 CREATE DATABASE
--   2) 切换到 postgis_36_sample, 执行两条 CREATE EXTENSION
--
-- 注意:
--   * 本脚本必须在"实例级"执行一次, 不能放在事务中
--   * postgis / pg_trgm 是数据库级扩展, 安装一次即可, 后续 01/02 脚本无需重复安装
--   * 架构(auth/business)由 01/02 脚本各自创建, 无需在此创建
-- ============================================================

-- 空间计算扩展（geometry 类型、ST_* 函数、GIST 空间索引）
CREATE EXTENSION IF NOT EXISTS postgis;

-- 模糊搜索扩展（gin_trgm_ops, 用于商铺/车位名的 ILIKE 搜索）
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 校验: 应能查询到 postgis 版本
SELECT postgis_version();
