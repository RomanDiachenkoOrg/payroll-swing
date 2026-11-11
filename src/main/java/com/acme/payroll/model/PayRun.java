package com.acme.payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PayRun {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US);

    private Long id;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate payDate;
    private PayRunStatus status = PayRunStatus.DRAFT;
    private String createdBy;
    private LocalDateTime createdAt;
    private String approvedBy;
    private LocalDateTime paidAt;

    // aggregates, filled by list queries
    private int employeeCount;
    private BigDecimal totalGross = BigDecimal.ZERO;
    private BigDecimal totalNet = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDate getPayDate() { return payDate; }
    public void setPayDate(LocalDate payDate) { this.payDate = payDate; }

    public PayRunStatus getStatus() { return status; }
    public void setStatus(PayRunStatus status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }

    public BigDecimal getTotalGross() { return totalGross; }
    public void setTotalGross(BigDecimal totalGross) { this.totalGross = totalGross; }

    public BigDecimal getTotalNet() { return totalNet; }
    public void setTotalNet(BigDecimal totalNet) { this.totalNet = totalNet; }

    public boolean isLocked() {
        return status == PayRunStatus.PAID;
    }

    public String getPeriodLabel() {
        if (periodStart == null) {
            return "";
        }
        if (periodEnd != null && periodStart.getDayOfMonth() == 1
                && periodEnd.equals(periodStart.withDayOfMonth(periodStart.lengthOfMonth()))) {
            return periodStart.format(PERIOD_FMT);
        }
        return periodStart + " - " + periodEnd;
    }

    @Override
    public String toString() {
        return "Pay run #" + id + " (" + getPeriodLabel() + ")";
    }
}
