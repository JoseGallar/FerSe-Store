package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable; // <--- ESTO ES VITAL PARA QUE NO CRASHEE AL VER DETALLE

@Entity(tableName = "products")
public class ProductEntity implements Serializable { // <--- Agregado el implements

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public double costPrice;
    public double salePrice;
    public int currentStock;
    public String imageUri;
    public String stockBreakdown; // Mantenemos tu nombre original

    // 1. Constructor vacío (Room)
    public ProductEntity() {}

    // 2. Constructor lleno (Ignorado por Room, usado por nosotros)
    @Ignore
    public ProductEntity(String name, String category, double costPrice, double salePrice, int currentStock, String imageUri, String stockBreakdown) {
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.currentStock = currentStock;
        this.imageUri = imageUri;
        this.stockBreakdown = stockBreakdown;
    }

    // --- GETTERS ORIGINALES (Para que MainActivity no falle) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getSalePrice() { return salePrice; }
    public int getCurrentStock() { return currentStock; }
    public String getImageUri() { return imageUri; }
    public String getStockBreakdown() { return stockBreakdown; }
    public double getCostPrice() { return costPrice; }

    // --- ALIAS NUEVOS (Para que ProductDetailActivity funcione) ---
    // El detalle busca "getPrice", nosotros le damos "salePrice"
    public double getPrice() { return salePrice; }

    // El detalle busca "getStock", nosotros le damos "currentStock"
    public int getStock() { return currentStock; }

    // El detalle busca "getDescription", nosotros le damos "stockBreakdown"
    public String getDescription() { return stockBreakdown; }
}