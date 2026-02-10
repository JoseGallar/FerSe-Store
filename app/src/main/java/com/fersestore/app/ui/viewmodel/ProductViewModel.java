package com.fersestore.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.data.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository mRepository;
    private LiveData<List<ProductWithVariants>> mAllProducts;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ProductRepository(application);
        mAllProducts = mRepository.getAllProducts();
    }

    public LiveData<List<ProductWithVariants>> getAllProducts() {
        return mAllProducts;
    }

    // Esta es la función que llamaremos desde "AddProductActivity"
    public void insert(ProductEntity product, List<ProductVariantEntity> variants) {
        mRepository.insertProductWithVariants(product, variants);
    }

    public void update(ProductEntity product) {
        mRepository.update(product);
    }

    // Función para actualizar SOLO una variante (ej: restar stock al vender)
    public void updateVariant(ProductVariantEntity variant) {
        mRepository.updateVariant(variant);
    }

    public void delete(ProductEntity product) {
        mRepository.delete(product);
    }

    // Agrega esto antes de la última llave }
    public LiveData<com.fersestore.app.data.entity.ProductWithVariants> getProductById(int id) {
        return mRepository.getProductById(id);
    }

    public void insertNewVariant(ProductVariantEntity variant) {
        mRepository.insertVariant(variant);
    }
}