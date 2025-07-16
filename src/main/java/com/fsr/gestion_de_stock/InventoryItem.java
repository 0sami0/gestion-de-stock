package com.fsr.gestion_de_stock;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class InventoryItem {
    private final int id;
    private final String inventoryNumber;
    private final String serialNumber;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public InventoryItem(int id, String inventoryNumber, String serialNumber) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.serialNumber = serialNumber;
    }
    public int getId() { return id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public String getSerialNumber() { return serialNumber; }
    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
}