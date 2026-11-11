package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimesheetController {

    private static final BigDecimal MAX_DAY_HOURS = new BigDecimal("24");

    private final AppContext ctx;

    public TimesheetController(AppContext ctx) {
        this.ctx = ctx;
    }

    /** Timesheets visible to the current user, optionally filtered by status. */
    public List<Timesheet> list(TimesheetStatus status) {
        User u = Session.getUser();
        if (u == null || u.isAdmin()) {
            return ctx.getTimesheetDao().find(status, null, null);
        }
        if (u.isManager()) {
            return ctx.getTimesheetDao().find(status, u.getDepartmentId(), null);
        }
        return ctx.getTimesheetDao().find(status, null, u.getEmployeeId());
    }

    /** Employees the current user may enter timesheets for. */
    public List<Employee> selectableEmployees() {
        User u = Session.getUser();
        if (u == null || u.isAdmin()) {
            return ctx.getEmployeeDao().findActive();
        }
        if (u.isManager()) {
            return ctx.getEmployeeDao().findActiveByDepartment(u.getDepartmentId());
        }
        List<Employee> own = new ArrayList<Employee>();
        if (u.getEmployeeId() != null) {
            own.add(ctx.getEmployeeDao().findById(u.getEmployeeId()));
        }
        return own;
    }

    public Timesheet save(Timesheet t) {
        List<String> errors = new ArrayList<String>();
        if (t.getEmployeeId() == null) {
            errors.add("Employee is required.");
        }
        if (t.getWeekStart() == null) {
            errors.add("Week start date is required.");
        } else if (t.getWeekStart().getDayOfWeek() != DayOfWeek.MONDAY) {
            errors.add("Week must start on a Monday.");
        }
        for (int i = 0; i < 7; i++) {
            BigDecimal h = t.getHours(i);
            if (h.signum() < 0 || h.compareTo(MAX_DAY_HOURS) > 0) {
                errors.add(Timesheet.DAY_NAMES[i] + ": hours must be between 0 and 24.");
            }
        }
        if (errors.isEmpty() && ctx.getTimesheetDao().exists(t.getEmployeeId(), t.getWeekStart(), t.getId())) {
            errors.add("A timesheet for this employee and week already exists.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        if (t.getId() == null) {
            t.setStatus(TimesheetStatus.DRAFT);
            ctx.getTimesheetDao().insert(t);
            audit("TIMESHEET_CREATE", t.getId(), "Week of " + t.getWeekStart());
        } else {
            Timesheet existing = ctx.getTimesheetDao().findById(t.getId());
            if (existing != null && !existing.getStatus().isEditable()) {
                throw new ValidationException("Only draft or rejected timesheets can be edited.");
            }
            ctx.getTimesheetDao().update(t);
            audit("TIMESHEET_UPDATE", t.getId(), "Week of " + t.getWeekStart());
        }
        return t;
    }

    public void submit(long id) {
        Timesheet t = load(id);
        if (!t.getStatus().isEditable()) {
            throw new ValidationException("Timesheet has already been submitted.");
        }
        if (t.getTotalHours().signum() == 0) {
            throw new ValidationException("Cannot submit an empty timesheet.");
        }
        t.setStatus(TimesheetStatus.SUBMITTED);
        t.setSubmittedAt(LocalDateTime.now());
        t.setRejectReason(null);
        ctx.getTimesheetDao().update(t);
        audit("TIMESHEET_SUBMIT", id, t.getEmployeeName() + ", week of " + t.getWeekStart());
    }

    public void approve(long id) {
        Timesheet t = loadForDecision(id);
        ctx.getTimesheetDao().updateDecision(id, TimesheetStatus.APPROVED, Session.actor(), null);
        audit("TIMESHEET_APPROVE", id, t.getEmployeeName() + ", week of " + t.getWeekStart());
    }

    public void reject(long id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("A reason is required to reject a timesheet.");
        }
        Timesheet t = loadForDecision(id);
        ctx.getTimesheetDao().updateDecision(id, TimesheetStatus.REJECTED, Session.actor(), reason.trim());
        audit("TIMESHEET_REJECT", id, t.getEmployeeName() + ": " + reason.trim());
    }

    public boolean canDecide() {
        return Session.hasRole(Role.ADMIN) || Session.hasRole(Role.MANAGER);
    }

    private Timesheet loadForDecision(long id) {
        if (Session.isLoggedIn() && !canDecide()) {
            throw new ValidationException("You are not allowed to approve timesheets.");
        }
        Timesheet t = load(id);
        if (t.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new ValidationException("Only submitted timesheets can be approved or rejected.");
        }
        User u = Session.getUser();
        if (u != null && u.isManager() && !t.getDepartmentId().equals(u.getDepartmentId())) {
            throw new ValidationException("You can only decide on timesheets of your own department.");
        }
        return t;
    }

    private Timesheet load(long id) {
        Timesheet t = ctx.getTimesheetDao().findById(id);
        if (t == null) {
            throw new ValidationException("Timesheet not found.");
        }
        return t;
    }

    private void audit(String action, Long id, String details) {
        ctx.getAuditLogDao().log(Session.actor(), action, "Timesheet", id, details);
    }
}
