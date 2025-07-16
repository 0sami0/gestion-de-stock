package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifyStockViewController {

    @FXML private TextField descriptionField;
    @FXML private TextField categoryField;
    @FXML private DatePicker datePicker;
    @FXML private TextField purchaseOrderField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> supplierComboBox;
    @FXML private ComboBox<String> affectationComboBox;
    @FXML private TextField observationsField;

    private StockDAO stockDAO;
    private StockArrival stockToModify;

    public void initialize() {
        stockDAO = new StockDAO();
        supplierComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllSupplierNames()));
        affectationComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllDepartmentNames()));
    }

    public void loadStockData(StockArrival stock) {
        this.stockToModify = stock;

        // Pre-fill the form with existing data
        descriptionField.setText(stock.getDescription());
        categoryField.setText(stock.getCategory());
        datePicker.setValue(stock.getArrivalDate());
        purchaseOrderField.setText(stock.getPurchaseOrderRef());
        quantityField.setText(String.valueOf(stock.getInitialQuantity()));
        supplierComboBox.setValue(stock.getSupplierName());
        affectationComboBox.setValue(stock.getDepartmentName());
        observationsField.setText(stock.getObservations());
    }

    @FXML
    private void handleSave() {
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire.");
            return;
        }

        try {
            stockToModify.setDescription(descriptionField.getText());
            stockToModify.setArrivalDate(datePicker.getValue());
            stockToModify.setPurchaseOrderRef(purchaseOrderField.getText());
            stockToModify.setSupplierName(supplierComboBox.getValue());
            stockToModify.setDepartmentName(affectationComboBox.getValue());
            stockToModify.setObservations(observationsField.getText());

            stockDAO.updateStockArrival(stockToModify);

            Stage stage = (Stage) descriptionField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert("Erreur de Base de Données", "Impossible de mettre à jour l'article : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}