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

    // --- ESTA ES LA LÍNEA QUE FALTABA ---
    @Delete
    void deleteVariant(ProductVariantEntity variant);
    // ------------------------------------

    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    LiveData<List<ProductWithVariants>> getAllProducts();

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<ProductWithVariants> getProductById(int id);

    @Query("UPDATE product_variants SET stock = stock + :cantidad WHERE id = :variantId")
    void devolverStock(int variantId, int cantidad);
}