package com.acme.payroll.ui.model;

import com.acme.payroll.model.Timesheet;
import com.acme.payroll.ui.util.Formats;

public class TimesheetTableModel extends ListTableModel<Timesheet> {

    private static final long serialVersionUID = 1L;

    public TimesheetTableModel() {
        super(new String[] {"Employee", "Week", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Total", "Status",
                        "Decided by", "Reason"},
                new Class<?>[] {String.class, String.class, String.class, String.class, String.class, String.class,
                        String.class, String.class, String.class, String.class, String.class, String.class,
                        String.class});
    }

    @Override
    protected Object value(Timesheet t, int column) {
        if (column >= 2 && column <= 8) {
            return Formats.number(t.getHours(column - 2));
        }
        switch (column) {
            case 0: return t.getEmployeeName();
            case 1: return Formats.date(t.getWeekStart());
            case 9: return Formats.number(t.getTotalHours());
            case 10: return t.getStatus().name();
            case 11: return t.getDecidedBy();
            case 12: return t.getRejectReason();
            default: return null;
        }
    }
}
