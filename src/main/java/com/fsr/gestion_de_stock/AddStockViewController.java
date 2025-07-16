package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddStockViewController {

    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField purchaseOrderField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> supplierComboBox;
    @FXML private ComboBox<String> affectationComboBox;
    @FXML private TextField observationsField;

    private StockDAO stockDAO;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        categoryComboBox.setItems(FXCollections.observableArrayList("Matériel", "Consommable"));
        datePicker.setValue(LocalDate.now());

        supplierComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllSupplierNames()));
        affectationComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllDepartmentNames()));
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        String category = categoryComboBox.getValue();
        int quantity = Integer.parseInt(quantityField.getText());
        List<String> serials = new ArrayList<>();

        if ("Matériel".equals(category) && quantity > 0) {
            try {
                serials = promptForSerialNumbers(quantity);
                if (serials == null) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir la fenêtre de saisie des numéros de série.");
                return;
            }
        }

        try {
            stockDAO.addStockArrival(
                    descriptionField.getText(),
                    category,
                    datePicker.getValue(),
                    purchaseOrderField.getText(),
                    quantity,
                    supplierComboBox.getValue(),
                    affectationComboBox.getValue(),
                    observationsField.getText(),
                    serials
            );

            Stage stage = (Stage) descriptionField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Impossible d'enregistrer l'article : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<String> promptForSerialNumbers(int quantity) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SerialEntryView.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Saisie des Numéros de Série");
        stage.setScene(new Scene(loader.load()));

        SerialEntryViewController controller = loader.getController();
        controller.createFields(quantity);

        stage.showAndWait();

        if (controller.isConfirmed()) {
            return controller.getSerials();
        } else {
            return null;
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            errorMessage += "La description est obligatoire.\n";
        }
        if (categoryComboBox.getValue() == null) {
            errorMessage += "La catégorie est obligatoire.\n";
        }
        if (quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
            errorMessage += "La quantité est obligatoire.\n";
        } else {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    errorMessage += "La quantité doit être positive.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "La quantité doit être un nombre entier.\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Champs Invalides", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}