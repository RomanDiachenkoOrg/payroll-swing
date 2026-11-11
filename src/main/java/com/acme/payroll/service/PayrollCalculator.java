package com.acme.payroll.service;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.model.Timesheet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monthly payroll calculation.
 *
 * <ul>
 *   <li>Salaried: gross = annual salary / 12</li>
 *   <li>Hourly: gross = rate * regular + rate * 1.5 * overtime; overtime = hours over 40 in a week</li>
 *   <li>Income tax is progressive on the monthly gross (see {@link TaxBracket})</li>
 *   <li>Social contribution and (optional) pension are flat percentages of gross</li>
 * </ul>
 * All money amounts are rounded to 2 decimals, half-up.
 */
public class PayrollCalculator {

    public static final BigDecimal WEEKLY_HOURS_THRESHOLD = new BigDecimal("40");
    public static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.5");

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MONTHS = new BigDecimal("12");

    private final List<TaxBracket> brackets;
    private final BigDecimal socialRatePercent;
    private final BigDecimal pensionRatePercent;

    public PayrollCalculator(List<TaxBracket> brackets, BigDecimal socialRatePercent, BigDecimal pensionRatePercent) {
        List<TaxBracket> copy = new ArrayList<TaxBracket>(brackets);
        Collections.sort(copy, (a, b) -> a.getLowerBound().compareTo(b.getLowerBound()));
        this.brackets = copy;
        this.socialRatePercent = socialRatePercent;
        this.pensionRatePercent = pensionRatePercent;
    }

    /** Default company rules: 0% / 15% / 25% brackets, 5% social, 3% pension. */
    public static PayrollCalculator withDefaults() {
        List<TaxBracket> b = new ArrayList<TaxBracket>();
        b.add(new TaxBracket(new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0")));
        b.add(new TaxBracket(new BigDecimal("1000"), new BigDecimal("4000"), new BigDecimal("15")));
        b.add(new TaxBracket(new BigDecimal("4000"), null, new BigDecimal("25")));
        return new PayrollCalculator(b, new BigDecimal("5"), new BigDecimal("3"));
    }

    public Payslip calculate(Employee employee, List<Timesheet> approvedTimesheets, BigDecimal otherDeductions) {
        Payslip p = new Payslip();
        p.setEmployeeId(employee.getId());
        p.setEmployeeNo(employee.getEmployeeNo());
        p.setEmployeeName(employee.getFullName());
        p.setDepartmentName(employee.getDepartmentName());

        BigDecimal gross;
        if (employee.isHourly()) {
            BigDecimal[] split = splitHours(approvedTimesheets);
            p.setRegularHours(split[0]);
            p.setOvertimeHours(split[1]);
            gross = hourlyGross(employee.getHourlyRate(), split[0], split[1]);
        } else {
            gross = salariedGross(employee.getAnnualSalary());
        }

        BigDecimal tax = incomeTax(gross);
        BigDecimal social = percentOf(gross, socialRatePercent);
        BigDecimal pension = employee.isPensionOptIn() ? percentOf(gross, pensionRatePercent) : zero();
        BigDecimal other = round(otherDeductions == null ? BigDecimal.ZERO : otherDeductions);

        p.setGross(gross);
        p.setIncomeTax(tax);
        p.setSocial(social);
        p.setPension(pension);
        p.setOtherDeductions(other);
        p.setNet(gross.subtract(tax).subtract(social).subtract(pension).subtract(other));
        return p;
    }

    public BigDecimal salariedGross(BigDecimal annualSalary) {
        if (annualSalary == null) {
            return zero();
        }
        return annualSalary.divide(MONTHS, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal hourlyGross(BigDecimal rate, BigDecimal regularHours, BigDecimal overtimeHours) {
        if (rate == null) {
            return zero();
        }
        BigDecimal regular = rate.multiply(regularHours);
        BigDecimal overtime = rate.multiply(OVERTIME_MULTIPLIER).multiply(overtimeHours);
        return round(regular.add(overtime));
    }

    /**
     * Splits the hours of each week into regular hours (up to 40) and overtime (above 40).
     *
     * @return {regular, overtime}
     */
    public BigDecimal[] splitHours(List<Timesheet> timesheets) {
        BigDecimal regular = BigDecimal.ZERO;
        BigDecimal overtime = BigDecimal.ZERO;
        if (timesheets != null) {
            for (Timesheet t : timesheets) {
                BigDecimal total = t.getTotalHours();
                if (total.compareTo(WEEKLY_HOURS_THRESHOLD) > 0) {
                    regular = regular.add(WEEKLY_HOURS_THRESHOLD);
                    overtime = overtime.add(total.subtract(WEEKLY_HOURS_THRESHOLD));
                } else {
                    regular = regular.add(total);
                }
            }
        }
        return new BigDecimal[] {regular, overtime};
    }

    /** Progressive tax: each bracket's rate applies only to the part of gross inside that bracket. */
    public BigDecimal incomeTax(BigDecimal gross) {
        BigDecimal tax = BigDecimal.ZERO;
        for (TaxBracket b : brackets) {
            if (gross.compareTo(b.getLowerBound()) <= 0) {
                break;
            }
            BigDecimal top = b.getUpperBound() == null || gross.compareTo(b.getUpperBound()) < 0
                    ? gross : b.getUpperBound();
            BigDecimal taxable = top.subtract(b.getLowerBound());
            tax = tax.add(taxable.multiply(b.getRate()).divide(HUNDRED));
        }
        return round(tax);
    }

    private static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return round(amount.multiply(percent).divide(HUNDRED));
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }
}
