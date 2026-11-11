package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimesheetDao extends AbstractDao {

    private static final String[] DAY_COLUMNS =
            {"hours_mon", "hours_tue", "hours_wed", "hours_thu", "hours_fri", "hours_sat", "hours_sun"};

    private static final String SELECT =
            "SELECT t.*, e.first_name, e.last_name, e.department_id FROM timesheet t "
            + "JOIN employee e ON e.id = t.employee_id ";

    private static final RowMapper<Timesheet> MAPPER = new RowMapper<Timesheet>() {
        @Override
        public Timesheet map(ResultSet rs) throws SQLException {
            Timesheet t = new Timesheet();
            t.setId(rs.getLong("id"));
            t.setEmployeeId(rs.getLong("employee_id"));
            t.setEmployeeName(rs.getString("first_name") + " " + rs.getString("last_name"));
            t.setDepartmentId(rs.getLong("department_id"));
            t.setWeekStart(getDate(rs, "week_start"));
            for (int i = 0; i < DAY_COLUMNS.length; i++) {
                t.setHours(i, rs.getBigDecimal(DAY_COLUMNS[i]));
            }
            t.setStatus(TimesheetStatus.valueOf(rs.getString("status")));
            t.setRejectReason(rs.getString("reject_reason"));
            t.setSubmittedAt(getDateTime(rs, "submitted_at"));
            t.setDecidedBy(rs.getString("decided_by"));
            t.setDecidedAt(getDateTime(rs, "decided_at"));
            return t;
        }
    };

    public TimesheetDao(Database db) {
        super(db);
    }

    /**
     * @param status       null for all statuses
     * @param departmentId null for all departments
     * @param employeeId   null for all employees
     */
    public List<Timesheet> find(TimesheetStatus status, Long departmentId, Long employeeId) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1");
        List<Object> params = new ArrayList<Object>();
        if (status != null) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        if (departmentId != null) {
            sql.append(" AND e.department_id = ?");
            params.add(departmentId);
        }
        if (employeeId != null) {
            sql.append(" AND t.employee_id = ?");
            params.add(employeeId);
        }
        sql.append(" ORDER BY t.week_start DESC, e.last_name, e.first_name");
        return query(sql.toString(), MAPPER, params.toArray());
    }

    public Timesheet findById(long id) {
        return queryOne(SELECT + "WHERE t.id = ?", MAPPER, id);
    }

    public boolean exists(long employeeId, LocalDate weekStart, Long excludeId) {
        return queryLong("SELECT COUNT(*) FROM timesheet WHERE employee_id = ? AND week_start = ? AND id <> ?",
                employeeId, weekStart, excludeId == null ? -1L : excludeId) > 0;
    }

    /** Approved timesheets whose week starts inside the given period. */
    public List<Timesheet> findApproved(long employeeId, LocalDate from, LocalDate to) {
        return query(SELECT + "WHERE t.employee_id = ? AND t.status = 'APPROVED' AND t.week_start BETWEEN ? AND ? "
                + "ORDER BY t.week_start", MAPPER, employeeId, from, to);
    }

    public int countByStatus(TimesheetStatus status, Long departmentId) {
        if (departmentId == null) {
            return (int) queryLong("SELECT COUNT(*) FROM timesheet WHERE status = ?", status);
        }
        return (int) queryLong("SELECT COUNT(*) FROM timesheet t JOIN employee e ON e.id = t.employee_id "
                + "WHERE t.status = ? AND e.department_id = ?", status, departmentId);
    }

    public long insert(Timesheet t) {
        long id = insert("INSERT INTO timesheet (employee_id, week_start, hours_mon, hours_tue, hours_wed, hours_thu, "
                        + "hours_fri, hours_sat, hours_sun, status, reject_reason, submitted_at, decided_by, decided_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                t.getEmployeeId(), t.getWeekStart(), t.getHours(0), t.getHours(1), t.getHours(2), t.getHours(3),
                t.getHours(4), t.getHours(5), t.getHours(6), t.getStatus(), t.getRejectReason(), t.getSubmittedAt(),
                t.getDecidedBy(), t.getDecidedAt());
        t.setId(id);
        return id;
    }

    public void update(Timesheet t) {
        update("UPDATE timesheet SET week_start = ?, hours_mon = ?, hours_tue = ?, hours_wed = ?, hours_thu = ?, "
                        + "hours_fri = ?, hours_sat = ?, hours_sun = ?, status = ?, reject_reason = ?, submitted_at = ? "
                        + "WHERE id = ?",
                t.getWeekStart(), t.getHours(0), t.getHours(1), t.getHours(2), t.getHours(3), t.getHours(4),
                t.getHours(5), t.getHours(6), t.getStatus(), t.getRejectReason(), t.getSubmittedAt(), t.getId());
    }

    public void updateDecision(long id, TimesheetStatus status, String decidedBy, String reason) {
        update("UPDATE timesheet SET status = ?, decided_by = ?, decided_at = ?, reject_reason = ? WHERE id = ?",
                status, decidedBy, LocalDateTime.now(), reason, id);
    }

    public void delete(long id) {
        update("DELETE FROM timesheet WHERE id = ?", id);
    }
}
