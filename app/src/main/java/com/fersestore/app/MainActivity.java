package com.fersestore.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.adapter.ProductAdapter;
import com.fersestore.app.ui.view.AddProductActivity;
import com.fersestore.app.ui.view.CalculatorActivity;
import com.fersestore.app.ui.view.FinancialActivity;
import com.fersestore.app.ui.view.ProductDetailActivity;
import com.fersestore.app.ui.view.WalletActivity;
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private TransactionViewModel transactionViewModel;
    private ProductAdapter adapter;

    private TextView tvTodaySales, tvEmpty;

    // --- LISTAS PARA EL FILTRO ---
    private List<ProductWithVariants> fullProductList = new ArrayList<>();
    private List<ProductWithVariants> filteredList = new ArrayList<>();

    // Categoría seleccionada
    private String currentCategory = "Todos";

    // --- PAGINACIÓN ---
    private int currentPage = 0;
    private final int ITEMS_PER_PAGE = 20;
    private TextView tvPageInfo;
    private android.widget.Button btnPrev, btnNext;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Cargar Tema
        sharedPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(" FerSe Store");
            getSupportActionBar().setElevation(0);
        }

        // --- ACCESOS RÁPIDOS ---
        findViewById(R.id.btn_quick_wallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.btn_quick_finance).setOnClickListener(v -> startActivity(new Intent(this, FinancialActivity.class)));
        findViewById(R.id.btn_quick_calc).setOnClickListener(v -> startActivity(new Intent(this, CalculatorActivity.class)));

        // Configurar Botones y Paginación
        setupCategoryButtons();
        setupPaginationControls();

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_data", product);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tvTodaySales = findViewById(R.id.tv_home_today_sales);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // --- OBSERVADORES ---

        // 1. Productos
        productViewModel.getAllProducts().observe(this, products -> {
            fullProductList = products;
            filterByCategory(currentCategory);
        });

        // 2. Transacciones (Aquí calculamos "Ventas de Hoy")
        transactionViewModel.getHistory().observe(this, this::calculateTodaySales);

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        // --- AGREGAR ESTO AL FINAL DEL ONCREATE ---
        if (getSupportActionBar() != null) {
            // 1. Habilitar la vista personalizada
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);

            // 2. Decirle qué archivo XML cargar (TU BARRA CON LOGO)
            getSupportActionBar().setCustomView(R.layout.custom_toolbar);

            // 3. (Opcional) Quitar la elevación (sombra) si quieres que se vea plano
            getSupportActionBar().setElevation(0);
        }
    }

    // --- LÓGICA DE VENTAS DE HOY (CORREGIDA) ---
    // --- LÓGICA DE VENTAS DE HOY (CORREGIDA) ---
    private void calculateTodaySales(List<TransactionEntity> transactions) {
        double todayTotal = 0;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTimeInMillis(t.timestamp);

                // 1. Verificamos que sea HOY
                if (transCal.get(Calendar.DAY_OF_YEAR) == day && transCal.get(Calendar.YEAR) == year) {

                    // 2. CORRECCIÓN IMPORTANTE: Usamos .equals() en lugar de ==
                    // Además verificamos que t.type no sea null para evitar cierres inesperados
                    boolean esIngreso = t.type != null && t.type.equals(TransactionType.INCOME);

                    if (esIngreso && !esInversion(t)) {
                        todayTotal += t.totalAmount;
                    }
                }
            }
        }
        tvTodaySales.setText("$ " + String.format("%.0f", todayTotal));
    }

    // Filtro para ignorar inversiones en la tarjeta de Ventas
    private boolean esInversion(TransactionEntity t) {
        if (t.description == null) return false;
        String desc = t.description.toLowerCase();
        return desc.contains("inversión") || desc.contains("inversion") || desc.contains("capital");
    }

    // --- FILTROS DE CATEGORÍA ---
    private void setupCategoryButtons() {
        int[] ids = {
                R.id.btn_cat_todos, R.id.btn_cat_remeras, R.id.btn_cat_pantalones,
                R.id.btn_cat_accesorios, R.id.btn_cat_abrigos, R.id.btn_cat_otros
        };
        String[] cats = {"Todos", "Remeras", "Pantalones", "Accesorios", "Abrigos", "Otros"};

        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            String cat = cats[i];
            findViewById(id).setOnClickListener(v -> {
                filterByCategory(cat);
                actualizarColoresBotones(id);
            });
        }
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        filteredList.clear();

        if (category.equalsIgnoreCase("Todos")) {
            filteredList.addAll(fullProductList);
        } else {
            for (ProductWithVariants item : fullProductList) {
                if (item.product.category != null && item.product.category.equalsIgnoreCase(category)) {
                    filteredList.add(item);
                }
            }
        }

        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        if (filteredList.isEmpty()) {
            tvEmpty.setText("No hay productos en " + category);
        } else {
            tvEmpty.setText("No hay productos cargados");
        }

        currentPage = 0; // Reiniciar página
        actualizarPaginacion();
    }

    // --- PAGINACIÓN ---
    private void setupPaginationControls() {
        tvPageInfo = findViewById(R.id.tv_page_info);
        btnPrev = findViewById(R.id.btn_page_prev);
        btnNext = findViewById(R.id.btn_page_next);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                actualizarPaginacion();
            }
        });

        btnNext.setOnClickListener(v -> {
            int totalPages = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                actualizarPaginacion();
            }
        });
    }

    private void actualizarPaginacion() {
        int totalItems = filteredList.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        if (totalPages == 0) totalPages = 1;
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalItems);

        List<ProductWithVariants> pageItems = new ArrayList<>();
        if (totalItems > 0) {
            pageItems = filteredList.subList(start, end);
        }

        adapter.setProductList(pageItems);

        if (tvPageInfo != null) tvPageInfo.setText((currentPage + 1) + " / " + totalPages);

        if (btnPrev != null && btnNext != null) {
            btnPrev.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            btnPrev.setAlpha(currentPage > 0 ? 1.0f : 0.3f);
            btnNext.setAlpha(currentPage < totalPages - 1 ? 1.0f : 0.3f);
        }
    }

    // --- MENÚ Y EXTRAS ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, 0, Menu.NONE, "Buscar");
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        SearchView searchView = new SearchView(this);
        searchItem.setActionView(searchView);
        searchView.setQueryHint("Buscar ropa...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) { filterListByName(newText); return true; }
        });

        menu.add(Menu.NONE, 4, Menu.NONE, "💾 Copia de Seguridad");
        boolean isDark = sharedPreferences.getBoolean("DARK_MODE", false);
        String themeTitle = isDark ? "☀️ Modo Claro" : "🌙 Modo Oscuro";
        menu.add(Menu.NONE, 5, Menu.NONE, themeTitle);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 4) checkPermissionAndExport();
        if (id == 5) { toggleTheme(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        boolean isDark = sharedPreferences.getBoolean("DARK_MODE", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("DARK_MODE", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("DARK_MODE", true);
        }
        editor.apply();
    }

    private void filterListByName(String text) {
        List<ProductWithVariants> searchResults = new ArrayList<>();
        for (ProductWithVariants item : filteredList) {
            if (item.product.getName().toLowerCase().contains(text.toLowerCase())) {
                searchResults.add(item);
            }
        }
        adapter.setProductList(searchResults);
    }

    private void checkPermissionAndExport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                exportToCSV();
            }
        } else {
            exportToCSV();
        }
    }

    private void exportToCSV() {
        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "FerSe_Backup_" + System.currentTimeMillis() + ".csv";
            File file = new File(folder, fileName);
            FileWriter writer = new FileWriter(file);
            writer.append("ID,Producto,Costo,Venta,Stock Total\n");
            for (ProductWithVariants item : fullProductList) {
                ProductEntity p = item.product;
                writer.append(p.id + "," + p.name + "," + p.costPrice + "," + p.salePrice + "," + item.getTotalStock() + "\n");
            }
            writer.flush();
            writer.close();
            Toast.makeText(this, "Backup guardado en Descargas", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarColoresBotones(int idBotonActivo) {
        int[] ids = {
                R.id.btn_cat_todos, R.id.btn_cat_remeras, R.id.btn_cat_pantalones,
                R.id.btn_cat_abrigos, R.id.btn_cat_accesorios, R.id.btn_cat_otros
        };
        int colorActivo = android.graphics.Color.parseColor("#546E7A");
        int colorInactivo = android.graphics.Color.parseColor("#CFD8DC");
        int textoBlanco = android.graphics.Color.WHITE;
        int textoGris = android.graphics.Color.parseColor("#455A64");

        for (int id : ids) {
            android.widget.Button btn = findViewById(id);
            if (btn != null) {
                if (id == idBotonActivo) {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivo));
                    btn.setTextColor(textoBlanco);
                } else {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivo));
                    btn.setTextColor(textoGris);
                }
            }
        }
    }
}