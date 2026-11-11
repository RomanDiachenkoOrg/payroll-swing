package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.Department;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DepartmentDao extends AbstractDao {

    private static final String SELECT =
            "SELECT d.id, d.name, d.cost_center, "
            + "(SELECT COUNT(*) FROM employee e WHERE e.department_id = d.id AND e.active = TRUE) AS active_count "
            + "FROM department d ";

    private static final RowMapper<Department> MAPPER = new RowMapper<Department>() {
        @Override
        public Department map(ResultSet rs) throws SQLException {
            Department d = new Department(rs.getLong("id"), rs.getString("name"), rs.getString("cost_center"));
            d.setActiveEmployees(rs.getInt("active_count"));
            return d;
        }
    };

    public DepartmentDao(Database db) {
        super(db);
    }

    public List<Department> findAll() {
        return query(SELECT + "ORDER BY d.name", MAPPER);
    }

    public Department findById(long id) {
        return queryOne(SELECT + "WHERE d.id = ?", MAPPER, id);
    }

    public Department findByName(String name) {
        return queryOne(SELECT + "WHERE LOWER(d.name) = LOWER(?)", MAPPER, name);
    }

    public long insert(Department d) {
        long id = insert("INSERT INTO department (name, cost_center) VALUES (?, ?)", d.getName(), d.getCostCenter());
        d.setId(id);
        return id;
    }

    public void update(Department d) {
        update("UPDATE department SET name = ?, cost_center = ? WHERE id = ?", d.getName(), d.getCostCenter(), d.getId());
    }

    public void delete(long id) {
        update("DELETE FROM department WHERE id = ?", id);
    }

    public int countActiveEmployees(long departmentId) {
        return (int) queryLong("SELECT COUNT(*) FROM employee WHERE department_id = ? AND active = TRUE", departmentId);
    }

    public int countAllEmployees(long departmentId) {
        return (int) queryLong("SELECT COUNT(*) FROM employee WHERE department_id = ?", departmentId);
    }
}
