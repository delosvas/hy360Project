package model;

import java.time.LocalDate;

public class Contract {
    private int id;
    private int employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double grossSalary;

    public Contract() {
    }

    public Contract(int employeeId, LocalDate startDate, LocalDate endDate, double grossSalary) {
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.grossSalary = grossSalary;
    }

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

    public boolean isValid(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
