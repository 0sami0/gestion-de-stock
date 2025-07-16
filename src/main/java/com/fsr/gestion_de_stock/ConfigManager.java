package com.fsr.gestion_de_stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + "gestion_de_stock_config.properties";
    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Could not load config file, using defaults.");
                loadDefaults();
            }
        } else {
            System.out.println("Config file not found, creating with defaults.");
            loadDefaults();
            save();
        }
    }

    private void loadDefaults() {
        properties.setProperty("db.host", "localhost");
        properties.setProperty("db.port", "3306");
        properties.setProperty("db.name", "gestion_stock_fsr");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.pass", "");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(fos, "Gestion de Stock DB Configuration");
            System.out.println("Configuration saved to: " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}