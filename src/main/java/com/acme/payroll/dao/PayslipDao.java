package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.Payslip;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PayslipDao extends AbstractDao {

    private static final String SELECT =
            "SELECT p.*, e.employee_no, e.first_name, e.last_name, d.name AS department_name, "
            + "r.period_start, r.period_end, r.pay_date, r.status AS run_status "
            + "FROM payslip p "
            + "JOIN employee e ON e.id = p.employee_id "
            + "JOIN department d ON d.id = e.department_id "
            + "JOIN pay_run r ON r.id = p.pay_run_id ";

    private static final RowMapper<Payslip> MAPPER = new RowMapper<Payslip>() {
        @Override
        public Payslip map(ResultSet rs) throws SQLException {
            Payslip p = new Payslip();
            p.setId(rs.getLong("id"));
            p.setPayRunId(rs.getLong("pay_run_id"));
            p.setEmployeeId(rs.getLong("employee_id"));
            p.setEmployeeNo(rs.getString("employee_no"));
            p.setEmployeeName(rs.getString("first_name") + " " + rs.getString("last_name"));
            p.setDepartmentName(rs.getString("department_name"));
            p.setPeriodStart(getDate(rs, "period_start"));
            p.setPeriodEnd(getDate(rs, "period_end"));
            p.setPayDate(getDate(rs, "pay_date"));
            p.setRunStatus(PayRunStatus.valueOf(rs.getString("run_status")));
            p.setRegularHours(rs.getBigDecimal("regular_hours"));
            p.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
            p.setGross(rs.getBigDecimal("gross"));
            p.setIncomeTax(rs.getBigDecimal("income_tax"));
            p.setSocial(rs.getBigDecimal("social"));
            p.setPension(rs.getBigDecimal("pension"));
            p.setOtherDeductions(rs.getBigDecimal("other_deductions"));
            p.setNet(rs.getBigDecimal("net"));
            return p;
        }
    };

    public PayslipDao(Database db) {
        super(db);
    }

    public List<Payslip> findByPayRun(long payRunId) {
        return query(SELECT + "WHERE p.pay_run_id = ? ORDER BY e.employee_no", MAPPER, payRunId);
    }

    /** Payslips of one employee; only runs that were approved or paid are visible to the employee. */
    public List<Payslip> findByEmployee(long employeeId, boolean releasedOnly) {
        String extra = releasedOnly ? " AND r.status IN ('APPROVED', 'PAID')" : "";
        return query(SELECT + "WHERE p.employee_id = ?" + extra + " ORDER BY r.period_start DESC", MAPPER, employeeId);
    }

    public Payslip findById(long id) {
        return queryOne(SELECT + "WHERE p.id = ?", MAPPER, id);
    }

    public void deleteByPayRun(long payRunId) {
        update("DELETE FROM payslip WHERE pay_run_id = ?", payRunId);
    }

    public long insert(Payslip p) {
        long id = insert("INSERT INTO payslip (pay_run_id, employee_id, regular_hours, overtime_hours, gross, income_tax, "
                        + "social, pension, other_deductions, net) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getPayRunId(), p.getEmployeeId(), p.getRegularHours(), p.getOvertimeHours(), p.getGross(),
                p.getIncomeTax(), p.getSocial(), p.getPension(), p.getOtherDeductions(), p.getNet());
        p.setId(id);
        return id;
    }
}
