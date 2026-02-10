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

    // CORREGIDO: La lista maestra maneja "Paquetes" (Padre + Hijos)
    private List<ProductWithVariants> fullProductList = new ArrayList<>();

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Cargar Tema guardado antes de inflar la vista
        sharedPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
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

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar Adapter
        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            // Al hacer click, mandamos al Detalle (ProductDetailActivity)
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_data", product);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Vistas y ViewModel
        tvTodaySales = findViewById(R.id.tv_home_today_sales);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // --- OBSERVADORES ---

        // 1. Productos: Actualizamos la lista cuando la base de datos cambie
        productViewModel.getAllProducts().observe(this, products -> {
            fullProductList = products;
            adapter.setProductList(products);
            tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 2. Transacciones: Calculamos lo vendido hoy
        transactionViewModel.getHistory().observe(this, this::calculateTodaySales);

        // Botón agregar
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
    }

    private void calculateTodaySales(List<TransactionEntity> transactions) {
        double todayTotal = 0;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                // Verificar si la fecha es HOY
                Calendar transCal = Calendar.getInstance();
                transCal.setTimeInMillis(t.timestamp);

                if (transCal.get(Calendar.DAY_OF_YEAR) == day && transCal.get(Calendar.YEAR) == year) {
                    if (t.type == TransactionType.INCOME) {
                        todayTotal += t.totalAmount;
                    }
                }
            }
        }
        // CORREGIDO: Formato sin decimales (%.0f)
        tvTodaySales.setText("$ " + String.format("%.0f", todayTotal));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menú Buscar
        MenuItem searchItem = menu.add(Menu.NONE, 0, Menu.NONE, "Buscar");
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        SearchView searchView = new SearchView(this);
        searchItem.setActionView(searchView);
        searchView.setQueryHint("Buscar ropa...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) { filterList(newText); return true; }
        });

        // Menús Extras
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
        // No hace falta recreate(), el cambio de modo noche lo hace solo a veces,
        // pero por seguridad podríamos forzarlo si no refresca.
    }

    // --- CORREGIDO: FILTRADO CON LA NUEVA ESTRUCTURA ---
    private void filterList(String text) {
        List<ProductWithVariants> filtered = new ArrayList<>();

        for (ProductWithVariants item : fullProductList) {
            // Buscamos dentro del objeto "product" del paquete
            if (item.product.getName().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.setProductList(filtered);
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

    // --- CORREGIDO: EXPORTAR CSV CON STOCK CALCULADO ---
    private void exportToCSV() {
        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "FerSe_Backup_" + System.currentTimeMillis() + ".csv";
            File file = new File(folder, fileName);
            FileWriter writer = new FileWriter(file);

            writer.append("ID,Producto,Costo,Venta,Stock Total\n");

            for (ProductWithVariants item : fullProductList) {
                ProductEntity p = item.product;
                // Usamos item.getTotalStock() en vez de p.currentStock
                writer.append(p.id + "," + p.name + "," + p.costPrice + "," + p.salePrice + "," + item.getTotalStock() + "\n");
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "Backup guardado en Descargas", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}