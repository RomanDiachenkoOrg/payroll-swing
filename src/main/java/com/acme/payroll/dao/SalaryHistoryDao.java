package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.SalaryHistory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SalaryHistoryDao extends AbstractDao {

    private static final RowMapper<SalaryHistory> MAPPER = new RowMapper<SalaryHistory>() {
        @Override
        public SalaryHistory map(ResultSet rs) throws SQLException {
            SalaryHistory h = new SalaryHistory();
            h.setId(rs.getLong("id"));
            h.setEmployeeId(rs.getLong("employee_id"));
            h.setEffectiveDate(getDate(rs, "effective_date"));
            h.setAnnualSalary(rs.getBigDecimal("annual_salary"));
            h.setHourlyRate(rs.getBigDecimal("hourly_rate"));
            h.setReason(rs.getString("reason"));
            return h;
        }
    };

    public SalaryHistoryDao(Database db) {
        super(db);
    }

    public List<SalaryHistory> findByEmployee(long employeeId) {
        return query("SELECT * FROM salary_history WHERE employee_id = ? ORDER BY effective_date DESC, id DESC",
                MAPPER, employeeId);
    }

    public long insert(SalaryHistory h) {
        long id = insert("INSERT INTO salary_history (employee_id, effective_date, annual_salary, hourly_rate, reason) "
                        + "VALUES (?, ?, ?, ?, ?)",
                h.getEmployeeId(), h.getEffectiveDate(), h.getAnnualSalary(), h.getHourlyRate(), h.getReason());
        h.setId(id);
        return id;
    }
}
