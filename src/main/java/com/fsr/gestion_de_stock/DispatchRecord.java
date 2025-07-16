package com.fsr.gestion_de_stock;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DispatchRecord {
    private final SimpleStringProperty dispatchDate;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty inventoryNumber;
    private final SimpleStringProperty serialNumber; // ADDED
    private final SimpleStringProperty destination;
    private final SimpleStringProperty recipient;
    private final SimpleStringProperty purchaseOrderRef;
    private final SimpleStringProperty supplier;

    public DispatchRecord(String dispatchDate, int quantity, String inventoryNumber, String serialNumber, String destination, String recipient, String purchaseOrderRef, String supplier) {
        this.dispatchDate = new SimpleStringProperty(dispatchDate);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.inventoryNumber = new SimpleStringProperty(inventoryNumber);
        this.serialNumber = new SimpleStringProperty(serialNumber); // ADDED
        this.destination = new SimpleStringProperty(destination);
        this.recipient = new SimpleStringProperty(recipient);
        this.purchaseOrderRef = new SimpleStringProperty(purchaseOrderRef);
        this.supplier = new SimpleStringProperty(supplier);
    }

    // Getters
    public String getDispatchDate() { return dispatchDate.get(); }
    public SimpleStringProperty dispatchDateProperty() { return dispatchDate; }
    public int getQuantity() { return quantity.get(); }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public String getInventoryNumber() { return inventoryNumber.get(); }
    public SimpleStringProperty inventoryNumberProperty() { return inventoryNumber; }
    public String getSerialNumber() { return serialNumber.get(); } // ADDED
    public SimpleStringProperty serialNumberProperty() { return serialNumber; } // ADDED
    public String getDestination() { return destination.get(); }
    public SimpleStringProperty destinationProperty() { return destination; }
    public String getRecipient() { return recipient.get(); }
    public SimpleStringProperty recipientProperty() { return recipient; }
    public String getPurchaseOrderRef() { return purchaseOrderRef.get(); }
    public SimpleStringProperty purchaseOrderRefProperty() { return purchaseOrderRef; }
    public String getSupplier() { return supplier.get(); }
    public SimpleStringProperty supplierProperty() { return supplier; }
}