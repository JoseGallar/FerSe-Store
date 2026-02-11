package com.fersestore.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.database.AppDatabase;
import com.fersestore.app.data.dao.ProductDao;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.ProductWithVariants;

import java.util.List;

public class ProductRepository {

    private ProductDao productDao;
    private LiveData<List<ProductWithVariants>> allProducts;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<ProductWithVariants>> getAllProducts() {
        return allProducts;
    }

    // Insertar producto con sus variantes (Padre + Hijos)
    public void insertProductWithVariants(ProductEntity product, List<ProductVariantEntity> variants) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long newId = productDao.insertProduct(product);
            for (ProductVariantEntity variant : variants) {
                variant.productId = (int) newId;
            }
            productDao.insertVariants(variants);
        });
    }

    public void update(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateProduct(product));
    }

    public void updateVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateVariant(variant));
    }

    // Borrar producto entero (Padre)
    public void delete(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.deleteProduct(product));
    }

    // Borrar solo una variante (Hijo) - PARA LA "X" ROJA 🔴
    public void deleteVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.deleteVariant(variant));
    }

    public LiveData<ProductWithVariants> getProductById(int id) {
        return productDao.getProductById(id);
    }

    // Insertar solo una variante nueva - PARA EL BOTÓN "AGREGAR COLOR"
    public void insertVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insertVariant(variant));
    }
}