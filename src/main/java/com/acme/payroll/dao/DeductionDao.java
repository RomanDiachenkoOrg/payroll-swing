package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.DeductionType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeductionDao extends AbstractDao {

    private static final RowMapper<DeductionType> MAPPER = new RowMapper<DeductionType>() {
        @Override
        public DeductionType map(ResultSet rs) throws SQLException {
            DeductionType t = new DeductionType();
            t.setId(rs.getLong("id"));
            t.setCode(rs.getString("code"));
            t.setName(rs.getString("name"));
            t.setDefaultAmount(rs.getBigDecimal("default_amount"));
            return t;
        }
    };

    public DeductionDao(Database db) {
        super(db);
    }

    public List<DeductionType> findTypes() {
        return query("SELECT * FROM deduction_type ORDER BY name", MAPPER);
    }

    /** Sum of fixed monthly deductions (health insurance etc.) of one employee. */
    public BigDecimal totalForEmployee(long employeeId) {
        return queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM employee_deduction WHERE employee_id = ?", employeeId);
    }

    public List<String> describeForEmployee(long employeeId) {
        return query("SELECT t.name, d.amount FROM employee_deduction d JOIN deduction_type t ON t.id = d.deduction_type_id "
                + "WHERE d.employee_id = ? ORDER BY t.name", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1) + ": " + rs.getBigDecimal(2).toPlainString();
            }
        }, employeeId);
    }

    public void addToEmployee(long employeeId, long typeId, BigDecimal amount) {
        insert("INSERT INTO employee_deduction (employee_id, deduction_type_id, amount) VALUES (?, ?, ?)",
                employeeId, typeId, amount);
    }
}
