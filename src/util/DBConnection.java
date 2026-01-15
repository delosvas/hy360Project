package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database URL - matches schema.sql
    private static final String URL = "jdbc:mysql://localhost:3306/university_payroll?serverTimezone=UTC";

    // Default XAMPP credentials
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static Connection connection = null;

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the MySQL Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to MySQL database: university_payroll");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found! Add the library to your project.", e);
            }
        }
        return connection;
    }

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
