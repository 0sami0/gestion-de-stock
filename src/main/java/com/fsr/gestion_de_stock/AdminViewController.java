package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.CheckListView;

import java.sql.SQLException;
import java.util.List;

public class AdminViewController {
    @FXML private ListView<User> userListView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckListView<String> rolesCheckListView;
    @FXML private Button createUserButton;
    @FXML private Button updateUserButton;
    @FXML private Button deleteUserButton;
    @FXML private ListView<String> allRolesListView;
    @FXML private TextField newRoleField;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<String> allRolesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        userListView.setItems(userList);
        allRolesListView.setItems(allRolesList);
        rolesCheckListView.setItems(allRolesList);

        loadData();

        userListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> populateUserDetails(newSelection)
        );

        updateUserButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
        deleteUserButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
    }

    private void loadData() {
        userList.setAll(userDAO.getAllUsers());
        allRolesList.setAll(userDAO.getAllRoleNames());
        clearFields();
    }

    private void clearFields() {
        userListView.getSelectionModel().clearSelection();
        usernameField.clear();
        usernameField.setDisable(false);
        passwordField.clear();
        rolesCheckListView.getCheckModel().clearChecks();
    }

    private void populateUserDetails(User user) {
        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setDisable(true);
            passwordField.clear();
            rolesCheckListView.getCheckModel().clearChecks();
            for (String role : user.getRoles()) {
                rolesCheckListView.getCheckModel().check(role);
            }
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleCreateUser() {
        try {
            List<String> selectedRoles = rolesCheckListView.getCheckModel().getCheckedItems();
            userDAO.createUser(usernameField.getText(), passwordField.getText(), selectedRoles);
            loadData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de créer l'utilisateur.");
        }
    }

    @FXML
    private void handleUpdateUser() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        try {
            List<String> selectedRoles = rolesCheckListView.getCheckModel().getCheckedItems();
            userDAO.updateUser(selectedUser.getId(), passwordField.getText(), selectedRoles);
            loadData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de mettre à jour l'utilisateur.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        try {
            userDAO.deleteUser(selectedUser.getId());
            loadData();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCreateRole() {
        String roleName = newRoleField.getText();
        if (roleName == null || roleName.trim().isEmpty()) {
            showAlert("Erreur", "Le nom du rôle ne peut pas être vide.");
            return;
        }
        try {
            userDAO.createRole(roleName.trim().toUpperCase());
            newRoleField.clear();
            loadData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de créer le rôle. Il existe peut-être déjà.");
        }
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}