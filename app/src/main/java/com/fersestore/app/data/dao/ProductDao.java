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

    // --- EL TIMBRE (WAKE UP CALL) ---
    // Engañamos a Room actualizando el producto con su mismo ID para forzar el refresco de pantalla
    @Query("UPDATE products SET id = id WHERE id = :productId")
    void tocarTimbreProducto(int productId);

    // Timbre especial para cuando devolvemos stock desde una deuda
    @Query("UPDATE products SET id = id WHERE id = (SELECT productId FROM product_variants WHERE id = :variantId)")
    void tocarTimbrePorVariante(int variantId);

    // --- CONSULTAS LIMPIAS Y RÁPIDAS (Sin LEFT JOIN) ---

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<ProductWithVariants> getProductById(int id);

    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    List<ProductWithVariants> getAllProductsSync();

    @Query("UPDATE product_variants SET stock = stock + :cantidad WHERE id = :variantId")
    void devolverStock(int variantId, int cantidad);

    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> getPagedProducts(int limit, int offset);

    @Transaction
    @Query("SELECT * FROM products WHERE category = :category ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> getPagedProductsByCategory(String category, int limit, int offset);

    @Transaction
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ProductWithVariants>> searchPagedProducts(String query, int limit, int offset);

    @Query("SELECT COUNT(*) FROM products")
    LiveData<Integer> getCountAll();

    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    LiveData<Integer> getCountByCategory(String category);

    @Query("SELECT COUNT(*) FROM products WHERE name LIKE '%' || :query || '%'")
    LiveData<Integer> getCountBySearch(String query);

    @Query("SELECT SUM(v.stock * p.costPrice) FROM product_variants v INNER JOIN products p ON v.productId = p.id")
    LiveData<Double> getTotalStockValue();
}