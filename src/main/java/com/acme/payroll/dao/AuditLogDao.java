package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.AuditEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao extends AbstractDao {

    private static final RowMapper<AuditEntry> MAPPER = new RowMapper<AuditEntry>() {
        @Override
        public AuditEntry map(ResultSet rs) throws SQLException {
            AuditEntry a = new AuditEntry();
            a.setId(rs.getLong("id"));
            a.setTimestamp(getDateTime(rs, "ts"));
            a.setUserEmail(rs.getString("user_email"));
            a.setAction(rs.getString("action"));
            a.setEntity(rs.getString("entity"));
            a.setEntityId(getLongOrNull(rs, "entity_id"));
            a.setDetails(rs.getString("details"));
            return a;
        }
    };

    public AuditLogDao(Database db) {
        super(db);
    }

    public void log(String userEmail, String action, String entity, Long entityId, String details) {
        log(LocalDateTime.now(), userEmail, action, entity, entityId, details);
    }

    public void log(LocalDateTime when, String userEmail, String action, String entity, Long entityId, String details) {
        insert("INSERT INTO audit_log (ts, user_email, action, entity, entity_id, details) VALUES (?, ?, ?, ?, ?, ?)",
                when, userEmail, action, entity, entityId, details);
    }

    /**
     * @param userText substring of the user email, may be null
     * @param action   exact action, may be null
     * @param from     inclusive, may be null
     * @param to       inclusive, may be null
     */
    public List<AuditEntry> find(String userText, String action, LocalDate from, LocalDate to, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1 = 1");
        List<Object> params = new ArrayList<Object>();
        if (userText != null && !userText.trim().isEmpty()) {
            sql.append(" AND LOWER(user_email) LIKE ?");
            params.add("%" + userText.trim().toLowerCase() + "%");
        }
        if (action != null && !action.isEmpty()) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        if (from != null) {
            sql.append(" AND ts >= ?");
            params.add(from.atStartOfDay());
        }
        if (to != null) {
            sql.append(" AND ts < ?");
            params.add(to.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY ts DESC, id DESC LIMIT ?");
        params.add(limit);
        return query(sql.toString(), MAPPER, params.toArray());
    }

    public List<String> findDistinctActions() {
        return query("SELECT DISTINCT action FROM audit_log ORDER BY action", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        });
    }
}
