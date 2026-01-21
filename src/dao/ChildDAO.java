package dao;

import model.Child;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ChildDAO
 * 
 * Data Access Object responsible for all database operations in the 
 * children's table.
 * 
 * It's responsible for:
 * 1. inserting new children for an employee
 * 2. retrieving all children that belong to an employee
 * 3. count minor children
 * 4. deleting a specific child
 * 5. delete all children for an employee
 * 
 */
public class ChildDAO {

    /**
     * Add a child for an employee 
     * 
     * @param child Child object containing emp_id and birth_date
     * @return the generated child_id (primary key)
     * @throws SQLException if insertion fails
     */
    public int addChild(Child child) throws SQLException {
        String sql = "INSERT INTO children (emp_id, birth_date) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        	// Build Parameters
            pstmt.setInt(1, child.getEmpId());
            pstmt.setDate(2, Date.valueOf(child.getBirthDate()));

            int res = pstmt.executeUpdate(); // Insert

            if (res == 0) {
                throw new SQLException("Creating child failed, no rows affected.");
            }

            // Get generated primary key 
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return new childID
                } else {
                    throw new SQLException("Creating child failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Get all children for an employee
     * 
     * @param empId employee ID
     * @return list of Child objects
     * @throws SQLException if query fails
     */
    public List<Child> getChildrenByEmployeeId(int empId) throws SQLException {
        List<Child> children = new ArrayList<>();
        String sql = "SELECT * FROM children WHERE emp_id = ? ORDER BY birth_date";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);

            try (ResultSet rs = pstmt.executeQuery()) {
            	// Convert each row into a Child Object 
                while (rs.next()) {
                    Child child = new Child();
                    child.setChildId(rs.getInt("child_id"));
                    child.setEmpId(rs.getInt("emp_id"));
                    child.setBirthDate(rs.getDate("birth_date").toLocalDate());
                    children.add(child);
                }
            }
        }
        return children;
    }

    /**
     * Count minor children (<18 years) for an employee on a given date
     * 
     * @param empId employee ID
     * @param date  reference date (usually payroll date)
     * @return number of minor children
     * @throws SQLException if query fails
     */
    public int countMinorChildren(int empId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) as minor_count FROM children " +
                "WHERE emp_id = ? AND DATEDIFF(?, birth_date) / 365.25 < 18";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);
            pstmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("minor_count");
                }
            }
        }
        return 0;
    }

    /**
     * Delete a child
     * @param childId primary key of the child
     * @throws SQLException if deletion fails
     */
    public void deleteChild(int childId) throws SQLException {
        String sql = "DELETE FROM children WHERE child_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, childId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all children for an employee
     * 
     * @param empId employee ID
     * @throws SQLException if deletion fails
     */
    public void deleteChildrenByEmployeeId(int empId) throws SQLException {
        String sql = "DELETE FROM children WHERE emp_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);
            pstmt.executeUpdate();
        }
    }
}
