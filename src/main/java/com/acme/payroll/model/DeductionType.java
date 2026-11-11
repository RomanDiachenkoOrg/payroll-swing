package com.acme.payroll.model;

import java.math.BigDecimal;

public class DeductionType {

    private Long id;
    private String code;
    private String name;
    private BigDecimal defaultAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getDefaultAmount() { return defaultAmount; }
    public void setDefaultAmount(BigDecimal defaultAmount) { this.defaultAmount = defaultAmount; }

    @Override
    public String toString() {
        return name;
    }
}
