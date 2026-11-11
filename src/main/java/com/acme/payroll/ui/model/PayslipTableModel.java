package com.acme.payroll.ui.model;

import com.acme.payroll.model.Payslip;
import com.acme.payroll.ui.util.Formats;

import java.math.BigDecimal;

public class PayslipTableModel extends ListTableModel<Payslip> {

    private static final long serialVersionUID = 1L;

    private final boolean showPeriod;

    /**
     * @param showPeriod true for an employee's payslip history, false for the lines of one pay run
     */
    public PayslipTableModel(boolean showPeriod) {
        super(new String[] {showPeriod ? "Period" : "Employee", showPeriod ? "Pay date" : "Department",
                        "Reg. hrs", "OT hrs", "Gross", "Tax", "Social", "Pension", "Other", "Net"},
                new Class<?>[] {String.class, String.class, String.class, String.class, BigDecimal.class,
                        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class});
        this.showPeriod = showPeriod;
    }

    @Override
    protected Object value(Payslip p, int column) {
        switch (column) {
            case 0: return showPeriod ? Formats.date(p.getPeriodStart()) + " - " + Formats.date(p.getPeriodEnd())
                    : p.getEmployeeName();
            case 1: return showPeriod ? Formats.date(p.getPayDate()) : p.getDepartmentName();
            case 2: return Formats.number(p.getRegularHours());
            case 3: return Formats.number(p.getOvertimeHours());
            case 4: return p.getGross();
            case 5: return p.getIncomeTax();
            case 6: return p.getSocial();
            case 7: return p.getPension();
            case 8: return p.getOtherDeductions();
            case 9: return p.getNet();
            default: return null;
        }
    }
}
