package com.fsr.gestion_de_stock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static ConfigManager configManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        configManager = new ConfigManager();
        launch();
    }

    public void launch() {
        try {
            DatabaseManager.closeConnection();
            DatabaseManager.initializeDatabase();
            showLogin(primaryStage);
        } catch (Exception e) {
            System.err.println("Database connection failed. Showing config screen.");
            showDatabaseConfig(primaryStage);
        }
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    private void showLogin(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Connexion - Gestion de Stock");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDatabaseConfig(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("DatabaseConfigView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Configuration de la Base de Données");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            DatabaseConfigViewController controller = fxmlLoader.getController();
            controller.setApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}