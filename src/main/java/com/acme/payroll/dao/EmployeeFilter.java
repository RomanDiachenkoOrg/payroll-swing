package com.acme.payroll.dao;

/**
 * Search criteria for the employees list.
 */
public class EmployeeFilter {

    public enum Status { ALL, ACTIVE, INACTIVE }

    public enum SortField {
        EMPLOYEE_NO("e.employee_no"),
        NAME("e.last_name, e.first_name"),
        DEPARTMENT("d.name"),
        JOB_TITLE("e.job_title"),
        HIRE_DATE("e.hire_date");

        private final String sql;

        SortField(String sql) {
            this.sql = sql;
        }

        String sql(boolean ascending) {
            String dir = ascending ? " ASC" : " DESC";
            return sql.replace(",", dir + ",") + dir;
        }
    }

    private String text;
    private Long departmentId;
    private Status status = Status.ACTIVE;
    private SortField sortField = SortField.NAME;
    private boolean ascending = true;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public SortField getSortField() { return sortField; }
    public void setSortField(SortField sortField) { this.sortField = sortField; }

    public boolean isAscending() { return ascending; }
    public void setAscending(boolean ascending) { this.ascending = ascending; }
}
