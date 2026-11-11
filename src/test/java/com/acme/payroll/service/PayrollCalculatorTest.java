package com.acme.payroll.service;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Timesheet;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PayrollCalculatorTest {

    private PayrollCalculator calc;

    @Before
    public void setUp() {
        calc = PayrollCalculator.withDefaults();
    }

    @Test
    public void salariedEmployeeWithPensionAndHealthInsurance() {
        Employee e = salaried("60000", true);
        Payslip p = calc.calculate(e, null, new BigDecimal("120"));

        assertEquals(new BigDecimal("5000.00"), p.getGross());
        assertEquals(new BigDecimal("700.00"), p.getIncomeTax());
        assertEquals(new BigDecimal("250.00"), p.getSocial());
        assertEquals(new BigDecimal("150.00"), p.getPension());
        assertEquals(new BigDecimal("120.00"), p.getOtherDeductions());
        assertEquals(new BigDecimal("3780.00"), p.getNet());
    }

    @Test
    public void noPensionWhenNotOptedIn() {
        Payslip p = calc.calculate(salaried("36000", false), null, BigDecimal.ZERO);
        assertEquals(new BigDecimal("3000.00"), p.getGross());
        assertEquals(new BigDecimal("0.00"), p.getPension());
        assertEquals(new BigDecimal("300.00"), p.getIncomeTax());
        assertEquals(new BigDecimal("2550.00"), p.getNet());
    }

    @Test
    public void monthlyGrossIsRoundedHalfUp() {
        assertEquals(new BigDecimal("4166.67"), calc.salariedGross(new BigDecimal("50000")));
        assertEquals(new BigDecimal("491.67"), calc.incomeTax(new BigDecimal("4166.67")));
    }

    @Test
    public void taxIsZeroUpToFirstBracketLimit() {
        assertEquals(new BigDecimal("0.00"), calc.incomeTax(new BigDecimal("1000.00")));
        assertEquals(new BigDecimal("0.00"), calc.incomeTax(new BigDecimal("650.00")));
        assertEquals(new BigDecimal("0.15"), calc.incomeTax(new BigDecimal("1001.00")));
    }

    @Test
    public void hourlyOvertimeIsCalculatedPerWeek() {
        Employee e = hourly("20");
        Payslip p = calc.calculate(e, Arrays.asList(week(9, 9, 9, 9, 8), week(8, 8, 8, 8, 6)), BigDecimal.ZERO);

        assertEquals(new BigDecimal("78"), p.getRegularHours());
        assertEquals(new BigDecimal("4"), p.getOvertimeHours());
        assertEquals(new BigDecimal("1680.00"), p.getGross());
        assertEquals(new BigDecimal("102.00"), p.getIncomeTax());
        assertEquals(new BigDecimal("84.00"), p.getSocial());
        assertEquals(new BigDecimal("1494.00"), p.getNet());
    }

    @Test
    public void exactlyFortyHoursHasNoOvertime() {
        BigDecimal[] split = calc.splitHours(Collections.singletonList(week(8, 8, 8, 8, 8)));
        assertEquals(new BigDecimal("40"), split[0]);
        assertEquals(BigDecimal.ZERO, split[1]);
    }

    @Test
    public void hourlyWithoutTimesheetsEarnsNothing() {
        Payslip p = calc.calculate(hourly("25"), Collections.<Timesheet>emptyList(), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), p.getGross());
        assertEquals(new BigDecimal("0.00"), p.getNet());
    }

    private static Employee salaried(String annual, boolean pension) {
        Employee e = new Employee();
        e.setId(1L);
        e.setPayType(PayType.SALARIED);
        e.setAnnualSalary(new BigDecimal(annual));
        e.setPensionOptIn(pension);
        return e;
    }

    private static Employee hourly(String rate) {
        Employee e = new Employee();
        e.setId(2L);
        e.setPayType(PayType.HOURLY);
        e.setHourlyRate(new BigDecimal(rate));
        return e;
    }

    private static Timesheet week(int... hours) {
        Timesheet t = new Timesheet();
        for (int i = 0; i < hours.length; i++) {
            t.setHours(i, new BigDecimal(hours[i]));
        }
        return t;
    }
}
