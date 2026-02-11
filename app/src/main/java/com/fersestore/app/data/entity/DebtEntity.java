package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts")
public class DebtEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String clientName;       // Ej: "Juan Perez"
    public String productName;      // Ej: "Mochila Nike"
    public String variantInfo;      // Ej: "Negra"

    public double totalAmount;      // Precio total ($25.000)
    public double paidAmount;       // Lo que ya pagó ($20.000)
    public double remainingAmount;  // Lo que debe ($5.000)

    public int productId;           // ID para devolver stock
    public int variantId;           // ID de variante para devolver stock (si aplica)
    public int quantity;            // Cantidad reservada

    public long timestamp;          // Fecha de la reserva

    // Constructor vacío
    public DebtEntity() {}

    // Constructor completo
    @Ignore
    public DebtEntity(String clientName, String productName, String variantInfo, double totalAmount, double paidAmount, int productId, int variantId, int quantity, long timestamp) {
        this.clientName = clientName;
        this.productName = productName;
        this.variantInfo = variantInfo;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = totalAmount - paidAmount; // Se calcula solo
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
}