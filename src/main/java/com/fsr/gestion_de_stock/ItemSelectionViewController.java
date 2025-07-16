package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSelectionViewController {
    @FXML private TableView<InventoryItem> itemsTableView;
    @FXML private TableColumn<InventoryItem, Boolean> selectCol;
    @FXML private TableColumn<InventoryItem, String> inventoryNumberCol;
    @FXML private TableColumn<InventoryItem, String> serialNumberCol;
    @FXML private Button selectButton;

    private List<InventoryItem> selectedItems = null;
    private final ObservableList<InventoryItem> allItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        itemsTableView.setEditable(true);
        inventoryNumberCol.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        serialNumberCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        selectCol.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        itemsTableView.setItems(allItems);
    }

    public void loadItems(List<InventoryItem> items) {
        allItems.setAll(items);
    }

    @FXML
    private void handleSelect() {
        this.selectedItems = allItems.stream()
                .filter(InventoryItem::isSelected)
                .collect(Collectors.toList());
        if (this.selectedItems.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner au moins un article.").showAndWait();
        } else {
            ((Stage) selectButton.getScene().getWindow()).close();
        }
    }

    public List<InventoryItem> getSelectedItems() {
        return selectedItems;
    }
}