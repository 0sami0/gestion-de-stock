package com.fsr.gestion_de_stock;

import java.time.LocalDate;

public class StockArrival {
    private final int id;
    private final String description;
    private final String category;
    private final LocalDate arrivalDate;
    private final String purchaseOrderRef;
    private final int initialQuantity;
    private final int availableQuantity;
    private final String observations;
    private final int supplierId;
    private final int initialDepartmentId;

    public StockArrival(int id, String description, String category, LocalDate arrivalDate,
                        String purchaseOrderRef, int initialQuantity, int availableQuantity,
                        String observations, int supplierId, int initialDepartmentId) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.arrivalDate = arrivalDate;
        this.purchaseOrderRef = purchaseOrderRef;
        this.initialQuantity = initialQuantity;
        this.availableQuantity = availableQuantity;
        this.observations = observations;
        this.supplierId = supplierId;
        this.initialDepartmentId = initialDepartmentId;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public String getPurchaseOrderRef() { return purchaseOrderRef; }
    public int getInitialQuantity() { return initialQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public String getObservations() { return observations; }
    public int getSupplierId() { return supplierId; }
    public int getInitialDepartmentId() { return initialDepartmentId; }
}