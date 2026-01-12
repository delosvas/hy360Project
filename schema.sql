-- HY360 Project 2025
-- University Payroll System

CREATE DATABASE IF NOT EXISTS university_payroll;
USE university_payroll;

-- departments table
CREATE TABLE IF NOT EXISTS departments (
    dept_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- main table for employees
-- holds all the shared info for everyone (admin, teaching, etc)
CREATE TABLE IF NOT EXISTS employees (
    emp_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    
    -- PA = Perm Admin, CA = Contract Admin, PT = Perm Teaching, CT = Contract Teaching
    emp_type ENUM('PA', 'CA', 'PT', 'CT') NOT NULL,
    
    dept_id INT,
    
    -- family info
    is_married BOOLEAN DEFAULT FALSE,
    child_count INT DEFAULT 0,
    
    address VARCHAR(255),
    phone VARCHAR(20),
    iban VARCHAR(34),
    bank_name VARCHAR(50),
    
    -- when they started or when the contract started
    start_date DATE NOT NULL, 
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL
);

-- contracts for CA and CT employees
CREATE TABLE IF NOT EXISTS contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    gross_salary DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- history of payments made
CREATE TABLE IF NOT EXISTS payroll_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    payment_date DATE NOT NULL,
    
    -- saved values for the report
    base_salary DECIMAL(10, 2) DEFAULT 0.00,
    family_allowance DECIMAL(10, 2) DEFAULT 0.00,
    experience_allowance DECIMAL(10, 2) DEFAULT 0.00, 
    research_allowance DECIMAL(10, 2) DEFAULT 0.00,   
    library_allowance DECIMAL(10, 2) DEFAULT 0.00,    
    
    total_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
);

-- system configuration to hold salary constants
-- avoiding hardcoding values in java
CREATE TABLE IF NOT EXISTS system_settings (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
);

-- default values
INSERT IGNORE INTO system_settings (config_key, config_value, description) VALUES
('BASE_SALARY_PA', 1000.00, 'Base salary for Permanent Admin'),
('BASE_SALARY_PT', 1200.00, 'Base salary for Permanent Teaching'),
('RESEARCH_ALLOWANCE', 300.00, 'Research allowance'),
('LIBRARY_ALLOWANCE', 100.00, 'Library allowance'),
('EXPERIENCE_RATE', 0.15, '15% increase per year > 1'),
('SPOUSE_ALLOWANCE_RATE', 0.05, '5% of base'),
('CHILD_ALLOWANCE_RATE', 0.05, '5% of base per child');

-- VIEWS ------------------------------------------

-- easy way to see who is currently working here
CREATE OR REPLACE VIEW view_active_employees AS
SELECT 
    e.emp_id, e.full_name, e.emp_type, d.name AS department, 
    e.start_date
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE e.is_active = TRUE;

-- monthly cost report
CREATE OR REPLACE VIEW view_monthly_cost_by_category AS
SELECT 
    e.emp_type,
    SUM(p.total_amount) as total_cost,
    COUNT(DISTINCT p.emp_id) as employee_count,
    AVG(p.total_amount) as average_salary
FROM payroll_log p
JOIN employees e ON p.emp_id = e.emp_id
WHERE MONTH(p.payment_date) = MONTH(CURRENT_DATE()) 
  AND YEAR(p.payment_date) = YEAR(CURRENT_DATE())
GROUP BY e.emp_type;

-- for the payslip generation
CREATE OR REPLACE VIEW view_pay_slip_details AS
SELECT 
    p.payment_date,
    e.full_name,
    e.iban,
    p.base_salary,
    p.family_allowance,
    p.experience_allowance + p.research_allowance + p.library_allowance AS other_allowances,
    p.total_amount
FROM payroll_log p
JOIN employees e ON p.emp_id = e.emp_id;

-- initial depts
INSERT IGNORE INTO departments (name) VALUES ('Computer Science'), ('Physics'), ('Mathematics'), ('Administration');
