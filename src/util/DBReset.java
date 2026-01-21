package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/*
 * DBReset
 * 
 * Utility class used to completely wipe and reinitialize the DB.
 * It connects to MySQL without selecting a specific DB, it drops
 * the entire university_payroll DB if it exists and calls the DataBaseInitializer
 * to recreate schema and seed data
 */
public class DBReset {

    private static final String DB_URL_NO_DB = "jdbc:mysql://localhost:3306?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234"; // Assuming same password as Initializer

    /*
     * Main method that is the entry point for resetting the DB
     */
    public static void main(String[] args) {
        System.out.println("!!! WARNING: THIS WILL WIPE THE ENTIRE DATABASE !!!");
        System.out.println("Starting Database Reset...");

        // connecting to MySQL server
        try (Connection conn = DriverManager.getConnection(DB_URL_NO_DB, USER, PASSWORD);
                Statement stmt = conn.createStatement()) {

            // Drop the database
            System.out.println("Dropping database 'university_payroll'...");
            stmt.executeUpdate("DROP DATABASE IF EXISTS university_payroll");
            System.out.println("Database dropped.");

        } catch (Exception e) {
            System.err.println("Error dropping database: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Re-initialize
        System.out.println("Re-initializing database...");
        DatabaseInitializer.initialize();
        System.out.println("DONE. Database is clean and ready.");
    }
}
