package com.fsr.gestion_de_stock;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class DashboardViewController {

    @FXML private Label totalItemsLabel;
    @FXML private Label dispatchedThisMonthLabel;
    @FXML private Label suppliersCountLabel;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> topItemsBarChart;
    @FXML private MenuItem inventoryMenuItem;
    @FXML private MenuItem quitMenuItem;

    private StockDAO stockDAO;

    @FXML
    public void initialize() {
        stockDAO = new StockDAO();
        loadDashboardData();

        inventoryMenuItem.setOnAction(event -> openInventoryWindow());
        quitMenuItem.setOnAction(event -> Platform.exit());
    }

    private void loadDashboardData() {
        // Load KPI Cards
        totalItemsLabel.setText(String.valueOf(stockDAO.getTotalItemsInStock()));
        dispatchedThisMonthLabel.setText(String.valueOf(stockDAO.getDispatchedThisMonthMySQL()));
        suppliersCountLabel.setText(String.valueOf(stockDAO.getActiveSuppliersCount()));

        // Load Pie Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Integer> categoryData = stockDAO.getCategoryDistribution();
        for (Map.Entry<String, Integer> entry : categoryData.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }
        categoryPieChart.setData(pieChartData);

        // Load Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> topItemsData = stockDAO.getTopFiveItems();
        for (Map.Entry<String, Integer> entry : topItemsData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        topItemsBarChart.getData().clear();
        topItemsBarChart.getData().add(series);
        topItemsBarChart.setLegendVisible(false);
    }

    private void openInventoryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Inventaire Détaillé");
            stage.setScene(new Scene(loader.load(), 1200, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}