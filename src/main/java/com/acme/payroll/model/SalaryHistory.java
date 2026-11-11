package com.acme.payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalaryHistory {

    private Long id;
    private Long employeeId;
    private LocalDate effectiveDate;
    private BigDecimal annualSalary;
    private BigDecimal hourlyRate;
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
