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

    // 1. Insertar el Padre (Devuelve el ID nuevo, ej: 45)
    @Insert
    long insertProduct(ProductEntity product);

    // 2. Insertar los Hijos (Las variantes)
    @Insert
    void insertVariants(List<ProductVariantEntity> variants);

    @Insert
    void insertVariant(ProductVariantEntity variant);

    // 3. Actualizar
    @Update
    void updateProduct(ProductEntity product);

    @Update
    void updateVariant(ProductVariantEntity variant);

    // 4. Borrar (Al borrar el padre, Room borrará los hijos por el CASCADE que pusimos antes)
    @Delete
    void deleteProduct(ProductEntity product);

    // 5. LEER TODO (La consulta maestra)
    // @Transaction es OBLIGATORIO porque Room hace 2 consultas internamente (1. Padre, 2. Hijos)
    @Transaction
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    LiveData<List<ProductWithVariants>> getAllProducts();

    // Buscar un producto específico
    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<ProductWithVariants> getProductById(int id);
}