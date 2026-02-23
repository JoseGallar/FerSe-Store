package com.fersestore.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.data.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository mRepository;

    // Estado de la Paginación
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("Todos");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private static final int ITEMS_PER_PAGE = 20;

    // Resultados observables
    private final MediatorLiveData<List<ProductWithVariants>> pagedProducts = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> totalCount = new MediatorLiveData<>();

    // --- CORRECCIÓN: Variables globales para recordar la consulta anterior ---
    private LiveData<List<ProductWithVariants>> currentSource = null;
    private LiveData<Integer> currentCountSource = null;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ProductRepository(application);
        setupTriggers();
    }

    // Configura los "gatillos": Si cambia la página, categoría o búsqueda -> Recargar DB
    private void setupTriggers() {
        pagedProducts.addSource(currentPage, page -> refreshData());

        pagedProducts.addSource(currentCategory, cat -> {
            if (currentPage.getValue() != 0) {
                currentPage.setValue(0); // Esto disparará refreshData por el observer de arriba
            } else {
                refreshData(); // Si ya estaba en 0, forzamos el refresco
            }
        });

        pagedProducts.addSource(searchQuery, query -> {
            if (currentPage.getValue() != 0) {
                currentPage.setValue(0);
            } else {
                refreshData();
            }
        });
    }

    private void refreshData() {
        int page = currentPage.getValue() != null ? currentPage.getValue() : 0;
        String cat = currentCategory.getValue();
        String query = searchQuery.getValue();
        int offset = page * ITEMS_PER_PAGE;

        // 1. LIMPIEZA: Removemos las fuentes ANTERIORES si existen
        if (currentSource != null) {
            pagedProducts.removeSource(currentSource);
        }
        if (currentCountSource != null) {
            totalCount.removeSource(currentCountSource);
        }

        // 2. ASIGNACIÓN: Obtenemos las nuevas fuentes del repositorio
        if (query != null && !query.isEmpty()) {
            currentSource = mRepository.searchPagedProducts(query, ITEMS_PER_PAGE, offset);
            currentCountSource = mRepository.getCountBySearch(query);
        } else if (cat != null && !cat.equals("Todos")) {
            currentSource = mRepository.getPagedProductsByCategory(cat, ITEMS_PER_PAGE, offset);
            currentCountSource = mRepository.getCountByCategory(cat);
        } else {
            currentSource = mRepository.getPagedProducts(ITEMS_PER_PAGE, offset);
            currentCountSource = mRepository.getCountAll();
        }

        // 3. CONEXIÓN: Agregamos las nuevas fuentes al MediatorLiveData
        pagedProducts.addSource(currentSource, list -> pagedProducts.setValue(list));
        totalCount.addSource(currentCountSource, count -> totalCount.setValue(count));
    }

    // --- GETTERS PARA LA UI ---
    public LiveData<List<ProductWithVariants>> getPagedProducts() { return pagedProducts; }
    public LiveData<Integer> getTotalItemCount() { return totalCount; }
    public LiveData<Integer> getCurrentPage() { return currentPage; }
    public int getItemsPerPage() { return ITEMS_PER_PAGE; }

    // --- ACCIONES DE UI ---
    public void setPage(int page) {
        if (page < 0) page = 0;
        currentPage.setValue(page);
    }

    public void nextPage() {
        int current = currentPage.getValue() != null ? currentPage.getValue() : 0;
        setPage(current + 1);
    }

    public void prevPage() {
        int current = currentPage.getValue() != null ? currentPage.getValue() : 0;
        if (current > 0) setPage(current - 1);
    }

    public void setCategory(String category) {
        // Solo actualizamos si es diferente para evitar recargas innecesarias
        if (!category.equals(currentCategory.getValue())) {
            searchQuery.setValue("");
            currentCategory.setValue(category);
        }
    }

    public void setSearch(String query) {
        // Solo actualizamos si es diferente
        if (!query.equals(searchQuery.getValue())) {
            searchQuery.setValue(query);
        }
    }

    // --- MÉTODOS EXISTENTES (Insert, Update, Delete) ---
    public void insert(ProductEntity product, List<ProductVariantEntity> variants) {
        mRepository.insertProductWithVariants(product, variants);
    }
    public void update(ProductEntity product) { mRepository.update(product); }
    public void updateVariant(ProductVariantEntity variant) { mRepository.updateVariant(variant); }
    public void delete(ProductEntity product) { mRepository.delete(product); }
    public void deleteVariant(ProductVariantEntity variant) { mRepository.deleteVariant(variant); }
    public void insertNewVariant(ProductVariantEntity variant) {
        mRepository.insertVariant(variant); // ✅ AHORA SÍ LLAMA AL MÉTODO REAL
    }
    public LiveData<ProductWithVariants> getProductById(int id) { return mRepository.getProductById(id); }
    public void devolverStock(int variantId, int cantidad) { mRepository.devolverStock(variantId, cantidad); }

    public List<ProductWithVariants> getAllProductsForBackup() {
        return mRepository.getAllProductsSync();
    }

    public LiveData<Double> getTotalStockValue() {
        return mRepository.getTotalStockValue();
    }
}