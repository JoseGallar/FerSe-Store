package com.fersestore.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.fersestore.app.ui.view.CalculatorActivity;
import com.fersestore.app.ui.view.ProductDetailActivity;
import com.fersestore.app.ui.view.WalletActivity; // Usamos la nueva Billetera
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private TransactionViewModel transactionViewModel;
    private ProductAdapter adapter;
    private TextView tvTodaySales, tvEmpty;

    // Filtros de categoría
    private Button btnAll, btnShirts, btnPants, btnAccessories, btnOther;
    private List<ProductEntity> fullProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuración inicial de vistas
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tvTodaySales = findViewById(R.id.tv_home_today_sales);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        // Botones de categoría
        btnAll = findViewById(R.id.cat_all);
        btnShirts = findViewById(R.id.cat_shirts);
        btnPants = findViewById(R.id.cat_pants);
        btnAccessories = findViewById(R.id.cat_accessories);
        btnOther = findViewById(R.id.cat_other);

        setupCategoryButtons();

        // ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // 1. OBSERVAR PRODUCTOS
        productViewModel.getAllProducts().observe(this, products -> {
            fullProductList = products;
            filterByCategory("Todos"); // Por defecto mostramos todo
            updateEmptyState(products.isEmpty());
        });

        // 2. OBSERVAR VENTAS (Aquí actualizamos para usar la lógica nueva)
        transactionViewModel.getHistory().observe(this, this::calculateTodaySales);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            startActivity(intent);
        });
    }

    // --- CÁLCULO DE VENTAS DE HOY ---
    private void calculateTodaySales(List<TransactionEntity> transactions) {
        double todayTotal = 0;
        Calendar cal = Calendar.getInstance();
        int todayDay = cal.get(Calendar.DAY_OF_YEAR);
        int todayYear = cal.get(Calendar.YEAR);

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                cal.setTimeInMillis(t.timestamp);
                // Si es HOY y es un INGRESO (Venta)
                if (cal.get(Calendar.DAY_OF_YEAR) == todayDay &&
                        cal.get(Calendar.YEAR) == todayYear &&
                        t.type == TransactionType.INCOME) {

                    // CORRECCIÓN CLAVE: Usamos totalAmount en lugar de amount
                    todayTotal += t.totalAmount;
                }
            }
        }
        tvTodaySales.setText("$ " + String.format("%.2f", todayTotal));
    }

    // --- MENÚ SUPERIOR ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Buscador
        MenuItem searchItem = menu.add(Menu.NONE, R.id.action_search, Menu.NONE, "Buscar");
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        androidx.appcompat.widget.SearchView searchView = new androidx.appcompat.widget.SearchView(this);
        searchItem.setActionView(searchView);
        searchView.setQueryHint("Buscar producto...");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                resetCategoryButtons(btnAll);
                filterList(newText);
                return true;
            }
        });

        // NUEVO: Billetera en lugar de Finanzas simple
        menu.add(Menu.NONE, 1, Menu.NONE, "💼 Mi Billetera");

        // Calculadora
        menu.add(Menu.NONE, 2, Menu.NONE, "🧮 Calculadora de Costos");

        // Backup
        menu.add(Menu.NONE, 99, Menu.NONE, "💾 Copia de Seguridad");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) { // Abrir Billetera
            startActivity(new Intent(this, WalletActivity.class));
            return true;
        }

        if (id == 2) { // Abrir Calculadora
            startActivity(new Intent(this, CalculatorActivity.class));
            return true;
        }

        if (id == 99) { // Backup
            checkPermissionAndExport();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // --- LÓGICA DE FILTROS Y BUSCADOR ---
    private void setupCategoryButtons() {
        View.OnClickListener listener = v -> {
            Button clicked = (Button) v;
            resetCategoryButtons(clicked);
            String cat = clicked.getText().toString();
            filterByCategory(cat);
        };

        btnAll.setOnClickListener(listener);
        btnShirts.setOnClickListener(listener);
        btnPants.setOnClickListener(listener);
        btnAccessories.setOnClickListener(listener);
        btnOther.setOnClickListener(listener);
    }

    private void resetCategoryButtons(Button active) {
        btnAll.setAlpha(0.5f); btnShirts.setAlpha(0.5f);
        btnPants.setAlpha(0.5f); btnAccessories.setAlpha(0.5f); btnOther.setAlpha(0.5f);
        active.setAlpha(1.0f);
    }

    private void filterByCategory(String category) {
        if (category.equals("Todos")) {
            adapter.setProductList(fullProductList);
        } else {
            List<ProductEntity> filtered = new ArrayList<>();
            for (ProductEntity p : fullProductList) {
                if (p.getCategory().equalsIgnoreCase(category)) {
                    filtered.add(p);
                }
            }
            adapter.setProductList(filtered);
        }
    }

    private void filterList(String text) {
        List<ProductEntity> filteredList = new ArrayList<>();
        for (ProductEntity item : fullProductList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.setProductList(filteredList);
        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // --- BACKUP (Exportar CSV) ---
    private void checkPermissionAndExport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
            String fileName = "Backup_FerSe_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".csv";
            File file = new File(folder, fileName);

            FileWriter writer = new FileWriter(file);
            writer.append("ID,Producto,Stock,PrecioVenta\n");

            for (ProductEntity p : fullProductList) {
                writer.append(String.valueOf(p.getId())).append(",");
                writer.append(p.getName()).append(",");
                writer.append(String.valueOf(p.getCurrentStock())).append(",");
                writer.append(String.valueOf(p.getSalePrice())).append("\n");
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "Backup guardado en Descargas: " + fileName, Toast.LENGTH_LONG).show();

            // Compartir archivo
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(intent, "Compartir Backup"));

        } catch (Exception e) {
            Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}