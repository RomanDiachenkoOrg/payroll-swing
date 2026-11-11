package com.acme.payroll.model;

/**
 * Lifecycle of a pay run: DRAFT -&gt; CALCULATED -&gt; APPROVED -&gt; PAID.
 * A paid run is locked and can no longer be modified.
 */
public enum PayRunStatus {
    DRAFT, CALCULATED, APPROVED, PAID
}
