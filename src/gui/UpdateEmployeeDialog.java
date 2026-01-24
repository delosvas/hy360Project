package gui;

import dao.ChildDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import model.Child;
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
 * Update Employee Dialog
 * 
 * This dialog allows editing the info of an existing employee.
 * 
 * It's responsible for:
 * 1. editing personal info 
 * 2. marital status update
 * 3. removing or adding children for family allowance
 * 
 */
public class UpdateEmployeeDialog extends JDialog {
    private boolean isSaved = false;
    private EmployeeDAO employeeDAO;
    private DepartmentDAO departmentDAO;
    private ChildDAO childDAO;
    private Employee currentEmployee;

    private JTextField txtName = new JTextField(20);
    private JComboBox<Department> cmbDept = new JComboBox<>();
    private JCheckBox chkMarried = new JCheckBox("Married");

    // Child Management
    private DefaultTableModel childTableModel;
    private JTable childTable;
    private List<Child> tempChildrenList = new ArrayList<>();

    private JTextField txtAddress = new JTextField(20);
    private JTextField txtPhone = new JTextField(15);
    private JTextField txtIban = new JTextField(20);
    private JTextField txtBank = new JTextField(15);

    /*
     * Constructor that builds the dialog UI and loads all the data from the
     * employees table
     * 
     * @param owner The parent window
     * 
     * @param emp The employee to be edited
     */
    public UpdateEmployeeDialog(Window owner, Employee emp) {
        super(owner, "Edit Employee Details", ModalityType.APPLICATION_MODAL);
        this.currentEmployee = emp;
        employeeDAO = new EmployeeDAO();
        departmentDAO = new DepartmentDAO();
        childDAO = new ChildDAO();

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Details Panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        // Disable uneditable fields (Type, Start Date)
        formPanel.add(new JLabel("ID:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        JTextField txtId = new JTextField(String.valueOf(emp.getId()));
        txtId.setEditable(false);
        formPanel.add(txtId);

        // Type
        formPanel.add(new JLabel("Type:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        JTextField txtType = new JTextField(emp.getType().toString());
        txtType.setEditable(false);
        formPanel.add(txtType);

        // Name
        formPanel.add(new JLabel("Full Name:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtName.setText(emp.getFullName());
        formPanel.add(txtName);

        // Department
        formPanel.add(new JLabel("Department:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        loadDepartments(emp.getDeptId());
        formPanel.add(cmbDept);

        // Marital Status
        formPanel.add(new JLabel("Marital Status:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        chkMarried.setSelected(emp.isMarried());
        formPanel.add(chkMarried);

        // Address
        formPanel.add(new JLabel("Address:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtAddress.setText(emp.getAddress());
        formPanel.add(txtAddress);

        // Phone
        formPanel.add(new JLabel("Phone:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtPhone.setText(emp.getPhone());
        formPanel.add(txtPhone);

        // IBAN
        formPanel.add(new JLabel("IBAN:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtIban.setText(emp.getIban());
        formPanel.add(txtIban);

        // Bank Name
        formPanel.add(new JLabel("Bank Name:") {
            {
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        });
        txtBank.setText(emp.getBankName());
        formPanel.add(txtBank);

        mainPanel.add(formPanel);

        // Children Panel
        JPanel childrenPanel = new JPanel(new BorderLayout());
        childrenPanel.setBorder(BorderFactory.createTitledBorder("Children (Family Allowance)"));

        // Table Columns: Birth Date, Age, Status
        String[] columns = { "Birth Date", "Age", "Minor?" };
        childTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        childTable = new JTable(childTableModel);
        childTable.setRowHeight(20);
        childTable.setFont(new Font("Arial", Font.PLAIN, 12));
        childTable.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 12));
        childrenPanel.add(new JScrollPane(childTable), BorderLayout.CENTER);

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

        // Load existing children
        loadChildren();

        // Listeners for each button
        btnAddChild.addActionListener(e -> onAddChild());
        btnRemoveChild.addActionListener(e -> onRemoveChild());

        add(new JScrollPane(mainPanel), BorderLayout.CENTER); // Add main panel to dialog

        // Save and Cancel Buttons
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Update");
        btnSave.setFont(new Font("Arial", Font.PLAIN, 12));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveChanges());

        pack(); // Dialog setup
        setLocationRelativeTo(owner);
    }

    /*
     * Method that loads all the departments from the database
     * and fills the combo box
     * 
     * @param currentDeptId
     */
    private void loadDepartments(int currentDeptId) {
        try {
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                cmbDept.addItem(d);
                if (d.getId() == currentDeptId) {
                    cmbDept.setSelectedItem(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Loads all children associated with the employee and displays them in the
     * table.
     */
    private void loadChildren() {
        try {
            tempChildrenList = childDAO.getChildrenByEmployeeId(currentEmployee.getId());
            refreshChildTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load children: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * This method adds a new child to the temporary list after validating the birth
     * date
     */
    private void onAddChild() {
        String birthDateStr = JOptionPane.showInputDialog(this, "Enter Child Birth Date (YYYY-MM-DD):");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthDateStr.trim());
                if (birthDate.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Birth date cannot be in the future.", "Invalid Date",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Child child = new Child();
                child.setEmpId(currentEmployee.getId());
                child.setBirthDate(birthDate);

                tempChildrenList.add(child);
                refreshChildTable();

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * Removes the selected child from the temp list
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
     * Refreshes the children table to reflect the temporary list.
     */
    private void refreshChildTable() {
        childTableModel.setRowCount(0);
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
     * Saves all changes made to the the employee and the children
     * in the database, by updating employee basic info, deleting all
     * existing children and re-inserting the updated children list
     */
    private void saveChanges() {
        try {
            // Update employee fields
            currentEmployee.setFullName(txtName.getText().trim());
            Department d = (Department) cmbDept.getSelectedItem();
            if (d != null)
                currentEmployee.setDeptId(d.getId());

            currentEmployee.setMarried(chkMarried.isSelected());
            // childCount removed - handled by ChildDAO interactions

            currentEmployee.setAddress(txtAddress.getText().trim());
            currentEmployee.setPhone(txtPhone.getText().trim());
            currentEmployee.setIban(txtIban.getText().trim());
            currentEmployee.setBankName(txtBank.getText().trim());

            // Validate required fields
            if (currentEmployee.getFullName().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Update Employee
            employeeDAO.updateEmployee(currentEmployee);

            // 2. Update Children (Strategy: Delete all and re-insert)
            // This is simple and effective for this scale.
            childDAO.deleteChildrenByEmployeeId(currentEmployee.getId());
            for (Child c : tempChildrenList) {
                // Ensure empID is set (it should be, but safety check)
                c.setEmpId(currentEmployee.getId());
                childDAO.addChild(c);
            }

            // Successfully saved an employee
            isSaved = true;
            JOptionPane.showMessageDialog(this, "Employee updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Returns true if the update was successfully saved on the database
     */
    public boolean isSaved() {
        return isSaved;
    }
}
