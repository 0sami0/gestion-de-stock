package com.fsr.gestion_de_stock;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginViewController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Les champs ne peuvent pas être vides.");
            return;
        }

        User user = userDAO.authenticateUser(username, password);

        if (user != null) {
            statusLabel.setText("Connexion réussie !");
            launchMainApplication(user);
        } else {
            statusLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    private void launchMainApplication(User user) throws IOException {
        Stage mainStage = new Stage();
        FXMLLoader loader;

        if (user.getRoles().contains("ADMIN")) {
            loader = new FXMLLoader(getClass().getResource("AdminView.fxml"));
            mainStage.setTitle("Panneau d'Administration");
            mainStage.setScene(new Scene(loader.load(), 700, 500));
        } else if (user.getRoles().contains("MAGAZINIER")) {
            loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            mainStage.setTitle("Gestion de Stock - Faculté des Sciences");
            mainStage.setScene(new Scene(loader.load(), 1100, 750));
        } else {
            statusLabel.setText("L'utilisateur n'a aucun rôle valide pour accéder à l'application.");
            return;
        }

        Stage loginStage = (Stage) loginButton.getScene().getWindow();
        mainStage.show();
        loginStage.close();
    }
}