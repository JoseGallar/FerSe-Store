package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "products")
public class ProductEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public double costPrice;
    public double salePrice;
    public String imageUri;

    // Agregamos fecha para poder ordenar por "Más nuevos"
    public long createdAt;

    public ProductEntity() {}

    @Ignore
    public ProductEntity(String name, String category, double costPrice, double salePrice, String imageUri) {
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.imageUri = imageUri;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters para que la UI funcione (parcialmente)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getCostPrice() { return costPrice; }
    public double getSalePrice() { return salePrice; }
    public String getImageUri() { return imageUri; }
}