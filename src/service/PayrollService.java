package service;

import dao.ChildDAO;
import dao.PayrollDAO;
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
    private PayrollDAO payrollDAO;
    private Map<String, Double> config;

    /*
     * Constructor that initializes DAO and loads configuration settings.
     */
    public PayrollService() {
        settingsDAO = new SettingsDAO();
        childDAO = new ChildDAO();
        payrollDAO = new PayrollDAO();
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

    /*
     * Helper method to retrieve safely the configuration values
     */
    private double getCfg(String key) {
        return config.getOrDefault(key, 0.0);
    }

    /*
     * Main salary calculation method
     * 
     * @param emp            The employee whose salary is being calculated
     * @param paymentDate    The payroll date
     * @param activeContract The employee's active contract (if applicable)
     *
     * @return PayrollResult containing all salary components
     *
     */
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
            	// checking if the contract employees have a solid contract
                if (activeContract != null && activeContract.isValid(paymentDate)) {
                    baseSalary = activeContract.getGrossSalary();
                } else {
                    return new PayrollResult();
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

        // Additional allowances for the teaching staff
        if (emp.getType() == Employee.EmployeeType.PT) {
            researchAllowance = getCfg("RESEARCH_ALLOWANCE");
        }
        if (emp.getType() == Employee.EmployeeType.CT) {
            libraryAllowance = getCfg("LIBRARY_ALLOWANCE");
        }

        // Total Salary 
        double total = baseSalary + experienceAllowance + familyAllowance + researchAllowance + libraryAllowance;
        
        // Save the payroll log entry
        PayrollResult res = new PayrollResult();
        res.setEmpId(emp.getId());
        res.setDate(paymentDate);
        res.setbase(baseSalary);
        res.setExperience(experienceAllowance); 
        res.setFamily(familyAllowance);
        res.setLibrary(libraryAllowance);
        res.setResearch(researchAllowance);
        res.setTotal(total);
        
        if(res.getTotal()>0) {
        	payrollDAO.saveLog(res);
        }
        
        return res;
    }

    // Inner class to hold result for a single payroll calculation
    public static class PayrollResult {
        private int empId;
        private LocalDate date;
        private double base;
        private double family;
        private double experience;
        private double research;
        private double library;
        private double total;

        // Constructor
        public PayrollResult() {
        }
        
        // Following setters and getters for each field
        
        public int getEmpId() {
            return empId;
        }

        public void setEmpId(int empId) {
            this.empId = empId;
        }
        
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
        	this.date = date;
        }
        
        public double getBase() {
            return base;
        }

        public void setbase(double base) {
        	this.base = base;
        }
        
        public double getFamily() {
            return family;
        }

        public void setFamily(double family) {
        	this.family = family;
        }
       
        public double getExperience() {
            return experience;
        }

        public void setExperience(double experience) {
        	this.experience = experience;
        }
        
        public double getResearch() {
            return research;
        }

        public void setResearch(double research) {
        	this.research = research;
        }
        
        public double getLibrary() {
            return library;
        }

        public void setLibrary(double library) {
        	this.library = library;
        }
        
        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
        	this.total = total;
        }
    }
}
