package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String category;
    public double costPrice;
    public double salePrice;
    public int currentStock;
    public String imageUri;
    public String stockBreakdown; // Ej: "Rojo:5;Azul:3"

    // Constructor vacío (necesario para Room a veces)
    public ProductEntity() {}

    public ProductEntity(String name, String category, double costPrice, double salePrice, int currentStock, String imageUri, String stockBreakdown) {
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.currentStock = currentStock;
        this.imageUri = imageUri;
        this.stockBreakdown = stockBreakdown;
    }

    // --- MÉTODOS GETTERS (ESTO ES LO QUE FALTABA) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getSalePrice() { return salePrice; }
    public int getCurrentStock() { return currentStock; }
    public String getImageUri() { return imageUri; }
}