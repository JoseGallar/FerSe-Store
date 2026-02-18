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

    private TextView tvTodaySales, tvEmpty, tvPageInfo;
    private Button btnPrev, btnNext;
    private SharedPreferences sharedPreferences;

    // Variables de estado local para la paginación visual
    private int totalItemsCount = 0;

    // NUEVA VARIABLE PARA GUARDAR LA POSICIÓN
    private android.os.Parcelable listState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configuración de tema
        sharedPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupQuickAccess();

        // Inicializar ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Inicializar UI de Lista
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Al adaptador le pasamos una lista vacía, se llenará con el Observer
        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_data", product);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tvTodaySales = findViewById(R.id.tv_home_today_sales);
        tvEmpty = findViewById(R.id.tv_empty);
        tvPageInfo = findViewById(R.id.tv_page_info);
        btnPrev = findViewById(R.id.btn_page_prev);
        btnNext = findViewById(R.id.btn_page_next);

        setupCategoryButtons();
        setupPaginationControls();

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        // --- OBSERVAR DATOS (La parte optimizada) ---

        // 1. Observar la LISTA PAGINADA (Solo llegan 20 items)
        productViewModel.getPagedProducts().observe(this, products -> {
            if (products != null) {
                adapter.setProductList(products);
                tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);

                // Texto de vacío más descriptivo
                if (products.isEmpty()) {
                    tvEmpty.setText("No se encontraron productos");
                }

                if (listState != null && recyclerView.getLayoutManager() != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(listState);
                    listState = null; // Ya lo usamos, lo limpiamos
                }
            }
        });

        // 2. Observar el CONTEO TOTAL (Para saber si habilitar botón Next)
        productViewModel.getTotalItemCount().observe(this, count -> {
            totalItemsCount = count != null ? count : 0;
            updatePaginationInfo();
        });

        // 3. Observar la PÁGINA ACTUAL (Para actualizar el texto 1/X)
        productViewModel.getCurrentPage().observe(this, page -> {
            updatePaginationInfo();
        });

        // 4. Calcular Ventas de Hoy
        transactionViewModel.getHistory().observe(this, this::calculateTodaySales);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_toolbar);
            getSupportActionBar().setElevation(0);
        }
    }

    private void setupQuickAccess() {
        findViewById(R.id.btn_quick_wallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.btn_quick_finance).setOnClickListener(v -> startActivity(new Intent(this, FinancialActivity.class)));
        findViewById(R.id.btn_quick_calc).setOnClickListener(v -> startActivity(new Intent(this, CalculatorActivity.class)));
    }

    // --- PAGINACIÓN ---
    private void setupPaginationControls() {
        btnPrev.setOnClickListener(v -> productViewModel.prevPage());
        btnNext.setOnClickListener(v -> productViewModel.nextPage());
    }

    private void updatePaginationInfo() {
        int currentPage = productViewModel.getCurrentPage().getValue() != null ? productViewModel.getCurrentPage().getValue() : 0;
        int itemsPerPage = productViewModel.getItemsPerPage();

        // Calcular total de páginas
        int totalPages = (int) Math.ceil((double) totalItemsCount / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Actualizar texto visual (Humano ve página 1, código es 0)
        tvPageInfo.setText((currentPage + 1) + " / " + totalPages);

        // Habilitar/Deshabilitar botones
        btnPrev.setEnabled(currentPage > 0);
        btnPrev.setAlpha(currentPage > 0 ? 1.0f : 0.3f);

        boolean hayMasPaginas = (currentPage < totalPages - 1);
        btnNext.setEnabled(hayMasPaginas);
        btnNext.setAlpha(hayMasPaginas ? 1.0f : 0.3f);
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
                productViewModel.setCategory(cat); // Esto dispara la recarga en BD
                actualizarColoresBotones(id);
            });
        }
    }

    // --- BÚSQUEDA ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, 0, Menu.NONE, "Buscar");
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        SearchView searchView = new SearchView(this);
        searchItem.setActionView(searchView);
        searchView.setQueryHint("Buscar ropa...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                productViewModel.setSearch(query);
                return false;
            }
            @Override public boolean onQueryTextChange(String newText) {
                productViewModel.setSearch(newText); // Búsqueda en tiempo real optimizada
                return true;
            }
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

    // --- EXPORTAR CSV (Usamos el método síncrono del ViewModel nuevo) ---
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
        new Thread(() -> {
            try {
                // PEDIMOS TODOS LOS DATOS (SIN PAGINAR) EN UN HILO SECUNDARIO
                List<ProductWithVariants> allProducts = productViewModel.getAllProductsForBackup();

                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String fileName = "FerSe_Backup_" + System.currentTimeMillis() + ".csv";
                File file = new File(folder, fileName);
                FileWriter writer = new FileWriter(file);
                writer.append("ID,Producto,Costo,Venta,Stock Total\n");

                if (allProducts != null) {
                    for (ProductWithVariants item : allProducts) {
                        ProductEntity p = item.product;
                        writer.append(p.id + "," + p.name + "," + p.costPrice + "," + p.salePrice + "," + item.getTotalStock() + "\n");
                    }
                }

                writer.flush();
                writer.close();

                runOnUiThread(() -> Toast.makeText(this, "Backup guardado en Descargas", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // --- UTILIDADES ---
    private void calculateTodaySales(List<TransactionEntity> transactions) {
        double todayTotal = 0;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTimeInMillis(t.timestamp);
                if (transCal.get(Calendar.DAY_OF_YEAR) == day && transCal.get(Calendar.YEAR) == year) {
                    boolean esIngreso = t.type != null && t.type.equals(TransactionType.INCOME);
                    if (esIngreso && !esInversion(t)) {
                        todayTotal += t.totalAmount;
                    }
                }
            }
        }
        tvTodaySales.setText("$ " + String.format("%.0f", todayTotal));
    }

    private boolean esInversion(TransactionEntity t) {
        if (t.description == null) return false;
        String desc = t.description.toLowerCase();
        return desc.contains("inversión") || desc.contains("inversion") || desc.contains("capital");
    }

    private void toggleTheme() {
        boolean isDark = sharedPreferences.getBoolean("DARK_MODE", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("DARK_MODE", !isDark);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(!isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void actualizarColoresBotones(int idBotonActivo) {
        int[] ids = {
                R.id.btn_cat_todos, R.id.btn_cat_remeras, R.id.btn_cat_pantalones,
                R.id.btn_cat_abrigos, R.id.btn_cat_accesorios, R.id.btn_cat_otros
        };
        int colorActivo = android.graphics.Color.parseColor("#546E7A");
        int colorInactivo = android.graphics.Color.parseColor("#CFD8DC");

        for (int id : ids) {
            Button btn = findViewById(id);
            if (btn != null) {
                if (id == idBotonActivo) {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivo));
                    btn.setTextColor(android.graphics.Color.WHITE);
                } else {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivo));
                    btn.setTextColor(android.graphics.Color.parseColor("#455A64"));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardamos la posición exacta del scroll
        RecyclerView rv = findViewById(R.id.recyclerView);
        if (rv != null && rv.getLayoutManager() != null) {
            listState = rv.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("recycler_state", listState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Recuperamos la posición cuando la actividad revive
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable("recycler_state");
        }
    }
}