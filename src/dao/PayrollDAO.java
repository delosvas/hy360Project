package dao;

import service.PayrollService;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * PayrollDAO
 * 
 * Data Access Object that it's responsible for writing payroll 
 * calculation results into the payroll_log table.
 */
public class PayrollDAO {

	/*
	 * Saves a single payroll calculation entry into the payroll_log table.
	 * 
	 * @param res A PayrollResult object containing all computed salary components
     * @throws SQLException if the INSERT operation fails
	 */
    public void saveLog(PayrollService.PayrollResult res) throws SQLException {
        String sql = "INSERT INTO payroll_log (emp_id, payment_date, base_salary, family_allowance, experience_allowance, research_allowance, library_allowance, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	// Bind parameters
            pstmt.setInt(1, res.getEmpId());
            pstmt.setDate(2, Date.valueOf(res.getDate()));
            pstmt.setDouble(3, res.getBase());
            pstmt.setDouble(4, res.getFamily());
            pstmt.setDouble(5, res.getExperience());
            pstmt.setDouble(6, res.getResearch());
            pstmt.setDouble(7, res.getLibrary());
            pstmt.setDouble(8, res.getTotal());

            pstmt.executeUpdate();
        }
    }
}
