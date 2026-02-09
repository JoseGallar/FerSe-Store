package com.fersestore.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fersestore.app.data.entity.ProductEntity;
import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(ProductEntity product);

    @Update
    void update(ProductEntity product);

    @Delete
    void delete(ProductEntity product);

    // AQUÍ ESTABA EL ERROR: Quitamos cualquier referencia a "WHERE is_active = 1"
    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<ProductEntity>> getAllProducts();
}