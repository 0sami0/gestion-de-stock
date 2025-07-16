package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class SettingsViewController {

    @FXML private ListView<String> supplierListView;
    @FXML private TextField supplierNameField;
    @FXML private Button saveSupplierButton;
    @FXML private Button deleteSupplierButton;

    @FXML private ListView<String> departmentListView;
    @FXML private TextField departmentNameField;
    @FXML private Button saveDepartmentButton;
    @FXML private Button deleteDepartmentButton;


    private StockDAO stockDAO;
    private ObservableList<String> supplierList = FXCollections.observableArrayList();
    private ObservableList<String> departmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();

        supplierListView.setItems(supplierList);
        departmentListView.setItems(departmentList);

        // Enable/disable buttons based on selection
        deleteSupplierButton.disableProperty().bind(supplierListView.getSelectionModel().selectedItemProperty().isNull());
        saveSupplierButton.disableProperty().bind(supplierListView.getSelectionModel().selectedItemProperty().isNull());
        deleteDepartmentButton.disableProperty().bind(departmentListView.getSelectionModel().selectedItemProperty().isNull());
        saveDepartmentButton.disableProperty().bind(departmentListView.getSelectionModel().selectedItemProperty().isNull());

        // Add listeners to update text fields when an item is selected
        supplierListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> supplierNameField.setText(newVal)
        );
        departmentListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> departmentNameField.setText(newVal)
        );

        loadLists();
    }

    private void loadLists() {
        supplierList.setAll(stockDAO.getAllSupplierNames());
        departmentList.setAll(stockDAO.getAllDepartmentNames());
        supplierListView.getSelectionModel().clearSelection();
        departmentListView.getSelectionModel().clearSelection();
        supplierNameField.clear();
        departmentNameField.clear();
    }

    @FXML private void handleAddSupplier() {
        String name = supplierNameField.getText();
        if (isInvalid(name)) return;
        try {
            stockDAO.addSupplier(name.trim());
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter le fournisseur. Il existe peut-être déjà.");
        }
    }

    @FXML private void handleSaveSupplier() {
        String selected = supplierListView.getSelectionModel().getSelectedItem();
        String newName = supplierNameField.getText();
        if (selected == null || isInvalid(newName)) return;

        try {
            stockDAO.updateSupplier(selected, newName.trim());
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de modifier le fournisseur.\n" + e.getMessage());
        }
    }

    @FXML private void handleDeleteSupplier() {
        String selected = supplierListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            stockDAO.deleteSupplier(selected);
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le fournisseur.\n" + e.getMessage());
        }
    }

    @FXML private void handleAddDepartment() {
        String name = departmentNameField.getText();
        if (isInvalid(name)) return;
        try {
            stockDAO.addDepartment(name.trim());
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter le département. Il existe peut-être déjà.");
        }
    }

    @FXML private void handleSaveDepartment() {
        String selected = departmentListView.getSelectionModel().getSelectedItem();
        String newName = departmentNameField.getText();
        if (selected == null || isInvalid(newName)) return;
        try {
            stockDAO.updateDepartment(selected, newName.trim());
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de modifier le département.\n" + e.getMessage());
        }
    }

    @FXML private void handleDeleteDepartment() {
        String selected = departmentListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            stockDAO.deleteDepartment(selected);
            loadLists();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le département.\n" + e.getMessage());
        }
    }

    private boolean isInvalid(String name) {
        if (name == null || name.trim().isEmpty()) {
            showAlert("Erreur", "Le nom ne peut pas être vide.");
            return true;
        }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}