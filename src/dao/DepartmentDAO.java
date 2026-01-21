package dao;

import model.Department;
import util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * DepartmentDAO
 * 
 * Data Access Object responsible for retrieving department info
 * from the DB.
 * It's used mainly by the AddEmployeeDialog and the UpdateEmployeeDialog
 */
public class DepartmentDAO {

	/*
	 * Function that retrieves all the departments from the database
	 * 
	 * @return a List<Department> containing all department records
     * @throws SQLException if any database error occurs
	 */
    public List<Department> getAllDepartments() throws SQLException {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments"; // SQL query for all records

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) { // Convert each row into a Department Object
                departments.add(new Department(
                        rs.getInt("dept_id"),
                        rs.getString("name")));
            }
        }
        return departments; // return the list
    }
}
