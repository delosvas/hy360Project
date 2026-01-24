package gui;

import dao.ViewsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/*
 * ViewsPanel
 * 
 * A panel for displaying the database views bonus content.
 * It uses a JTabbedPane to show each view in a separate tab.
 * designed to be included as a main tab in the application.
 */
public class ViewsPanel extends JPanel {

    private ViewsDAO viewsDAO;

    // Store table models or refresh logic?
    // Easier to just rebuild the tab content or refresh the models.

    // Storing the created panels to refresh them
    private final JTabbedPane tabbedPane;

    public ViewsPanel() {
        this.viewsDAO = new ViewsDAO();

        setLayout(new BorderLayout());

        // Header with Refresh Button (Styled like EmployeesPanel)
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 13));
        refreshBtn.setBackground(new Color(0, 153, 76));
        refreshBtn.addActionListener(e -> refreshCurrentTab());

        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(refreshBtn);

        add(toolBar, BorderLayout.NORTH);

        // Inner tabbed pane for the different views
        tabbedPane = new JTabbedPane();

        // Tab 1: Full Details
        tabbedPane.addTab("Full Details", createTablePanel("getEmployeeFullDetails"));

        // Tab 2: Active Employees
        tabbedPane.addTab("Active Staff", createTablePanel("getActiveEmployees"));

        // Tab 3: Renewals
        tabbedPane.addTab("Contract Renewals", createTablePanel("getContractRenewalStatus"));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshCurrentTab() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx == -1)
            return;

        // Improve: Instead of full recreate, we could just reload data.
        // For simplicity/robustness, we can just reload the data for the component in
        // that tab.
        Component c = tabbedPane.getComponentAt(idx);
        if (c instanceof JPanel) {
            // Find the component, or just re-run the creation logic if we can replace the
            // tab content?
            // Actually, re-fetching data is better.
            JPanel panel = (JPanel) c;
            // Assume the structure we built: JPanel -> JScrollPane -> JTable
            JScrollPane scroll = (JScrollPane) panel.getComponent(0);
            JTable table = (JTable) scroll.getViewport().getView();

            // Deduce method from index/title (hacky but works)
            String method = "";
            switch (idx) {
                case 0:
                    method = "getEmployeeFullDetails";
                    break;
                case 1:
                    method = "getActiveEmployees";
                    break;
                case 2:
                    method = "getContractRenewalStatus";
                    break;
            }

            if (!method.isEmpty()) {
                loadData(method, table, panel);
            }
        }
    }

    private JPanel createTablePanel(String method) {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable();
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        // Initial load
        loadData(method, table, panel);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadData(String method, JTable table, JPanel panel) {
        // Load data in background
        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                switch (method) {
                    case "getEmployeeFullDetails":
                        return viewsDAO.getEmployeeFullDetails();
                    case "getActiveEmployees":
                        return viewsDAO.getActiveEmployees();
                    case "getContractRenewalStatus":
                        return viewsDAO.getContractRenewalStatus();
                    default:
                        return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> data = get();
                    if (data != null && !data.isEmpty()) {
                        Vector<String> columns = new Vector<>(data.get(0).keySet());
                        DefaultTableModel model = new DefaultTableModel(columns, 0) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                        for (Map<String, Object> row : data) {
                            Vector<Object> rowData = new Vector<>();
                            for (String col : columns) {
                                rowData.add(row.get(col));
                            }
                            model.addRow(rowData);
                        }
                        table.setModel(model);
                    } else {
                        table.setModel(new DefaultTableModel());
                        // Consider showing a message if empty, but be careful not to pile up labels
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Error loading view: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
