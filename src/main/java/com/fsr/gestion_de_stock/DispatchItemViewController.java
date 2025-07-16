package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DispatchItemViewController {
    @FXML private Label descriptionLabel, quantityLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private DatePicker dispatchDatePicker;
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private TextField recipientNameField;
    @FXML private Button saveButton, selectItemButton;

    private StockDAO stockDAO;
    private StockArrival selectedArrival;
    private MainViewController mainViewController;
    private List<InventoryItem> selectedItems;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        dispatchDatePicker.setValue(LocalDate.now());
    }

    public void setItemToDispatch(StockArrival arrival, MainViewController mainController) {
        this.selectedArrival = arrival;
        this.mainViewController = mainController;
        descriptionLabel.setText(arrival.getDescription());
        destinationComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllDepartmentNames()));
        if ("Matériel".equals(selectedArrival.getCategory())) {
            quantitySpinner.setVisible(false);
            selectItemButton.setVisible(true);
        } else {
            selectItemButton.setVisible(false);
            quantitySpinner.setVisible(true);
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, selectedArrival.getAvailableQuantity(), 1));
        }
    }

    @FXML
    private void handleSelectItem() throws IOException {
        List<InventoryItem> availableItems = stockDAO.getAvailableItemsByArrival(selectedArrival.getId());
        if(availableItems.isEmpty()) { showAlert("Stock Épuisé", "Il n'y a plus d'articles disponibles pour ce lot."); return; }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemSelectionView.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Sélectionner les Articles");
        stage.setScene(new Scene(loader.load()));
        ItemSelectionViewController controller = loader.getController();
        controller.loadItems(availableItems);
        stage.showAndWait();
        this.selectedItems = controller.getSelectedItems();
        if (this.selectedItems != null && !this.selectedItems.isEmpty()) {
            saveButton.setText("Sortir " + this.selectedItems.size() + " article(s)");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        try {
            int dispatchId;
            if ("Matériel".equals(selectedArrival.getCategory())) {
                if (selectedItems == null || selectedItems.isEmpty()) { showAlert("Erreur", "Veuillez sélectionner au moins un article."); return; }
                List<Integer> itemIds = selectedItems.stream().map(InventoryItem::getId).collect(Collectors.toList());
                dispatchId = stockDAO.dispatchMultipleItems(selectedArrival.getId(), itemIds, dispatchDatePicker.getValue(), destinationComboBox.getValue(), recipientNameField.getText());
            } else {
                int quantity = quantitySpinner.getValue();
                dispatchId = stockDAO.dispatchConsumable(selectedArrival.getId(), quantity, dispatchDatePicker.getValue(), destinationComboBox.getValue(), recipientNameField.getText());
            }
            generateDispatchDocument(dispatchId);
            mainViewController.refreshTableData();
            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de sortir les articles: " + e.getMessage());
        }
    }

    private void generateDispatchDocument(int dispatchId) {
        if (dispatchId == -1) { return; }
        // This now correctly expects HashMap<String, String>
        HashMap<String, String> data = stockDAO.getDispatchDetailsForDocument(dispatchId);
        if (data == null) { showAlert("Erreur", "Impossible de récupérer les détails pour la génération du document."); return; }

        String templateName = "Matériel".equals(selectedArrival.getCategory()) ? "bon_de_reception_template.docx" : "decharge_template.docx";

        File generatedFile = DocumentGenerator.generateDocument(templateName, data);
        DocumentOpener.askToOpen(generatedFile);
    }

    private void closeWindow() { ((Stage)saveButton.getScene().getWindow()).close(); }
    private void showAlert(String title, String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
    private boolean validateInput() { return true; }
}