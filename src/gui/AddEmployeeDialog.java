package gui;

import dao.ChildDAO;
import dao.ContractDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import model.Child;
import model.Contract;
import model.Department;
import model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/*
 * AddEmployeeDialog
 * 
 * This class is all about the modal dialog window used for hiring a new 
 * employee and inserting into the payroll system.
 * 
 * It's responsible for:
 * 1. displaying and validating personal info 
 * 2. loading available departments from the database and allow assignment
 * 3. manage marital status and optional children for family allowance
 * 4. enforce payroll policies
 * 5. the new employee and it's declared children to the DB 
 * 
 */
public class AddEmployeeDialog extends JDialog {
    private boolean isSaved = false;
    private EmployeeDAO employeeDAO;
    private ContractDAO contractDAO;
    private DepartmentDAO departmentDAO;
    private ChildDAO childDAO;

    // Fields
    private JTextField txtName = new JTextField(20);
    private JComboBox<Employee.EmployeeType> cmbType = new JComboBox<>(Employee.EmployeeType.values());
    private JComboBox<Department> cmbDept = new JComboBox<>();
    private JCheckBox chkMarried = new JCheckBox("Married");
    private JTextField txtAddress = new JTextField(20);
    private JTextField txtPhone = new JTextField(15);
    private JTextField txtIban = new JTextField(20);
    private JTextField txtBank = new JTextField(15);
    private JTextField txtStartDate = new JTextField(LocalDate.now().toString(), 10);

    // Child management
    private DefaultTableModel childTableModel;
    private JTable childTable;
    private List<Child> tempChildrenList = new ArrayList<>();

    // Contract fields
    private JPanel contractPanel;
    private JTextField txtContractEnd = new JTextField(10);
    private JTextField txtContractAmount = new JTextField(10);

    /*
     * Constructor for this class that builds the dialog and is
     * responsible for the DAOs' initialization
     * 
     * @param owner
     */
    public AddEmployeeDialog(Window owner) {
        super(owner, "Hire New Employee", ModalityType.APPLICATION_MODAL);

        // DAOs' initialization
        employeeDAO = new EmployeeDAO();
        contractDAO = new ContractDAO();
        departmentDAO = new DepartmentDAO();
        childDAO = new ChildDAO();

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(); // Main panel for sections
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Employee details panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Personal & Employment Details"));
        formPanel.setBackground(new Color(250, 250, 250));

        // Name
        formPanel.add(new JLabel("Full Name (*):") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtName.setToolTipText("Required. The full name of the employee.");
        formPanel.add(txtName);

        // Type
        formPanel.add(new JLabel("Employee Type:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(cmbType);

        // Department
        formPanel.add(new JLabel("Department (*):") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        loadDepartments();
        formPanel.add(cmbDept);

        // Marital Status
        formPanel.add(new JLabel("Marital Status:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(chkMarried);

        // Address
        formPanel.add(new JLabel("Address:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(txtAddress);

        // Phone
        formPanel.add(new JLabel("Phone:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(txtPhone);

        // IBAN
        formPanel.add(new JLabel("IBAN:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(txtIban);

        // Bank Name
        formPanel.add(new JLabel("Bank Name:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        formPanel.add(txtBank);

        // Start date field which is not editable and auto-filled
        formPanel.add(new JLabel("Start Date (YYYY-MM-DD) (*):") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtStartDate.setToolTipText("Format: YYYY-MM-DD. Used for Experience Allowance calculation.");
        txtStartDate.setEditable(false);
        txtStartDate.setFocusable(false);
        formPanel.add(txtStartDate);

        mainPanel.add(formPanel);

        // Children panel
        JPanel childrenPanel = new JPanel(new BorderLayout());
        childrenPanel.setBorder(BorderFactory.createTitledBorder("Children (Family Allowance)"));
        childrenPanel.setBackground(new Color(250, 250, 255));

        // Table for children's data: Birth Date, Age, Status
        String[] columns = { "Birth Date", "Age", "Minor?" };
        childTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        childTable = new JTable(childTableModel);
        childTable.setRowHeight(24);
        childTable.setFont(new Font("Arial", Font.PLAIN, 14));
        childTable.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 12));
        childrenPanel.add(new JScrollPane(childTable), BorderLayout.CENTER);

        // Buttons for adding or removing a child from the employee
        JPanel childBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddChild = new JButton("Add Child");
        btnAddChild.setFont(new Font("Arial", Font.PLAIN, 12));
        JButton btnRemoveChild = new JButton("Remove Child");
        btnRemoveChild.setFont(new Font("Arial", Font.PLAIN, 12));

        // Adding the buttons to the panel
        childBtnPanel.add(btnAddChild);
        childBtnPanel.add(btnRemoveChild);
        childrenPanel.add(childBtnPanel, BorderLayout.SOUTH);

        // Limit the height of children panel
        childrenPanel.setPreferredSize(new Dimension(400, 150));
        mainPanel.add(childrenPanel);

        // Contract details panel
        contractPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        contractPanel.setBorder(BorderFactory.createTitledBorder("Contract Details"));
        contractPanel.setBackground(new Color(250, 250, 250));

        contractPanel.add(new JLabel("Contract End Date (*):") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtContractEnd.setToolTipText("Required for Contract employees.");
        contractPanel.add(txtContractEnd);

        contractPanel.add(new JLabel("Gross Salary Amount (*):") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtContractAmount.setToolTipText("Monthly gross salary.");
        contractPanel.add(txtContractAmount);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(contractPanel);

        updateContractVisibility(); // Hide or show contract panel based on the empType

        // Listeners for the buttons
        cmbType.addActionListener(e -> updateContractVisibility());
        btnAddChild.addActionListener(e -> onAddChild());
        btnRemoveChild.addActionListener(e -> onRemoveChild());

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // Buttons for cancel and save options
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        btnSave.setFont(new Font("Arial", Font.PLAIN, 12));
        buttonPanel.add(btnCancel);
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveEmployee());

        // Dialog setup
        pack();
        setLocationRelativeTo(owner);
    }

    /*
     * Method that loads all the departments from the database
     * and fills the combo box
     */
    private void loadDepartments() {
        try {
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                cmbDept.addItem(d);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load departments: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * This method shows or hides the contract panel based on the employee type
     */
    private void updateContractVisibility() {
        Employee.EmployeeType type = (Employee.EmployeeType) cmbType.getSelectedItem();

        // Checking if employee is the contract type
        boolean isContract = (type == Employee.EmployeeType.CA || type == Employee.EmployeeType.CT);
        contractPanel.setVisible(isContract);
        pack(); // Resizing dialog
    }

    /*
     * Method adding a child to the temp list after validating the birth date
     */
    private void onAddChild() {
        String birthDateStr = JOptionPane.showInputDialog(this, "Enter Child Birth Date (YYYY-MM-DD):");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthDateStr.trim());
                if (birthDate.isAfter(LocalDate.now())) { // Checking that the birth date is not in the future
                    JOptionPane.showMessageDialog(this, "Birth date cannot be in the future.", "Invalid Date",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Child child = new Child(); // Create child
                child.setBirthDate(birthDate);
                tempChildrenList.add(child); // Add in temporary list
                refreshChildTable(); // Refresh table

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * Removes a selected child from the temporary list by not affecting the DB
     */
    private void onRemoveChild() {
        int selectedRow = childTable.getSelectedRow();
        if (selectedRow >= 0) {
            tempChildrenList.remove(selectedRow);
            refreshChildTable();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a child to remove.", "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /*
     * This function refreshes the children table to reflect the temporary list
     * calculates age and determines whether the child is a minor or not
     */
    private void refreshChildTable() {
        childTableModel.setRowCount(0); // clear table
        LocalDate now = LocalDate.now();

        for (Child c : tempChildrenList) {
            int age = c.getAge(now);
            boolean isMinor = c.isMinor(now);
            childTableModel.addRow(new Object[] {
                    (c.getBirthDate() != null
                            ? c.getBirthDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            : ""),
                    age,
                    isMinor ? "Yes" : "No"
            });
        }
    }

    /*
     * Validates all fields needed and saves and inserts the employee, children and
     * the
     * contract (in case of contract employee), in the database
     */
    private void saveEmployee() {
        try {
            Employee emp = new Employee();
            emp.setFullName(txtName.getText().trim());
            emp.setType((Employee.EmployeeType) cmbType.getSelectedItem());

            // Validate department selection
            Department selectedDept = (Department) cmbDept.getSelectedItem();
            if (selectedDept != null) {
                emp.setDeptId(selectedDept.getId());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a department.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            emp.setMarried(chkMarried.isSelected());
            emp.setAddress(txtAddress.getText().trim());
            emp.setPhone(txtPhone.getText().trim());
            emp.setIban(txtIban.getText().trim());
            emp.setBankName(txtBank.getText().trim());
            LocalDate startDate = LocalDate.parse(txtStartDate.getText().trim());

            // Enforcing payroll policy meaning that the start date must be
            // the 1st of the month
            if (startDate.getDayOfMonth() != 1) {
                LocalDate adjustedDate = startDate.plusMonths(1).withDayOfMonth(1);
                JOptionPane.showMessageDialog(this,
                        "Input Date: " + startDate + "\n" +
                                "Policy: Hiring starts on the 1st of the month.\n" +
                                "ADJUSTED Start Date to: " + adjustedDate,
                        "Date Policy Auto-Adjustment", JOptionPane.INFORMATION_MESSAGE);
                startDate = adjustedDate;
            }

            emp.setStartDate(startDate);
            emp.setActive(true);

            // Validating required fields
            if (emp.getFullName().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Save Employee
            int empId = employeeDAO.addEmployee(emp);

            // 2. Save Children
            for (Child c : tempChildrenList) {
                c.setEmpId(empId); // Assign foreign key
                childDAO.addChild(c);
            }

            // 3. Save Contract if needed
            boolean isContract = (emp.getType() == Employee.EmployeeType.CA
                    || emp.getType() == Employee.EmployeeType.CT);
            if (isContract) {
                LocalDate endDate = LocalDate.parse(txtContractEnd.getText().trim());
                double amount = Double.parseDouble(txtContractAmount.getText().trim());

                // Validating contract end date
                if (endDate.isBefore(startDate)) {

                    JOptionPane.showMessageDialog(this,
                            "Contract End Date cannot be before Start Date.\nWarning: Employee was created but Contract failed.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    // For now, return allows the user to correct input, but the employee is already
                    // saved.

                } else {
                    Contract contract = new Contract(empId, startDate, endDate, amount);
                    contractDAO.addContract(contract);
                }
            }

            // Successfully saved an employee
            isSaved = true;
            JOptionPane.showMessageDialog(this, "Employee hired successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number for Amount.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /*
     * Returns true if the employee was successfully hired/saved on the database
     */
    public boolean isSaved() {
        return isSaved;
    }
}
