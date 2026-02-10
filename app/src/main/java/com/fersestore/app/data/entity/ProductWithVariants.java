package com.fersestore.app.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;
import java.util.ArrayList;

public class ProductWithVariants {

    // @Embedded: Significa "Aquí va toda la info del Padre (Nombre, Precio, Foto)"
    @Embedded
    public ProductEntity product;

    // @Relation: Significa "Busca automáticamente los Hijos que coincidan con el ID"
    @Relation(
            parentColumn = "id",
            entityColumn = "productId"
    )
    public List<ProductVariantEntity> variants;

    // --- MÉTODOS ÚTILES PARA LA UI ---

    // Como borramos el "currentStock" del padre, ahora lo calculamos sumando los hijos
    public int getTotalStock() {
        int total = 0;
        if (variants != null) {
            for (ProductVariantEntity v : variants) {
                total += v.stock;
            }
        }
        return total;
    }

    // Para mostrar "Rojo: 5, Verde: 3" en el detalle
    public String getVariantsText() {
        if (variants == null || variants.isEmpty()) return "Sin stock cargado";
        StringBuilder sb = new StringBuilder();
        for (ProductVariantEntity v : variants) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(v.color).append(": ").append(v.stock);
        }
        return sb.toString();
    }
}