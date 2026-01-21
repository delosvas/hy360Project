package model;

import util.DateUtils;
import java.time.LocalDate;

/*
 * Child
 * 
 * Model class for representing a dependent child of an employee.
 * Used for calculating family allowance in payroll.
 */
public class Child {
    private int childId; // Unique Identifier
    private int empId; /// Foreign key 
    private LocalDate birthDate; // Child birth date
    
    public Child() {
    }

    /*
     * Constructor for creating a new child object
     */
    public Child(int empId, LocalDate birthDate) {
        this.empId = empId;
        this.birthDate = birthDate;
    }

    // Following setters and Getters for every field
    
    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    /*
     * Reveals whether a child of an employee is a minor or not based on its
     * birth date
     * 
     * @param date The date on which to check minor status
     * @return true if child is under 18, false otherwise
     */
    public boolean isMinor(LocalDate date) {
        return java.time.temporal.ChronoUnit.YEARS.between(birthDate, date) < 18;
    }

    /*
     * Calculates the age of a child in full years
     * 
     * @param date The reference date
     * @return Age in years
     */
    public int getAge(LocalDate date) {
        return (int) java.time.temporal.ChronoUnit.YEARS.between(birthDate, date);
    }

    /*
     * Returns an expression abut the child's birth date
     */
    @Override
    public String toString() {
        return "Birth Date: " + DateUtils.formatDate(birthDate);
    }
}
