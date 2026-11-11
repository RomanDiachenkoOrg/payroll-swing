package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.db.Database;
import com.acme.payroll.db.DbInitializer;
import com.acme.payroll.model.Department;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DepartmentControllerTest {

    private DepartmentController controller;

    @Before
    public void setUp() {
        Session.logout();
        Database db = Database.inMemory("dept-" + UUID.randomUUID());
        DbInitializer.initialize(db, false);
        controller = new AppContext(db).getDepartmentController();
    }

    @Test
    public void cannotDeleteDepartmentWithActiveEmployees() {
        try {
            controller.delete(1L);
            fail("expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("active employee"));
        }
    }

    @Test
    public void createRenameAndDeleteEmptyDepartment() {
        Department d = controller.save(new Department(null, "  Legal ", "CC-600"));
        assertEquals("Legal", d.getName());
        assertEquals(6, controller.list().size());

        d.setName("Legal & Compliance");
        controller.save(d);
        controller.delete(d.getId());
        assertEquals(5, controller.list().size());
    }

    @Test(expected = ValidationException.class)
    public void nameMustBeUnique() {
        controller.save(new Department(null, "finance", null));
    }

    @Test(expected = ValidationException.class)
    public void nameIsRequired() {
        controller.save(new Department(null, " ", null));
    }
}
