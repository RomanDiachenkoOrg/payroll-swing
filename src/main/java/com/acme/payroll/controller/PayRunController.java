package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.service.PayrollCalculator;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class PayRunController {

    private static final Logger LOG = Logger.getLogger(PayRunController.class.getName());

    private final AppContext ctx;

    public PayRunController(AppContext ctx) {
        this.ctx = ctx;
    }

    public List<PayRun> list() {
        return ctx.getPayRunDao().findAll();
    }

    public PayRun find(long id) {
        return ctx.getPayRunDao().findById(id);
    }

    public List<Payslip> payslips(long payRunId) {
        return ctx.getPayslipDao().findByPayRun(payRunId);
    }

    public PayRun create(LocalDate periodStart, LocalDate periodEnd, LocalDate payDate) {
        if (periodStart == null || periodEnd == null || payDate == null) {
            throw new ValidationException("Period start, period end and pay date are required.");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new ValidationException("Period end must be on or after period start.");
        }
        if (payDate.isBefore(periodStart)) {
            throw new ValidationException("Pay date cannot be before the period starts.");
        }
        if (ctx.getPayRunDao().overlaps(periodStart, periodEnd)) {
            throw new ValidationException("Another pay run already covers part of this period.");
        }
        PayRun run = new PayRun();
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setPayDate(payDate);
        run.setStatus(PayRunStatus.DRAFT);
        run.setCreatedBy(Session.actor());
        ctx.getPayRunDao().insert(run);
        audit("PAYRUN_CREATE", run.getId(), run.getPeriodLabel());
        return run;
    }

    /** (Re)calculates all payslips of a draft or calculated run. */
    public int calculate(long id) {
        PayRun run = load(id);
        if (run.getStatus() != PayRunStatus.DRAFT && run.getStatus() != PayRunStatus.CALCULATED) {
            throw new ValidationException("Only draft or calculated pay runs can be (re)calculated.");
        }
        PayrollCalculator calc = buildCalculator();
        ctx.getPayslipDao().deleteByPayRun(id);
        List<Employee> employees = ctx.getEmployeeDao().findPayable(run.getPeriodEnd());
        int count = 0;
        for (Employee e : employees) {
            List<Timesheet> timesheets = e.isHourly()
                    ? ctx.getTimesheetDao().findApproved(e.getId(), run.getPeriodStart(), run.getPeriodEnd())
                    : null;
            if (e.isHourly() && timesheets.isEmpty()) {
                LOG.fine("No approved timesheets for " + e.getEmployeeNo() + ", skipping");
                continue;
            }
            BigDecimal other = ctx.getDeductionDao().totalForEmployee(e.getId());
            Payslip p = calc.calculate(e, timesheets, other);
            p.setPayRunId(id);
            ctx.getPayslipDao().insert(p);
            count++;
        }
        ctx.getPayRunDao().updateStatus(id, PayRunStatus.CALCULATED);
        audit("PAYRUN_CALCULATE", id, count + " payslips");
        return count;
    }

    public void approve(long id) {
        PayRun run = load(id);
        if (run.getStatus() != PayRunStatus.CALCULATED) {
            throw new ValidationException("Only calculated pay runs can be approved.");
        }
        ctx.getPayRunDao().markApproved(id, Session.actor());
        audit("PAYRUN_APPROVE", id, run.getPeriodLabel());
    }

    public void markPaid(long id) {
        PayRun run = load(id);
        if (run.isLocked()) {
            throw new ValidationException("This pay run has already been paid.");
        }
        if (run.getStatus() == PayRunStatus.DRAFT) {
            throw new ValidationException("Calculate the pay run before marking it as paid.");
        }
        ctx.getPayRunDao().markPaid(id, LocalDateTime.now());
        audit("PAYRUN_PAID", id, run.getPeriodLabel());
    }

    public void delete(long id) {
        PayRun run = load(id);
        if (run.getStatus() == PayRunStatus.APPROVED || run.isLocked()) {
            throw new ValidationException("Approved or paid pay runs cannot be deleted.");
        }
        ctx.getPayslipDao().deleteByPayRun(id);
        ctx.getPayRunDao().delete(id);
        audit("PAYRUN_DELETE", id, run.getPeriodLabel());
    }

    public PayrollCalculator buildCalculator() {
        SettingsDao s = ctx.getSettingsDao();
        return new PayrollCalculator(s.findTaxBrackets(),
                s.getDecimal(SettingsDao.SOCIAL_RATE, new BigDecimal("5")),
                s.getDecimal(SettingsDao.PENSION_RATE, new BigDecimal("3")));
    }

    private PayRun load(long id) {
        PayRun run = ctx.getPayRunDao().findById(id);
        if (run == null) {
            throw new ValidationException("Pay run not found.");
        }
        return run;
    }

    private void audit(String action, Long id, String details) {
        ctx.getAuditLogDao().log(Session.actor(), action, "PayRun", id, details);
    }
}
