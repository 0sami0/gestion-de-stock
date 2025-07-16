package com.fsr.gestion_de_stock;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ItemHistoryViewController {

    @FXML private Label descriptionLabel;
    @FXML private Label initialQuantityLabel;
    @FXML private TableView<DispatchRecord> historyTableView;
    @FXML private TableColumn<DispatchRecord, String> dispatchDateCol;
    @FXML private TableColumn<DispatchRecord, Integer> quantityCol;
    @FXML private TableColumn<DispatchRecord, String> inventoryNumberCol;
    @FXML private TableColumn<DispatchRecord, String> serialNumberCol;
    @FXML private TableColumn<DispatchRecord, String> destinationCol;
    @FXML private TableColumn<DispatchRecord, String> recipientCol;
    @FXML private TableColumn<DispatchRecord, String> purchaseOrderCol;
    @FXML private TableColumn<DispatchRecord, String> supplierCol;

    private StockDAO stockDAO;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        setupTableColumns();
    }

    public void loadHistory(int arrivalId) {
        StockArrival arrival = stockDAO.getArrivalById(arrivalId);
        if (arrival != null) {
            descriptionLabel.setText(arrival.getDescription());
            initialQuantityLabel.setText(String.valueOf(arrival.getInitialQuantity()));
            historyTableView.setItems(FXCollections.observableArrayList(stockDAO.getDispatchHistory(arrivalId)));
        }
    }

    private void setupTableColumns() {
        dispatchDateCol.setCellValueFactory(new PropertyValueFactory<>("dispatchDate"));
        purchaseOrderCol.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderRef"));
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        inventoryNumberCol.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        serialNumberCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        destinationCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        recipientCol.setCellValueFactory(new PropertyValueFactory<>("recipient"));
    }
}