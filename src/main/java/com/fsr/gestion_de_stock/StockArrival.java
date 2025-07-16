package com.fsr.gestion_de_stock;

import java.time.LocalDate;

public class StockArrival {
    private int id;
    private String description;
    private String category;
    private LocalDate arrivalDate;
    private String purchaseOrderRef;
    private int initialQuantity;
    private int availableQuantity;
    private String supplierName;
    private String departmentName;
    private String observations;

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public String getPurchaseOrderRef() { return purchaseOrderRef; }
    public int getInitialQuantity() { return initialQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public String getSupplierName() { return supplierName; }
    public String getDepartmentName() { return departmentName; }
    public String getObservations() { return observations; }

    // Setters for modifiable fields
    public void setDescription(String description) { this.description = description; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
    public void setPurchaseOrderRef(String purchaseOrderRef) { this.purchaseOrderRef = purchaseOrderRef; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public void setObservations(String observations) { this.observations = observations; }

    // Constructor
    public StockArrival(int id, String description, String category, LocalDate arrivalDate, String purchaseOrderRef, int initialQuantity, int availableQuantity, String supplierName, String departmentName, String observations) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.arrivalDate = arrivalDate;
        this.purchaseOrderRef = purchaseOrderRef;
        this.initialQuantity = initialQuantity;
        this.availableQuantity = availableQuantity;
        this.supplierName = supplierName;
        this.departmentName = departmentName;
        this.observations = observations;
    }
}