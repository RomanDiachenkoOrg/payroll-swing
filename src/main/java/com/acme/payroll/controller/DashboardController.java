package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.service.Session;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DashboardController {

    /** Snapshot of the numbers shown on the dashboard. */
    public static class Stats {
        public int headcount;
        public BigDecimal lastPayrollTotal = BigDecimal.ZERO;
        public String lastPayrollLabel = "-";
        public int pendingTimesheets;
        public LocalDate nextPayDate;
        public List<DepartmentCost> lastRunByDepartment = Collections.emptyList();
        public List<PayRun> recentRuns = Collections.emptyList();
    }

    private final AppContext ctx;

    public DashboardController(AppContext ctx) {
        this.ctx = ctx;
    }

    public Stats load() {
        Stats s = new Stats();
        s.headcount = ctx.getEmployeeDao().countActive();

        PayRun last = ctx.getPayRunDao().findLastPaid();
        if (last != null) {
            s.lastPayrollTotal = last.getTotalGross();
            s.lastPayrollLabel = last.getPeriodLabel();
            s.lastRunByDepartment = ctx.getReportDao().costByDepartment(last.getPayDate(), last.getPayDate());
        }

        User u = Session.getUser();
        Long dept = u != null && u.isManager() ? u.getDepartmentId() : null;
        s.pendingTimesheets = ctx.getTimesheetDao().countByStatus(TimesheetStatus.SUBMITTED, dept);

        LocalDate today = LocalDate.now();
        PayRun next = ctx.getPayRunDao().findNextUnpaid(today);
        s.nextPayDate = next != null ? next.getPayDate() : lastBusinessDayOfMonth(today);

        List<PayRun> runs = ctx.getPayRunDao().findAll();
        s.recentRuns = runs.size() > 5 ? runs.subList(0, 5) : runs;
        return s;
    }

    static LocalDate lastBusinessDayOfMonth(LocalDate day) {
        LocalDate d = day.withDayOfMonth(day.lengthOfMonth());
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.minusDays(1);
        }
        return d;
    }
}
