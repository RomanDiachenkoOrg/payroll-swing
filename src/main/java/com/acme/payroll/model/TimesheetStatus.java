package com.acme.payroll.model;

public enum TimesheetStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED;

    /** Draft and rejected timesheets can still be changed by their owner. */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
}
