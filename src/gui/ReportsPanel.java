package gui;

import util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

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

    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Pre-defined Reports ---
        JPanel predefinedPanel = new JPanel(new BorderLayout());

        // Top: Filter/Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Report:"));

        String[] reports = {
                "Payroll Status per Category",
                "Salary Stats (Min/Max/Avg) per Category",
                "Average Salary Trend per Period",
                "Employee Specific Details & Payroll",
                "Total Payroll Cost per Category"
        };
        reportSelector = new JComboBox<>(reports);
        topPanel.add(reportSelector);

        JButton btnRun = new JButton("Generate Report");
        topPanel.add(btnRun);

        predefinedPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Results
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        predefinedPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Bottom: Status
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        predefinedPanel.add(statusLabel, BorderLayout.SOUTH);

        // Events
        btnRun.addActionListener(e -> generateReport());

        tabbedPane.addTab("Standard Reports", predefinedPanel);

        // --- TAB 2: Custom SQL ---
        JPanel customSqlPanel = new JPanel(new BorderLayout());

        // Top: Text Area + Button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter SQL Query"));

        sqlTextArea = new JTextArea(5, 40);
        inputPanel.add(new JScrollPane(sqlTextArea), BorderLayout.CENTER);

        JButton executeBtn = new JButton("Execute SQL");
        executeBtn.addActionListener(e -> executeCustomQuery());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(executeBtn);
        inputPanel.add(btnPanel, BorderLayout.SOUTH);

        customSqlPanel.add(inputPanel, BorderLayout.NORTH);

        // Center: Results
        customTableModel = new DefaultTableModel();
        customTable = new JTable(customTableModel);
        customSqlPanel.add(new JScrollPane(customTable), BorderLayout.CENTER);

        // Bottom: Status
        customStatusLabel = new JLabel("Ready to execute custom queries.");
        customStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        customSqlPanel.add(customStatusLabel, BorderLayout.SOUTH);

        tabbedPane.addTab("Custom SQL Queries", customSqlPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void generateReport() {
        String selected = (String) reportSelector.getSelectedItem();
        String sql = "";

        if ("Payroll Status per Category".equals(selected)) {
            // "Κατάσταση μισθοδοσίας ανά κατηγορία προσωπικού"
            sql = "SELECT emp_type, employee_count, total_cost, average_salary, payment_year, payment_month " +
                    "FROM view_monthly_payroll_analysis ORDER BY payment_year DESC, payment_month DESC, emp_type";

        } else if ("Salary Stats (Min/Max/Avg) per Category".equals(selected)) {
            // "Μεγιστος, ελάχιστος και μέσος μισθός ανά κατηγορία προσωπικού"
            sql = "SELECT e.emp_type, " +
                    "MAX(p.total_amount) as Max_Salary, " +
                    "MIN(p.total_amount) as Min_Salary, " +
                    "AVG(p.total_amount) as Ang_Salary " +
                    "FROM payroll_log p JOIN employees e ON p.emp_id = e.emp_id " +
                    "GROUP BY e.emp_type";

        } else if ("Average Salary Trend per Period".equals(selected)) {
            // "Μέση αύξηση μισθών και επιδομάτων ανά χρονική περίοδο"
            sql = "SELECT YEAR(payment_date) as Year, MONTH(payment_date) as Month, " +
                    "AVG(total_amount) as Avg_Salary, " +
                    "AVG(family_allowance + experience_allowance + research_allowance + library_allowance) as Avg_Allowances "
                    +
                    "FROM payroll_log GROUP BY Year, Month ORDER BY Year DESC, Month DESC";

        } else if ("Employee Specific Details & Payroll".equals(selected)) {
            // "Στοιχεία και μισθοδοσία συγκεκριμένου υπαλλήλου"
            String input = JOptionPane.showInputDialog(this, "Enter Employee ID:");
            if (input == null || input.trim().isEmpty())
                return;
            try {
                int empId = Integer.parseInt(input.trim());
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
            // "Συνολικό ύψος μισθοδοσίας ανά κατηγορία προσωπικού"
            sql = "SELECT e.emp_type, SUM(p.total_amount) as Total_Cost " +
                    "FROM payroll_log p JOIN employees e ON p.emp_id = e.emp_id " +
                    "GROUP BY e.emp_type";
        }

        if (sql != null && !sql.isEmpty()) {
            executeAndDisplay(sql, reportTable, statusLabel);
        }
    }

    private void executeCustomQuery() {
        String sql = sqlTextArea.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a SQL query.", "Empty Query",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Basic safety check (very primitive)
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

    private void executeAndDisplay(String sql, JTable targetTable, JLabel targetStatus) {
        targetStatus.setText("Executing query...");

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql)) {

                    return buildTableModel(rs);
                }
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = get();
                    targetTable.setModel(model);
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

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnLabel(column)); // Use getColumnLabel for aliases
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
