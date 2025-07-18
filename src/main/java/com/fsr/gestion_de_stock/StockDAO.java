package com.fsr.gestion_de_stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class StockDAO {
    private static final Logger log = LoggerFactory.getLogger(StockDAO.class);

// In StockDAO.java

    public List<StockItemView> getAllStockForView() {
        String sql = "SELECT " +
                "sa.arrival_id, " +
                "CASE " +
                // For SQLite, use PRINTF. For MySQL, use LPAD. Let's use LPAD for MySQL.
                "  WHEN sa.category = 'Consommable' THEN 'N/A' " +
                "  WHEN sa.initial_quantity = 1 THEN CONCAT(sa.inventory_prefix, ' ', LPAD(sa.inventory_start_num, 7, '0'), '/', sa.inventory_year) " +
                "  ELSE CONCAT(sa.inventory_prefix, ' ', LPAD(sa.inventory_start_num, 7, '0'), '-', LPAD((sa.inventory_start_num + sa.initial_quantity - 1), 7, '0'), '/', sa.inventory_year) " +
                "END AS inventory_number, " +
                "sa.description, " +
                "d.name AS affectation, " +
                "sa.arrival_date, " +
                "sa.available_quantity, " +
                "s.name AS supplier, " +
                "sa.observations, " +
                "sa.purchase_order_ref, " +
                "sa.category " + // THIS LINE IS NOW CORRECT
                "FROM stock_arrivals sa " +
                "LEFT JOIN suppliers s ON sa.supplier_id = s.supplier_id " +
                "LEFT JOIN departments d ON sa.initial_department_id = d.department_id " +
                "ORDER BY sa.arrival_id DESC";

        List<StockItemView> stockList = new ArrayList<>();
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stockList.add(new StockItemView(
                        rs.getInt("arrival_id"),
                        rs.getString("inventory_number"),
                        rs.getString("description"),
                        rs.getString("affectation"),
                        rs.getString("arrival_date"),
                        rs.getInt("available_quantity"),
                        rs.getString("supplier"),
                        rs.getString("observations"),
                        rs.getString("purchase_order_ref"),
                        rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // This is what printed the error to your console
        }
        return stockList;
    }

    public void addStockArrival(String description, String category, LocalDate date, String poRef, int quantity,
                                String supplierName, String departmentName, String observations, List<String> serialNumbers) throws SQLException {
        long supplierId = getOrCreateId("suppliers", "name", "supplier_id", supplierName);
        long departmentId = getOrCreateId("departments", "name", "department_id", departmentName);
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            int lastInventoryNum = 0;
            String prefix = null;
            int year = 0;
            if ("Matériel".equals(category)) {
                lastInventoryNum = getLastInventoryNumber();
                prefix = "FSR";
                year = date.getYear() % 100;
            }
            String sqlArrival = "INSERT INTO stock_arrivals(description, category, arrival_date, purchase_order_ref, initial_quantity, available_quantity, supplier_id, initial_department_id, observations, inventory_prefix, inventory_start_num, inventory_year) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmtArrival = conn.prepareStatement(sqlArrival, Statement.RETURN_GENERATED_KEYS)) {
                pstmtArrival.setString(1, description);
                pstmtArrival.setString(2, category);
                pstmtArrival.setString(3, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                pstmtArrival.setString(4, poRef);
                pstmtArrival.setInt(5, quantity);
                pstmtArrival.setInt(6, quantity);
                if (supplierId != -1) pstmtArrival.setLong(7, supplierId); else pstmtArrival.setNull(7, Types.INTEGER);
                if (departmentId != -1) pstmtArrival.setLong(8, departmentId); else pstmtArrival.setNull(8, Types.INTEGER);
                pstmtArrival.setString(9, observations);
                pstmtArrival.setString(10, prefix);
                if ("Matériel".equals(category)) pstmtArrival.setInt(11, lastInventoryNum + 1); else pstmtArrival.setNull(11, Types.INTEGER);
                if ("Matériel".equals(category)) pstmtArrival.setInt(12, year); else pstmtArrival.setNull(12, Types.INTEGER);
                pstmtArrival.executeUpdate();
                if ("Matériel".equals(category)) {
                    ResultSet generatedKeys = pstmtArrival.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        long arrivalId = generatedKeys.getLong(1);
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
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public int dispatchMultipleItems(int arrivalId, List<Integer> itemIds, LocalDate dispatchDate, String destinationName, String recipientName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            long destinationId = getOrCreateId("departments", "name", "department_id", destinationName);

            String insertDispatchSql = "INSERT INTO dispatches (arrival_id, dispatched_quantity, dispatch_date, destination_id, recipient_name) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(insertDispatchSql, Statement.RETURN_GENERATED_KEYS);
            pstmtInsert.setInt(1, arrivalId);
            pstmtInsert.setInt(2, itemIds.size());
            pstmtInsert.setString(3, dispatchDate.toString());
            pstmtInsert.setLong(4, destinationId);
            pstmtInsert.setString(5, recipientName);
            pstmtInsert.executeUpdate();

            ResultSet rs = pstmtInsert.getGeneratedKeys();
            int dispatchId = -1;
            if (rs.next()) dispatchId = rs.getInt(1);

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

    public int dispatchConsumable(int arrivalId, int quantity, LocalDate dispatchDate, String destinationName, String recipientName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            long destinationId = getOrCreateId("departments", "name", "department_id", destinationName);
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


    public HashMap<String, String> getDispatchDetailsForDocument(int dispatchId) {
        log.info("Fetching document details for dispatch ID: {}", dispatchId);
        HashMap<String, String> details = new HashMap<>();

        String dispatchSql = "SELECT " +
                "d.recipient_name, d.dispatch_date, dept.name as destination, " +
                "sa.description, sa.purchase_order_ref, s.name as supplier, d.dispatched_quantity, sa.category " +
                "FROM dispatches d " +
                "JOIN stock_arrivals sa ON d.arrival_id = sa.arrival_id " +
                "LEFT JOIN suppliers s ON s.supplier_id = sa.supplier_id " +
                "LEFT JOIN departments dept ON dept.department_id = d.destination_id " +
                "WHERE d.dispatch_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(dispatchSql)) {
                pstmt.setInt(1, dispatchId);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) {
                    details.put("MARCHE_BC", rs.getString("purchase_order_ref") != null ? rs.getString("purchase_order_ref") : "");
                    details.put("SOCIETE", rs.getString("supplier") != null ? rs.getString("supplier") : "");
                    details.put("DESTINATION", rs.getString("destination") != null ? rs.getString("destination") : "");
                    details.put("DATE", rs.getString("dispatch_date"));
                    details.put("NOM_PRENOM", rs.getString("recipient_name"));
                    details.put("DESIGNATION", rs.getString("description"));
                    details.put("AFFECTATION", rs.getString("destination") != null ? rs.getString("destination") : "");
                    details.put("QUANTITE", String.valueOf(rs.getInt("dispatched_quantity")));
                    if ("Matériel".equals(rs.getString("category"))) {
                        String itemsSql = "SELECT inventory_number, serial_number FROM inventory_items WHERE dispatch_id = ?";
                        List<String> inventoryNumbers = new ArrayList<>();
                        List<String> serialNumbers = new ArrayList<>();
                        try(PreparedStatement itemsPstmt = conn.prepareStatement(itemsSql)) {
                            itemsPstmt.setInt(1, dispatchId);
                            ResultSet itemsRs = itemsPstmt.executeQuery();
                            while(itemsRs.next()) {
                                inventoryNumbers.add(itemsRs.getString("inventory_number"));
                                String serial = itemsRs.getString("serial_number");
                                serialNumbers.add(serial != null ? serial : "N/A");
                            }
                        }
                        details.put("N_INVENTAIRE", String.join("\n", inventoryNumbers));
                        details.put("N_SERIE", String.join("\n", serialNumbers));
                    } else {
                        details.put("N_INVENTAIRE", "");
                        details.put("N_SERIE", "");
                    }
                } else {
                    log.warn("No dispatch details found for dispatch_id: {}", dispatchId);
                    return null;
                }
            }

            log.info("Successfully fetched and formatted details for document: {}", details);
            return details;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DispatchRecord> getDispatchHistory(int arrivalId) {
        List<DispatchRecord> history = new ArrayList<>();
        String sql = "SELECT d.dispatch_date, d.dispatched_quantity, d.recipient_name, dept.name AS destination, s.name as supplier, sa.purchase_order_ref, GROUP_CONCAT(ii.inventory_number SEPARATOR ', ') as inventory_numbers, GROUP_CONCAT(ii.serial_number SEPARATOR ', ') as serial_numbers FROM dispatches d JOIN stock_arrivals sa ON d.arrival_id = sa.arrival_id LEFT JOIN departments dept ON d.destination_id = dept.department_id LEFT JOIN suppliers s ON sa.supplier_id = s.supplier_id LEFT JOIN inventory_items ii ON d.dispatch_id = ii.dispatch_id WHERE d.arrival_id = ? GROUP BY d.dispatch_id ORDER BY d.dispatch_date DESC";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, arrivalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String invNumbers = rs.getString("inventory_numbers");
                if (invNumbers == null) invNumbers = "N/A (Consommable)";
                history.add(new DispatchRecord(
                        rs.getString("dispatch_date"),
                        rs.getInt("dispatched_quantity"),
                        invNumbers,
                        rs.getString("serial_numbers"),
                        rs.getString("destination"),
                        rs.getString("recipient_name"),
                        rs.getString("purchase_order_ref"),
                        rs.getString("supplier")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
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

    // In StockDAO.java

    public StockArrival getArrivalById(int arrivalId) {
        String sql = "SELECT * FROM stock_arrivals WHERE arrival_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, arrivalId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new StockArrival(
                        rs.getInt("arrival_id"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getDate("arrival_date").toLocalDate(),
                        rs.getString("purchase_order_ref"),
                        rs.getInt("initial_quantity"),
                        rs.getInt("available_quantity"),
                        rs.getString("observations"),
                        rs.getInt("supplier_id"),
                        rs.getInt("initial_department_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStockArrival(int arrivalId, String description, LocalDate date, String poRef, String supplierName, String departmentName, String observations) throws SQLException {
        long supplierId = getOrCreateId("suppliers", "name", "supplier_id", supplierName);
        long departmentId = getOrCreateId("departments", "name", "department_id", departmentName);

        String sql = "UPDATE stock_arrivals SET description = ?, arrival_date = ?, purchase_order_ref = ?, supplier_id = ?, initial_department_id = ?, observations = ? WHERE arrival_id = ?";
        try(PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setString(3, poRef);
            if(supplierId != -1) pstmt.setLong(4, supplierId); else pstmt.setNull(4, Types.INTEGER);
            if(departmentId != -1) pstmt.setLong(5, departmentId); else pstmt.setNull(5, Types.INTEGER);
            pstmt.setString(6, observations);
            pstmt.setInt(7, arrivalId);
            pstmt.executeUpdate();
        }
    }

    private long getOrCreateId(String tableName, String nameColumn, String idColumn, String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) return -1;
        String selectSql = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + nameColumn + " = ?";
        String insertSql = "INSERT INTO " + tableName + "(" + nameColumn + ") VALUES(?)";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(idColumn);
            }
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, name);
            insertStmt.executeUpdate();
            ResultSet rs = insertStmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to create or find ID for " + name);
    }

    private int getLastInventoryNumber() throws SQLException {
        String sql = "SELECT MAX(inventory_start_num + initial_quantity - 1) as max_num FROM stock_arrivals WHERE category = 'Matériel' AND inventory_start_num IS NOT NULL";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("max_num");
            }
        }
        return 20000;
    }

    private boolean isSupplierInUse(long supplierId) throws SQLException {
        String sql = "SELECT 1 FROM stock_arrivals WHERE supplier_id = ? LIMIT 1";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isDepartmentInUse(long departmentId) throws SQLException {
        String sqlArrivals = "SELECT 1 FROM stock_arrivals WHERE initial_department_id = ? LIMIT 1";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sqlArrivals)) {
            pstmt.setLong(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        String sqlDispatches = "SELECT 1 FROM dispatches WHERE destination_id = ? LIMIT 1";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sqlDispatches)) {
            pstmt.setLong(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteSupplier(String name) throws SQLException {
        long supplierId = getOrCreateId("suppliers", "name", "supplier_id", name);
        if (isSupplierInUse(supplierId)) {
            throw new SQLException("Ce fournisseur est utilisé et ne peut pas être supprimé.");
        }
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, supplierId);
            pstmt.executeUpdate();
        }
    }

    public void deleteDepartment(String name) throws SQLException {
        long departmentId = getOrCreateId("departments", "name", "department_id", name);
        if (isDepartmentInUse(departmentId)) {
            throw new SQLException("Ce département est utilisé et ne peut pas être supprimé.");
        }
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, departmentId);
            pstmt.executeUpdate();
        }
    }

    public void updateSupplier(String oldName, String newName) throws SQLException {
        long oldId = getOrCreateId("suppliers", "name", "supplier_id", oldName);
        long existingNewId = getOrCreateId("suppliers", "name", "supplier_id", newName);
        if (existingNewId != -1 && existingNewId != oldId) {
            String updateArrivalsSql = "UPDATE stock_arrivals SET supplier_id = ? WHERE supplier_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(updateArrivalsSql)) {
                pstmt.setLong(1, existingNewId);
                pstmt.setLong(2, oldId);
                pstmt.executeUpdate();
            }
            deleteSupplier(oldName);
        } else {
            String sql = "UPDATE suppliers SET name = ? WHERE supplier_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, newName);
                pstmt.setLong(2, oldId);
                pstmt.executeUpdate();
            }
        }
    }

    public void updateDepartment(String oldName, String newName) throws SQLException {
        long oldId = getOrCreateId("departments", "name", "department_id", oldName);
        long existingNewId = getOrCreateId("departments", "name", "department_id", newName);
        if (existingNewId != -1 && existingNewId != oldId) {
            String updateArrivalsSql = "UPDATE stock_arrivals SET initial_department_id = ? WHERE initial_department_id = ?";
            String updateDispatchesSql = "UPDATE dispatches SET destination_id = ? WHERE destination_id = ?";
            try (PreparedStatement pstmtArrivals = DatabaseManager.getConnection().prepareStatement(updateArrivalsSql);
                 PreparedStatement pstmtDispatches = DatabaseManager.getConnection().prepareStatement(updateDispatchesSql)) {
                pstmtArrivals.setLong(1, existingNewId);
                pstmtArrivals.setLong(2, oldId);
                pstmtArrivals.executeUpdate();
                pstmtDispatches.setLong(1, existingNewId);
                pstmtDispatches.setLong(2, oldId);
                pstmtDispatches.executeUpdate();
            }
            deleteDepartment(oldName);
        } else {
            String sql = "UPDATE departments SET name = ? WHERE department_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, newName);
                pstmt.setLong(2, oldId);
                pstmt.executeUpdate();
            }
        }
    }

    public List<String> getAllSupplierNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM suppliers ORDER BY name";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public List<String> getAllDepartmentNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM departments ORDER BY name";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public void addSupplier(String name) throws SQLException {
        String sql = "INSERT INTO suppliers(name) VALUES(?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public void addDepartment(String name) throws SQLException {
        String sql = "INSERT INTO departments(name) VALUES(?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public int getTotalItemsInStock() {
        String sql = "SELECT SUM(available_quantity) FROM stock_arrivals";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getDispatchedThisMonth() {
        String sql = "SELECT SUM(dispatched_quantity) FROM dispatches WHERE STRFTIME('%Y-%m', dispatch_date) = STRFTIME('%Y-%m', 'now')";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // For MySQL, the date function is different
    public int getDispatchedThisMonthMySQL() {
        String sql = "SELECT SUM(dispatched_quantity) FROM dispatches WHERE YEAR(dispatch_date) = YEAR(CURDATE()) AND MONTH(dispatch_date) = MONTH(CURDATE())";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveSuppliersCount() {
        String sql = "SELECT COUNT(*) FROM suppliers";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT category, SUM(available_quantity) as total FROM stock_arrivals GROUP BY category";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                distribution.put(rs.getString("category"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    public Map<String, Integer> getTopFiveItems() {
        Map<String, Integer> topItems = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        String sql = "SELECT description, SUM(available_quantity) as total FROM stock_arrivals GROUP BY description ORDER BY total DESC LIMIT 5";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                topItems.put(rs.getString("description"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topItems;
    }
    public String getSupplierNameById(int supplierId) {
        if (supplierId == 0) return null;
        String sql = "SELECT name FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getDepartmentNameById(int departmentId) {
        if (departmentId == 0) return null;
        String sql = "SELECT name FROM departments WHERE department_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void deleteStockArrival(int arrivalId) throws SQLException {
        String checkSql = "SELECT 1 FROM inventory_items WHERE arrival_id = ? AND status = 'Dispatched' LIMIT 1";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(checkSql)) {
            pstmt.setInt(1, arrivalId);
            if (pstmt.executeQuery().next()) {
                throw new SQLException("Impossible de supprimer cette entrée car un ou plusieurs articles associés ont déjà été sortis.");
            }
        }

        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            // Must delete from child tables first due to foreign key constraints
            String deleteDispatchesSql = "DELETE FROM dispatches WHERE arrival_id = ?";
            try(PreparedStatement pstmt = conn.prepareStatement(deleteDispatchesSql)) {
                pstmt.setInt(1, arrivalId);
                pstmt.executeUpdate();
            }

            String deleteItemsSql = "DELETE FROM inventory_items WHERE arrival_id = ?";
            try(PreparedStatement pstmt = conn.prepareStatement(deleteItemsSql)) {
                pstmt.setInt(1, arrivalId);
                pstmt.executeUpdate();
            }

            String deleteArrivalSql = "DELETE FROM stock_arrivals WHERE arrival_id = ?";
            try(PreparedStatement pstmt = conn.prepareStatement(deleteArrivalSql)) {
                pstmt.setInt(1, arrivalId);
                pstmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
