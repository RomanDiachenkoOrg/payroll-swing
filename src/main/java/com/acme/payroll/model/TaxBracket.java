package com.acme.payroll.model;

import java.math.BigDecimal;

/**
 * Progressive income tax bracket on monthly gross. Upper bound null means "no limit".
 * Rate is a percentage (e.g. 15.00).
 */
public class TaxBracket {

    private Long id;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private BigDecimal rate;

    public TaxBracket() {
    }

    public TaxBracket(BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rate = rate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getLowerBound() { return lowerBound; }
    public void setLowerBound(BigDecimal lowerBound) { this.lowerBound = lowerBound; }

    public BigDecimal getUpperBound() { return upperBound; }
    public void setUpperBound(BigDecimal upperBound) { this.upperBound = upperBound; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}
