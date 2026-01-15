package model;

import util.DateUtils;
import java.time.LocalDate;

public class Child {
    private int childId;
    private int empId;
    private LocalDate birthDate;

    public Child() {
    }

    public Child(int empId, LocalDate birthDate) {
        this.empId = empId;
        this.birthDate = birthDate;
    }

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

    public boolean isMinor(LocalDate date) {
        return java.time.temporal.ChronoUnit.YEARS.between(birthDate, date) < 18;
    }

    public int getAge(LocalDate date) {
        return (int) java.time.temporal.ChronoUnit.YEARS.between(birthDate, date);
    }

    @Override
    public String toString() {
        return "Birth Date: " + DateUtils.formatDate(birthDate);
    }
}
