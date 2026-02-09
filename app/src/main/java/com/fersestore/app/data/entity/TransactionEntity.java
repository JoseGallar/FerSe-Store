package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.fersestore.app.domain.model.TransactionType;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public TransactionType type;
    public double totalAmount;
    public double paidAmount;
    public int quantity;
    public int productId;
    public String description;
    public long timestamp;
    public String note;
    public String clientName;
    public String status;

    // Constructor Vacío (Para Room)
    public TransactionEntity() {}

    // Constructor Lleno (Para nosotros) - Lo marcamos con @Ignore
    @Ignore
    public TransactionEntity(TransactionType type, double totalAmount, double paidAmount, int quantity, int productId, String description, long timestamp, String note, String clientName, String status) {
        this.type = type;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.quantity = quantity;
        this.productId = productId;
        this.description = description;
        this.timestamp = timestamp;
        this.note = note;
        this.clientName = clientName;
        this.status = status;
    }
}