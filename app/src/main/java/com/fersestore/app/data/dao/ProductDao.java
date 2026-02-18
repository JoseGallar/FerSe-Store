package com.fersestore.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.ProductWithVariants;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    long insertProduct(ProductEntity product);

    @Insert
    void insertVariants(List<ProductVariantEntity> variants);

    @Insert
    void insertVariant(ProductVariantEntity variant);

    @Update
    void updateProduct(ProductEntity product);

    @Update
    void updateVariant(ProductVariantEntity variant);

    @Delete
    void deleteProduct(ProductEntity product);

    @Delete
    void deleteVariant(ProductVariantEntity variant);

    // --- MANTENEMOS ESTA PARA EL BACKUP CSV (Necesita todo) ---
    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    List<ProductWithVariants> getAllProductsSync();

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<ProductWithVariants> getProductById(int id);

    @Query("UPDATE product_variants SET stock = stock + :cantidad WHERE id = :variantId")
    void devolverStock(int variantId, int cantidad);

    // --- NUEVAS CONSULTAS OPTIMIZADAS (PAGINACIÓN REAL) ---

    // 1. Obtener solo una página (LIMIT = cantidad, OFFSET = desde donde empezar)
    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> getPagedProducts(int limit, int offset);

    // 2. Filtrar por categoría desde la base de datos (Mucho más rápido)
    @Transaction
    @Query("SELECT * FROM products WHERE category = :category ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> getPagedProductsByCategory(String category, int limit, int offset);

    // 3. Buscar por nombre desde la base de datos
    @Transaction
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> searchPagedProducts(String query, int limit, int offset);

    // 4. Contar productos (Para saber cuántas páginas hay en total)
    @Query("SELECT COUNT(*) FROM products")
    LiveData<Integer> getCountAll();

    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    LiveData<Integer> getCountByCategory(String category);

    @Query("SELECT COUNT(*) FROM products WHERE name LIKE '%' || :query || '%'")
    LiveData<Integer> getCountBySearch(String query);

    // NUEVO: Calcular valor total del inventario directamente en la Base de Datos
    // Suma (Stock de la variante * Costo del Producto Padre)
    @Query("SELECT SUM(v.stock * p.costPrice) FROM product_variants v INNER JOIN products p ON v.productId = p.id")
    LiveData<Double> getTotalStockValue();
}