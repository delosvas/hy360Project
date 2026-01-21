package model;

import java.time.LocalDate;

/*
 * Contract
 * 
 * Model class that represents a contract-based employment agreement.
 * It stores contract start and ends, gross salary for the contract period
 * and the employee id to whom the contract belongs
 */
public class Contract {
    private int id; // Unique Identifier
    private int employeeId; // Foreign key
    private LocalDate startDate;
    private LocalDate endDate;
    private double grossSalary;
    
    public Contract() {
    }

    /*
     * Constructor for creating a new contract object
     */
    public Contract(int employeeId, LocalDate startDate, LocalDate endDate, double grossSalary) {
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.grossSalary = grossSalary;
    }
    
    // Following setters and Getters for every field

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(double grossSalary) {
        this.grossSalary = grossSalary;
    }

    /*
     * The method checks whether the contract is valid on a given date.
     * 
     * @param date The date to check
     * @return true if date is within [startDate, endDate], false otherwise
     */
    public boolean isValid(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
