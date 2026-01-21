package gui;

import dao.ContractDAO;
import dao.EmployeeDAO;
import model.Contract;
import model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/*
 * Employees Panel
 * 
 * It's responsible for displaying and managing all employees
 * in the payroll system. 
 * 
 * Creates a toolbar with actions:
 * 1. Hire Employee
 * 2. Edit Employee Details
 * 3. Terminate/Retire Employee
 * 4. Refresh List
 * 
 * Initializes and displays the employee table with the correct info
 */
public class EmployeesPanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private EmployeeDAO myDao; 
    
    /*
     * Constructor that initializes the layout and the toolbar,
     * the buttons and its' listeners, the employee table
     */
    public EmployeesPanel() {
    	
        setLayout(new BorderLayout());
        myDao = new EmployeeDAO();

        // Setup the toolbar on the top of the panel
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        
        // Toolbar buttons creation
        JButton hireBtn = new JButton("Hire Employee");
        JButton editBtn = new JButton("Edit Details");
        JButton refreshBtn = new JButton("Refresh List");
        JButton terminateBtn = new JButton("Terminate/Retire");

        // Customizing each button and also adding then to the panel
        hireBtn.setFont(new Font("Arial", Font.BOLD, 13));
        hireBtn.setBackground(new Color(51, 153, 255));
        toolBar.add(hireBtn);
        
        editBtn.setFont(new Font("Arial", Font.BOLD, 13));
        editBtn.setBackground(new Color(255, 153, 51));
        toolBar.add(editBtn); // Edit button
        toolBar.add(Box.createHorizontalStrut(10));
        
        terminateBtn.setFont(new Font("Arial", Font.BOLD, 13));
        terminateBtn.setBackground(new Color(220, 53, 69));
        toolBar.add(terminateBtn);
        
        toolBar.add(Box.createHorizontalGlue()); // Refresh button added on the right
        
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 13));
        refreshBtn.setBackground(new Color(0, 153, 76));
        toolBar.add(refreshBtn);

        add(toolBar, BorderLayout.NORTH);

        // Table setup in the center of the panel
        String[] cols = { "ID", "Name", "Type", "Dept ID", "Start Date", "Status" };
        tableModel = new DefaultTableModel(cols, 0) { 
            @Override
            // Table is read only
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel);
        employeeTable.setFillsViewportHeight(true);
        employeeTable.setRowHeight(20);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
        	// Renderer for coloring rightly the active/inactive status row
        	public Component getTableCellRendererComponent(JTable table, Object value, 
        			boolean isSelected, boolean hasFocus, int row, int column) { 
        		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
        		String status = value.toString(); 
        		// Green for active, red for inactive
        		if (status.equalsIgnoreCase("Active")) { 
        			c.setForeground(new Color(0, 153, 0)); 
        		} else { 
        			c.setForeground(Color.RED);
        		} // Keep selection color correct 
        		if (isSelected) { 
        			c.setForeground(table.getSelectionForeground()); 
        		} 
        		return c; 
        		} 
        	});
    
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Buttons 
        refreshBtn.addActionListener(e -> loadEmployees()); // Refresh employee table

        hireBtn.addActionListener(e -> { // Opening the dialog to hire a new employee
            AddEmployeeDialog dialog = new AddEmployeeDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadEmployees();
            }
        });

        editBtn.addActionListener(e -> { // Edit the employee selected by the user
            int r = employeeTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to edit.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) tableModel.getValueAt(r, 0);
            try {
                // Re-fetch from DB to get full details (address, bank etc.)
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

        terminateBtn.addActionListener(e -> {  // Button for terminating an employee
            int r = employeeTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to terminate.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
            	// Getting all the employees
            	Employee emp = myDao.getAllEmployees().stream().filter(e1 -> e1.getId() ==
            			(int)tableModel.getValueAt(r, 0)).findFirst().orElse(null);		
                if(emp == null) {
                	return;
                }
                // Just permanent employees can be terminated
                if(emp.getType() == Employee.EmployeeType.CA || emp.getType() == Employee.EmployeeType.CT) {
                	JOptionPane.showMessageDialog(this, "Termination must concern only permanent employees!",
                            "Action is prohibited", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Ask for termination date
                String dateStr = JOptionPane.showInputDialog(this,
                        "Enter Termination Date (YYYY-MM-DD).\nMust be the last day of the month:",
                        LocalDate.now().toString());

                if (dateStr == null) {
                    return; // Cancelled
                }
                
                LocalDate termDate = LocalDate.parse(dateStr.trim());
                // Validate last day of month
                if (termDate.getDayOfMonth() != termDate.lengthOfMonth()) {
                    JOptionPane.showMessageDialog(this, "Termination must be on the last day of the month!",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Confirming the action about to terminate the employee
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

        // Load employees on startup
        loadEmployees();
    }

    /*
     * Method that loads all employees from the DB 
     * SwingWorker used to prevent UI freezing during slow DB operations.
     */
    private void loadEmployees() {
        tableModel.setRowCount(0); // Clear the table 
        new SwingWorker<List<Employee>, Void>() {
        	
            @Override
            protected List<Employee> doInBackground() throws Exception {
                return myDao.getAllEmployees(); // Get all employees from the database
            }

            @Override
            protected void done() {
                try {
                    List<Employee> data = get();
                    for (Employee e : data) {  // Active or inactive status determined
                    	String activeStatus = (e.isActive() && !LocalDate.now().isBefore(e.getStartDate())) ? "Active" : "Inactive";
               
                        Object[] row = {
                                e.getId(),
                                e.getFullName(),
                                e.getType(),
                                e.getDeptId(),
                                e.getStartDate(),
                                activeStatus
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
