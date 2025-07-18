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
    private StockArrival currentArrival;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
    }

    public void initData(StockArrival arrival) {
        this.currentArrival = arrival;

        descriptionField.setText(arrival.getDescription());
        categoryField.setText(arrival.getCategory());
        datePicker.setValue(arrival.getArrivalDate());
        purchaseOrderField.setText(arrival.getPurchaseOrderRef());
        quantityField.setText(String.valueOf(arrival.getInitialQuantity()));
        observationsField.setText(arrival.getObservations());

        supplierComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllSupplierNames()));
        affectationComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllDepartmentNames()));

        // These calls are now correct because the methods exist in StockArrival
        supplierComboBox.setValue(stockDAO.getSupplierNameById(arrival.getSupplierId()));
        affectationComboBox.setValue(stockDAO.getDepartmentNameById(arrival.getInitialDepartmentId()));
    }

    @FXML
    private void handleSave() {
        try {
            stockDAO.updateStockArrival(
                    currentArrival.getId(),
                    descriptionField.getText(),
                    datePicker.getValue(),
                    purchaseOrderField.getText(),
                    supplierComboBox.getValue(),
                    affectationComboBox.getValue(),
                    observationsField.getText()
            );
            closeWindow();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la mise à jour : " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) descriptionField.getScene().getWindow()).close();
    }
}