package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.service.CsvWriter;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportController {

    private final AppContext ctx;

    public ReportController(AppContext ctx) {
        this.ctx = ctx;
    }

    public List<DepartmentCost> costByDepartment(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ValidationException("Both dates are required.");
        }
        if (to.isBefore(from)) {
            throw new ValidationException("'To' date must be on or after 'From' date.");
        }
        return ctx.getReportDao().costByDepartment(from, to);
    }

    public DepartmentCost totals(List<DepartmentCost> rows) {
        DepartmentCost t = new DepartmentCost();
        t.setDepartmentName("Total");
        for (DepartmentCost c : rows) {
            t.setHeadcount(t.getHeadcount() + c.getHeadcount());
            t.setGross(t.getGross().add(c.getGross()));
            t.setIncomeTax(t.getIncomeTax().add(c.getIncomeTax()));
            t.setSocial(t.getSocial().add(c.getSocial()));
            t.setPension(t.getPension().add(c.getPension()));
            t.setOtherDeductions(t.getOtherDeductions().add(c.getOtherDeductions()));
            t.setNet(t.getNet().add(c.getNet()));
        }
        return t;
    }

    public void exportCostReport(File file, List<DepartmentCost> rows, LocalDate from, LocalDate to) throws IOException {
        List<String> header = Arrays.asList("Department", "Headcount", "Gross", "Income tax", "Social",
                "Pension", "Other deductions", "Net");
        List<List<Object>> data = new ArrayList<List<Object>>();
        for (DepartmentCost c : rows) {
            data.add(Arrays.<Object>asList(c.getDepartmentName(), c.getHeadcount(), c.getGross(), c.getIncomeTax(),
                    c.getSocial(), c.getPension(), c.getOtherDeductions(), c.getNet()));
        }
        CsvWriter.write(file, header, data);
        ctx.getAuditLogDao().log(Session.actor(), "REPORT_EXPORT", "Report", null,
                "Cost by department " + from + " - " + to);
    }

    public void exportPayslips(File file, List<Payslip> payslips) throws IOException {
        List<String> header = Arrays.asList("Employee no", "Employee", "Department", "Regular hours",
                "Overtime hours", "Gross", "Income tax", "Social", "Pension", "Other deductions", "Net");
        List<List<Object>> data = new ArrayList<List<Object>>();
        for (Payslip p : payslips) {
            data.add(Arrays.<Object>asList(p.getEmployeeNo(), p.getEmployeeName(), p.getDepartmentName(),
                    p.getRegularHours(), p.getOvertimeHours(), p.getGross(), p.getIncomeTax(), p.getSocial(),
                    p.getPension(), p.getOtherDeductions(), p.getNet()));
        }
        CsvWriter.write(file, header, data);
    }

    /** Single payslip as "item,amount" lines, for import into spreadsheets. */
    public void exportPayslip(File file, Payslip p) throws IOException {
        List<String> header = Arrays.asList("Item", "Amount");
        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(Arrays.<Object>asList("Employee", p.getEmployeeName() + " (" + p.getEmployeeNo() + ")"));
        data.add(Arrays.<Object>asList("Period", p.getPeriodStart() + " - " + p.getPeriodEnd()));
        data.add(Arrays.<Object>asList("Pay date", p.getPayDate()));
        if (p.getRegularHours().signum() > 0) {
            data.add(Arrays.<Object>asList("Regular hours", p.getRegularHours()));
            data.add(Arrays.<Object>asList("Overtime hours", p.getOvertimeHours()));
        }
        data.add(Arrays.<Object>asList("Gross pay", p.getGross()));
        data.add(Arrays.<Object>asList("Income tax", negate(p.getIncomeTax())));
        data.add(Arrays.<Object>asList("Social contribution", negate(p.getSocial())));
        data.add(Arrays.<Object>asList("Pension", negate(p.getPension())));
        data.add(Arrays.<Object>asList("Other deductions", negate(p.getOtherDeductions())));
        data.add(Arrays.<Object>asList("Net pay", p.getNet()));
        CsvWriter.write(file, header, data);
        ctx.getAuditLogDao().log(Session.actor(), "PAYSLIP_EXPORT", "Payslip", p.getId(), p.getEmployeeName());
    }

    private static BigDecimal negate(BigDecimal v) {
        return v.signum() == 0 ? v : v.negate();
    }
}
