package com.fsr.gestion_de_stock;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class DatabaseManager {

    private static Connection connection;
    private static ConfigManager configManager;

    public static void loadConfig() {
        if (configManager == null) {
            configManager = new ConfigManager();
        }
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                ConfigManager config = App.getConfigManager();
                String dbUrl = "jdbc:mysql://" + config.getProperty("db.host") + ":"
                        + config.getProperty("db.port") + "/" + config.getProperty("db.name")
                        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

                String dbUser = config.getProperty("db.user");
                String dbPass = config.getProperty("db.pass");

                connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                System.out.println("Connection to MySQL has been established.");

            } catch (SQLException e) {
                System.err.println("Failed to connect to MySQL. Check config or if server is running.");
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeDatabase() throws SQLException {
        String sqlSuppliers = "CREATE TABLE IF NOT EXISTS suppliers (supplier_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE) ENGINE=InnoDB;";
        String sqlDepartments = "CREATE TABLE IF NOT EXISTS departments (department_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE) ENGINE=InnoDB;";
        String sqlRoles = "CREATE TABLE IF NOT EXISTS roles (role_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE) ENGINE=InnoDB;";
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL) ENGINE=InnoDB;";
        String sqlUserRoles = "CREATE TABLE IF NOT EXISTS user_roles (user_id INT, role_id INT, PRIMARY KEY (user_id, role_id), FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE) ENGINE=InnoDB;";
        String sqlStockArrivals = "CREATE TABLE IF NOT EXISTS stock_arrivals (arrival_id INT AUTO_INCREMENT PRIMARY KEY, description TEXT NOT NULL, category ENUM('Matériel', 'Consommable') NOT NULL, arrival_date DATE NOT NULL, purchase_order_ref VARCHAR(100), initial_quantity INT NOT NULL, available_quantity INT NOT NULL, supplier_id INT, initial_department_id INT, observations TEXT, inventory_prefix VARCHAR(10), inventory_start_num INT, inventory_year INT, FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id), FOREIGN KEY (initial_department_id) REFERENCES departments(department_id)) ENGINE=InnoDB;";
        String sqlDispatches = "CREATE TABLE IF NOT EXISTS dispatches ("
                + " dispatch_id INT AUTO_INCREMENT PRIMARY KEY,"
                + " arrival_id INT NOT NULL,"
                + " dispatched_quantity INT NOT NULL,"
                + " dispatch_date DATE NOT NULL,"
                + " destination_id INT NOT NULL,"
                + " recipient_name VARCHAR(255) NOT NULL,"
                + " FOREIGN KEY (arrival_id) REFERENCES stock_arrivals(arrival_id),"
                + " FOREIGN KEY (destination_id) REFERENCES departments(department_id)"
                + ") ENGINE=InnoDB;";
        String sqlInventoryItems = "CREATE TABLE IF NOT EXISTS inventory_items ("
                + " item_id INT AUTO_INCREMENT PRIMARY KEY,"
                + " arrival_id INT NOT NULL,"
                + " inventory_number VARCHAR(50) NOT NULL UNIQUE,"
                + " serial_number VARCHAR(100),"
                + " dispatch_id INT NULL," // Link to the dispatch event
                + " status ENUM('In Stock', 'Dispatched', 'Reformed') NOT NULL,"
                + " FOREIGN KEY (arrival_id) REFERENCES stock_arrivals(arrival_id),"
                + " FOREIGN KEY (dispatch_id) REFERENCES dispatches(dispatch_id)"
                + ") ENGINE=InnoDB;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // THE DESTRUCTIVE 'DROP TABLE' LINES HAVE BEEN REMOVED

            stmt.execute(sqlSuppliers);
            stmt.execute(sqlDepartments);
            stmt.execute(sqlRoles);
            stmt.execute(sqlUsers);
            stmt.execute(sqlUserRoles);
            stmt.execute(sqlStockArrivals);
            stmt.execute(sqlDispatches);
            stmt.execute(sqlInventoryItems);
            createDefaultRolesAndAdmin(conn);
        }
    }

    private static void createDefaultRolesAndAdmin(Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO roles (name) VALUES (?), (?)")) {
            pstmt.setString(1, "ADMIN");
            pstmt.setString(2, "MAGAZINIER");
            pstmt.executeUpdate();
        }

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("No users found. Creating default admin user.");
                String hashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt());

                try (PreparedStatement pstmtUser = conn.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    pstmtUser.setString(1, "admin");
                    pstmtUser.setString(2, hashedPassword);
                    pstmtUser.executeUpdate();

                    try (ResultSet generatedKeys = pstmtUser.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long userId = generatedKeys.getLong(1);
                            try (PreparedStatement pstmtRole = conn.prepareStatement("INSERT INTO user_roles (user_id, role_id) SELECT ?, role_id FROM roles WHERE name = 'ADMIN'")) {
                                pstmtRole.setLong(1, userId);
                                pstmtRole.executeUpdate();
                                System.out.println("Default admin user created with username 'admin' and password 'admin'.");
                            }
                        }
                    }
                }
            }
        }
    }
}