package com.acme.payroll.ui.model;

import com.acme.payroll.model.Department;

public class DepartmentTableModel extends ListTableModel<Department> {

    private static final long serialVersionUID = 1L;

    public DepartmentTableModel() {
        super(new String[] {"Name", "Cost center", "Active employees"},
                new Class<?>[] {String.class, String.class, Integer.class});
    }

    @Override
    protected Object value(Department d, int column) {
        switch (column) {
            case 0: return d.getName();
            case 1: return d.getCostCenter();
            case 2: return d.getActiveEmployees();
            default: return null;
        }
    }
}
