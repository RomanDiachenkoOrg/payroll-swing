package com.acme.payroll.model;

public enum PayType {
    SALARIED("Salaried (monthly)"),
    HOURLY("Hourly");

    private final String label;

    PayType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
