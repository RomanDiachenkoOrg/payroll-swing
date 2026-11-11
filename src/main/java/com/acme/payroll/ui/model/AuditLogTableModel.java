package com.acme.payroll.ui.model;

import com.acme.payroll.model.AuditEntry;
import com.acme.payroll.ui.util.Formats;

public class AuditLogTableModel extends ListTableModel<AuditEntry> {

    private static final long serialVersionUID = 1L;

    public AuditLogTableModel() {
        super(new String[] {"Time", "User", "Action", "Entity", "Id", "Details"},
                new Class<?>[] {String.class, String.class, String.class, String.class, Long.class, String.class});
    }

    @Override
    protected Object value(AuditEntry a, int column) {
        switch (column) {
            case 0: return Formats.dateTime(a.getTimestamp());
            case 1: return a.getUserEmail();
            case 2: return a.getAction();
            case 3: return a.getEntity();
            case 4: return a.getEntityId();
            case 5: return a.getDetails();
            default: return null;
        }
    }
}
