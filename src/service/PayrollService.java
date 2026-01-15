package service;

import dao.ChildDAO;
import dao.SettingsDAO;
import model.Contract;
import model.Employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

/**
 * Payroll Service - Calculates salaries based on business rules
 * 
 * Salary Calculation Rules:
 * - Permanent Admin (PA): Base Salary + 15% per year (after 1st) + Family
 * Allowance
 * - Contract Admin (CA): Contract Salary + Family Allowance
 * - Permanent Teaching (PT): Base Salary + 15% per year (after 1st) + Family
 * Allowance + Research Allowance
 * - Contract Teaching (CT): Contract Salary + Family Allowance + Library
 * Allowance
 * 
 * Family Allowance:
 * 0 for Single
 * +5% of Base Salary for spouse (if married)
 * +5% of Base Salary per minor child (<18 years)
 */
public class PayrollService {

    private SettingsDAO settingsDAO;
    private ChildDAO childDAO;
    private Map<String, Double> config;

    public PayrollService() {
        settingsDAO = new SettingsDAO();
        childDAO = new ChildDAO();
        try {
            config = settingsDAO.getAllSettings();
            if (config.isEmpty()) {
                System.err.println("Warning: No settings found in database. Using default values.");
                config = getDefaultSettings();
            }
        } catch (SQLException e) {
            System.err.println("Error loading settings from database: " + e.getMessage());
            e.printStackTrace();
            config = getDefaultSettings();
        }
    }

    /** Get default settings if database is empty or unavailable */
    private Map<String, Double> getDefaultSettings() {
        Map<String, Double> defaults = new HashMap<>();
        defaults.put("BASE_SALARY_PA", 1000.00);
        defaults.put("BASE_SALARY_PT", 1200.00);
        defaults.put("RESEARCH_ALLOWANCE", 300.00);
        defaults.put("LIBRARY_ALLOWANCE", 100.00);
        defaults.put("EXPERIENCE_RATE", 0.15);
        defaults.put("SPOUSE_ALLOWANCE_RATE", 0.05);
        defaults.put("CHILD_ALLOWANCE_RATE", 0.05);
        return defaults;
    }

    private double getCfg(String key) {
        return config.getOrDefault(key, 0.0);
    }

    public PayrollResult calculateSalary(Employee emp, LocalDate paymentDate, Contract activeContract)
            throws SQLException {
        double baseSalary = 0.0;
        double experienceAllowance = 0.0;
        double familyAllowance = 0.0;
        double researchAllowance = 0.0;
        double libraryAllowance = 0.0;

        // Base Salary
        switch (emp.getType()) {
            case PA:
                baseSalary = getCfg("BASE_SALARY_PA");
                break;
            case PT:
                baseSalary = getCfg("BASE_SALARY_PT");
                break;
            case CA:
            case CT:
                if (activeContract != null && activeContract.isValid(paymentDate)) {
                    baseSalary = activeContract.getGrossSalary();
                } else {
                    return new PayrollResult(emp.getId(), paymentDate, 0, 0, 0, 0, 0, 0);
                }
                break;
        }

        // 15% increase for every year of service AFTER the first year
        if (emp.getType() == Employee.EmployeeType.PA || emp.getType() == Employee.EmployeeType.PT) {
            int years = Period.between(emp.getStartDate(), paymentDate).getYears();
            if (years > 1) {
                // Calculate years after the first (years - 1)
                double rate = getCfg("EXPERIENCE_RATE"); // Should be 0.15 (15%)
                experienceAllowance = baseSalary * rate * (years - 1);
            }
        }

        // Family Allowance 0 for Single, 5% if married +5% per child (<18)
        double familyRate = 0.0;

        if (emp.isMarried()) {
            familyRate += getCfg("SPOUSE_ALLOWANCE_RATE");
        }

        // Count children
        int minorChildrenCount = childDAO.countMinorChildren(emp.getId(), paymentDate);
        familyRate += (minorChildrenCount * getCfg("CHILD_ALLOWANCE_RATE")); // +5% per minor child

        familyAllowance = baseSalary * familyRate;

        if (emp.getType() == Employee.EmployeeType.PT) {
            researchAllowance = getCfg("RESEARCH_ALLOWANCE");
        }
        if (emp.getType() == Employee.EmployeeType.CT) {
            libraryAllowance = getCfg("LIBRARY_ALLOWANCE");
        }

        double total = baseSalary + experienceAllowance + familyAllowance + researchAllowance + libraryAllowance;

        return new PayrollResult(emp.getId(), paymentDate, baseSalary, familyAllowance, experienceAllowance,
                researchAllowance,
                libraryAllowance, total);
    }

    // Inner class to hold result
    public static class PayrollResult {
        public int empId;
        public LocalDate date;
        public double base;
        public double family;
        public double experience;
        public double research;
        public double library;
        public double total;

        public PayrollResult(int empId, LocalDate date, double base, double family, double experience, double research,
                double library, double total) {
            this.empId = empId;
            this.date = date;
            this.base = base;
            this.family = family;
            this.experience = experience;
            this.research = research;
            this.library = library;
            this.total = total;
        }
    }
}
