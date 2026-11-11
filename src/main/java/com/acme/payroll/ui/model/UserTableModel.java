package com.acme.payroll.ui.model;

import com.acme.payroll.model.User;
import com.acme.payroll.ui.util.Formats;

public class UserTableModel extends ListTableModel<User> {

    private static final long serialVersionUID = 1L;

    public UserTableModel() {
        super(new String[] {"Email", "Role", "Employee id", "Department id", "Active", "Last login"},
                new Class<?>[] {String.class, String.class, Long.class, Long.class, Boolean.class, String.class});
    }

    @Override
    protected Object value(User u, int column) {
        switch (column) {
            case 0: return u.getEmail();
            case 1: return u.getRole().getDisplayName();
            case 2: return u.getEmployeeId();
            case 3: return u.getDepartmentId();
            case 4: return u.isActive();
            case 5: return Formats.dateTime(u.getLastLogin());
            default: return null;
        }
    }
}
