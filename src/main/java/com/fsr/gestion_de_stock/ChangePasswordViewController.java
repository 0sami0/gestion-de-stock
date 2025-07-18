package com.fsr.gestion_de_stock;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ChangePasswordViewController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
    }

    @FXML
    private void handleSave() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            statusLabel.setText("Tous les champs sont obligatoires.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            statusLabel.setText("Le nouveau mot de passe ne correspond pas à la confirmation.");
            return;
        }

        try {
            boolean success = userDAO.changeUserPassword(currentUser.getId(), oldPass, newPass);
            if (success) {
                statusLabel.setText("Mot de passe changé avec succès !");
                // Optional: Close window after a delay or on button click
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.close();
            } else {
                statusLabel.setText("L'ancien mot de passe est incorrect.");
            }
        } catch (SQLException e) {
            statusLabel.setText("Erreur de base de données.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}