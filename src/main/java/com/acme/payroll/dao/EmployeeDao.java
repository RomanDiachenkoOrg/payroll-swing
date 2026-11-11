package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao extends AbstractDao {

    private static final String SELECT =
            "SELECT e.*, d.name AS department_name FROM employee e "
            + "JOIN department d ON d.id = e.department_id ";

    private static final RowMapper<Employee> MAPPER = new RowMapper<Employee>() {
        @Override
        public Employee map(ResultSet rs) throws SQLException {
            Employee e = new Employee();
            e.setId(rs.getLong("id"));
            e.setEmployeeNo(rs.getString("employee_no"));
            e.setFirstName(rs.getString("first_name"));
            e.setLastName(rs.getString("last_name"));
            e.setEmail(rs.getString("email"));
            e.setPhone(rs.getString("phone"));
            e.setDepartmentId(rs.getLong("department_id"));
            e.setDepartmentName(rs.getString("department_name"));
            e.setJobTitle(rs.getString("job_title"));
            e.setPayType(PayType.valueOf(rs.getString("pay_type")));
            e.setAnnualSalary(rs.getBigDecimal("annual_salary"));
            e.setHourlyRate(rs.getBigDecimal("hourly_rate"));
            e.setHireDate(getDate(rs, "hire_date"));
            e.setActive(rs.getBoolean("active"));
            e.setPensionOptIn(rs.getBoolean("pension_opt_in"));
            return e;
        }
    };

    public EmployeeDao(Database db) {
        super(db);
    }

    public List<Employee> search(EmployeeFilter filter, int offset, int limit) {
        List<Object> params = new ArrayList<Object>();
        String where = buildWhere(filter, params);
        params.add(limit);
        params.add(offset);
        return query(SELECT + where + " ORDER BY " + filter.getSortField().sql(filter.isAscending())
                + ", e.id LIMIT ? OFFSET ?", MAPPER, params.toArray());
    }

    public int count(EmployeeFilter filter) {
        List<Object> params = new ArrayList<Object>();
        String where = buildWhere(filter, params);
        return (int) queryLong("SELECT COUNT(*) FROM employee e JOIN department d ON d.id = e.department_id " + where,
                params.toArray());
    }

    private String buildWhere(EmployeeFilter filter, List<Object> params) {
        StringBuilder sb = new StringBuilder("WHERE 1 = 1");
        if (filter.getText() != null && !filter.getText().trim().isEmpty()) {
            String like = "%" + filter.getText().trim().toLowerCase() + "%";
            sb.append(" AND (LOWER(e.first_name) LIKE ? OR LOWER(e.last_name) LIKE ?"
                    + " OR LOWER(e.email) LIKE ? OR LOWER(e.employee_no) LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (filter.getDepartmentId() != null) {
            sb.append(" AND e.department_id = ?");
            params.add(filter.getDepartmentId());
        }
        if (filter.getStatus() == EmployeeFilter.Status.ACTIVE) {
            sb.append(" AND e.active = TRUE");
        } else if (filter.getStatus() == EmployeeFilter.Status.INACTIVE) {
            sb.append(" AND e.active = FALSE");
        }
        return sb.toString();
    }

    public Employee findById(long id) {
        return queryOne(SELECT + "WHERE e.id = ?", MAPPER, id);
    }

    public Employee findByEmail(String email) {
        return queryOne(SELECT + "WHERE LOWER(e.email) = LOWER(?)", MAPPER, email);
    }

    public List<Employee> findActive() {
        return query(SELECT + "WHERE e.active = TRUE ORDER BY e.last_name, e.first_name", MAPPER);
    }

    public List<Employee> findActiveByDepartment(long departmentId) {
        return query(SELECT + "WHERE e.active = TRUE AND e.department_id = ? ORDER BY e.last_name, e.first_name",
                MAPPER, departmentId);
    }

    /** Employees that should be paid in a period: active and hired on or before the period end. */
    public List<Employee> findPayable(LocalDate periodEnd) {
        return query(SELECT + "WHERE e.active = TRUE AND e.hire_date <= ? ORDER BY e.employee_no", MAPPER, periodEnd);
    }

    public boolean emailExists(String email, Long excludeId) {
        return queryLong("SELECT COUNT(*) FROM employee WHERE LOWER(email) = LOWER(?) AND id <> ?",
                email, excludeId == null ? -1L : excludeId) > 0;
    }

    public int countActive() {
        return (int) queryLong("SELECT COUNT(*) FROM employee WHERE active = TRUE");
    }

    public String nextEmployeeNo(long departmentId) {
        String max = queryOne("SELECT MAX(employee_no) FROM employee WHERE department_id = ?", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        }, departmentId);
        if (max == null) {
            return "E" + departmentId + "001";
        }
        int n = Integer.parseInt(max.substring(1)) + 1;
        return "E" + n;
    }

    public long insert(Employee e) {
        if (e.getEmployeeNo() == null) {
            e.setEmployeeNo(nextEmployeeNo(e.getDepartmentId()));
        }
        long id = insert("INSERT INTO employee (employee_no, first_name, last_name, email, phone, department_id, job_title, "
                        + "pay_type, annual_salary, hourly_rate, hire_date, active, pension_opt_in) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getEmployeeNo(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(), e.getDepartmentId(),
                e.getJobTitle(), e.getPayType(), e.getAnnualSalary(), e.getHourlyRate(), e.getHireDate(),
                e.isActive(), e.isPensionOptIn());
        e.setId(id);
        return id;
    }

    public void update(Employee e) {
        update("UPDATE employee SET first_name = ?, last_name = ?, email = ?, phone = ?, department_id = ?, job_title = ?, "
                        + "pay_type = ?, annual_salary = ?, hourly_rate = ?, hire_date = ?, active = ?, pension_opt_in = ? "
                        + "WHERE id = ?",
                e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(), e.getDepartmentId(), e.getJobTitle(),
                e.getPayType(), e.getAnnualSalary(), e.getHourlyRate(), e.getHireDate(), e.isActive(),
                e.isPensionOptIn(), e.getId());
    }

    public void updateRate(long employeeId, java.math.BigDecimal annualSalary, java.math.BigDecimal hourlyRate) {
        update("UPDATE employee SET annual_salary = ?, hourly_rate = ? WHERE id = ?", annualSalary, hourlyRate, employeeId);
    }

    public void setActive(long id, boolean active) {
        update("UPDATE employee SET active = ? WHERE id = ?", active, id);
    }
}
