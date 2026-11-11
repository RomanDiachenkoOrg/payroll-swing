package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PayRunDao extends AbstractDao {

    private static final String SELECT =
            "SELECT r.*, "
            + "(SELECT COUNT(*) FROM payslip p WHERE p.pay_run_id = r.id) AS employee_count, "
            + "(SELECT COALESCE(SUM(p.gross), 0) FROM payslip p WHERE p.pay_run_id = r.id) AS total_gross, "
            + "(SELECT COALESCE(SUM(p.net), 0) FROM payslip p WHERE p.pay_run_id = r.id) AS total_net "
            + "FROM pay_run r ";

    private static final RowMapper<PayRun> MAPPER = new RowMapper<PayRun>() {
        @Override
        public PayRun map(ResultSet rs) throws SQLException {
            PayRun r = new PayRun();
            r.setId(rs.getLong("id"));
            r.setPeriodStart(getDate(rs, "period_start"));
            r.setPeriodEnd(getDate(rs, "period_end"));
            r.setPayDate(getDate(rs, "pay_date"));
            r.setStatus(PayRunStatus.valueOf(rs.getString("status")));
            r.setCreatedBy(rs.getString("created_by"));
            r.setCreatedAt(getDateTime(rs, "created_at"));
            r.setApprovedBy(rs.getString("approved_by"));
            r.setPaidAt(getDateTime(rs, "paid_at"));
            r.setEmployeeCount(rs.getInt("employee_count"));
            r.setTotalGross(rs.getBigDecimal("total_gross"));
            r.setTotalNet(rs.getBigDecimal("total_net"));
            return r;
        }
    };

    public PayRunDao(Database db) {
        super(db);
    }

    public List<PayRun> findAll() {
        return query(SELECT + "ORDER BY r.period_start DESC, r.id DESC", MAPPER);
    }

    public PayRun findById(long id) {
        return queryOne(SELECT + "WHERE r.id = ?", MAPPER, id);
    }

    public PayRun findLastPaid() {
        return queryOne(SELECT + "WHERE r.status = 'PAID' ORDER BY r.pay_date DESC LIMIT 1", MAPPER);
    }

    /** First not-yet-paid run with a pay date on or after the given day. */
    public PayRun findNextUnpaid(LocalDate from) {
        return queryOne(SELECT + "WHERE r.status <> 'PAID' AND r.pay_date >= ? ORDER BY r.pay_date LIMIT 1", MAPPER, from);
    }

    public boolean overlaps(LocalDate start, LocalDate end) {
        return queryLong("SELECT COUNT(*) FROM pay_run WHERE period_start <= ? AND period_end >= ?", end, start) > 0;
    }

    public long insert(PayRun r) {
        long id = insert("INSERT INTO pay_run (period_start, period_end, pay_date, status, created_by, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                r.getPeriodStart(), r.getPeriodEnd(), r.getPayDate(), r.getStatus(), r.getCreatedBy(),
                LocalDateTime.now());
        r.setId(id);
        return id;
    }

    public void updateStatus(long id, PayRunStatus status) {
        update("UPDATE pay_run SET status = ? WHERE id = ?", status, id);
    }

    public void markApproved(long id, String approvedBy) {
        update("UPDATE pay_run SET status = 'APPROVED', approved_by = ? WHERE id = ?", approvedBy, id);
    }

    public void markPaid(long id, LocalDateTime paidAt) {
        update("UPDATE pay_run SET status = 'PAID', paid_at = ? WHERE id = ?", paidAt, id);
    }

    public void delete(long id) {
        update("DELETE FROM pay_run WHERE id = ?", id);
    }
}
