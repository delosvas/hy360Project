

import util.DBConnection;
import gui.MainFrame;
import util.DatabaseInitializer;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== University Payroll System Starting ===");

        // Test DB Connection
        try {
            DBConnection.getConnection();
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Please ensure MySQL is running and configured correctly.");

        }
        System.out.println("Initializing Database...");
        DatabaseInitializer.initialize();
        // Launch GUI
        MainFrame.launch();
    }
}
