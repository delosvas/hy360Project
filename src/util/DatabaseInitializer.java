package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Initializer 
 * 
 * Utility class that creates database and tables if they don't exist 
 */
public class DatabaseInitializer {

        private static final String DB_URL_NO_DB = "jdbc:mysql://localhost:3306?serverTimezone=UTC";
        private static final String DB_URL = "jdbc:mysql://localhost:3306/university_payroll?serverTimezone=UTC";
        private static final String USER = "root";
        private static final String PASSWORD = "1234";

        /*
         * Initializes the database schema and default data.
         */
        public static void initialize() {
                try {
                        // Create database if it doesn't exist
                        try (Connection conn = DriverManager.getConnection(DB_URL_NO_DB, USER, PASSWORD);
                                        Statement stmt = conn.createStatement()) {

                                System.out.println("Creating database if it doesn't exist...");
                                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS university_payroll");
                                System.out.println("Database 'university_payroll' ready.");
                        }

                        // Create tables and insert default data
                        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                                        Statement stmt = conn.createStatement()) {

                                System.out.println("Creating tables...");

                                // Departments table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS departments (" +
                                                                "dept_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                                "name VARCHAR(100) NOT NULL UNIQUE, " +
                                                                "INDEX idx_name (name)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                                // Employees table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS employees (" +
                                                                "emp_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                                "full_name VARCHAR(100) NOT NULL, " +
                                                                "emp_type ENUM('PA', 'CA', 'PT', 'CT') NOT NULL, " +
                                                                "dept_id INT, " +
                                                                "is_married BOOLEAN DEFAULT FALSE, " +
                                                                "address VARCHAR(255), " +
                                                                "phone VARCHAR(20), " +
                                                                "iban VARCHAR(34), " +
                                                                "bank_name VARCHAR(50), " +
                                                                "start_date DATE NOT NULL, " +
                                                                "is_active BOOLEAN DEFAULT TRUE, " +
                                                                "FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL, "
                                                                +
                                                                "INDEX idx_emp_type (emp_type), " +
                                                                "INDEX idx_dept (dept_id), " +
                                                                "INDEX idx_active (is_active), " +
                                                                "INDEX idx_start_date (start_date)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                                // Children table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS children (" +
                                                                "child_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                                "emp_id INT NOT NULL, " +
                                                                "birth_date DATE NOT NULL, " +
                                                                "FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE, "
                                                                +
                                                                "UNIQUE KEY unique_child (emp_id, birth_date), " +
                                                                "INDEX idx_emp_id (emp_id), " +
                                                                "INDEX idx_birth_date (birth_date)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                                // Contracts table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS contracts (" +
                                                                "contract_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                                "emp_id INT NOT NULL, " +
                                                                "start_date DATE NOT NULL, " +
                                                                "end_date DATE NOT NULL, " +
                                                                "gross_salary DECIMAL(10, 2) NOT NULL, " +
                                                                "FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE, "
                                                                +
                                                                "INDEX idx_emp_id (emp_id), " +
                                                                "INDEX idx_dates (start_date, end_date)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                                // Payroll log table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS payroll_log (" +
                                                                "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                                "emp_id INT NOT NULL, " +
                                                                "payment_date DATE NOT NULL, " +
                                                                "base_salary DECIMAL(10, 2) DEFAULT 0.00, " +
                                                                "family_allowance DECIMAL(10, 2) DEFAULT 0.00, " +
                                                                "experience_allowance DECIMAL(10, 2) DEFAULT 0.00, " +
                                                                "research_allowance DECIMAL(10, 2) DEFAULT 0.00, " +
                                                                "library_allowance DECIMAL(10, 2) DEFAULT 0.00, " +
                                                                "total_amount DECIMAL(10, 2) NOT NULL, " +
                                                                "FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE RESTRICT, "
                                                                +
                                                                "INDEX idx_emp_id (emp_id), " +
                                                                "INDEX idx_payment_date (payment_date)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                                // System Settings table
                                stmt.executeUpdate(
                                                "CREATE TABLE IF NOT EXISTS system_settings (" +
                                                                "config_key VARCHAR(50) PRIMARY KEY, " +
                                                                "config_value DECIMAL(10, 2) NOT NULL, " +
                                                                "description VARCHAR(255)" +
                                                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                                System.out.println("Tables created successfully.");
                                System.out.println("Inserting default data...");

                                // Default Departments
                                stmt.executeUpdate(
                                                "INSERT IGNORE INTO departments (name) VALUES " +
                                                                "('Computer Science'), ('Physics'), ('Mathematics'), " +
                                                                "('Chemistry'), ('Biology')");

                                // Default System settings
                                stmt.executeUpdate(
                                                "INSERT IGNORE INTO system_settings (config_key, config_value, description) VALUES "
                                                                +
                                                                "('BASE_SALARY_PA', 1000.00, 'Base salary for Permanent Admin'), "
                                                                +
                                                                "('BASE_SALARY_PT', 1200.00, 'Base salary for Permanent Teaching'), "
                                                                +
                                                                "('RESEARCH_ALLOWANCE', 300.00, 'Research allowance for Permanent Teaching'), "
                                                                +
                                                                "('LIBRARY_ALLOWANCE', 100.00, 'Library allowance for Contract Teaching'), "
                                                                +
                                                                "('EXPERIENCE_RATE', 0.15, '15% increase per year of service (after 1st year)'), "
                                                                +
                                                                "('SPOUSE_ALLOWANCE_RATE', 0.05, '5% of base salary for spouse'), "
                                                                +
                                                                "('CHILD_ALLOWANCE_RATE', 0.05, '5% of base salary per minor child (<18)')");

                                // Create views for bonus!
                                System.out.println("Creating views...");

                                // View 1: Active Employees
                                stmt.executeUpdate(
                                                "CREATE OR REPLACE VIEW view_active_employees AS " +
                                                                "SELECT e.emp_id, e.full_name, e.emp_type, d.name AS department, "
                                                                +
                                                                "e.start_date, e.is_married, " +
                                                                "(SELECT COUNT(*) FROM children WHERE emp_id = e.emp_id) AS child_count "
                                                                +
                                                                "FROM employees e " +
                                                                "LEFT JOIN departments d ON e.dept_id = d.dept_id " +
                                                                "WHERE e.is_active = TRUE " +
                                                                "ORDER BY e.emp_type, e.full_name");

                                // View 2: Contract Renewal Status
                                stmt.executeUpdate(
                                                "CREATE OR REPLACE VIEW view_contract_renewal_status AS "
                                                			+ "SELECT "
                                                			+ "	e.emp_id,"
                                                			+ "	e.full_name,"
                                                			+ "	d.name AS department,"
                                                			+ "	c.contract_id,"
                                                			+ "	c.start_date,"
                                                			+ "	c.end_date,"
                                                			+ "	CASE "
                                                			+ "		WHEN DATEDIFF(c.end_date, CURDATE())<=30 THEN \"URGENT: Less than a month left\""
                                                			+ "		WHEN DATEDIFF(c.end_date, CURDATE())<=60 THEN \"RENEWAL IS NEEDED\""
                                                			+ "		ELSE 'OK'"
                                                			+ "	END AS renewal_message"
                                                			+ "	FROM employees e "
                                                			+ "	JOIN contracts c ON e.emp_id=c.emp_id"
                                                			+ "	JOIN departments d ON d.dept_id=e.dept_id"
                                                			+ "	WHERE e.is_active = TRUE"
                                                			+ "		AND (e.emp_type='CA' OR e.emp_type='CT')"
                                                			+ "	    AND CURDATE()<=c.end_date;");

                                // View 3: Employee Full Details
                                stmt.executeUpdate(
                                                "CREATE OR REPLACE VIEW view_employee_full_details AS " +
                                                                "SELECT e.emp_id, e.full_name, e.emp_type, d.name AS department_name, "
                                                                +
                                                                "e.is_married, COUNT(c.child_id) AS total_children, " +
                                                                "COUNT(CASE WHEN DATEDIFF(CURDATE(), c.birth_date) / 365.25 < 18 THEN 1 END) AS minor_children, "
                                                                +
                                                                "e.address, e.phone, e.iban, e.bank_name, e.start_date, e.is_active, "
                                                                +
                                                                "CASE WHEN e.emp_type IN ('CA', 'CT') THEN " +
                                                                "(SELECT COUNT(*) FROM contracts WHERE emp_id = e.emp_id "
                                                                +
                                                                "AND CURDATE() BETWEEN start_date AND end_date) ELSE 0 END AS has_active_contract "
                                                                +
                                                                "FROM employees e " +
                                                                "LEFT JOIN departments d ON e.dept_id = d.dept_id " +
                                                                "LEFT JOIN children c ON e.emp_id = c.emp_id " +
                                                                "GROUP BY e.emp_id, e.full_name, e.emp_type, d.name, e.is_married, "
                                                                +
                                                                "e.address, e.phone, e.iban, e.bank_name, e.start_date, e.is_active");

                                System.out.println("Views created successfully.");
                                System.out.println("Database initialized successfully!");

                        } catch (SQLException e) {
                                if (e.getMessage().contains("CHECK") || e.getMessage().contains("constraint")) {
                                        System.out.println(
                                                        "Warning: Some CHECK constraints may not be supported by your MySQL version.");
                                        System.out.println(
                                                        "Tables created, but date validations should be handled in application code.");
                                } else {
                                        throw e;
                                }
                        }

                } catch (SQLException e) {
                        System.err.println("Database initialization error: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(
                                        "Failed to initialize database. Please check MySQL connection and credentials.",
                                        e);
                }
        }
}
