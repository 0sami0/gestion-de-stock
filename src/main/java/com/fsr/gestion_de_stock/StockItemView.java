package com.fsr.gestion_de_stock;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class StockItemView {
    private final SimpleIntegerProperty arrivalId;
    private final SimpleStringProperty inventoryNumber;
    private final SimpleStringProperty description;
    private final SimpleStringProperty affectation;
    private final SimpleStringProperty date;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty supplier;
    private final SimpleStringProperty observations;
    private final SimpleStringProperty purchaseOrderRef;

    public StockItemView(int arrivalId, String inventoryNumber, String description, String affectation, String date, int quantity, String supplier, String observations, String purchaseOrderRef) {
        this.arrivalId = new SimpleIntegerProperty(arrivalId);
        this.inventoryNumber = new SimpleStringProperty(inventoryNumber);
        this.description = new SimpleStringProperty(description);
        this.affectation = new SimpleStringProperty(affectation);
        this.date = new SimpleStringProperty(date);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.supplier = new SimpleStringProperty(supplier);
        this.observations = new SimpleStringProperty(observations);
        this.purchaseOrderRef = new SimpleStringProperty(purchaseOrderRef);
    }

    public int getArrivalId() { return arrivalId.get(); }
    public SimpleIntegerProperty arrivalIdProperty() { return arrivalId; }
    public String getInventoryNumber() { return inventoryNumber.get(); }
    public SimpleStringProperty inventoryNumberProperty() { return inventoryNumber; }
    public String getDescription() { return description.get(); }
    public SimpleStringProperty descriptionProperty() { return description; }
    public String getAffectation() { return affectation.get(); }
    public SimpleStringProperty affectationProperty() { return affectation; }
    public String getDate() { return date.get(); }
    public SimpleStringProperty dateProperty() { return date; }
    public int getQuantity() { return quantity.get(); }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public String getSupplier() { return supplier.get(); }
    public SimpleStringProperty supplierProperty() { return supplier; }
    public String getObservations() { return observations.get(); }
    public SimpleStringProperty observationsProperty() { return observations; }
    public String getPurchaseOrderRef() { return purchaseOrderRef.get(); }
    public SimpleStringProperty purchaseOrderRefProperty() { return purchaseOrderRef; }
}