package com.acme.payroll.dao;

import com.acme.payroll.db.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Small JDBC helper shared by all DAOs. Every call borrows a connection and closes it again.
 */
public abstract class AbstractDao {

    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected final Database db;

    protected AbstractDao(Database db) {
        this.db = db;
    }

    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<T>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DaoException("Query failed: " + sql, e);
        }
    }

    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    protected long queryLong(String sql, Object... params) {
        Long value = queryOne(sql, new RowMapper<Long>() {
            @Override
            public Long map(ResultSet rs) throws SQLException {
                return rs.getLong(1);
            }
        }, params);
        return value == null ? 0L : value;
    }

    protected BigDecimal queryDecimal(String sql, Object... params) {
        BigDecimal value = queryOne(sql, new RowMapper<BigDecimal>() {
            @Override
            public BigDecimal map(ResultSet rs) throws SQLException {
                return rs.getBigDecimal(1);
            }
        }, params);
        return value == null ? BigDecimal.ZERO : value;
    }

    protected int update(String sql, Object... params) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Update failed: " + sql, e);
        }
    }

    protected long insert(String sql, Object... params) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return -1L;
        } catch (SQLException e) {
            throw new DaoException("Insert failed: " + sql, e);
        }
    }

    protected static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof LocalDate) {
                ps.setDate(i + 1, Date.valueOf((LocalDate) p));
            } else if (p instanceof LocalDateTime) {
                ps.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) p));
            } else if (p instanceof Enum) {
                ps.setString(i + 1, ((Enum<?>) p).name());
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }

    protected static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    protected static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toLocalDateTime();
    }

    protected static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }
}
