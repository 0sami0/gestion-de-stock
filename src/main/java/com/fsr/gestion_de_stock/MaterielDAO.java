package com.fsr.gestion_de_stock;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaterielDAO {

    public void addMaterielArrival(Connection conn, long arrivalId, int quantity, String prefix, int lastInventoryNum, int year, List<String> serialNumbers) throws SQLException {
        String sqlItem = "INSERT INTO inventory_items(arrival_id, inventory_number, serial_number, status) VALUES(?,?,?,?)";
        try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
            for (int i = 0; i < quantity; i++) {
                int currentNum = lastInventoryNum + 1 + i;
                String invNum = String.format("%s %07d/%d", prefix, currentNum, year);
                pstmtItem.setLong(1, arrivalId);
                pstmtItem.setString(2, invNum);
                pstmtItem.setString(3, serialNumbers.get(i));
                pstmtItem.setString(4, "In Stock");
                pstmtItem.addBatch();
            }
            pstmtItem.executeBatch();
        }
    }

    public int dispatchMultipleItems(int arrivalId, List<Integer> itemIds, LocalDate dispatchDate, long destinationId, String recipientName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            String insertDispatchSql = "INSERT INTO dispatches (arrival_id, dispatched_quantity, dispatch_date, destination_id, recipient_name) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(insertDispatchSql, Statement.RETURN_GENERATED_KEYS);
            pstmtInsert.setInt(1, arrivalId);
            pstmtInsert.setInt(2, itemIds.size());
            pstmtInsert.setString(3, dispatchDate.toString());
            pstmtInsert.setLong(4, destinationId);
            pstmtInsert.setString(5, recipientName);
            pstmtInsert.executeUpdate();

            ResultSet rs = pstmtInsert.getGeneratedKeys();
            int dispatchId = rs.next() ? rs.getInt(1) : -1;

            String updateItemSql = "UPDATE inventory_items SET status = 'Dispatched', dispatch_id = ? WHERE item_id = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateItemSql)) {
                for (Integer itemId : itemIds) {
                    pstmtUpdate.setInt(1, dispatchId);
                    pstmtUpdate.setInt(2, itemId);
                    pstmtUpdate.addBatch();
                }
                pstmtUpdate.executeBatch();
            }

            String updateArrivalSql = "UPDATE stock_arrivals SET available_quantity = available_quantity - ? WHERE arrival_id = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateArrivalSql)) {
                pstmtUpdate.setInt(1, itemIds.size());
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

    public List<InventoryItem> getAvailableItemsByArrival(int arrivalId) {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT item_id, inventory_number, serial_number FROM inventory_items WHERE arrival_id = ? AND status = 'In Stock' ORDER BY item_id";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, arrivalId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                items.add(new InventoryItem(rs.getInt("item_id"), rs.getString("inventory_number"), rs.getString("serial_number")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}