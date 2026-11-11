package com.acme.payroll.dao;

import com.acme.payroll.db.Database;
import com.acme.payroll.db.DbInitializer;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EmployeeDaoTest {

    private EmployeeDao dao;
    private DepartmentDao departmentDao;

    @Before
    public void setUp() {
        Database db = Database.inMemory("emp-" + UUID.randomUUID());
        DbInitializer.initialize(db, false);
        dao = new EmployeeDao(db);
        departmentDao = new DepartmentDao(db);
    }

    @Test
    public void seedDataIsLoaded() {
        assertEquals(5, departmentDao.findAll().size());
        assertEquals(22, dao.countActive());
        EmployeeFilter all = new EmployeeFilter();
        all.setStatus(EmployeeFilter.Status.ALL);
        assertEquals(25, dao.count(all));
    }

    @Test
    public void filtersByDepartmentStatusAndText() {
        EmployeeFilter f = new EmployeeFilter();
        f.setDepartmentId(1L);
        f.setStatus(EmployeeFilter.Status.INACTIVE);
        List<Employee> inactiveEngineers = dao.search(f, 0, 50);
        assertEquals(1, inactiveEngineers.size());
        assertEquals("Silva", inactiveEngineers.get(0).getLastName());

        EmployeeFilter text = new EmployeeFilter();
        text.setText("WATANABE");
        assertEquals(1, dao.count(text));
    }

    @Test
    public void pagesThroughResultsInSortOrder() {
        EmployeeFilter f = new EmployeeFilter();
        f.setSortField(EmployeeFilter.SortField.EMPLOYEE_NO);
        List<Employee> first = dao.search(f, 0, 5);
        assertEquals(5, first.size());
        assertEquals("E1001", first.get(0).getEmployeeNo());

        f.setAscending(false);
        assertEquals("E5004", dao.search(f, 0, 1).get(0).getEmployeeNo());
    }

    @Test
    public void insertAndReadBack() {
        Employee e = new Employee();
        e.setFirstName("Nora");
        e.setLastName("Quinn");
        e.setEmail("nora.quinn@acme.test");
        e.setDepartmentId(2L);
        e.setJobTitle("Analyst");
        e.setPayType(PayType.SALARIED);
        e.setAnnualSalary(new BigDecimal("58000.00"));
        e.setHireDate(LocalDate.of(2025, 2, 3));
        long id = dao.insert(e);

        Employee loaded = dao.findById(id);
        assertNotNull(loaded);
        assertEquals("E2005", loaded.getEmployeeNo());
        assertEquals("Finance", loaded.getDepartmentName());
        assertEquals(0, new BigDecimal("58000").compareTo(loaded.getAnnualSalary()));
        assertTrue(loaded.isActive());
    }

    @Test
    public void emailUniquenessCheckIgnoresCaseAndSelf() {
        Employee tom = dao.findByEmail("tom.becker@acme.test");
        assertTrue(dao.emailExists("Tom.Becker@ACME.test", null));
        assertFalse(dao.emailExists("tom.becker@acme.test", tom.getId()));
        assertFalse(dao.emailExists("nobody@acme.test", null));
    }

    @Test
    public void deactivateExcludesFromPayable() {
        int before = dao.findPayable(LocalDate.of(2026, 11, 30)).size();
        dao.setActive(1L, false);
        assertEquals(before - 1, dao.findPayable(LocalDate.of(2026, 11, 30)).size());
    }
}
