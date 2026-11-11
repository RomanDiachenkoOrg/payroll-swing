package com.acme.payroll.service;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmployeeValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    private final EmployeeValidator validator = new EmployeeValidator(() -> TODAY);

    @Test
    public void validEmployeePasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    public void requiredFieldsAreReported() {
        List<String> errors = validator.validate(new Employee());
        assertTrue(errors.contains("First name is required."));
        assertTrue(errors.contains("Last name is required."));
        assertTrue(errors.contains("Email is required."));
        assertTrue(errors.contains("Department is required."));
        assertTrue(errors.contains("Hire date is required."));
    }

    @Test
    public void rejectsMalformedEmail() {
        Employee e = valid();
        e.setEmail("jane.doe@");
        assertTrue(validator.validate(e).contains("Email address is not valid."));
    }

    @Test
    public void salaryMustBePositive() {
        Employee e = valid();
        e.setAnnualSalary(BigDecimal.ZERO);
        assertTrue(validator.validate(e).contains("Annual salary must be greater than 0."));
    }

    @Test
    public void hourlyRateIsCheckedForHourlyStaff() {
        Employee e = valid();
        e.setPayType(PayType.HOURLY);
        e.setHourlyRate(new BigDecimal("-1"));
        assertTrue(validator.validate(e).contains("Hourly rate must be greater than 0."));
    }

    @Test
    public void hireDateTodayIsAccepted() {
        Employee e = valid();
        e.setHireDate(TODAY);
        assertTrue(validator.validate(e).isEmpty());
    }

    @Test
    public void hireDateNextMonthIsRejected() {
        Employee e = valid();
        e.setHireDate(TODAY.plusMonths(1));
        assertTrue(validator.validate(e).contains("Hire date cannot be in the future."));
    }

    @Test
    public void emailFormat() {
        assertTrue(EmployeeValidator.isValidEmail("a.b@acme.test"));
        assertFalse(EmployeeValidator.isValidEmail("a b@acme.test"));
        assertFalse(EmployeeValidator.isValidEmail("acme.test"));
    }

    private static Employee valid() {
        Employee e = new Employee();
        e.setFirstName("Jane");
        e.setLastName("Doe");
        e.setEmail("jane.doe@acme.test");
        e.setDepartmentId(1L);
        e.setPayType(PayType.SALARIED);
        e.setAnnualSalary(new BigDecimal("55000"));
        e.setHireDate(LocalDate.of(2024, 5, 1));
        return e;
    }
}
