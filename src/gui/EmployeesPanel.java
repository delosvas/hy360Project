package gui;

import dao.EmployeeDAO;
import model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmployeesPanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private EmployeeDAO myDao; // renamed from employeeDAO

    public EmployeesPanel() {
        setLayout(new BorderLayout());
        myDao = new EmployeeDAO();

        // setup the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton hireBtn = new JButton("Hire Employee");
        JButton editBtn = new JButton("Edit Details");
        JButton refreshBtn = new JButton("Refresh List");
        JButton terminateBtn = new JButton("Terminate/Retire");

        toolBar.add(hireBtn);
        toolBar.add(editBtn); // Edit button
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(terminateBtn);
        // wanted to push this to the right but glue wasn't working well, using large
        // strut instead
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(refreshBtn);

        add(toolBar, BorderLayout.NORTH);

        // table stuff
        String[] cols = { "ID", "Name", "Type", "Dept ID", "Start Date", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel);
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Events
        refreshBtn.addActionListener(e -> loadEmployees());

        hireBtn.addActionListener(e -> {
            AddEmployeeDialog dialog = new AddEmployeeDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadEmployees();
            }
        });

        editBtn.addActionListener(e -> {
            int r = employeeTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to edit.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // get ID
            int id = (int) tableModel.getValueAt(r, 0);
            try {
                // inefficient but safe: re-fetch from DB to get full details (address, bank
                // etc)
                List<Employee> all = myDao.getAllEmployees();
                Employee target = all.stream().filter(emp -> emp.getId() == id).findFirst().orElse(null);

                if (target != null) {
                    UpdateEmployeeDialog dialog = new UpdateEmployeeDialog(SwingUtilities.getWindowAncestor(this),
                            target);
                    dialog.setVisible(true);
                    if (dialog.isSaved()) {
                        loadEmployees();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        terminateBtn.addActionListener(e -> {
            int r = employeeTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to terminate.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ask for termination date
            String dateStr = JOptionPane.showInputDialog(this,
                    "Enter Termination Date (YYYY-MM-DD).\nMust be the last day of the month:",
                    LocalDate.now().toString());

            if (dateStr == null)
                return; // cancelled

            try {
                LocalDate termDate = LocalDate.parse(dateStr.trim());

                // validate last day of month
                if (termDate.getDayOfMonth() != termDate.lengthOfMonth()) {
                    JOptionPane.showMessageDialog(this, "Termination must be on the last day of the month!",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int ans = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to terminate this employee on " + termDate + "?",
                        "Confirm Termination", JOptionPane.YES_NO_OPTION);

                if (ans == JOptionPane.YES_OPTION) {
                    try {
                        int id = (int) tableModel.getValueAt(r, 0);
                        myDao.deactivateEmployee(id);
                        loadEmployees();
                        JOptionPane.showMessageDialog(this, "Employee terminated successfully.", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error terminating employee: " + ex.getMessage(),
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // load data at start
        loadEmployees();
    }

    // using swingworker because otherwise the ui freezes when database is slow
    // found this solution on stackoverflow
    private void loadEmployees() {
        tableModel.setRowCount(0);
        new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                return myDao.getAllEmployees();
            }

            @Override
            protected void done() {
                try {
                    List<Employee> data = get();
                    for (Employee e : data) {
                        Object[] row = {
                                e.getId(),
                                e.getFullName(),
                                e.getType(),
                                e.getDeptId(),
                                e.getStartDate(),
                                e.isActive() ? "Active" : "Inactive"
                        };
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EmployeesPanel.this, "Error loading employees: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
