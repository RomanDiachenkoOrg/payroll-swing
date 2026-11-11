package com.acme.payroll.model;

import java.math.BigDecimal;

/** One row of the "payroll cost by department" report. */
public class DepartmentCost {

    private String departmentName;
    private int headcount;
    private BigDecimal gross = BigDecimal.ZERO;
    private BigDecimal incomeTax = BigDecimal.ZERO;
    private BigDecimal social = BigDecimal.ZERO;
    private BigDecimal pension = BigDecimal.ZERO;
    private BigDecimal otherDeductions = BigDecimal.ZERO;
    private BigDecimal net = BigDecimal.ZERO;

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getHeadcount() { return headcount; }
    public void setHeadcount(int headcount) { this.headcount = headcount; }

    public BigDecimal getGross() { return gross; }
    public void setGross(BigDecimal gross) { this.gross = gross; }

    public BigDecimal getIncomeTax() { return incomeTax; }
    public void setIncomeTax(BigDecimal incomeTax) { this.incomeTax = incomeTax; }

    public BigDecimal getSocial() { return social; }
    public void setSocial(BigDecimal social) { this.social = social; }

    public BigDecimal getPension() { return pension; }
    public void setPension(BigDecimal pension) { this.pension = pension; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }
}
