package com.acme.payroll.ui.model;

import com.acme.payroll.model.SalaryHistory;
import com.acme.payroll.ui.util.Formats;

import java.math.BigDecimal;

public class SalaryHistoryTableModel extends ListTableModel<SalaryHistory> {

    private static final long serialVersionUID = 1L;

    public SalaryHistoryTableModel() {
        super(new String[] {"Effective", "Annual salary", "Hourly rate", "Reason"},
                new Class<?>[] {String.class, BigDecimal.class, BigDecimal.class, String.class});
    }

    @Override
    protected Object value(SalaryHistory h, int column) {
        switch (column) {
            case 0: return Formats.date(h.getEffectiveDate());
            case 1: return h.getAnnualSalary();
            case 2: return h.getHourlyRate();
            case 3: return h.getReason();
            default: return null;
        }
    }
}
