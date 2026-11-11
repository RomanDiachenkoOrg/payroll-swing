-- Reference and sample data loaded on first start

INSERT INTO department (name, cost_center) VALUES ('Engineering', 'CC-100');
INSERT INTO department (name, cost_center) VALUES ('Finance', 'CC-200');
INSERT INTO department (name, cost_center) VALUES ('Sales', 'CC-300');
INSERT INTO department (name, cost_center) VALUES ('Operations', 'CC-400');
INSERT INTO department (name, cost_center) VALUES ('HR', 'CC-500');

INSERT INTO employee (employee_no, first_name, last_name, email, phone, department_id, job_title, pay_type, annual_salary, hourly_rate, hire_date, active, pension_opt_in) VALUES
('E1001', 'Daniel',  'Moreau',    'daniel.moreau@acme.test',    '555-0101', 1, 'Engineering Manager',          'SALARIED', 132000.00, NULL,  DATE '2015-03-02', TRUE,  TRUE),
('E1002', 'Priya',   'Raman',     'priya.raman@acme.test',      '555-0102', 1, 'Senior Software Engineer',     'SALARIED', 118000.00, NULL,  DATE '2017-06-12', TRUE,  TRUE),
('E1003', 'Tom',     'Becker',    'tom.becker@acme.test',       '555-0103', 1, 'Software Engineer',            'SALARIED',  92000.00, NULL,  DATE '2020-01-06', TRUE,  FALSE),
('E1004', 'Aisha',   'Khan',      'aisha.khan@acme.test',       '555-0104', 1, 'DevOps Engineer',              'SALARIED',  98000.00, NULL,  DATE '2019-09-16', TRUE,  TRUE),
('E1005', 'Lukas',   'Novak',     'lukas.novak@acme.test',      '555-0105', 1, 'Junior Developer (part-time)', 'HOURLY',        NULL, 34.00, DATE '2023-07-03', TRUE,  FALSE),
('E1006', 'Maria',   'Silva',     'maria.silva@acme.test',      '555-0106', 1, 'Software Engineer',            'SALARIED',  95500.00, NULL,  DATE '2018-11-19', FALSE, TRUE),
('E1007', 'Kenji',   'Watanabe',  'kenji.watanabe@acme.test',   '555-0107', 1, 'Contract Frontend Developer',  'HOURLY',        NULL, 52.00, DATE '2021-04-12', TRUE,  FALSE),
('E2001', 'Helen',   'Carter',    'helen.carter@acme.test',     '555-0201', 2, 'Finance Director',             'SALARIED', 125000.00, NULL,  DATE '2014-02-03', TRUE,  TRUE),
('E2002', 'Omar',    'Haddad',    'omar.haddad@acme.test',      '555-0202', 2, 'Senior Accountant',            'SALARIED',  78000.00, NULL,  DATE '2016-08-22', TRUE,  TRUE),
('E2003', 'Julia',   'Brandt',    'julia.brandt@acme.test',     '555-0203', 2, 'Payroll Specialist',           'SALARIED',  61000.00, NULL,  DATE '2019-05-13', TRUE,  FALSE),
('E2004', 'Victor',  'Lindqvist', 'victor.lindqvist@acme.test', '555-0204', 2, 'Accounts Clerk',               'HOURLY',        NULL, 24.50, DATE '2022-03-07', TRUE,  FALSE),
('E3001', 'Natalie', 'Brooks',    'natalie.brooks@acme.test',   '555-0301', 3, 'Sales Director',               'SALARIED', 115000.00, NULL,  DATE '2016-01-11', TRUE,  TRUE),
('E3002', 'Diego',   'Alvarez',   'diego.alvarez@acme.test',    '555-0302', 3, 'Account Executive',            'SALARIED',  72000.00, NULL,  DATE '2018-04-09', TRUE,  FALSE),
('E3003', 'Sophie',  'Laurent',   'sophie.laurent@acme.test',   '555-0303', 3, 'Account Executive',            'SALARIED',  70000.00, NULL,  DATE '2020-10-05', TRUE,  TRUE),
('E3004', 'Ben',     'Okafor',    'ben.okafor@acme.test',       '555-0304', 3, 'Sales Associate',              'HOURLY',        NULL, 21.00, DATE '2023-02-13', TRUE,  FALSE),
('E3005', 'Emily',   'Zhang',     'emily.zhang@acme.test',      '555-0305', 3, 'Sales Associate',              'HOURLY',        NULL, 21.00, DATE '2021-06-21', FALSE, FALSE),
('E4001', 'Marcus',  'Reed',      'marcus.reed@acme.test',      '555-0401', 4, 'Operations Manager',           'SALARIED',  98000.00, NULL,  DATE '2015-09-14', TRUE,  TRUE),
('E4002', 'Ana',     'Petrovic',  'ana.petrovic@acme.test',     '555-0402', 4, 'Warehouse Supervisor',         'HOURLY',        NULL, 28.00, DATE '2017-12-04', TRUE,  TRUE),
('E4003', 'Jamal',   'Wright',    'jamal.wright@acme.test',     '555-0403', 4, 'Warehouse Associate',          'HOURLY',        NULL, 19.50, DATE '2022-08-15', TRUE,  FALSE),
('E4004', 'Olga',    'Ivanova',   'olga.ivanova@acme.test',     '555-0404', 4, 'Logistics Coordinator',        'HOURLY',        NULL, 23.75, DATE '2020-03-23', TRUE,  TRUE),
('E4005', 'Peter',   'Hall',      'peter.hall@acme.test',       '555-0405', 4, 'Warehouse Associate',          'HOURLY',        NULL, 19.50, DATE '2024-01-08', FALSE, FALSE),
('E5001', 'Claire',  'Dubois',    'claire.dubois@acme.test',    '555-0501', 5, 'HR Manager',                   'SALARIED',  96000.00, NULL,  DATE '2016-05-30', TRUE,  TRUE),
('E5002', 'Samuel',  'Adeyemi',   'samuel.adeyemi@acme.test',   '555-0502', 5, 'HR Generalist',                'SALARIED',  67000.00, NULL,  DATE '2019-02-18', TRUE,  FALSE),
('E5003', 'Hannah',  'Fischer',   'hannah.fischer@acme.test',   '555-0503', 5, 'Recruiter',                    'SALARIED',  63000.00, NULL,  DATE '2021-09-06', TRUE,  TRUE),
('E5004', 'Ivan',    'Kowalski',  'ivan.kowalski@acme.test',    '555-0504', 5, 'HR Assistant',                 'HOURLY',        NULL, 20.00, DATE '2024-06-03', TRUE,  FALSE);

-- salary history: starting rate for long-tenured staff, then the 2025 review
INSERT INTO salary_history (employee_id, effective_date, annual_salary, hourly_rate, reason)
    SELECT id, hire_date, ROUND(annual_salary * 0.88, 0), ROUND(hourly_rate * 0.9, 2), 'Starting salary'
    FROM employee WHERE hire_date < DATE '2023-01-01' ORDER BY id;
INSERT INTO salary_history (employee_id, effective_date, annual_salary, hourly_rate, reason)
    SELECT id, DATE '2025-01-01', annual_salary, hourly_rate, 'Annual review 2025'
    FROM employee WHERE hire_date < DATE '2023-01-01' ORDER BY id;
INSERT INTO salary_history (employee_id, effective_date, annual_salary, hourly_rate, reason)
    SELECT id, hire_date, annual_salary, hourly_rate, 'Starting salary'
    FROM employee WHERE hire_date >= DATE '2023-01-01' ORDER BY id;

INSERT INTO deduction_type (code, name, default_amount) VALUES ('HEALTH', 'Health insurance', 120.00);
INSERT INTO deduction_type (code, name, default_amount) VALUES ('DENTAL', 'Dental plan', 25.00);
INSERT INTO deduction_type (code, name, default_amount) VALUES ('UNION', 'Union dues', 18.00);
INSERT INTO deduction_type (code, name, default_amount) VALUES ('PARKING', 'Parking permit', 40.00);

INSERT INTO employee_deduction (employee_id, deduction_type_id, amount) VALUES
(1, 1, 120.00), (1, 2, 25.00), (2, 1, 120.00), (3, 1, 120.00), (4, 1, 120.00), (4, 4, 40.00),
(7, 4, 40.00), (8, 1, 120.00), (8, 2, 25.00), (9, 1, 120.00), (10, 1, 120.00),
(12, 1, 120.00), (12, 4, 40.00), (13, 1, 120.00), (14, 2, 25.00), (17, 1, 120.00),
(18, 3, 18.00), (19, 3, 18.00), (20, 3, 18.00), (20, 1, 120.00), (22, 1, 120.00),
(23, 1, 120.00), (24, 2, 25.00);

INSERT INTO tax_bracket (lower_bound, upper_bound, rate) VALUES (0.00, 1000.00, 0.00);
INSERT INTO tax_bracket (lower_bound, upper_bound, rate) VALUES (1000.00, 4000.00, 15.00);
INSERT INTO tax_bracket (lower_bound, upper_bound, rate) VALUES (4000.00, NULL, 25.00);

INSERT INTO app_setting (setting_key, setting_value) VALUES ('company.name', 'Acme Corporation');
INSERT INTO app_setting (setting_key, setting_value) VALUES ('currency', 'USD');
INSERT INTO app_setting (setting_key, setting_value) VALUES ('rate.social', '5.00');
INSERT INTO app_setting (setting_key, setting_value) VALUES ('rate.pension', '3.00');
