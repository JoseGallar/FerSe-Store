package com.fersestore.app;

import androidx.appcompat.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.adapter.ProductAdapter;
import com.fersestore.app.ui.view.AddProductActivity;
import com.fersestore.app.ui.view.CalculatorActivity;
import com.fersestore.app.ui.view.FinancialActivity; // Recuperamos Finanzas
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
    private List<ProductEntity> fullProductList = new ArrayList<>();

    // Para guardar la preferencia de Modo Oscuro/Claro
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configuración DE LUJO para la Barra
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_toolbar);
            getSupportActionBar().setElevation(0); // Quita la sombra para que quede plano
        }

        // 1. Cargar Tema guardado ANTES de crear la vista
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
        // Configurar Toolbar (Logo Chiquito + Título)
        if (getSupportActionBar() != null) {
            // 1. Forzar que se vean AMBOS (Logo y Título)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true); // ¡Esto recupera el nombre "FerSe Store"!

            getSupportActionBar().setTitle(" FerSe Store"); // Le dejamos 2 espacios para separarlo del logo
            getSupportActionBar().setElevation(0);

            // 2. Lógica para ACHICAR el logo (para que no se vea gigante)
            try {
                // CAMBIÁ "ic_launcher" POR EL NOMBRE DE TU LOGO (ej: R.drawable.mi_logo)
                Drawable original = ContextCompat.getDrawable(this, R.drawable.logo_ferse);


            } catch (Exception e) {
                // Si falla algo, no pasa nada, no ponemos logo
                e.printStackTrace();
            }

            // --- ACCESOS RÁPIDOS ---
            // 1. Billetera
            findViewById(R.id.btn_quick_wallet).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, WalletActivity.class))
            );

            // 2. Finanzas (Historial)
            findViewById(R.id.btn_quick_finance).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, FinancialActivity.class))
            );

            // 3. Calculadora
            findViewById(R.id.btn_quick_calc).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, CalculatorActivity.class))
            );

        }


        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_data", product); // <--- ESTO ES LA CLAVE
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Vistas y ViewModel
        tvTodaySales = findViewById(R.id.tv_home_today_sales);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observadores
        productViewModel.getAllProducts().observe(this, products -> {
            fullProductList = products;
            adapter.setProductList(products);
            tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        transactionViewModel.getHistory().observe(this, this::calculateTodaySales);

        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddProductActivity.class)));
    }

    private void calculateTodaySales(List<TransactionEntity> transactions) {
        double todayTotal = 0;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                cal.setTimeInMillis(t.timestamp);
                if (cal.get(Calendar.DAY_OF_YEAR) == day && cal.get(Calendar.YEAR) == year) {
                    if (t.type == TransactionType.INCOME) {
                        todayTotal += t.totalAmount;
                    }
                }
            }
        }
        tvTodaySales.setText("$ " + String.format("%.2f", todayTotal));
    }

    // --- MENÚ DE LOS 3 PUNTITOS ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Buscador (Ese siempre es útil dejarlo)
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

        // --- SOLO DEJAMOS LO QUE PEDISTE ---
        menu.add(Menu.NONE, 4, Menu.NONE, "💾 Copia de Seguridad"); // ID 4

        // Opción de Tema
        boolean isDark = sharedPreferences.getBoolean("DARK_MODE", false);
        String themeTitle = isDark ? "☀️ Modo Claro" : "🌙 Modo Oscuro";
        menu.add(Menu.NONE, 5, Menu.NONE, themeTitle); // ID 5

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) startActivity(new Intent(this, WalletActivity.class));
        if (id == 2) startActivity(new Intent(this, FinancialActivity.class)); // Abre el historial
        if (id == 3) startActivity(new Intent(this, CalculatorActivity.class));
        if (id == 4) checkPermissionAndExport();

        if (id == 5) { toggleTheme(); return true; } // Cambiar tema

        return super.onOptionsItemSelected(item);
    }

    // --- LÓGICA DE CAMBIO DE TEMA CORREGIDA ---
    private void toggleTheme() {
        boolean isDark = sharedPreferences.getBoolean("DARK_MODE", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isDark) {
            // Cambiar a Claro
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("DARK_MODE", false);
            Toast.makeText(this, "Modo Claro Activado ☀️", Toast.LENGTH_SHORT).show();
        } else {
            // Cambiar a Oscuro
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("DARK_MODE", true);
            Toast.makeText(this, "Modo Oscuro Activado 🌙", Toast.LENGTH_SHORT).show();
        }
        editor.apply();

        // ¡IMPORTANTE! BORRAMOS LA LÍNEA "recreate();"
        // El AppCompatDelegate ya se encarga de reiniciar la actividad automáticamente.
    }

    private void filterList(String text) {
        List<ProductEntity> filtered = new ArrayList<>();
        for (ProductEntity p : fullProductList) {
            if (p.getName().toLowerCase().contains(text.toLowerCase())) filtered.add(p);
        }
        adapter.setProductList(filtered);
    }

    // --- BACKUP ---
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
            writer.append("ID,Producto,Costo,Venta,Stock\n");
            for (ProductEntity p : fullProductList) {
                writer.append(p.getId() + "," + p.getName() + "," + p.costPrice + "," + p.getSalePrice() + "," + p.getCurrentStock() + "\n");
            }
            writer.flush();
            writer.close();
            Toast.makeText(this, "Backup guardado en Descargas", Toast.LENGTH_LONG).show();
            shareFile(file);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, "Compartir Backup"));
    }

}