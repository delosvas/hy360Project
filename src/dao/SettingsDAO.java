package dao;

import util.DBConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * SettingsDao
 * 
 * Data Access Object for System Settings
 * Handles salary configuration parameters
 * 
 * It's responsible for:
 * 1. retrieve all settings as key-value pairs
 * 2. retrieve a specific key
 * 3. update a setting with a new value
 * 
 */
public class SettingsDAO {

    /**
     * Get all system settings from the database
     * @return A Map<String, Double> where:
     *         - key   = config_key (e.g., "BASE_SALARY")
     *         - value = config_value (numeric setting)
     *
     * @throws SQLException if any database error occurs
     */
    public Map<String, Double> getAllSettings() throws SQLException {
        Map<String, Double> settings = new HashMap<>();
        String sql = "SELECT config_key, config_value FROM system_settings";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

        	// Go through all rows and fill the map
            while (rs.next()) {
                String key = rs.getString(1); // Use column index instead of name
                double value = rs.getDouble(2); // Use column index instead of name
                settings.put(key, value);
            }
        }
        return settings;
    }

    /**
     * Update a setting value
     * Rule: Decreases are NOT allowed (only increases)
     * 
     * @param configKey The setting key to update
     * @param newValue  The new value (must be >= current value)
     * @return true if update was successful, false if decrease attempted
     * @throws SQLException if database error occurs
     */
    public boolean updateSetting(String configKey, double newValue) throws SQLException {
        // First, get current value
        String selectSql = "SELECT config_value FROM system_settings WHERE config_key = ?";
        double currentValue = 0.0;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(selectSql)) {

            pstmt.setString(1, configKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentValue = rs.getDouble("config_value");
                } else {
                	// If the key wasn't found then there's a data integrity issue
                    throw new SQLException("Setting key not found: " + configKey);
                }
            }
        }

        // Validate: No decreases allowed
        if (newValue < currentValue) {
            return false; // Decrease attempted
        }

        // Update the value
        String updateSql = "UPDATE system_settings SET config_value = ? WHERE config_key = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setDouble(1, newValue);
            pstmt.setString(2, configKey);
            pstmt.executeUpdate();
        }

        return true; // Update successful
    }

    /**
     * Get a specific setting value
     * 
     * @param configKey The key of the setting to retrieve
     * @return The numeric value of the setting
     *
     * @throws SQLException if:
     *         • The key does not exist
     *         • A database error occurs
     */
    public double getSetting(String configKey) throws SQLException {
        String sql = "SELECT config_value FROM system_settings WHERE config_key = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, configKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("config_value");
                } else {
                	// Missing key indicates incomplete configuration
                    throw new SQLException("Setting key not found: " + configKey);
                }
            }
        }
    }
}
