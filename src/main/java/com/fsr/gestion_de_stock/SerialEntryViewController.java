package com.fsr.gestion_de_stock;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SerialEntryViewController {
    @FXML private VBox serialFieldsContainer;
    @FXML private Label instructionLabel;

    private List<TextField> fields = new ArrayList<>();
    private List<String> serials = null;
    private boolean confirmed = false;

    public void createFields(int count) {
        instructionLabel.setText("Veuillez saisir les " + count + " numéro(s) de série :");
        for (int i = 0; i < count; i++) {
            TextField field = new TextField();
            field.setPromptText("N° de série pour l'article " + (i + 1));
            fields.add(field);
            serialFieldsContainer.getChildren().add(field);
        }
    }


    @FXML
    private void handleConfirm() {
        serials = fields.stream()
                .map(field -> {
                    String text = field.getText();
                    return (text == null || text.trim().isEmpty()) ? "N/A" : text.trim();
                })
                .collect(Collectors.toList());
        confirmed = true;
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeWindow();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getSerials() {
        return serials;
    }

    private void closeWindow() {
        ((Stage) serialFieldsContainer.getScene().getWindow()).close();
    }
}