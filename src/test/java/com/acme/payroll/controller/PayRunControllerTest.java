package com.acme.payroll.controller;

import com.acme.payroll.AppContext;
import com.acme.payroll.db.Database;
import com.acme.payroll.db.DbInitializer;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.service.Session;
import com.acme.payroll.service.ValidationException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PayRunControllerTest {

    private AppContext ctx;
    private PayRunController controller;

    @Before
    public void setUp() {
        Session.logout();
        Database db = Database.inMemory("payrun-" + UUID.randomUUID());
        DbInitializer.initialize(db, true);
        ctx = new AppContext(db);
        controller = ctx.getPayRunController();
    }

    @Test
    public void sampleDataHasTwoPaidRunsAndOneDraft() {
        List<PayRun> runs = controller.list();
        assertEquals(3, runs.size());
        assertEquals(PayRunStatus.DRAFT, runs.get(0).getStatus());
        assertEquals(PayRunStatus.PAID, runs.get(1).getStatus());
        assertEquals(PayRunStatus.PAID, runs.get(2).getStatus());
        assertTrue(runs.get(1).getEmployeeCount() > 15);
    }

    @Test
    public void fullLifecycle() {
        PayRun draft = controller.list().get(0);
        int count = controller.calculate(draft.getId());
        assertTrue(count > 0);
        assertEquals(PayRunStatus.CALCULATED, controller.find(draft.getId()).getStatus());

        controller.approve(draft.getId());
        assertEquals(PayRunStatus.APPROVED, controller.find(draft.getId()).getStatus());

        controller.markPaid(draft.getId());
        PayRun paid = controller.find(draft.getId());
        assertEquals(PayRunStatus.PAID, paid.getStatus());
        assertTrue(paid.isLocked());
    }

    @Test
    public void payslipNetMatchesComponents() {
        PayRun september = controller.list().get(2);
        for (Payslip p : controller.payslips(september.getId())) {
            BigDecimal expected = p.getGross().subtract(p.getIncomeTax()).subtract(p.getSocial())
                    .subtract(p.getPension()).subtract(p.getOtherDeductions());
            assertEquals(0, expected.compareTo(p.getNet()));
        }
    }

    @Test(expected = ValidationException.class)
    public void draftCannotBeApproved() {
        controller.approve(controller.list().get(0).getId());
    }

    @Test(expected = ValidationException.class)
    public void paidRunCannotBeRecalculated() {
        controller.calculate(controller.list().get(1).getId());
    }

    @Test(expected = ValidationException.class)
    public void paidRunCannotBeDeleted() {
        controller.delete(controller.list().get(1).getId());
    }

    @Test(expected = ValidationException.class)
    public void overlappingPeriodIsRejected() {
        controller.create(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 15));
    }

    @Test
    public void createDecemberRun() {
        PayRun r = controller.create(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), LocalDate.of(2026, 12, 31));
        assertEquals(PayRunStatus.DRAFT, r.getStatus());
        assertEquals("Dec 2026", r.getPeriodLabel());
    }
}
