package com.fersestore.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository mRepository;
    private LiveData<List<ProductEntity>> mAllProducts;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ProductRepository(application);
        mAllProducts = mRepository.getAllProducts();
    }

    public LiveData<List<ProductEntity>> getAllProducts() {
        return mAllProducts;
    }

    // AQUÍ ESTABA EL ERROR: Antes decía "Product product", ahora es "ProductEntity product"
    public void insert(ProductEntity product) {
        mRepository.insert(product);
    }

    public void update(ProductEntity product) {
        mRepository.update(product);
    }

    public void delete(ProductEntity product) {
        mRepository.delete(product);
    }
}