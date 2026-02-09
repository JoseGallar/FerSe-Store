package com.fersestore.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.dao.ProductDao;
import com.fersestore.app.data.database.AppDatabase;
import com.fersestore.app.data.entity.ProductEntity;

import java.util.List;

public class ProductRepository {

    private ProductDao productDao;
    private LiveData<List<ProductEntity>> allProducts;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<ProductEntity>> getAllProducts() {
        return allProducts;
    }

    public void insert(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.insert(product);
        });
    }

    public void update(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.update(product);
        });
    }

    public void delete(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.delete(product);
        });
    }
}