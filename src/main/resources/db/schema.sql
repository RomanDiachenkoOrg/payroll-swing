-- Acme Payroll schema (H2)

CREATE TABLE department (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    cost_center  VARCHAR(20),
    CONSTRAINT uq_department_name UNIQUE (name)
);

CREATE TABLE employee (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_no     VARCHAR(20)  NOT NULL,
    first_name      VARCHAR(60)  NOT NULL,
    last_name       VARCHAR(60)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    phone           VARCHAR(30),
    department_id   BIGINT       NOT NULL,
    job_title       VARCHAR(100),
    pay_type        VARCHAR(10)  NOT NULL,
    annual_salary   DECIMAL(12,2),
    hourly_rate     DECIMAL(8,2),
    hire_date       DATE         NOT NULL,
    active          BOOLEAN      DEFAULT TRUE  NOT NULL,
    pension_opt_in  BOOLEAN      DEFAULT FALSE NOT NULL,
    CONSTRAINT uq_employee_email UNIQUE (email),
    CONSTRAINT uq_employee_no UNIQUE (employee_no),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department(id)
);

CREATE TABLE salary_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    effective_date  DATE   NOT NULL,
    annual_salary   DECIMAL(12,2),
    hourly_rate     DECIMAL(8,2),
    reason          VARCHAR(200),
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE deduction_type (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    default_amount  DECIMAL(10,2) DEFAULT 0 NOT NULL,
    CONSTRAINT uq_deduction_code UNIQUE (code)
);

CREATE TABLE employee_deduction (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id        BIGINT NOT NULL,
    deduction_type_id  BIGINT NOT NULL,
    amount             DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_ed_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT fk_ed_type FOREIGN KEY (deduction_type_id) REFERENCES deduction_type(id)
);

CREATE TABLE timesheet (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id    BIGINT NOT NULL,
    week_start     DATE   NOT NULL,
    hours_mon      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_tue      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_wed      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_thu      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_fri      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_sat      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    hours_sun      DECIMAL(4,2) DEFAULT 0 NOT NULL,
    status         VARCHAR(12)  NOT NULL,
    reject_reason  VARCHAR(255),
    submitted_at   TIMESTAMP,
    decided_by     VARCHAR(120),
    decided_at     TIMESTAMP,
    CONSTRAINT uq_timesheet_week UNIQUE (employee_id, week_start),
    CONSTRAINT fk_timesheet_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE pay_run (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_start  DATE        NOT NULL,
    period_end    DATE        NOT NULL,
    pay_date      DATE        NOT NULL,
    status        VARCHAR(12) NOT NULL,
    created_by    VARCHAR(120),
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    approved_by   VARCHAR(120),
    paid_at       TIMESTAMP
);

CREATE TABLE payslip (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    pay_run_id        BIGINT NOT NULL,
    employee_id       BIGINT NOT NULL,
    regular_hours     DECIMAL(6,2) DEFAULT 0 NOT NULL,
    overtime_hours    DECIMAL(6,2) DEFAULT 0 NOT NULL,
    gross             DECIMAL(12,2) NOT NULL,
    income_tax        DECIMAL(12,2) NOT NULL,
    social            DECIMAL(12,2) NOT NULL,
    pension           DECIMAL(12,2) NOT NULL,
    other_deductions  DECIMAL(12,2) NOT NULL,
    net               DECIMAL(12,2) NOT NULL,
    CONSTRAINT uq_payslip UNIQUE (pay_run_id, employee_id),
    CONSTRAINT fk_payslip_run FOREIGN KEY (pay_run_id) REFERENCES pay_run(id),
    CONSTRAINT fk_payslip_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE app_user (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    email          VARCHAR(120) NOT NULL,
    password_hash  VARCHAR(200) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    employee_id    BIGINT,
    department_id  BIGINT,
    active         BOOLEAN DEFAULT TRUE NOT NULL,
    last_login     TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ts          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_email  VARCHAR(120),
    action      VARCHAR(40) NOT NULL,
    entity      VARCHAR(40),
    entity_id   BIGINT,
    details     VARCHAR(500)
);

CREATE TABLE tax_bracket (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    lower_bound  DECIMAL(12,2) NOT NULL,
    upper_bound  DECIMAL(12,2),
    rate         DECIMAL(5,2)  NOT NULL
);

CREATE TABLE app_setting (
    setting_key    VARCHAR(60) PRIMARY KEY,
    setting_value  VARCHAR(200)
);

CREATE INDEX ix_employee_department ON employee(department_id);
CREATE INDEX ix_timesheet_status ON timesheet(status);
CREATE INDEX ix_payslip_employee ON payslip(employee_id);
CREATE INDEX ix_audit_ts ON audit_log(ts);
