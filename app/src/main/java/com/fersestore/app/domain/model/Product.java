package com.fersestore.app.domain.model;

import java.io.Serializable;

public class Product implements Serializable {
    private int id;
    private String name;
    private String category;
    private String brand;
    private String size;
    private String color;
    private String season;
    private double costPrice;
    private double salePrice;
    private int currentStock;
    private String imageUri;
    private String stockBreakdown; // NUEVO CAMPO

    // Constructor Actualizado
    public Product(String name, String category, String brand, String size, String color, String season, double costPrice, double salePrice, int currentStock, String imageUri, String stockBreakdown) {
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.size = size;
        this.color = color;
        this.season = season;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.currentStock = currentStock;
        this.imageUri = imageUri;
        this.stockBreakdown = stockBreakdown;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    // NUEVOS
    public String getStockBreakdown() { return stockBreakdown; }
    public void setStockBreakdown(String stockBreakdown) { this.stockBreakdown = stockBreakdown; }
}