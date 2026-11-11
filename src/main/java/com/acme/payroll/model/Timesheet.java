package com.acme.payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Weekly timesheet. Hours are stored Monday (index 0) through Sunday (index 6).
 */
public class Timesheet {

    public static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long departmentId;
    private LocalDate weekStart;
    private final BigDecimal[] hours = new BigDecimal[7];
    private TimesheetStatus status = TimesheetStatus.DRAFT;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private String decidedBy;
    private LocalDateTime decidedAt;

    public Timesheet() {
        for (int i = 0; i < hours.length; i++) {
            hours[i] = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() {
        return weekStart == null ? null : weekStart.plusDays(6);
    }

    public BigDecimal getHours(int dayIndex) {
        return hours[dayIndex];
    }

    public void setHours(int dayIndex, BigDecimal value) {
        hours[dayIndex] = value == null ? BigDecimal.ZERO : value;
    }

    public BigDecimal getTotalHours() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal h : hours) {
            total = total.add(h);
        }
        return total;
    }

    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
