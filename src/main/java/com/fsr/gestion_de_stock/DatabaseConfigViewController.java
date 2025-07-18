package com.fsr.gestion_de_stock;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DatabaseConfigViewController {
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField nameField;
    @FXML private TextField userField;
    @FXML private PasswordField passField;

    private ConfigManager configManager;
    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        configManager = App.getConfigManager();
        hostField.setText(configManager.getProperty("db.host"));
        portField.setText(configManager.getProperty("db.port"));
        nameField.setText(configManager.getProperty("db.name"));
        userField.setText(configManager.getProperty("db.user"));
        passField.setText(configManager.getProperty("db.pass"));
    }

    @FXML
    private void handleSaveAndRestart() {
        configManager.setProperty("db.host", hostField.getText());
        configManager.setProperty("db.port", portField.getText());
        configManager.setProperty("db.name", nameField.getText());
        configManager.setProperty("db.user", userField.getText());
        configManager.setProperty("db.pass", passField.getText());
        configManager.save();

        new Alert(Alert.AlertType.INFORMATION, "Configuration sauvegardée. Tentative de reconnexion...").showAndWait();

        DatabaseManager.closeConnection();
        app.launch();
    }
}