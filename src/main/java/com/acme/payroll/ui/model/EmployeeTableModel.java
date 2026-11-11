package com.acme.payroll.ui.model;

import com.acme.payroll.dao.EmployeeFilter;
import com.acme.payroll.model.Employee;
import com.acme.payroll.ui.util.Formats;

import java.math.BigDecimal;

public class EmployeeTableModel extends ListTableModel<Employee> {

    private static final long serialVersionUID = 1L;

    /** Sort field for each column; null where server-side sorting is not supported. */
    public static final EmployeeFilter.SortField[] SORT_FIELDS = {
            EmployeeFilter.SortField.EMPLOYEE_NO, EmployeeFilter.SortField.NAME, EmployeeFilter.SortField.DEPARTMENT,
            EmployeeFilter.SortField.JOB_TITLE, null, null, EmployeeFilter.SortField.HIRE_DATE, null};

    public EmployeeTableModel() {
        super(new String[] {"No.", "Name", "Department", "Job title", "Pay type", "Rate", "Hire date", "Status"},
                new Class<?>[] {String.class, String.class, String.class, String.class, String.class,
                        BigDecimal.class, String.class, String.class});
    }

    @Override
    protected Object value(Employee e, int column) {
        switch (column) {
            case 0: return e.getEmployeeNo();
            case 1: return e.getLastName() + ", " + e.getFirstName();
            case 2: return e.getDepartmentName();
            case 3: return e.getJobTitle();
            case 4: return e.isHourly() ? "Hourly" : "Salaried";
            case 5: return e.getCurrentRate();
            case 6: return Formats.date(e.getHireDate());
            case 7: return e.isActive() ? "Active" : "Inactive";
            default: return null;
        }
    }
}
