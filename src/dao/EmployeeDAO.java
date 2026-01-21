package dao;

import model.Contract;
import model.Employee;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/*
 * EmployeeDAO
 * 
 * Data Access Object that is responsible for creating new employees, 
 * retrieving employees(either all of them either by specific ID, updates 
 * employee info and deactivates employees.
 */
public class EmployeeDAO {

	/*
	 * Inserts a new employee into the DB.
	 * 
	 * @param emp Employee object containing all required fields
     * @return the generated employee ID (primary key)
     * @throws SQLException if insertion fails
	 */
    public int addEmployee(Employee emp) throws SQLException {
        // SQL query to insert new employee (child_count removed - use children table)
        String sql = "INSERT INTO employees (full_name, emp_type, dept_id, is_married, address, phone, iban, bank_name, start_date, is_active) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        	// Bind parameters
            pstmt.setString(1, emp.getFullName());
            pstmt.setString(2, emp.getType().name());
            pstmt.setInt(3, emp.getDeptId());
            pstmt.setBoolean(4, emp.isMarried());
            pstmt.setString(5, emp.getAddress());
            pstmt.setString(6, emp.getPhone());
            pstmt.setString(7, emp.getIban());
            pstmt.setString(8, emp.getBankName());
            pstmt.setDate(9, Date.valueOf(emp.getStartDate()));
            pstmt.setBoolean(10, emp.isActive());

            int res = pstmt.executeUpdate();

            if (res == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            // Retrieve employee ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
        }
    }

    /*
     * Retrieves all the employees from the database.
     * 
     * @return List<Employee> containing all employees
     * @throws SQLException if query fails
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String query = "SELECT * FROM employees";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) { // Convert each row into an Employee Object
                Employee e = new Employee();
                e.setId(rs.getInt("emp_id"));
                e.setFullName(rs.getString("full_name"));
                e.setType(Employee.EmployeeType.valueOf(rs.getString("emp_type")));
                e.setDeptId(rs.getInt("dept_id"));
                e.setMarried(rs.getBoolean("is_married"));
                // child_count removed - use ChildDAO to get children
                e.setAddress(rs.getString("address"));
                e.setPhone(rs.getString("phone"));
                e.setIban(rs.getString("iban"));
                e.setBankName(rs.getString("bank_name"));
                // convert date to localdate
                e.setStartDate(rs.getDate("start_date").toLocalDate());
                e.setActive(rs.getBoolean("is_active"));
  
                list.add(e);
            }
            
            for(Employee e: list)
            	if(e.getType() == Employee.EmployeeType.CA || e.getType() == Employee.EmployeeType.CT) {
            		Contract active= ContractDAO.getActiveContract(e.getId(), LocalDate.now());
            		// In case that there's no active contract, employee active status is set to inactive
            		e.setActive(active!=null);
            	}
        	}
        
        return list;
    }
   
    /*
     * Get a single employee based on the ID given by the user
     * 
     * @param emp_id employee ID
     * @return Employee object or null if not found
     * @throws SQLException if query fails
     */
    public Employee getEmployeeById(int emp_id) throws SQLException {
    	String sql="SELECT * FROM employees WHERE emp_id = ?";
    	try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
                
    		pstmt.setInt(1, emp_id);
    		ResultSet rs = pstmt.executeQuery();
    			
    		if(rs.next()) {
    			Employee e = new Employee();
    			e.setId(rs.getInt("emp_id"));
                e.setFullName(rs.getString("full_name"));
                e.setType(Employee.EmployeeType.valueOf(rs.getString("emp_type")));
                e.setDeptId(rs.getInt("dept_id"));
                e.setMarried(rs.getBoolean("is_married"));
                // child_count removed - use ChildDAO to get children
                e.setAddress(rs.getString("address"));
                e.setPhone(rs.getString("phone"));
                e.setIban(rs.getString("iban"));
                e.setBankName(rs.getString("bank_name"));
                // convert date to localdate
                e.setStartDate(rs.getDate("start_date").toLocalDate());
                e.setActive(rs.getBoolean("is_active"));
                
                return e;   
    		}else {
    			return null;
    			
    		}
  	
    	}
		
    }

    /*
     * Changes active status field of an employee to inactive
     * 
     * @param empId employee ID
     * @throws SQLException if update fails 
     */
    public void deactivateEmployee(int empId) throws SQLException {
        String sql = "UPDATE employees SET is_active = FALSE WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * Updates editable employee fields
     * 
     * @param emp Employee object with updated values
     * @throws SQLException if update fails
     */
    public void updateEmployee(Employee emp) throws SQLException {
        String sql = "UPDATE employees SET full_name=?, dept_id=?, is_married=?, address=?, phone=?, iban=?, bank_name=? WHERE emp_id=?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	// Bind parameters
            pstmt.setString(1, emp.getFullName());
            pstmt.setInt(2, emp.getDeptId());
            pstmt.setBoolean(3, emp.isMarried());
            pstmt.setString(4, emp.getAddress());
            pstmt.setString(5, emp.getPhone());
            pstmt.setString(6, emp.getIban());
            pstmt.setString(7, emp.getBankName());
            pstmt.setInt(8, emp.getId());

            pstmt.executeUpdate();
        }
    }
}
