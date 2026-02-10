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
    // OJO: Ahora la lista es de "ProductWithVariants" (Padre + Hijos)
    private LiveData<List<ProductWithVariants>> allProducts;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<ProductWithVariants>> getAllProducts() {
        return allProducts;
    }

    // --- LA NUEVA FUNCIÓN DE INSERTAR ---
    // Recibe al Padre y una lista de sus Hijos
    public void insertProductWithVariants(ProductEntity product, List<ProductVariantEntity> variants) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Insertamos el Padre y obtenemos su nuevo ID (ej: 50)
            long newId = productDao.insertProduct(product);

            // 2. Le asignamos ese ID a todos los hijos
            for (ProductVariantEntity variant : variants) {
                variant.productId = (int) newId;
            }

            // 3. Insertamos los hijos
            productDao.insertVariants(variants);
        });
    }

    public void update(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateProduct(product));
    }

    // Función para actualizar stock cuando vendes (solo tocas al hijo)
    public void updateVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateVariant(variant));
    }

    public void delete(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.deleteProduct(product));
    }

    // Agrega esto antes de la última llave }
    public LiveData<com.fersestore.app.data.entity.ProductWithVariants> getProductById(int id) {
        return productDao.getProductById(id);
    }

    public void insertVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insertVariant(variant));
    }
}