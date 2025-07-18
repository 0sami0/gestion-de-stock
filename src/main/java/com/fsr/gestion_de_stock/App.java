package com.fsr.gestion_de_stock;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

public class App extends Application {

    private static ConfigManager configManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        configManager = new ConfigManager();

        try {
            FXMLLoader splashLoader = new FXMLLoader(App.class.getResource("SplashView.fxml"));
            Stage splashStage = new Stage(StageStyle.UNDECORATED);
            splashStage.setScene(new Scene(splashLoader.load()));

            Image appIcon = loadAppIcon();
            if (appIcon != null) {
                splashStage.getIcons().add(appIcon);
                this.primaryStage.getIcons().add(appIcon);
            }

            splashStage.show();

            new Thread(() -> {
                try {

                    DatabaseManager.initializeDatabase();

                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.err.println("An error occurred during initialization.");
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    ThemeManager.applyTheme(configManager);
                    splashStage.close();
                    launchNextScreen();
                });
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            ThemeManager.applyTheme(configManager);
            launchNextScreen();
        }
    }

    public void launchNextScreen() {
        try {
            DatabaseManager.getConnection();
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
            if (!stage.isShowing()) {
                stage.show();
            }
            LoginViewController controller = fxmlLoader.getController();
            controller.setAppIcon(stage.getIcons().isEmpty() ? null : stage.getIcons().get(0));
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
            if (!stage.isShowing()) {
                stage.show();
            }
            DatabaseConfigViewController controller = fxmlLoader.getController();
            controller.setApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyUserTheme() {
        String theme = configManager.getProperty("app.theme");
        if ("dark".equals(theme)) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }

    private Image loadAppIcon() {
        try (InputStream iconStream = getClass().getResourceAsStream("logo.png")) {
            if (iconStream != null) {
                return new Image(iconStream);
            }
        } catch (Exception e) {
            System.err.println("Icon 'logo.png' not found or failed to load. Skipping.");
        }
        return null;
    }

    @Override
    public void stop() {
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}