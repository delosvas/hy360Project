package gui;

import dao.ContractDAO;
import dao.EmployeeDAO;
import dao.PayrollDAO;
import model.Contract;
import model.Employee;
import service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/*
 * Payroll Pane
 * 
 * this panel is responsible for:
 * 1. selecting a month and year
 * 2. doing payroll calculations for all employees
 * 3. showing the payroll results in a table
 * 4. showing total cost payroll for a selected period
 */
public class PayrollPanel extends JPanel {

    private JComboBox<String> cmbMonth;
    private JComboBox<Integer> cmbYear;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCost;
    private JLabel lblStatus;

    /* 
     * Constructor that builds the payroll UI
     */
    public PayrollPanel() {
        setLayout(new BorderLayout());

        // Top Panel: Configuration
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setBackground(new Color(245, 245, 245));
        configPanel.setForeground(new Color(230, 230, 250));
        configPanel.add(new JLabel("Month:")); 
        // Month selector
        String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setFont(new Font("Arial" ,Font.PLAIN, 14));
        configPanel.add(cmbMonth);

        // Year selector
        configPanel.add(new JLabel("Year:") {{setFont(new Font("Arial" ,Font.PLAIN, 14));}});
        Integer[] years = new Integer[11];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i <= 10; i++)
            years[i] = currentYear - 5 + i; // 5 years back, 5 forward
        cmbYear = new JComboBox<>(years);
        cmbYear.setFont(new Font("Arial" ,Font.PLAIN, 14));
        cmbYear.setSelectedItem(currentYear);
        configPanel.add(cmbYear);

        // Run Payroll button
        JButton btnCalculate = new JButton("Run Payroll");
        btnCalculate.setFont(new Font("Arial" ,Font.PLAIN, 12));
        configPanel.add(btnCalculate);

        // Button for clearing results
        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Arial" ,Font.PLAIN, 12));
        configPanel.add(btnClear);

        add(configPanel, BorderLayout.NORTH);

        // Center: Results Table
        String[] columnNames = {
                "Emp ID", "Name", "Type", "Base Salary", "Family Allow.",
                "Experience Allow.", "Research/Lib Allow.", "TOTAL PAY"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            // Table model is not editable
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Table setup
        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Arial" ,Font.PLAIN, 14));
        resultTable.setRowHeight(20);
        resultTable.getTableHeader().setFont(new Font("Arial" ,Font.BOLD, 14));
        resultTable.setFillsViewportHeight(true);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer(){
        	public Component getTableCellRendererComponent(JTable table, Object value, 
        			boolean isSelected, boolean hasFocus, int row, int column) { 
        		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
        		if (!isSelected) { 
        			c.setBackground(new Color(200, 255, 200));  
        		} 
        		setHorizontalAlignment(SwingConstants.RIGHT);
        		return c; 
        		} 
        	});
        
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // Bottom: Status and Totals
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.setForeground(new Color(245, 245, 245));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Arial" ,Font.PLAIN, 14));
        lblTotalCost = new JLabel("Total Payroll Cost: 0.00 €");
        lblTotalCost.setForeground(new Color(0, 102, 204));
        lblTotalCost.setFont(new Font("Arial", Font.BOLD, 14));

        bottomPanel.add(lblStatus, BorderLayout.WEST);
        bottomPanel.add(lblTotalCost, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        btnCalculate.addActionListener(e -> runPayrollCalculation()); // Run payroll calculation
        btnClear.addActionListener(e -> { // Clear table and reset
            tableModel.setRowCount(0);
            lblTotalCost.setText("Total Payroll Cost: 0.00 €");
            lblStatus.setText("Cleared");
        });
    }

    /*
     * Does the payroll calculation using SwingWorker
     */
    private void runPayrollCalculation() {
        tableModel.setRowCount(0); // Clear previous results
        lblStatus.setText("Calculating...");

        // Get selected year and month
        int year = (Integer) cmbYear.getSelectedItem();
        int monthIndex = cmbMonth.getSelectedIndex() + 1; // 1-12

        // Payment date is LAST DAY of the selected month
        LocalDate firstOfMonth = LocalDate.of(year, monthIndex, 1);
        final LocalDate paymentDate = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());

        new SwingWorker<Void, Object[]>() {
            double grandTotal = 0.0;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    PayrollDAO payrollDAO = new PayrollDAO();
                    EmployeeDAO employeeDAO = new EmployeeDAO();
                    ContractDAO contractDAO = new ContractDAO();
                    PayrollService payrollService = new PayrollService();

                    List<Employee> employees = employeeDAO.getAllEmployees();

                    for (Employee emp : employees) { // Skip inactive employees
                        if (!emp.isActive())
                            continue;

                        // CRITICAL CHECK: Ignore employees who haven't started yet!
                        // Example: Hired Jan 11 -> Starts Feb 1.
                        // If running Jan payroll (Jan 31), StartDate(Feb 1) > PaymentDate(Jan 31) ->
                        // SKIP.
                        if (emp.getStartDate().isAfter(paymentDate)) {
                            continue;
                        }

                        // Get employee if it is a contract based employee
                        Contract contract = null;
                        if (emp.getType() == Employee.EmployeeType.CA || emp.getType() == Employee.EmployeeType.CT) {
                            contract = contractDAO.getActiveContract(emp.getId(), paymentDate);
                        }

                        // Do payroll calculation
                        PayrollService.PayrollResult result = payrollService.calculateSalary(emp, paymentDate,
                                contract);

                        // Save to DB
                        payrollDAO.saveLog(result);

                        grandTotal += result.getTotal(); // Add to total payroll cost

                        publish(new Object[] { // Publish row to the table
                                emp.getId(),
                                emp.getFullName(),
                                emp.getType(),
                                String.format("%.2f", result.getBase()),
                                String.format("%.2f", result.getFamily()),
                                String.format("%.2f", result.getExperience()),
                                String.format("%.2f", result.getResearch() + result.getLibrary()),
                                String.format("%.2f", result.getTotal())
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Log
                    throw ex; // Re-throw to handle in done()
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) { // Add every published row on the table
                    tableModel.addRow(row);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    // Update UI with the final results
                    lblTotalCost.setText(String.format("Total Payroll Cost: %.2f €", grandTotal));
                    lblStatus.setText("Calculation Completed for " + paymentDate);
                } catch (Exception e) {
                    lblStatus.setText("Error occurred.");
                    JOptionPane.showMessageDialog(PayrollPanel.this, "Error during calculation: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute(); // Start with background task
    }
}
