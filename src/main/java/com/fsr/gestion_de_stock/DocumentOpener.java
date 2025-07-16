package com.fsr.gestion_de_stock;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.File;

public class DocumentOpener {

    public static void askToOpen(File file) {
        if (file == null || !file.exists()) {
            showAlert("Erreur Fichier", "Le fichier n'a pas pu être créé ou trouvé.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Document Généré");
        confirmation.setHeaderText("Document généré avec succès à:\n" + file.getAbsolutePath());
        confirmation.setContentText("Voulez-vous ouvrir le fichier ?");

        ButtonType buttonTypeOpen = new ButtonType("Ouvrir");
        ButtonType buttonTypeCancel = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(buttonTypeOpen, buttonTypeCancel);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == buttonTypeOpen) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "L'ouverture de fichiers n'est pas supportée sur ce système.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible d'ouvrir le fichier automatiquement.");
                }
            }
        });
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}