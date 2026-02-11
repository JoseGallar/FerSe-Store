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

    public void insert(ProductEntity product, List<ProductVariantEntity> variants) {
        mRepository.insertProductWithVariants(product, variants);
    }

    public void update(ProductEntity product) {
        mRepository.update(product);
    }

    public void updateVariant(ProductVariantEntity variant) {
        mRepository.updateVariant(variant);
    }

    public void delete(ProductEntity product) {
        mRepository.delete(product);
    }

    // --- AQUÍ ESTABA EL PROBLEMA (YA ESTÁ ARREGLADO) ---
    public void deleteVariant(ProductVariantEntity variant) {
        mRepository.deleteVariant(variant);
    }

    // El error era aquí: mRepository tiene "insertVariant", no "insertNewVariant"
    public void insertNewVariant(ProductVariantEntity variant) {
        mRepository.insertVariant(variant); // <--- CORREGIDO
    }
    // ---------------------------------------------------

    public LiveData<ProductWithVariants> getProductById(int id) {
        return mRepository.getProductById(id);
    }
}