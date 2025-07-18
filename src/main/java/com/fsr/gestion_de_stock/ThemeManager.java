package com.fsr.gestion_de_stock;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.control.MenuItem;

public class ThemeManager {

    public static void applyTheme(ConfigManager config) {
        String theme = config.getProperty("app.theme");
        if ("dark".equals(theme)) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            // Default to light theme if the setting is anything else
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }

    public static void toggleTheme(ConfigManager config, MenuItem menuItem) {
        String currentTheme = config.getProperty("app.theme");

        if ("dark".equals(currentTheme)) {
            // Switch to light
            config.setProperty("app.theme", "light");
        } else {
            // Switch to dark
            config.setProperty("app.theme", "dark");
        }

        config.save();
        applyTheme(config); // Re-apply the theme to the whole application
        updateMenuItemText(menuItem, config); // Update the text of the menu item
    }

    public static void updateMenuItemText(MenuItem menuItem, ConfigManager config) {
        if (menuItem == null) return;
        String currentTheme = config.getProperty("app.theme");
        if ("dark".equals(currentTheme)) {
            menuItem.setText("Passer en Mode Clair");
        } else {
            menuItem.setText("Passer en Mode Sombre");
        }
    }
}