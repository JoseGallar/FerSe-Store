package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore; // Importante para el @Ignore

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;
    public double totalAmount;
    public double paidAmount;
    public double profit;      // <--- LA NUEVA COLUMNA GANANCIA
    public int quantity;
    public int productId;
    public String description;
    public long timestamp;
    public String relatedImageUri;
    public String customerName;
    public String status;

    // Constructor vacío (obligatorio para Room)
    public TransactionEntity() {}

    // Constructor completo
    @Ignore // <--- ESTO ES VITAL: EVITA EL ERROR AMARILLO DE "MULTIPLE CONSTRUCTORS"
    public TransactionEntity(String type, double totalAmount, double paidAmount, double profit, int quantity, int productId, String description, long timestamp, String relatedImageUri, String customerName, String status) {
        this.type = type;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.profit = profit;
        this.quantity = quantity;
        this.productId = productId;
        this.description = description;
        this.timestamp = timestamp;
        this.relatedImageUri = relatedImageUri;
        this.customerName = customerName;
        this.status = status;
    }
}