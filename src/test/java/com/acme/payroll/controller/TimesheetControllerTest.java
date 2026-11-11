package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.db.Database;
import com.acme.payroll.db.DbInitializer;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimesheetControllerTest {

    private AppContext ctx;
    private TimesheetController controller;

    @Before
    public void setUp() {
        Session.logout();
        Database db = Database.inMemory("ts-" + UUID.randomUUID());
        DbInitializer.initialize(db, false);
        ctx = new AppContext(db);
        controller = ctx.getTimesheetController();
    }

    @After
    public void tearDown() {
        Session.logout();
    }

    @Test
    public void hoursMustBeBetweenZeroAndTwentyFour() {
        Timesheet t = timesheet(LocalDate.of(2026, 1, 5));
        t.setHours(2, new BigDecimal("24.5"));
        try {
            controller.save(t);
            fail("expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getErrors().contains("Wed: hours must be between 0 and 24."));
        }
    }

    @Test(expected = ValidationException.class)
    public void weekMustStartOnMonday() {
        controller.save(timesheet(LocalDate.of(2026, 1, 6)));
    }

    @Test
    public void submitApproveFlow() {
        Timesheet t = controller.save(timesheet(LocalDate.of(2026, 1, 5)));
        assertEquals(TimesheetStatus.DRAFT, t.getStatus());
        controller.submit(t.getId());
        controller.approve(t.getId());
        assertEquals(TimesheetStatus.APPROVED, ctx.getTimesheetDao().findById(t.getId()).getStatus());
    }

    @Test
    public void rejectRequiresReason() {
        Timesheet t = controller.save(timesheet(LocalDate.of(2026, 1, 12)));
        controller.submit(t.getId());
        try {
            controller.reject(t.getId(), "  ");
            fail("expected ValidationException");
        } catch (ValidationException expected) {
            // ok
        }
        controller.reject(t.getId(), "Missing project code");
        Timesheet loaded = ctx.getTimesheetDao().findById(t.getId());
        assertEquals(TimesheetStatus.REJECTED, loaded.getStatus());
        assertEquals("Missing project code", loaded.getRejectReason());
    }

    @Test(expected = ValidationException.class)
    public void duplicateWeekIsRejected() {
        controller.save(timesheet(LocalDate.of(2026, 2, 2)));
        controller.save(timesheet(LocalDate.of(2026, 2, 2)));
    }

    private static Timesheet timesheet(LocalDate monday) {
        Timesheet t = new Timesheet();
        t.setEmployeeId(5L);
        t.setWeekStart(monday);
        for (int i = 0; i < 5; i++) {
            t.setHours(i, new BigDecimal("8"));
        }
        return t;
    }
}
