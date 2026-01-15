package gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("University of Crete Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Center on screen

        // Use a Tabbed Pane for navigation
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add Tabs
        tabbedPane.addTab("Employees", new EmployeesPanel());
        tabbedPane.addTab("Payroll Processing", new PayrollPanel());
        tabbedPane.addTab("Reports / Queries", new ReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Static launch method to separate from Main logic if needed
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for better aesthetics
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}
