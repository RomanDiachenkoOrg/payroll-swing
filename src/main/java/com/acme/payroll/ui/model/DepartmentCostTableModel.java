package com.acme.payroll.ui.model;

import com.acme.payroll.model.DepartmentCost;

import java.math.BigDecimal;

public class DepartmentCostTableModel extends ListTableModel<DepartmentCost> {

    private static final long serialVersionUID = 1L;

    public DepartmentCostTableModel() {
        super(new String[] {"Department", "Headcount", "Gross", "Income tax", "Social", "Pension", "Other", "Net"},
                new Class<?>[] {String.class, Integer.class, BigDecimal.class, BigDecimal.class, BigDecimal.class,
                        BigDecimal.class, BigDecimal.class, BigDecimal.class});
    }

    @Override
    protected Object value(DepartmentCost c, int column) {
        switch (column) {
            case 0: return c.getDepartmentName();
            case 1: return c.getHeadcount();
            case 2: return c.getGross();
            case 3: return c.getIncomeTax();
            case 4: return c.getSocial();
            case 5: return c.getPension();
            case 6: return c.getOtherDeductions();
            case 7: return c.getNet();
            default: return null;
        }
    }
}
