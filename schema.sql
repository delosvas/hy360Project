-- Database Schema for University Payroll System

CREATE DATABASE IF NOT EXISTS university_payroll;
USE university_payroll;

-- Departments Table
CREATE TABLE IF NOT EXISTS departments (
    dept_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Employees Table (with disjoint specialization: PA, CA, PT, CT)
CREATE TABLE IF NOT EXISTS employees (
    emp_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    
    -- Disjoint specialization: PA, CA, PT, CT
    -- PA = Permanent Admin, CA = Contract Admin
    -- PT = Permanent Teaching, CT = Contract Teaching
    emp_type ENUM('PA', 'CA', 'PT', 'CT') NOT NULL,
    
    dept_id INT,
    
    -- Family status
    is_married BOOLEAN DEFAULT FALSE,
    
    -- Contact and banking info
    address VARCHAR(255),
    phone VARCHAR(20),
    iban VARCHAR(34),
    bank_name VARCHAR(50),
    
    -- Employment dates
    start_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Foreign Keys
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT chk_start_date_first_of_month 
        CHECK (DAY(start_date) = 1),
    
    -- Indexes
    INDEX idx_emp_type (emp_type),
    INDEX idx_dept (dept_id),
    INDEX idx_active (is_active),
    INDEX idx_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS children (
    child_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    birth_date DATE NOT NULL,
    
    -- Foreign Keys
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE,
    
    -- prevent duplicate children for same employee
    UNIQUE KEY unique_child (emp_id, birth_date),
    
    -- Indexes
    INDEX idx_emp_id (emp_id),
    INDEX idx_birth_date (birth_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- For Contract Admin and Contract Teaching employees
CREATE TABLE IF NOT EXISTS contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    gross_salary DECIMAL(10, 2) NOT NULL CHECK (gross_salary >= 0),
    
    -- Foreign Keys
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_contract_start_first_of_month 
        CHECK (DAY(start_date) = 1),
    CONSTRAINT chk_contract_dates_valid 
        CHECK (end_date >= start_date),
    
    -- Indexes
    INDEX idx_emp_id (emp_id),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- Historical record of all payroll payments

CREATE TABLE IF NOT EXISTS payroll_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    payment_date DATE NOT NULL,
    
    -- Salary breakdown (saved for reporting)
    base_salary DECIMAL(10, 2) DEFAULT 0.00,
    family_allowance DECIMAL(10, 2) DEFAULT 0.00,
    experience_allowance DECIMAL(10, 2) DEFAULT 0.00,
    research_allowance DECIMAL(10, 2) DEFAULT 0.00,
    library_allowance DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    
    -- Foreign Keys
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE RESTRICT,
    
    -- Constraints
    CONSTRAINT chk_payment_date_last_of_month 
        CHECK (DAY(payment_date) = DAY(LAST_DAY(payment_date))),
    
    -- Indexes
    INDEX idx_emp_id (emp_id),
    INDEX idx_payment_date (payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- SYSTEM_SETTINGS TABLE

CREATE TABLE IF NOT EXISTS system_settings (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- departments
INSERT IGNORE INTO departments (name) VALUES 
    ('Computer Science'),
    ('Physics'),
    ('Mathematics'),
    ('Chemistry'),
    ('Biology');

-- System settings
INSERT IGNORE INTO system_settings (config_key, config_value, description) VALUES
    ('BASE_SALARY_PA', 1000.00, 'Base salary for Permanent Admin'),
    ('BASE_SALARY_PT', 1200.00, 'Base salary for Permanent Teaching'),
    ('RESEARCH_ALLOWANCE', 300.00, 'Research allowance for Permanent Teaching'),
    ('LIBRARY_ALLOWANCE', 100.00, 'Library allowance for Contract Teaching'),
    ('EXPERIENCE_RATE', 0.15, '15% increase per year of service (after 1st year)'),
    ('SPOUSE_ALLOWANCE_RATE', 0.05, '5% of base salary for spouse'),
    ('CHILD_ALLOWANCE_RATE', 0.05, '5% of base salary per minor child (<18)');


-- BONUS VIEWS

-- VIEW 1: Employee Full Details
-- Complete employee information
CREATE OR REPLACE VIEW view_employee_full_details AS
SELECT 
    e.emp_id,
    e.full_name,
    e.emp_type,
    d.name AS department_name,
    e.is_married,
    COUNT(c.child_id) AS total_children,
    COUNT(CASE WHEN DATEDIFF(CURDATE(), c.birth_date) / 365.25 < 18 THEN 1 END) AS minor_children,
    e.address,
    e.phone,
    e.iban,
    e.bank_name,
    e.start_date,
    e.is_active,
    CASE 
        WHEN e.emp_type IN ('CA', 'CT') THEN 
            (SELECT COUNT(*) FROM contracts WHERE emp_id = e.emp_id 
             AND CURDATE() BETWEEN start_date AND end_date)
        ELSE 0
    END AS has_active_contract
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
LEFT JOIN children c ON e.emp_id = c.emp_id
GROUP BY e.emp_id, e.full_name, e.emp_type, d.name, e.is_married, 
         e.address, e.phone, e.iban, e.bank_name, e.start_date, e.is_active;

-- View 2 : Active Employees Summary 
CREATE OR REPLACE VIEW view_active_employees AS
SELECT 
    e.emp_id,
    e.full_name,
    e.emp_type,
    d.name AS department,
    e.start_date,
    e.is_married,
    (SELECT COUNT(*) FROM children WHERE emp_id = e.emp_id) AS child_count
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE e.is_active = TRUE
ORDER BY e.emp_type, e.full_name;

-- View 3 : Contract Renewal Status
-- used to alert the system when a new cotract renewal should be made
CREATE OR REPLACE VIEW view_contract_renewal_status AS
SELECT 
	e.emp_id,
	e.full_name,
	d.name AS department,
	c.contract_id,
	c.start_date,
	c.end_date,
	CASE 
		WHEN DATEDIFF(c.end_date, CURDATE())<=30 THEN "URGENT: Less than a month left"
		WHEN DATEDIFF(c.end_date, CURDATE())<=60 THEN "RENEWAL IS NEEDED"
		ELSE 'OK'
	END AS renewal_message
	FROM employees e 
	JOIN contracts c ON e.emp_id=c.emp_id
	JOIN departments d ON d.dept_id=e.dept_id
	WHERE e.is_active = TRUE 
		AND (e.emp_type='CA' OR e.emp_type='CT')
		AND CURDATE()<=c.end_date;
	
-- delete these after execution of MySql / phpMyAdmin
DROP VIEW IF EXISTS view_monthly_cost_by_category;
DROP VIEW IF EXISTS view_payroll_statistics;
DROP VIEW IF EXISTS view_monthly_payroll_analysis;
DROP VIEW IF EXISTS view_pay_slip_details;



