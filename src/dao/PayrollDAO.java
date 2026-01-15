package dao;

import service.PayrollService;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PayrollDAO {

    public void saveLog(PayrollService.PayrollResult res) throws SQLException {
        String sql = "INSERT INTO payroll_log (emp_id, payment_date, base_salary, family_allowance, experience_allowance, research_allowance, library_allowance, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, res.empId);
            pstmt.setDate(2, Date.valueOf(res.date));
            pstmt.setDouble(3, res.base);
            pstmt.setDouble(4, res.family);
            pstmt.setDouble(5, res.experience);
            pstmt.setDouble(6, res.research);
            pstmt.setDouble(7, res.library);
            pstmt.setDouble(8, res.total);

            pstmt.executeUpdate();
        }
    }
}
