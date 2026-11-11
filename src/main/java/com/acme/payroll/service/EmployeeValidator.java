package com.acme.payroll.service;

import com.acme.payroll.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Field-level validation of the employee form. Uniqueness checks that need the database
 * are done by the controller.
 */
public class EmployeeValidator {

    static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Clock clock;

    public interface Clock {
        LocalDate today();
    }

    public EmployeeValidator() {
        this(new Clock() {
            @Override
            public LocalDate today() {
                return LocalDate.now();
            }
        });
    }

    public EmployeeValidator(Clock clock) {
        this.clock = clock;
    }

    public List<String> validate(Employee e) {
        List<String> errors = new ArrayList<String>();
        if (isBlank(e.getFirstName())) {
            errors.add("First name is required.");
        }
        if (isBlank(e.getLastName())) {
            errors.add("Last name is required.");
        }
        if (isBlank(e.getEmail())) {
            errors.add("Email is required.");
        } else if (!isValidEmail(e.getEmail())) {
            errors.add("Email address is not valid.");
        }
        if (e.getDepartmentId() == null) {
            errors.add("Department is required.");
        }
        if (e.getPayType() == null) {
            errors.add("Pay type is required.");
        } else if (e.isHourly()) {
            if (!isPositive(e.getHourlyRate())) {
                errors.add("Hourly rate must be greater than 0.");
            }
        } else if (!isPositive(e.getAnnualSalary())) {
            errors.add("Annual salary must be greater than 0.");
        }
        if (e.getHireDate() == null) {
            errors.add("Hire date is required.");
        } else {
            LocalDate latest = clock.today().plusDays(1);
            if (e.getHireDate().isAfter(latest)) {
                errors.add("Hire date cannot be in the future.");
            }
        }
        return errors;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
