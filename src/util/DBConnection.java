package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * DBConnection
 * 
 * Utility class that is responsible for creating and managing the connections
 * to the MySQL DB used by the payroll system.
 */
public class DBConnection {
    // Database URL - matches schema.sql
    private static final String URL = "jdbc:mysql://localhost:3306/university_payroll?serverTimezone=UTC";

    // Default XAMPP credentials
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static Connection connection = null;

    // Private constructor that prevents instantiation
    private DBConnection() {
    }

    /*
     * Returns a new database connection
     * 
     * @return a fresh JDBC Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
            try {
                // Load the MySQL Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to MySQL database: university_payroll");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found! Add the library to your project.", e);
            }
            return DriverManager.getConnection(URL, USER, PASSWORD);  
    }

    /*
     * Closes the shared connection if it exists
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
