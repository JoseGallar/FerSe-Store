package com.fersestore.app.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "product_variants",
        foreignKeys = @ForeignKey(
                entity = ProductEntity.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = CASCADE // Si borras el producto padre, se borran sus variantes
        ),
        indices = {@Index("productId")} // Esto hace que la app no se trabe al buscar variantes
)
public class ProductVariantEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int variantId;

    @ColumnInfo(name = "productId")
    public int productId; // El ID del Padre

    public String size;    // Ej: "XL"
    public String color;   // Ej: "Rojo"
    public int stock;      // Ej: 10

    // Constructor vacío (Obligatorio para Room)
    public ProductVariantEntity() {}

    // Constructor para nosotros (Ignorado por Room para evitar el warning)
    @Ignore
    public ProductVariantEntity(String size, String color, int stock) {
        this.size = size;
        this.color = color;
        this.stock = stock;
    }
}