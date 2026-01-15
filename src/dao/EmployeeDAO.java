package dao;

import model.Employee;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public int addEmployee(Employee emp) throws SQLException {

        String sql = "INSERT INTO employees (full_name, emp_type, dept_id, is_married, address, phone, iban, bank_name, start_date, is_active) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

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

            int res = pstmt.executeUpdate();// returns number of rows affected

            if (res == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);// returns id
            } else {
                throw new SQLException("Creating employee failed, no ID obtained.");
            }
        } finally {
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String query = "SELECT * FROM employees";

        // ResultSet
        Connection conn = DBConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("emp_id"));
                e.setFullName(rs.getString("full_name"));
                e.setType(Employee.EmployeeType.valueOf(rs.getString("emp_type")));
                e.setDeptId(rs.getInt("dept_id"));
                e.setMarried(rs.getBoolean("is_married"));
                e.setAddress(rs.getString("address"));
                e.setPhone(rs.getString("phone"));
                e.setIban(rs.getString("iban"));
                e.setBankName(rs.getString("bank_name"));
                e.setStartDate(rs.getDate("start_date").toLocalDate());
                e.setActive(rs.getBoolean("is_active"));

                list.add(e);
            }
        } finally {
            // Close ResultSet and Statement
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public void deactivateEmployee(int empId) throws SQLException {
        String sql = "UPDATE employees SET is_active = FALSE WHERE emp_id = ?";
        Connection conn = DBConnection.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, empId);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateEmployee(Employee emp) throws SQLException {
        String sql = "UPDATE employees SET full_name=?, dept_id=?, is_married=?, address=?, phone=?, iban=?, bank_name=? WHERE emp_id=?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, emp.getFullName());
            pstmt.setInt(2, emp.getDeptId());
            pstmt.setBoolean(3, emp.isMarried());
            pstmt.setString(4, emp.getAddress());
            pstmt.setString(5, emp.getPhone());
            pstmt.setString(6, emp.getIban());
            pstmt.setString(7, emp.getBankName());
            pstmt.setInt(8, emp.getId());

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
