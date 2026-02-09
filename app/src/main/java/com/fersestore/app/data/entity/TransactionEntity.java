package com.fersestore.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fersestore.app.domain.model.TransactionType;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public TransactionType type; // INCOME (Venta/Ingreso), EXPENSE (Gasto), INVESTMENT (Inversión Inicial)
    public double totalAmount;   // El precio TOTAL de la venta (ej: 25.000)
    public double paidAmount;    // Lo que realmente te pagaron (ej: 20.000)
    public int quantity;
    public int productId;        // ID del producto (para devolver stock si hace falta)
    public String description;   // Ej: "Mochila Marrón (Mamá)"
    public long timestamp;
    public String note;

    // NUEVOS CAMPOS PARA CRÉDITOS Y RESERVAS
    public String clientName;    // Nombre del cliente (ej: "Hermana")
    public String status;        // "COMPLETED" (Pagado todo), "PARTIAL" (Debe), "RESERVED" (Apartado), "REFUNDED" (Devuelto)

    // Constructor vacío requerido por Room
    public TransactionEntity() {}

    public TransactionEntity(TransactionType type, double totalAmount, double paidAmount,
                             int quantity, int productId, String description,
                             long timestamp, String note, String clientName, String status) {
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