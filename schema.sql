-- HY360 PROJECT 2025/26: UNIVERSITY PAYROLL

--creating database
CREATE DATABASE IF NOT EXISTS UNI_PAYROLL;
USE UNI_PAYROLL;

--employee table 
CREATE TABLE IF NOT EXISTS EMPLOYEE(
    EmpID INTEGER NOT NULL AUTO_INCREMENT,
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    Address VARCHAR(50),
    IBAN VARCHAR(100),
    BankName VARCHAR(50),
    MaritalStatus ENUM('Married', 'Single', 'with kid/kids'),
    StaffCategory ENUM('Academic', 'Administrative')
    Department VARCHAR(50),
    IsActive BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (EmpID),
);

--table for phones 
CREATE TABLE IF NOT EXISTS PHONE(
    EmpID INTEGER, 
    PhoneID VARCHAR(50) NOT NULL,
    CONSTRAINT CON_PHONE PRIMARY KEY (EmpID,PhoneID), 
    FOREIGN KEY (EmpID) REFERENCES EMPLOYEE(EmpID)
);

--table for weak entity CHILD in case the employee has childen
CREATE TABLE IF NOT EXISTS CHILD(
    ChildID INTEGER NOT NULL AUTO_INCREMENT,
    BirthDate DATE,
    EmpID INTEGER,
    CONSTRAINT CON_CHILD PRIMARY KEY (EmpID,ChildID),
    FOREIGN KEY (EmpID) REFERENCES EMPLOYEE(EmpID),
    CHECK (DATE_SUB(CURRENT_DATE(), INTERVAL 18 YEAR)<BirthDate)
);

--table for the payroll
CREATE TABLE IF NOT EXISTS PAYROLL(
    PayrollID INTEGER NOT NULL, 
    PaymentDate DATE,
    EmpID INTEGER,
    PaymentDate DATE,
    BaseSalary DECIMAL(10,2) DEFAULT 0.00,
    FamilyAllowance DECIMAL(8,2) DEFAULT 0.00,
    LibraryAlloance DECIMAL(8,2) DEFAULT 0.00,
    ResearchAllowance DECIMAL(10,2) DEFAULT 0.00,
    YearlyBonus DECIMAL(10,2) DEFAULT 0.00,
    TotalAmount DECIMAL(10,2) DEFAULT 0.00,
    CONSTRAINT FOREIGN KEY (EmpID) REFERENCES EMPLOYEE(EmpID),
);

--table for emplyoee working permanently in the university
CREATE TABLE IF NOT EXISTS PERMANENT_EMPLOYEE(
    EmpID INTEGER,
    HireDate DATE,
    CONSTRAINT FOREIGN KEY (EmpID) REFERENCES EMPLOYEE(EmpID),
);

--table for emplyoee with a contract
CREATE TABLE IF NOT EXISTS CONTRACT_EMPLOYEE(
    EmpID INTEGER,
    ContractStart DATE,
    ContractEnd DATE,
    ContractSalary DECIMAL(10,2) DEFAULT 0.00,,
    CONSTRAINT FOREIGN KEY (EmpID) REFERENCES EMPLOYEE(EmpID),
);

--system setting table
CREATE TABLE IF NOT EXISTS SYSTEM_SETTINGS(
    SettingKey VARCHAR(50),
    SettingValue DECIMAL(,2)
);

INSERT INTO SYSTEM_SETTINGS (SettingKey, SettingValue) VALUES
('BASE_SALARY', );
('FAMILY_ALLOWANCE', );
('LIBRARY_ALLOWANCE', );
('RESEARCH_ALLOWANCE', );
('YEARLY_BONUS', 0.15);

-- VIEWS