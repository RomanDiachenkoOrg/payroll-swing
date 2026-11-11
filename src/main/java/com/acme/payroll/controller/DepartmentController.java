package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.model.Department;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;

import java.util.List;

public class DepartmentController {

    private final AppContext ctx;

    public DepartmentController(AppContext ctx) {
        this.ctx = ctx;
    }

    public List<Department> list() {
        return ctx.getDepartmentDao().findAll();
    }

    public Department save(Department d) {
        String name = d.getName() == null ? "" : d.getName().trim();
        if (name.isEmpty()) {
            throw new ValidationException("Department name is required.");
        }
        if (name.length() > 100) {
            throw new ValidationException("Department name is too long (max 100 characters).");
        }
        Department existing = ctx.getDepartmentDao().findByName(name);
        if (existing != null && !existing.getId().equals(d.getId())) {
            throw new ValidationException("A department named '" + name + "' already exists.");
        }
        d.setName(name);
        if (d.getCostCenter() != null) {
            d.setCostCenter(d.getCostCenter().trim());
        }
        if (d.getId() == null) {
            ctx.getDepartmentDao().insert(d);
            ctx.getAuditLogDao().log(Session.actor(), "DEPARTMENT_CREATE", "Department", d.getId(), name);
        } else {
            ctx.getDepartmentDao().update(d);
            ctx.getAuditLogDao().log(Session.actor(), "DEPARTMENT_UPDATE", "Department", d.getId(), name);
        }
        return d;
    }

    public void delete(long id) {
        Department d = ctx.getDepartmentDao().findById(id);
        if (d == null) {
            throw new ValidationException("Department not found.");
        }
        int active = ctx.getDepartmentDao().countActiveEmployees(id);
        if (active > 0) {
            throw new ValidationException("Cannot delete department '" + d.getName() + "': it has "
                    + active + " active employee(s).");
        }
        int all = ctx.getDepartmentDao().countAllEmployees(id);
        if (all > 0) {
            throw new ValidationException("Cannot delete department '" + d.getName()
                    + "': it is still referenced by " + all + " former employee record(s).");
        }
        ctx.getDepartmentDao().delete(id);
        ctx.getAuditLogDao().log(Session.actor(), "DEPARTMENT_DELETE", "Department", id, d.getName());
    }
}
