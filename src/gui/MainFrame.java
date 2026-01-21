package gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import java.awt.*;

/*
 * Main Frame 
 * 
 * This class is repsonsible for:
 * 1.initializing the main window 
 * 2.displaying a header banner
 * 3.hosts a JTabbedPane with all the needed tabs 
 * 
 * It only assembles and displays the main components of the application
 */
public class MainFrame extends JFrame {

	/*
	 * Constructor initializes the main frame, the header and adds to it the tabs
	 */
    public MainFrame() {
    	
        setTitle("University of Crete Payroll System"); // Title of the main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Header Label 
        JLabel title = new JLabel("University of Crete Payroll System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(20, 60, 120));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        add(title, BorderLayout.NORTH);

        // Use a customized tabbed pane for navigation
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));
        tabbedPane.setBackground(new Color(230, 240, 255));
        tabbedPane.setForeground(new Color(30, 60, 120));
       
        // Adding all needed panels as tabs
        tabbedPane.addTab("Employees", new EmployeesPanel());
        tabbedPane.addTab("Payroll Processing", new PayrollPanel());
        tabbedPane.addTab("Salary & Allowances Management", new SettingsPanel());
        tabbedPane.addTab("Reports / Queries", new ReportsPanel());
  
        add(tabbedPane, BorderLayout.CENTER);

    }
    
    /*
     * Launch method for the application window 
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Getting the systems natural look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true); // Creating the main application window
        });
    }
}
