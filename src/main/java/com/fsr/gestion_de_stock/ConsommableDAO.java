package com.fsr.gestion_de_stock;

import java.sql.*;
import java.time.LocalDate;

public class ConsommableDAO {

    public int dispatchConsommable(int arrivalId, int quantity, LocalDate dispatchDate, long destinationId, String recipientName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            String insertDispatchSql = "INSERT INTO dispatches (arrival_id, dispatched_quantity, dispatch_date, destination_id, recipient_name) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(insertDispatchSql, Statement.RETURN_GENERATED_KEYS);
            pstmtInsert.setInt(1, arrivalId);
            pstmtInsert.setInt(2, quantity);
            pstmtInsert.setString(3, dispatchDate.toString());
            pstmtInsert.setLong(4, destinationId);
            pstmtInsert.setString(5, recipientName);
            pstmtInsert.executeUpdate();

            ResultSet rs = pstmtInsert.getGeneratedKeys();
            int dispatchId = rs.next() ? rs.getInt(1) : -1;

            String updateArrivalSql = "UPDATE stock_arrivals SET available_quantity = available_quantity - ? WHERE arrival_id = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateArrivalSql)) {
                pstmtUpdate.setInt(1, quantity);
                pstmtUpdate.setInt(2, arrivalId);
                pstmtUpdate.executeUpdate();
            }
            conn.commit();
            return dispatchId;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}