package com.positioning.common.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * PostgreSQL TIMESTAMPTZ &lt;-&gt; LocalDateTime 类型处理器
 * <p>
 * 背景: 数据库时间列统一为 TIMESTAMPTZ(带时区), 而实体字段统一为 java.time.LocalDateTime;
 * PostgreSQL JDBC 42.7+ 对 timestamptz 列调用 getObject(LocalDateTime.class) 会直接抛
 * "Cannot convert the column of type TIMESTAMPTZ to requested type java.time.LocalDateTime",
 * 导致所有含时间字段的 SELECT 失败。本处理器把读取结果统一转换为 LocalDateTime:
 * OffsetDateTime/Instant/Timestamp 均按 JVM 默认时区截断为本地时间;
 * 写入时保持 ps.setObject(LocalDateTime), 由 PG 按会话时区解释(与建表默认 now() 语义一致)。
 * <p>
 * 注册方式: 在 biz 模块 application.yml 配置
 * mybatis-plus.configuration.type-handlers-package: com.positioning.common.mybatis
 */
@MappedTypes(LocalDateTime.class)
public class PostgresTimestamptzTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getObject(columnName));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getObject(columnIndex));
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getObject(columnIndex));
    }

    /** 兼容驱动返回的各种时间类型, 统一转为 LocalDateTime */
    private LocalDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        // 驱动返回字符串等异常形态时兜底解析
        return LocalDateTime.parse(value.toString());
    }
}
