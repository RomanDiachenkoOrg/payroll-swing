package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserDao extends AbstractDao {

    private static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User map(ResultSet rs) throws SQLException {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setRole(Role.valueOf(rs.getString("role")));
            u.setEmployeeId(getLongOrNull(rs, "employee_id"));
            u.setDepartmentId(getLongOrNull(rs, "department_id"));
            u.setActive(rs.getBoolean("active"));
            u.setLastLogin(getDateTime(rs, "last_login"));
            return u;
        }
    };

    public UserDao(Database db) {
        super(db);
    }

    public User findByEmail(String email) {
        return queryOne("SELECT * FROM app_user WHERE LOWER(email) = LOWER(?)", MAPPER, email);
    }

    public List<User> findAll() {
        return query("SELECT * FROM app_user ORDER BY role, email", MAPPER);
    }

    public long insert(User u) {
        long id = insert("INSERT INTO app_user (email, password_hash, role, employee_id, department_id, active) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                u.getEmail(), u.getPasswordHash(), u.getRole(), u.getEmployeeId(), u.getDepartmentId(), u.isActive());
        u.setId(id);
        return id;
    }

    public void updateLastLogin(long id, LocalDateTime when) {
        update("UPDATE app_user SET last_login = ? WHERE id = ?", when, id);
    }

    public void setActive(long id, boolean active) {
        update("UPDATE app_user SET active = ? WHERE id = ?", active, id);
    }
}
