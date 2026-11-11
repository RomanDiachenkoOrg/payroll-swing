package com.acme.payroll.ui.model;

import com.acme.payroll.model.PayRun;
import com.acme.payroll.ui.util.Formats;

import java.math.BigDecimal;

public class PayRunTableModel extends ListTableModel<PayRun> {

    private static final long serialVersionUID = 1L;

    public PayRunTableModel() {
        super(new String[] {"#", "Period", "Pay date", "Status", "Employees", "Total gross", "Total net", "Created by"},
                new Class<?>[] {Long.class, String.class, String.class, String.class, Integer.class,
                        BigDecimal.class, BigDecimal.class, String.class});
    }

    @Override
    protected Object value(PayRun r, int column) {
        switch (column) {
            case 0: return r.getId();
            case 1: return r.getPeriodLabel();
            case 2: return Formats.date(r.getPayDate());
            case 3: return r.getStatus().name();
            case 4: return r.getEmployeeCount();
            case 5: return r.getTotalGross();
            case 6: return r.getTotalNet();
            case 7: return r.getCreatedBy();
            default: return null;
        }
    }
}
