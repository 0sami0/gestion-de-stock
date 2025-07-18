package com.fsr.gestion_de_stock;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    @FXML private TableColumn<StockItemView, String> observationsCol;
    @FXML private TableColumn<StockItemView, Void> operationsCol;
    @FXML private Button addStockButton;
    @FXML private Button dispatchItemButton;
    @FXML private Button dashboardButton;
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> supplierFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private Button clearFiltersButton;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem quitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem changePasswordMenuItem;
    @FXML private MenuItem toggleThemeMenuItem;

    private StockDAO stockDAO;
    private User currentUser;
    private ObservableList<StockItemView> stockList = FXCollections.observableArrayList();
    private FilteredList<StockItemView> filteredData;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        setupTableColumns();
        loadTableData();
        populateFilterComboBoxes();
        setupSearchFilter();
        setupDoubleClickForHistory();
        addButtonsToTable();

        addStockButton.setOnAction(event -> handleAddStock());
        dispatchItemButton.setOnAction(event -> handleDispatchItem());
        dashboardButton.setOnAction(event -> handleOpenDashboard());
        clearFiltersButton.setOnAction(event -> clearAllFilters());
        settingsMenuItem.setOnAction(event -> handleOpenSettings());
        aboutMenuItem.setOnAction(event -> handleOpenAbout());
        quitMenuItem.setOnAction(event -> Platform.exit());
        changePasswordMenuItem.setOnAction(event -> handleChangePassword());
        exportMenuItem.setOnAction(event -> handleExportToCSV());
        toggleThemeMenuItem.setOnAction(event -> ThemeManager.toggleTheme(App.getConfigManager(), toggleThemeMenuItem));

        ThemeManager.updateMenuItemText(toggleThemeMenuItem, App.getConfigManager());

        dispatchItemButton.setDisable(true);
        stockTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, val) -> dispatchItemButton.setDisable(val == null)
        );
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void setupTableColumns() {
        inventoryNumberCol.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        purchaseOrderCol.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderRef"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        affectationCol.setCellValueFactory(new PropertyValueFactory<>("affectation"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        observationsCol.setCellValueFactory(new PropertyValueFactory<>("observations"));
    }

    private void addButtonsToTable() {
        Callback<TableColumn<StockItemView, Void>, TableCell<StockItemView, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button editBtn = new Button("Modifier");
                private final Button deleteBtn = new Button("Supprimer");

                {
                    deleteBtn.setStyle("-fx-text-fill: red;");
                    editBtn.setOnAction(event -> {
                        StockItemView item = getTableView().getItems().get(getIndex());
                        handleModifyStock(item);
                    });
                    deleteBtn.setOnAction(event -> {
                        StockItemView item = getTableView().getItems().get(getIndex());
                        handleDeleteStock(item);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(editBtn, deleteBtn);
                        hbox.setSpacing(5);
                        setGraphic(hbox);
                    }
                }
            };
        };
        operationsCol.setCellFactory(cellFactory);
    }

    public void refreshTableData() {
        stockList.setAll(stockDAO.getAllStockForView());
    }

    private void loadTableData() {
        refreshTableData();
    }

    private void populateFilterComboBoxes() {
        supplierFilterComboBox.setItems(FXCollections.observableArrayList(stockDAO.getAllSupplierNames()));
        categoryFilterComboBox.setItems(FXCollections.observableArrayList("Matériel", "Consommable"));
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(stockList, p -> true);

        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        startDatePicker.valueProperty().addListener((obs, old, val) -> applyFilters());
        endDatePicker.valueProperty().addListener((obs, old, val) -> applyFilters());
        supplierFilterComboBox.valueProperty().addListener((obs, old, val) -> applyFilters());
        categoryFilterComboBox.valueProperty().addListener((obs, old, val) -> applyFilters());

        SortedList<StockItemView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(stockTableView.comparatorProperty());
        stockTableView.setItems(sortedData);
    }

    private void applyFilters() {
        filteredData.setPredicate(itemView -> {
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!( (itemView.getDescription() != null && itemView.getDescription().toLowerCase().contains(lowerCaseFilter)) ||
                        (itemView.getInventoryNumber() != null && itemView.getInventoryNumber().toLowerCase().contains(lowerCaseFilter)) ||
                        (itemView.getPurchaseOrderRef() != null && itemView.getPurchaseOrderRef().toLowerCase().contains(lowerCaseFilter)) ||
                        (itemView.getObservations() != null && itemView.getObservations().toLowerCase().contains(lowerCaseFilter))
                )) return false;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            try {
                LocalDate itemDate = LocalDate.parse(itemView.getDate());
                if (startDate != null && itemDate.isBefore(startDate)) return false;
                if (endDate != null && itemDate.isAfter(endDate)) return false;
            } catch (DateTimeParseException e) {
                // ignore
            }

            String selectedSupplier = supplierFilterComboBox.getValue();
            if (selectedSupplier != null && !selectedSupplier.isEmpty()) {
                if (!selectedSupplier.equals(itemView.getSupplier())) return false;
            }

            String selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                if (!selectedCategory.equals(itemView.getCategory())) return false;
            }

            return true;
        });
    }

    private void clearAllFilters() {
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        supplierFilterComboBox.getSelectionModel().clearSelection();
        categoryFilterComboBox.getSelectionModel().clearSelection();
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

    private void handleModifyStock(StockItemView view) {
        StockArrival arrival = stockDAO.getArrivalById(view.getArrivalId());
        if (arrival == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModifyStockView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier l'Article en Stock");
            stage.setScene(new Scene(loader.load()));
            ModifyStockViewController controller = loader.getController();
            controller.initData(arrival);
            stage.showAndWait();
            refreshTableData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteStock(StockItemView view) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Suppression");
        alert.setHeaderText("Supprimer : " + view.getDescription());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette entrée ? Cette action est irréversible.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                stockDAO.deleteStockArrival(view.getArrivalId());
                refreshTableData();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    private void handleDispatchItem() {
        StockItemView selectedViewItem = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedViewItem == null) return;
        StockArrival selectedArrival = stockDAO.getArrivalById(selectedViewItem.getArrivalId());
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

    private void handleOpenDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DashboardView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dashboardButton.getScene().getWindow());
            stage.setTitle("Tableau de Bord - Aperçu du Stock");
            stage.setScene(new Scene(loader.load(), 1100, 750));
            stage.show();
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

    private void handleChangePassword() {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChangePasswordView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Changer le Mot de Passe");
            stage.setScene(new Scene(loader.load()));
            ChangePasswordViewController controller = loader.getController();
            controller.initData(currentUser);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleOpenAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos de Gestion de Stock");
        alert.setHeaderText("Gestion de Stock v1.0");
        alert.setContentText("Application de gestion de stock développée pour la Faculté des Sciences par Sami Hilali (étudiants ingénieur-EMG).");
        alert.showAndWait();
    }

    private void handleExportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'Exportation CSV");
        fileChooser.setInitialFileName("export_inventaire_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        Stage stage = (Stage) stockTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("N° d'Inventaire;Marché/BC;Description;Affectation;Date Dépôt;Quantité;Fournisseur;Observations\n");
                for (StockItemView item : stockTableView.getItems()) {
                    writer.append(escapeCsv(item.getInventoryNumber())).append(';')
                            .append(escapeCsv(item.getPurchaseOrderRef())).append(';')
                            .append(escapeCsv(item.getDescription())).append(';')
                            .append(escapeCsv(item.getAffectation())).append(';')
                            .append(escapeCsv(item.getDate())).append(';')
                            .append(String.valueOf(item.getQuantity())).append(';')
                            .append(escapeCsv(item.getSupplier())).append(';')
                            .append(escapeCsv(item.getObservations())).append('\n');
                }
                new Alert(Alert.AlertType.INFORMATION, "Le fichier a été exporté avec succès !").showAndWait();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'exportation du fichier.").showAndWait();
                e.printStackTrace();
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}