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

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
    }

    public LiveData<List<ProductWithVariants>> getPagedProducts(int limit, int offset) {
        return productDao.getPagedProducts(limit, offset);
    }

    public LiveData<List<ProductWithVariants>> getPagedProductsByCategory(String category, int limit, int offset) {
        return productDao.getPagedProductsByCategory(category, limit, offset);
    }

    public LiveData<List<ProductWithVariants>> searchPagedProducts(String query, int limit, int offset) {
        return productDao.searchPagedProducts(query, limit, offset);
    }

    public LiveData<Integer> getCountAll() { return productDao.getCountAll(); }
    public LiveData<Integer> getCountByCategory(String cat) { return productDao.getCountByCategory(cat); }
    public LiveData<Integer> getCountBySearch(String query) { return productDao.getCountBySearch(query); }

    public List<ProductWithVariants> getAllProductsSync() {
        try {
            return AppDatabase.databaseWriteExecutor.submit(() -> productDao.getAllProductsSync()).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.updateVariant(variant);
            productDao.tocarTimbreProducto(variant.productId); // 🔔 TIMBRE
        });
    }

    public void delete(ProductEntity product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.deleteProduct(product));
    }

    public void deleteVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.deleteVariant(variant);
            productDao.tocarTimbreProducto(variant.productId); // 🔔 TIMBRE
        });
    }

    public LiveData<ProductWithVariants> getProductById(int id) {
        return productDao.getProductById(id);
    }

    public void insertVariant(ProductVariantEntity variant) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                productDao.insertVariant(variant);
                productDao.tocarTimbreProducto(variant.productId); // 🔔 TIMBRE
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void devolverStock(int variantId, int cantidad) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.devolverStock(variantId, cantidad);
            productDao.tocarTimbrePorVariante(variantId); // 🔔 TIMBRE ESPECIAL
        });
    }

    public LiveData<Double> getTotalStockValue() {
        return productDao.getTotalStockValue();
    }

}