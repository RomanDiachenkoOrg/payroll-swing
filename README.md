# Acme Payroll Desktop

Payroll desktop application for a small company: employees, departments, weekly timesheets,
monthly pay runs, payslips, cost reports and basic administration. It is a classic
single-user Swing client with an embedded H2 database - no server to install.

## Tech stack

- Java 8 language level (builds and runs on any JDK 8+; tested on JDK 21)
- Swing UI (JFrame, menu bar, toolbar, tabbed screens, modal dialogs, custom `TableModel`s)
- Plain JDBC DAO layer over an embedded **H2 2.2** database (file mode)
- `java.awt.print` for payslip printing, CSV export
- `java.util.logging`
- Maven 3 build with the shade plugin (single executable jar)
- JUnit 4 tests

## Prerequisites

| Tool  | Version                  |
|-------|--------------------------|
| JDK   | 8 or newer (21 recommended) |
| Maven | 3.6 or newer             |

## How to run

```bash
mvn package
java -jar target/payroll-swing.jar
```

On Windows you can also double-click `run.bat` (it builds the jar first if needed).

On first start the application creates its database in `~/.payroll-swing/payroll.mv.db`
and loads sample data (5 departments, 25 employees, timesheets, two paid pay runs and one draft).
To start over, close the application and delete the `~/.payroll-swing` folder.
Use `-Dpayroll.home=<folder>` to keep the database somewhere else.

## Demo accounts

All accounts use the password `Passw0rd!`.

| Email                | Role     | Can do                                                        |
|----------------------|----------|---------------------------------------------------------------|
| `admin@acme.test`    | Admin    | everything                                                    |
| `manager@acme.test`  | Manager  | Engineering: approve/reject timesheets, view employees, reports |
| `employee@acme.test` | Employee | own profile and payslips, enter and submit own timesheets     |

## Screens

| Screen / window          | Who               | What                                                                    |
|--------------------------|-------------------|-------------------------------------------------------------------------|
| Sign-in dialog           | all               | email + password, validation and error messages                         |
| Dashboard tab            | admin, manager    | headcount, last payroll total, pending timesheets, next pay date, chart, recent runs |
| Employees tab            | admin, manager    | search, department / status filter, sort by column header, paging; new / edit / deactivate |
| Employee form dialog     | admin             | required fields, email format and uniqueness, salary > 0, hire date checks |
| Employee details dialog  | admin, manager    | profile, salary history (add raise), payslip list                       |
| Timesheets tab + dialog  | all               | status filter, weekly entry (7 days, 0-24 h), submit, approve / reject with reason |
| Pay runs tab             | admin             | create for a period, calculate, review lines, approve, mark paid (locked), CSV export |
| Payslip dialog           | all               | breakdown, print, CSV export                                            |
| Departments tab          | admin             | create / rename / delete (blocked while it has active employees)        |
| Reports tab              | admin, manager    | payroll cost by department for a date range, CSV export                 |
| Admin tab                | admin             | tax brackets and contribution rates, users, audit log with filters      |
| My profile tab           | employee          | own details and released payslips                                       |

Menu: **File** (Refresh F5, Sign out, Exit), **View** (Ctrl+1..9 switch tabs), **Payroll** (admin), **Help**.

## Payroll rules

- Salaried: gross = annual salary / 12
- Hourly: gross = rate x regular hours + rate x 1.5 x overtime hours (overtime = hours above 40 in a week,
  taken from approved timesheets whose week starts inside the pay period)
- Income tax on the monthly gross: 0 - 1,000 at 0%, 1,000 - 4,000 at 15%, above 4,000 at 25% (editable)
- Social contribution 5% of gross; pension 3% of gross if the employee opted in (editable)
- Other fixed deductions per employee (health insurance, dental, union dues, parking)
- net = gross - tax - social - pension - other, rounded half-up to cents, USD

## Project structure

```
src/main/java/com/acme/payroll
  PayrollApp.java       entry point (look & feel, DB init, login loop)
  AppContext.java       wires DAOs and controllers
  model/                domain beans and enums
  db/                   Database (H2 connection), DbInitializer, SeedData
  dao/                  JDBC DAOs (AbstractDao helper + one DAO per table group)
  service/              PayrollCalculator, AuthService, PasswordHasher, CsvWriter, validation
  controller/           application logic used by the screens (one per area)
  ui/                   MainFrame, LoginDialog, panels and dialogs
  ui/model/             Swing TableModels
  ui/util/              formatting, small widgets (DateField, BarChartPanel)
src/main/resources/db   schema.sql, seed.sql
src/test/java           JUnit tests (calculator, validation, DAOs and controllers on in-memory H2)
```

## Running the tests

```bash
mvn test        # unit tests only
mvn verify      # tests + executable jar
```

The DAO and controller tests use a private in-memory H2 database per test and run headless.
