package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.DepartmentCost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportDao extends AbstractDao {

    public ReportDao(Database db) {
        super(db);
    }

    /**
     * Payroll cost per department for all approved or paid runs whose pay date falls in the period.
     */
    public List<DepartmentCost> costByDepartment(LocalDate from, LocalDate to) {
        String sql = "SELECT d.name, COUNT(DISTINCT p.employee_id) AS headcount, "
                + "SUM(p.gross) AS gross, SUM(p.income_tax) AS tax, SUM(p.social) AS social, "
                + "SUM(p.pension) AS pension, SUM(p.other_deductions) AS other, SUM(p.net) AS net "
                + "FROM payslip p "
                + "JOIN pay_run r ON r.id = p.pay_run_id "
                + "JOIN employee e ON e.id = p.employee_id "
                + "JOIN department d ON d.id = e.department_id "
                + "WHERE r.status IN ('APPROVED', 'PAID') AND r.pay_date BETWEEN ? AND ? "
                + "GROUP BY d.name ORDER BY d.name";
        return query(sql, new RowMapper<DepartmentCost>() {
            @Override
            public DepartmentCost map(ResultSet rs) throws SQLException {
                DepartmentCost c = new DepartmentCost();
                c.setDepartmentName(rs.getString("name"));
                c.setHeadcount(rs.getInt("headcount"));
                c.setGross(rs.getBigDecimal("gross"));
                c.setIncomeTax(rs.getBigDecimal("tax"));
                c.setSocial(rs.getBigDecimal("social"));
                c.setPension(rs.getBigDecimal("pension"));
                c.setOtherDeductions(rs.getBigDecimal("other"));
                c.setNet(rs.getBigDecimal("net"));
                return c;
            }
        }, from, to);
    }
}
