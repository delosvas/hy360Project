package dao;

import model.Contract;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class ContractDAO {

    public void addContract(Contract contract) throws SQLException {
        String sql = "INSERT INTO contracts (emp_id, start_date, end_date, gross_salary) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, contract.getEmployeeId());
            pstmt.setDate(2, Date.valueOf(contract.getStartDate()));
            pstmt.setDate(3, Date.valueOf(contract.getEndDate()));
            pstmt.setDouble(4, contract.getGrossSalary());

            pstmt.executeUpdate();
        }
    }

    public static Contract getActiveContract(int empId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM contracts WHERE emp_id = ? AND ? BETWEEN start_date AND end_date ORDER BY start_date DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, empId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Contract c = new Contract();
                    c.setId(rs.getInt("contract_id"));
                    c.setEmployeeId(rs.getInt("emp_id"));
                    c.setStartDate(rs.getDate("start_date").toLocalDate());
                    c.setEndDate(rs.getDate("end_date").toLocalDate());
                    c.setGrossSalary(rs.getDouble("gross_salary"));
                    return c;
                }
            }
        }
        return null;
    }
}
