package gui;

import util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.EmployeeDAO;
import model.Employee;

import java.awt.*;
import java.sql.*;
import java.util.Vector;

/*
 * Reports Panel
 * 
 * This panel provides:
 * 1. Pre-Defined Reports in Tab1 -> user selects a report type from a dropdown
 * 								  -> system executes the corresponding SQL query
 * 2. Custom SQL execution in Tab2 ->user u=inserts any SQL query manually
 * 								   -> system executes and displays results
 * 
 */
public class ReportsPanel extends JPanel {

    // Components for Tab 1 (Pre-defined)
    private JComboBox<String> reportSelector;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // Components for Tab 2 (Custom SQL)
    private JTextArea sqlTextArea;
    private JTable customTable;
    private DefaultTableModel customTableModel;
    private JLabel customStatusLabel;

    /**
     * Constructor that builds the entire UI. Creates a tabbed interface with 
     * Pre-Defined Reports and Custom SQL execution
     */
    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Pre-defined Reports
        JPanel predefinedPanel = new JPanel(new BorderLayout());
        predefinedPanel.setBackground(new Color(245, 245, 245));

        // Top: Filter/Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(new JLabel("Select Report:") {{setFont(new Font("Arial" ,Font.PLAIN, 12));}});

        String[] reports = { // List of available predefined reports
                "Payroll Status per Category",
                "Salary Stats (Min/Max/Avg) per Category",
                "Average Salary Trend per Period",
                "Employee Specific Details & Payroll",
                "Total Payroll Cost per Category"
        };
        
        reportSelector = new JComboBox<>(reports);
        reportSelector.setFont(new Font("Arial" ,Font.PLAIN, 12));
        topPanel.add(reportSelector);

        JButton btnRun = new JButton("Generate Report"); // Button to run the selected report
        btnRun.setFont(new Font("Arial" ,Font.PLAIN, 12));
        topPanel.add(btnRun);

        predefinedPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Results table
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        reportTable.setRowHeight(22);
        reportTable.getTableHeader().setFont(new Font("Arial" ,Font.BOLD, 12));
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Alternate row coloring for readability
        reportTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
        	public Component getTableCellRendererComponent(JTable table, Object value, 
        			boolean isSelected, boolean hasFocus, int row, int column) { 
        		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
        		if (!isSelected) { 
        			c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(235, 245, 255)); 
        		} 
        		return c; 
        		} 
        	});
        predefinedPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Bottom: Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial" ,Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        predefinedPanel.add(statusLabel, BorderLayout.SOUTH);

        // Run report button action
        btnRun.addActionListener(e -> {
			try {
				generateReport();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});

        // Custom tab label 
        JLabel tab1= new JLabel("Standard Reports");
        tab1.setFont(new Font("Arial" ,Font.BOLD, 14));
        tab1.setForeground(new Color(30, 30, 30));
        tab1.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        
        tabbedPane.addTab(null, predefinedPanel);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab1);

        // TAB 2: Custom SQL
        JPanel customSqlPanel = new JPanel(new BorderLayout());
        customSqlPanel.setBackground(new Color(245, 245, 245));

        // Top: Text Area + Button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter SQL Query"));
        inputPanel.setBackground(new Color(245, 245, 245));

        sqlTextArea = new JTextArea(5, 40);
        sqlTextArea.setFont(new Font("Arial" ,Font.PLAIN, 12));
        inputPanel.add(new JScrollPane(sqlTextArea), BorderLayout.CENTER);

        JButton executeBtn = new JButton("Execute SQL");
        executeBtn.setFont(new Font("Arial" ,Font.PLAIN, 12));
        executeBtn.addActionListener(e -> executeCustomQuery());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(245, 245, 245));
        btnPanel.add(executeBtn);
        inputPanel.add(btnPanel, BorderLayout.SOUTH);
        
        customSqlPanel.add(inputPanel, BorderLayout.NORTH);

        // Center: Results table
        customTableModel = new DefaultTableModel();
        customTable = new JTable(customTableModel);
        customTable.setFillsViewportHeight(true);
        customTable.setRowHeight(20);
        customTable.getTableHeader().setFont(new Font("Arial" ,Font.BOLD, 12));
        customTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customSqlPanel.add(new JScrollPane(customTable), BorderLayout.CENTER);

        // Bottom: Status label
        customStatusLabel = new JLabel("Ready to execute custom queries.");
        customStatusLabel.setFont(new Font("Arial" ,Font.BOLD, 12));
        customStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        customSqlPanel.add(customStatusLabel, BorderLayout.SOUTH);

        JLabel tab2= new JLabel("Custom SQL Queries");
        tab2.setFont(new Font("Arial" ,Font.BOLD, 14));
        tab2.setForeground(new Color(30, 30, 30));
        tab2.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        tabbedPane.addTab(null, customSqlPanel);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab2);

        add(tabbedPane, BorderLayout.CENTER); // Add tabbed pane to the main panel
    }

    /*
     * Generates a predefined report based on the user's selection of a query.
     * 
     * @throws SQLException
     */
    private void generateReport() throws SQLException {
        String selected = (String) reportSelector.getSelectedItem();
        String sql = "";

        if ("Payroll Status per Category".equals(selected)) {
       
            sql = "SELECT e.emp_type, COUNT(DISTINCT e.emp_id) employee_count, SUM(total_amount) AS total_cost, "
            		+ "AVG(total_amount) AS average_salary, YEAR(payment_date) AS payment_year, MONTH(payment_date) AS payment_month " +
                    "FROM payroll_log p JOIN employees e ON p.emp_id = e.emp_id "
            		+ "GROUP BY emp_type "
                    + "ORDER BY emp_type";

        } else if ("Salary Stats (Min/Max/Avg) per Category".equals(selected)) {
            
            sql = "SELECT e.emp_type, " +
                    "MAX(p.total_amount) as Max_Salary, " +
                    "MIN(p.total_amount) as Min_Salary, " +
                    "AVG(p.total_amount) as Avg_Salary " +
                    "FROM payroll_log p JOIN employees e ON p.emp_id = e.emp_id " +
                    "GROUP BY e.emp_type";

        } else if ("Average Salary Trend per Period".equals(selected)) {
            
            sql = "SELECT YEAR(payment_date) as Year, MONTH(payment_date) as Month, " +
                    "AVG(total_amount) as Avg_Salary, " +
                    "AVG(family_allowance + experience_allowance + research_allowance + library_allowance) as Avg_Allowances "
                    +"FROM payroll_log GROUP BY Year, Month ORDER BY Year DESC, Month DESC";

        } else if ("Employee Specific Details & Payroll".equals(selected)) {
            
        	// Asking user for employeeID
            String input = JOptionPane.showInputDialog(this, "Enter Employee ID:");
            if (input == null || input.trim().isEmpty())
                return;
            try {
                int empId = Integer.parseInt(input.trim());
                EmployeeDAO empDAO = new EmployeeDAO();
                if(empDAO.getEmployeeById(empId)==null) {
                	JOptionPane.showMessageDialog(this, "Employee with Id " + empId + " is not registered!", "Error",
                			JOptionPane.ERROR_MESSAGE);
                	return;
                }
                
                sql = "SELECT e.emp_id, e.full_name, e.emp_type, e.start_date, " +
                        "p.payment_date, p.total_amount, p.base_salary, p.family_allowance " +
                        "FROM employees e " +
                        "LEFT JOIN payroll_log p ON e.emp_id = p.emp_id " +
                        "WHERE e.emp_id = " + empId + " " +
                        "ORDER BY p.payment_date DESC";
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } else if ("Total Payroll Cost per Category".equals(selected)) {
           
            sql = "SELECT e.emp_type, SUM(p.total_amount) as Total_Cost " +
                    "FROM payroll_log p JOIN employees e ON p.emp_id = e.emp_id " +
                    "GROUP BY e.emp_type";
        }

        // Execute SQL query and display results
        if (sql != null && !sql.isEmpty()) {
            executeAndDisplay(sql, reportTable, statusLabel);
        }
    }

    /*
     * Executes a custom SQL query entered by the user.
     */
    private void executeCustomQuery() {
        String sql = sqlTextArea.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a SQL query.", "Empty Query",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Primitive safety check for potentially "dangerous" queries
        if (!sql.toUpperCase().startsWith("SELECT") && !sql.toUpperCase().startsWith("SHOW")
                && !sql.toUpperCase().startsWith("DESCRIBE")) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "This does not look like a SELECT query. Are you sure you want to execute it?\n(Modifying data is risky)",
                    "Confirm Non-Select Query", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
        }

        executeAndDisplay(sql, customTable, customStatusLabel);
    }

    /*
     *Executes a SQL query and displays the results in a JTable.
     *
     *@param sql
     *@param targetTable
     *@param targetStatus
     */
    private void executeAndDisplay(String sql, JTable targetTable, JLabel targetStatus) {
        targetStatus.setText("Executing query...");

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql)) {

                    return buildTableModel(rs); // Convert ResultSet into TableModel
                }
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = get(); // Retrieve result from background thread
                    targetTable.setModel(model); // Update the table 
                    targetStatus.setText("Query executed successfully. Rows: " + model.getRowCount());
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = e.getMessage();
                    if (e.getCause() != null)
                        msg = e.getCause().getMessage();

                    targetStatus.setText("Error: " + msg);
                    JOptionPane.showMessageDialog(ReportsPanel.this, "Error executing query:\n" + msg,
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /*
     * Converts a ResultSet into a DefaultTableModel, allowing the table to adapt to any SQL query result.
     * 
     * @param rs
     * @throws SQLException
     */
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Extract names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnLabel(column)); // Use getColumnLabel for aliases
        }

        // Get data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
 
        // Non-editable table model
        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
