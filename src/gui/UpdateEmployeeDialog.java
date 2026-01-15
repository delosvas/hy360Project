package gui;

import dao.ChildDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import model.Child;
import model.Department;
import model.Employee;
import util.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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

    public UpdateEmployeeDialog(Window owner, Employee emp) {
        super(owner, "Edit Employee Details", ModalityType.APPLICATION_MODAL);
        this.currentEmployee = emp;
        employeeDAO = new EmployeeDAO();
        departmentDAO = new DepartmentDAO();
        childDAO = new ChildDAO();

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // --- Details Panel ---
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        // Disable uneditable fields (Type, Start Date)
        formPanel.add(new JLabel("ID:"));
        JTextField txtId = new JTextField(String.valueOf(emp.getId()));
        txtId.setEditable(false);
        formPanel.add(txtId);

        formPanel.add(new JLabel("Type:"));
        JTextField txtType = new JTextField(emp.getType().toString());
        txtType.setEditable(false);
        formPanel.add(txtType);

        formPanel.add(new JLabel("Full Name:"));
        txtName.setText(emp.getFullName());
        formPanel.add(txtName);

        formPanel.add(new JLabel("Department:"));
        loadDepartments(emp.getDeptId());
        formPanel.add(cmbDept);

        formPanel.add(new JLabel("Marital Status:"));
        chkMarried.setSelected(emp.isMarried());
        formPanel.add(chkMarried);

        formPanel.add(new JLabel("Address:"));
        txtAddress.setText(emp.getAddress());
        formPanel.add(txtAddress);

        formPanel.add(new JLabel("Phone:"));
        txtPhone.setText(emp.getPhone());
        formPanel.add(txtPhone);

        formPanel.add(new JLabel("IBAN:"));
        txtIban.setText(emp.getIban());
        formPanel.add(txtIban);

        formPanel.add(new JLabel("Bank Name:"));
        txtBank.setText(emp.getBankName());
        formPanel.add(txtBank);

        mainPanel.add(formPanel);

        // --- Children Panel ---
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
        childrenPanel.add(new JScrollPane(childTable), BorderLayout.CENTER);

        JPanel childBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddChild = new JButton("Add Child");
        JButton btnRemoveChild = new JButton("Remove Child");
        childBtnPanel.add(btnAddChild);
        childBtnPanel.add(btnRemoveChild);
        childrenPanel.add(childBtnPanel, BorderLayout.SOUTH);

        childrenPanel.setPreferredSize(new Dimension(400, 150));
        mainPanel.add(childrenPanel);

        // Load existing children
        loadChildren();

        // Listeners
        btnAddChild.addActionListener(e -> onAddChild());
        btnRemoveChild.addActionListener(e -> onRemoveChild());

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Update");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveChanges());

        pack();
        setLocationRelativeTo(owner);
    }

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

    private void loadChildren() {
        try {
            tempChildrenList = childDAO.getChildrenByEmployeeId(currentEmployee.getId());
            refreshChildTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load children: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

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

    private void refreshChildTable() {
        childTableModel.setRowCount(0);
        LocalDate now = LocalDate.now();
        for (Child c : tempChildrenList) {
            int age = c.getAge(now);
            boolean isMinor = c.isMinor(now);
            childTableModel.addRow(new Object[] {
                    DateUtils.formatDate(c.getBirthDate()),
                    age,
                    isMinor ? "Yes" : "No"
            });
        }
    }

    private void saveChanges() {
        try {
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

            isSaved = true;
            JOptionPane.showMessageDialog(this, "Employee updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return isSaved;
    }
}
