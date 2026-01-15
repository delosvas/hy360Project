package model;

import java.time.LocalDate;

public class Employee {
    public enum EmployeeType {
        PA,
        CA,
        PT,
        CT;
        
        public String getDisplayName() {
            switch (this) {
                case PA: return "Permanent Admin";
                case CA: return "Contract Admin";
                case PT: return "Permanent Teaching";
                case CT: return "Contract Teaching";
                default: return name();
            }
        }
    }

    private int id;
    private String fullName;
    private EmployeeType type;
    private int deptId;
    private boolean isMarried;
    //i removed childCount- use ChildDAO to get children
    private String address;
    private String phone;
    private String iban;
    private String bankName;
    private LocalDate startDate;
    private boolean isActive;

    public Employee() {
    }

    public Employee(String fullName, EmployeeType type, int deptId, boolean isMarried,
            LocalDate startDate) {
        this.fullName = fullName;
        this.type = type;
        this.deptId = deptId;
        this.isMarried = isMarried;
        this.startDate = startDate;
        this.isActive = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public EmployeeType getType() {
        return type;
    }

    public void setType(EmployeeType type) {
        this.type = type;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public boolean isMarried() {
        return isMarried;
    }

    public void setMarried(boolean married) {
        isMarried = married;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return fullName + " (" + type + ")";
    }
}
