package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.dao.EmployeeFilter;
import com.acme.payroll.model.Department;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.SalaryHistory;
import com.acme.payroll.service.EmployeeValidator;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EmployeeController {

    private final AppContext ctx;
    private EmployeeValidator validator = new EmployeeValidator();

    public EmployeeController(AppContext ctx) {
        this.ctx = ctx;
    }

    void setValidator(EmployeeValidator validator) {
        this.validator = validator;
    }

    public List<Employee> search(EmployeeFilter filter, int page, int pageSize) {
        return ctx.getEmployeeDao().search(filter, page * pageSize, pageSize);
    }

    public int count(EmployeeFilter filter) {
        return ctx.getEmployeeDao().count(filter);
    }

    public Employee find(long id) {
        return ctx.getEmployeeDao().findById(id);
    }

    public List<Department> departments() {
        return ctx.getDepartmentDao().findAll();
    }

    public List<Employee> activeEmployees() {
        return ctx.getEmployeeDao().findActive();
    }

    /** Creates or updates an employee after validation. */
    public Employee save(Employee e) {
        requireAdmin();
        trim(e);
        List<String> errors = validator.validate(e);
        if (e.getEmail() != null && EmployeeValidator.isValidEmail(e.getEmail())
                && ctx.getEmployeeDao().emailExists(e.getEmail(), e.getId())) {
            errors.add("Another employee already uses this email address.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        if (e.isHourly()) {
            e.setAnnualSalary(null);
        } else {
            e.setHourlyRate(null);
        }
        if (e.getId() == null) {
            ctx.getEmployeeDao().insert(e);
            SalaryHistory h = new SalaryHistory();
            h.setEmployeeId(e.getId());
            h.setEffectiveDate(e.getHireDate());
            h.setAnnualSalary(e.getAnnualSalary());
            h.setHourlyRate(e.getHourlyRate());
            h.setReason("Starting salary");
            ctx.getSalaryHistoryDao().insert(h);
            audit("EMPLOYEE_CREATE", e.getId(), e.getFullName() + " <" + e.getEmail() + ">");
        } else {
            ctx.getEmployeeDao().update(e);
            audit("EMPLOYEE_UPDATE", e.getId(), e.getFullName());
        }
        return e;
    }

    public void deactivate(long id) {
        requireAdmin();
        Employee e = ctx.getEmployeeDao().findById(id);
        if (e == null) {
            throw new ValidationException("Employee not found.");
        }
        if (!e.isActive()) {
            throw new ValidationException(e.getFullName() + " is already inactive.");
        }
        ctx.getEmployeeDao().setActive(id, false);
        audit("EMPLOYEE_DEACTIVATE", id, e.getFullName());
    }

    public void reactivate(long id) {
        requireAdmin();
        ctx.getEmployeeDao().setActive(id, true);
        audit("EMPLOYEE_REACTIVATE", id, null);
    }

    public List<SalaryHistory> salaryHistory(long employeeId) {
        return ctx.getSalaryHistoryDao().findByEmployee(employeeId);
    }

    public List<Payslip> payslips(long employeeId) {
        boolean releasedOnly = !Session.hasRole(Role.ADMIN);
        return ctx.getPayslipDao().findByEmployee(employeeId, releasedOnly);
    }

    public List<String> deductions(long employeeId) {
        return ctx.getDeductionDao().describeForEmployee(employeeId);
    }

    /** Records a raise and updates the employee's current rate. */
    public void addRaise(long employeeId, LocalDate effectiveDate, BigDecimal newAmount, String reason) {
        requireAdmin();
        Employee e = ctx.getEmployeeDao().findById(employeeId);
        if (e == null) {
            throw new ValidationException("Employee not found.");
        }
        if (effectiveDate == null) {
            throw new ValidationException("Effective date is required.");
        }
        if (newAmount == null || newAmount.signum() <= 0) {
            throw new ValidationException(e.isHourly() ? "Hourly rate must be greater than 0."
                    : "Annual salary must be greater than 0.");
        }
        SalaryHistory h = new SalaryHistory();
        h.setEmployeeId(employeeId);
        h.setEffectiveDate(effectiveDate);
        h.setReason(reason == null || reason.trim().isEmpty() ? "Salary adjustment" : reason.trim());
        if (e.isHourly()) {
            h.setHourlyRate(newAmount);
        } else {
            h.setAnnualSalary(newAmount);
        }
        ctx.getSalaryHistoryDao().insert(h);
        if (!effectiveDate.isAfter(LocalDate.now())) {
            ctx.getEmployeeDao().updateRate(employeeId, h.getAnnualSalary(), h.getHourlyRate());
        }
        audit("SALARY_CHANGE", employeeId, "New rate " + newAmount.toPlainString() + " from " + effectiveDate);
    }

    private void trim(Employee e) {
        if (e.getFirstName() != null) e.setFirstName(e.getFirstName().trim());
        if (e.getLastName() != null) e.setLastName(e.getLastName().trim());
        if (e.getEmail() != null) e.setEmail(e.getEmail().trim());
        if (e.getPhone() != null) e.setPhone(e.getPhone().trim());
    }

    private void requireAdmin() {
        if (Session.isLoggedIn() && !Session.hasRole(Role.ADMIN)) {
            throw new ValidationException("Only administrators can change employee records.");
        }
    }

    private void audit(String action, Long id, String details) {
        ctx.getAuditLogDao().log(Session.actor(), action, "Employee", id, details);
    }
}
