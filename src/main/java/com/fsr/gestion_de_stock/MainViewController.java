package com.fsr.gestion_de_stock;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class MainViewController {

    @FXML private TableView<StockItemView> stockTableView;
    @FXML private TableColumn<StockItemView, String> inventoryNumberCol;
    @FXML private TableColumn<StockItemView, String> purchaseOrderCol;
    @FXML private TableColumn<StockItemView, String> descriptionCol;
    @FXML private TableColumn<StockItemView, String> affectationCol;
    @FXML private TableColumn<StockItemView, String> dateCol;
    @FXML private TableColumn<StockItemView, Integer> quantityCol;
    @FXML private TableColumn<StockItemView, String> supplierCol;
    // RE-ADDING THE OBSERVATIONS COLUMN VARIABLE
    @FXML private TableColumn<StockItemView, String> observationsCol;
    @FXML private TableColumn<StockItemView, Void> actionsCol;

    @FXML private Button addStockButton;
    @FXML private Button dispatchItemButton;
    @FXML private TextField searchField;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem quitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem exportMenuItem;

    private StockDAO stockDAO;
    private ObservableList<StockItemView> stockList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        setupTableColumns();
        loadTableData();
        setupSearchFilter();
        setupDoubleClickForHistory();

        addStockButton.setOnAction(event -> handleAddStock());
        dispatchItemButton.setOnAction(event -> handleDispatchItem());
        settingsMenuItem.setOnAction(event -> handleOpenSettings());
        aboutMenuItem.setOnAction(event -> handleOpenAbout());
        quitMenuItem.setOnAction(event -> Platform.exit());
        exportMenuItem.setOnAction(event -> handleExportToCSV());

        dispatchItemButton.setDisable(true);
        stockTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> dispatchItemButton.setDisable(newSelection == null)
        );
    }

    private void setupTableColumns() {
        inventoryNumberCol.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        purchaseOrderCol.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderRef"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        affectationCol.setCellValueFactory(new PropertyValueFactory<>("affectation"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        // RE-ADDING THE SETUP FOR THE OBSERVATIONS COLUMN
        observationsCol.setCellValueFactory(new PropertyValueFactory<>("observations"));

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                deleteButton.setStyle("-fx-text-fill: red;");
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    StockItemView item = getTableView().getItems().get(getIndex());
                    handleModifyStock(item);
                });

                deleteButton.setOnAction(event -> {
                    StockItemView item = getTableView().getItems().get(getIndex());
                    handleDeleteStock(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    public void refreshTableData() {
        stockList.setAll(stockDAO.getAllStockForView());
    }

    private void loadTableData() {
        refreshTableData();
    }

    private void setupSearchFilter() {
        FilteredList<StockItemView> filteredData = new FilteredList<>(stockList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(itemView -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (itemView.getDescription() != null && itemView.getDescription().toLowerCase().contains(lowerCaseFilter)) return true;
                if (itemView.getInventoryNumber() != null && itemView.getInventoryNumber().toLowerCase().contains(lowerCaseFilter)) return true;
                if (itemView.getPurchaseOrderRef() != null && itemView.getPurchaseOrderRef().toLowerCase().contains(lowerCaseFilter)) return true;
                if (itemView.getSupplier() != null && itemView.getSupplier().toLowerCase().contains(lowerCaseFilter)) return true;
                if (itemView.getAffectation() != null && itemView.getAffectation().toLowerCase().contains(lowerCaseFilter)) return true;
                // Don't forget to include observations in the search!
                if (itemView.getObservations() != null && itemView.getObservations().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<StockItemView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(stockTableView.comparatorProperty());
        stockTableView.setItems(sortedData);
    }

    private void handleModifyStock(StockItemView item) {
        StockArrival fullStockItem = stockDAO.getArrivalById(item.getArrivalId());
        if (fullStockItem == null) {
            new Alert(Alert.AlertType.ERROR, "Impossible de trouver les détails de l'article.").show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModifyStockView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier l'Article");
            stage.setScene(new Scene(loader.load()));

            ModifyStockViewController controller = loader.getController();
            controller.loadStockData(fullStockItem);

            stage.showAndWait();
            refreshTableData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteStock(StockItemView item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de Suppression");
        confirmation.setHeaderText("Supprimer l'article : " + item.getDescription());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet enregistrement ? Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                stockDAO.deleteStockArrival(item.getArrivalId());
                refreshTableData();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    private void handleExportToCSV() {
        new Alert(Alert.AlertType.INFORMATION, "La fonctionnalité d'exportation sera ajoutée dans une future version.").showAndWait();
    }

    private void setupDoubleClickForHistory() {
        stockTableView.setRowFactory(tv -> {
            TableRow<StockItemView> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    StockItemView rowData = row.getItem();
                    showHistoryWindow(rowData.getArrivalId());
                }
            });
            return row;
        });
    }

    private void showHistoryWindow(int arrivalId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemHistoryView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Historique de l'Article");
            stage.setScene(new Scene(loader.load()));
            ItemHistoryViewController controller = loader.getController();
            controller.loadHistory(arrivalId);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAddStock() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddStockView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Nouvel Article");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
            refreshTableData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDispatchItem() {
        StockItemView selectedViewItem = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedViewItem == null) return;
        int arrivalId = selectedViewItem.getArrivalId();
        StockArrival selectedArrival = stockDAO.getArrivalById(arrivalId);
        if (selectedArrival == null || selectedArrival.getAvailableQuantity() <= 0) {
            new Alert(Alert.AlertType.INFORMATION, "Il n'y a plus d'articles disponibles pour ce lot.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DispatchItemView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sortir un Article");
            stage.setScene(new Scene(loader.load()));
            DispatchItemViewController controller = loader.getController();
            controller.setItemToDispatch(selectedArrival, this);
            stage.showAndWait();
            refreshTableData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Paramètres");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleOpenAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos de Gestion de Stock");
        alert.setHeaderText("Gestion de Stock v1.0");
        alert.setContentText("Application de gestion de stock développée pour la Faculté des Sciences.\n\nCréé par: sami hilali");
        alert.showAndWait();
    }
}