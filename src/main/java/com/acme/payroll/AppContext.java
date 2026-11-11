package com.acme.payroll;

import com.acme.payroll.controller.AdminController;
import com.acme.payroll.controller.DashboardController;
import com.acme.payroll.controller.DepartmentController;
import com.acme.payroll.controller.EmployeeController;
import com.acme.payroll.controller.PayRunController;
import com.acme.payroll.controller.ReportController;
import com.acme.payroll.controller.TimesheetController;
import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.DeductionDao;
import com.acme.payroll.dao.DepartmentDao;
import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.PayRunDao;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.dao.ReportDao;
import com.acme.payroll.dao.SalaryHistoryDao;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.dao.UserDao;
import com.acme.payroll.db.Database;
import com.acme.payroll.service.AuthService;

/**
 * Wires DAOs and controllers together. One instance per running application.
 */
public class AppContext {

    private final Database database;

    private final DepartmentDao departmentDao;
    private final EmployeeDao employeeDao;
    private final SalaryHistoryDao salaryHistoryDao;
    private final TimesheetDao timesheetDao;
    private final PayRunDao payRunDao;
    private final PayslipDao payslipDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;
    private final SettingsDao settingsDao;
    private final DeductionDao deductionDao;
    private final ReportDao reportDao;

    private final AuthService authService;
    private final EmployeeController employeeController;
    private final DepartmentController departmentController;
    private final TimesheetController timesheetController;
    private final PayRunController payRunController;
    private final ReportController reportController;
    private final AdminController adminController;
    private final DashboardController dashboardController;

    public AppContext(Database database) {
        this.database = database;
        departmentDao = new DepartmentDao(database);
        employeeDao = new EmployeeDao(database);
        salaryHistoryDao = new SalaryHistoryDao(database);
        timesheetDao = new TimesheetDao(database);
        payRunDao = new PayRunDao(database);
        payslipDao = new PayslipDao(database);
        userDao = new UserDao(database);
        auditLogDao = new AuditLogDao(database);
        settingsDao = new SettingsDao(database);
        deductionDao = new DeductionDao(database);
        reportDao = new ReportDao(database);

        authService = new AuthService(userDao, auditLogDao);
        employeeController = new EmployeeController(this);
        departmentController = new DepartmentController(this);
        timesheetController = new TimesheetController(this);
        payRunController = new PayRunController(this);
        reportController = new ReportController(this);
        adminController = new AdminController(this);
        dashboardController = new DashboardController(this);
    }

    public Database getDatabase() { return database; }

    public DepartmentDao getDepartmentDao() { return departmentDao; }
    public EmployeeDao getEmployeeDao() { return employeeDao; }
    public SalaryHistoryDao getSalaryHistoryDao() { return salaryHistoryDao; }
    public TimesheetDao getTimesheetDao() { return timesheetDao; }
    public PayRunDao getPayRunDao() { return payRunDao; }
    public PayslipDao getPayslipDao() { return payslipDao; }
    public UserDao getUserDao() { return userDao; }
    public AuditLogDao getAuditLogDao() { return auditLogDao; }
    public SettingsDao getSettingsDao() { return settingsDao; }
    public DeductionDao getDeductionDao() { return deductionDao; }
    public ReportDao getReportDao() { return reportDao; }

    public AuthService getAuthService() { return authService; }
    public EmployeeController getEmployeeController() { return employeeController; }
    public DepartmentController getDepartmentController() { return departmentController; }
    public TimesheetController getTimesheetController() { return timesheetController; }
    public PayRunController getPayRunController() { return payRunController; }
    public ReportController getReportController() { return reportController; }
    public AdminController getAdminController() { return adminController; }
    public DashboardController getDashboardController() { return dashboardController; }
}
