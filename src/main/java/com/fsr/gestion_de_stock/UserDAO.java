package com.fsr.gestion_de_stock;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, storedHash)) {
                    int userId = rs.getInt("user_id");
                    List<String> roles = getUserRoles(conn, userId);
                    return new User(userId, username, roles);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> getUserRoles(Connection conn, int userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String rolesSql = "SELECT r.name FROM roles r JOIN user_roles ur ON r.role_id = ur.role_id WHERE ur.user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(rolesSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roles.add(rs.getString("name"));
            }
        }
        return roles;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                List<String> roles = getUserRoles(conn, userId);
                users.add(new User(userId, rs.getString("username"), roles));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void createUser(String username, String password, List<String> roles) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
            PreparedStatement pstmtUser = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, username);
            pstmtUser.setString(2, hashedPassword);
            pstmtUser.executeUpdate();

            ResultSet generatedKeys = pstmtUser.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                assignRolesToUser(conn, userId, roles);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void updateUser(int userId, String newPassword, List<String> newRoles) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            if (newPassword != null && !newPassword.isEmpty()) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, hashedPassword);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM user_roles WHERE user_id = ?")) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            assignRolesToUser(conn, userId, newRoles);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void assignRolesToUser(Connection conn, int userId, List<String> roles) throws SQLException {
        String sql = "INSERT INTO user_roles (user_id, role_id) SELECT ?, role_id FROM roles WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String role : roles) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, role);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void deleteUser(int userId) throws SQLException {
        if (userId == 1) {
            throw new SQLException("Impossible de supprimer l'administrateur par défaut.");
        }
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public boolean changeUserPassword(int userId, String oldPassword, String newPassword) throws SQLException {
        String sqlSelect = "SELECT password_hash FROM users WHERE user_id = ?";
        String storedHash = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                storedHash = rs.getString("password_hash");
            }
        }

        if (storedHash == null || !BCrypt.checkpw(oldPassword, storedHash)) {
            return false;
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sqlUpdate = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        }
    }

    public List<String> getAllRoleNames() {
        List<String> roles = new ArrayList<>();
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM roles ORDER BY name")) {
            while (rs.next()) {
                roles.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public void createRole(String roleName) throws SQLException {
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement("INSERT INTO roles (name) VALUES (?)")) {
            pstmt.setString(1, roleName);
            pstmt.executeUpdate();
        }
    }
}