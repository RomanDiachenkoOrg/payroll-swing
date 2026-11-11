package com.acme.payroll.db;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.dao.UserDao;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.service.PasswordHasher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sample data that needs application logic (password hashes, payroll calculation).
 * Reference data lives in {@code db/seed.sql}.
 */
public class SeedData {

    public static final String DEMO_PASSWORD = "Passw0rd!";

    private static final LocalDate FIRST_WEEK = LocalDate.of(2026, 9, 7);
    private static final int WEEKS = 10;
    private static final LocalDate CURRENT_MONTH = LocalDate.of(2026, 11, 1);

    /** Active hourly staff (ids from seed.sql) plus one salaried engineer who tracks time. */
    private static final long[] TIMESHEET_EMPLOYEES = {3, 5, 7, 11, 15, 18, 19, 20, 25};
    private static final long ENGINEERING = 1L;

    private final Database db;

    public SeedData(Database db) {
        this.db = db;
    }

    public void load() {
        AppContext ctx = new AppContext(db);
        createUsers(ctx.getUserDao());
        createTimesheets(ctx.getTimesheetDao());
        createPayRuns(ctx);
        rewriteAuditTrail(ctx.getAuditLogDao());
    }

    private void createUsers(UserDao dao) {
        dao.insert(user("admin@acme.test", Role.ADMIN, null, null, true));
        dao.insert(user("manager@acme.test", Role.MANAGER, 1L, ENGINEERING, true));
        dao.insert(user("employee@acme.test", Role.EMPLOYEE, 3L, ENGINEERING, true));
        dao.insert(user("hr.manager@acme.test", Role.MANAGER, 22L, 5L, true));
        dao.insert(user("j.brandt@acme.test", Role.ADMIN, 10L, 2L, false));
    }

    private static User user(String email, Role role, Long employeeId, Long departmentId, boolean active) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(PasswordHasher.hash(DEMO_PASSWORD.toCharArray()));
        u.setRole(role);
        u.setEmployeeId(employeeId);
        u.setDepartmentId(departmentId);
        u.setActive(active);
        return u;
    }

    private void createTimesheets(TimesheetDao dao) {
        for (long empId : TIMESHEET_EMPLOYEES) {
            for (int w = 0; w < WEEKS; w++) {
                LocalDate week = FIRST_WEEK.plusWeeks(w);
                TimesheetStatus status = statusFor(empId, week);
                if (status == null) {
                    continue;
                }
                Timesheet t = new Timesheet();
                t.setEmployeeId(empId);
                t.setWeekStart(week);
                fillHours(t, empId, w);
                t.setStatus(status);
                if (status != TimesheetStatus.DRAFT) {
                    t.setSubmittedAt(week.plusDays(4).atTime(17, 30));
                }
                if (status == TimesheetStatus.APPROVED || status == TimesheetStatus.REJECTED) {
                    t.setDecidedBy(empId == 3 || empId == 5 || empId == 7 ? "manager@acme.test" : "admin@acme.test");
                    t.setDecidedAt(week.plusDays(7).atTime(9, 15));
                }
                if (status == TimesheetStatus.REJECTED) {
                    t.setRejectReason("Saturday shift was not pre-approved");
                }
                dao.insert(t);
            }
        }
    }

    private static TimesheetStatus statusFor(long empId, LocalDate week) {
        if (week.isBefore(CURRENT_MONTH)) {
            return TimesheetStatus.APPROVED;
        }
        if (week.equals(LocalDate.of(2026, 11, 2))) {
            if (empId == 18 || empId == 19) {
                return TimesheetStatus.APPROVED;
            }
            if (empId == 20) {
                return TimesheetStatus.REJECTED;
            }
            return TimesheetStatus.SUBMITTED;
        }
        // current week: a few people have started filling it in
        return empId == 3 || empId == 5 || empId == 11 ? TimesheetStatus.DRAFT : null;
    }

    private static void fillHours(Timesheet t, long empId, int week) {
        BigDecimal day = new BigDecimal(empId == 5 ? "5" : "8");
        for (int d = 0; d < 5; d++) {
            t.setHours(d, day);
        }
        if (empId == 7 && week % 3 == 1) {
            for (int d = 0; d < 4; d++) {
                t.setHours(d, new BigDecimal("9.5"));
            }
        } else if (empId != 3 && empId != 5 && (empId + week) % 4 == 0) {
            t.setHours(5, new BigDecimal("6"));
        }
        if (empId == 20 && week == 8) {
            t.setHours(5, new BigDecimal("7.5"));
        }
    }

    private void createPayRuns(AppContext ctx) {
        paidRun(ctx, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 30));
        paidRun(ctx, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 30));
        PayRun draft = ctx.getPayRunController().create(CURRENT_MONTH, LocalDate.of(2026, 11, 30),
                LocalDate.of(2026, 11, 30));
        exec("UPDATE pay_run SET created_by = 'admin@acme.test', created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 11, 2, 8, 40)), draft.getId());
    }

    private void paidRun(AppContext ctx, LocalDate start, LocalDate end, LocalDate payDate) {
        PayRun run = ctx.getPayRunController().create(start, end, payDate);
        ctx.getPayRunController().calculate(run.getId());
        ctx.getPayRunController().approve(run.getId());
        ctx.getPayRunController().markPaid(run.getId());
        exec("UPDATE pay_run SET created_by = 'admin@acme.test', created_at = ?, approved_by = 'admin@acme.test', "
                        + "paid_at = ? WHERE id = ?",
                Timestamp.valueOf(end.minusDays(5).atTime(10, 0)),
                Timestamp.valueOf(payDate.atTime(16, 0)), run.getId());
    }

    /** Replaces the entries generated while seeding with a realistic history. */
    private void rewriteAuditTrail(AuditLogDao audit) {
        exec("DELETE FROM audit_log");
        audit.log(at(2026, 9, 25, 10, 0), "admin@acme.test", "PAYRUN_CREATE", "PayRun", 1L, "Sep 2026");
        audit.log(at(2026, 9, 28, 14, 5), "admin@acme.test", "PAYRUN_CALCULATE", "PayRun", 1L, "Sep 2026");
        audit.log(at(2026, 9, 29, 11, 30), "admin@acme.test", "PAYRUN_APPROVE", "PayRun", 1L, "Sep 2026");
        audit.log(at(2026, 9, 30, 16, 0), "admin@acme.test", "PAYRUN_PAID", "PayRun", 1L, "Sep 2026");
        audit.log(at(2026, 10, 5, 9, 12), "admin@acme.test", "SALARY_CHANGE", "Employee", 2L,
                "Annual review adjustment recorded");
        audit.log(at(2026, 10, 12, 8, 55), "manager@acme.test", "LOGIN", "User", 2L, null);
        audit.log(at(2026, 10, 12, 9, 1), "manager@acme.test", "TIMESHEET_APPROVE", "Timesheet", 6L,
                "Kenji Watanabe, week of 2026-10-05");
        audit.log(at(2026, 10, 20, 15, 42), "admin@acme.test", "EMPLOYEE_DEACTIVATE", "Employee", 21L, "Peter Hall");
        audit.log(at(2026, 10, 26, 10, 0), "admin@acme.test", "PAYRUN_CREATE", "PayRun", 2L, "Oct 2026");
        audit.log(at(2026, 10, 28, 13, 20), "admin@acme.test", "PAYRUN_CALCULATE", "PayRun", 2L, "Oct 2026");
        audit.log(at(2026, 10, 29, 9, 45), "admin@acme.test", "PAYRUN_APPROVE", "PayRun", 2L, "Oct 2026");
        audit.log(at(2026, 10, 30, 16, 0), "admin@acme.test", "PAYRUN_PAID", "PayRun", 2L, "Oct 2026");
        audit.log(at(2026, 11, 2, 8, 40), "admin@acme.test", "PAYRUN_CREATE", "PayRun", 3L, "Nov 2026");
        audit.log(at(2026, 11, 3, 7, 58), "unknown@acme.test", "LOGIN_FAILED", "User", null, null);
        audit.log(at(2026, 11, 6, 17, 31), "employee@acme.test", "TIMESHEET_SUBMIT", "Timesheet", 9L,
                "Tom Becker, week of 2026-11-02");
        audit.log(at(2026, 11, 9, 9, 10), "admin@acme.test", "TIMESHEET_REJECT", "Timesheet", 79L,
                "Olga Ivanova: Saturday shift was not pre-approved");
        audit.log(at(2026, 11, 10, 11, 3), "admin@acme.test", "DEPARTMENT_UPDATE", "Department", 4L, "Operations");
    }

    private static LocalDateTime at(int y, int m, int d, int h, int min) {
        return LocalDateTime.of(y, m, d, h, min);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Seeding failed: " + sql, e);
        }
    }
}
