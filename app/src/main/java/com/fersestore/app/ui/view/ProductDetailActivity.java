package com.fersestore.app.ui.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private TransactionViewModel transactionViewModel;
    private ProductEntity product; // CAMBIO: Ahora usamos ProductEntity
    private int productId;

    // Vistas
    private TextView tvName, tvPrice, tvStock, tvCategory;
    private ImageView imgProduct;

    // Mapa para manejar stock por colores (Ej: "Rojo" -> 5)
    private Map<String, Integer> stockMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Recibir ID del producto
        productId = getIntent().getIntExtra("PRODUCT_ID", -1);

        initViews();

        // Buscar el producto en la base de datos
        productViewModel.getAllProducts().observe(this, products -> {
            for (ProductEntity p : products) {
                if (p.getId() == productId) {
                    this.product = p;
                    loadProductData();
                    break;
                }
            }
        });

        findViewById(R.id.btn_sell).setOnClickListener(v -> showSellDialog());
    }

    private void initViews() {
        tvName = findViewById(R.id.detail_name);
        tvPrice = findViewById(R.id.detail_price);
        tvStock = findViewById(R.id.detail_stock);
        tvCategory = findViewById(R.id.detail_category);
        imgProduct = findViewById(R.id.detail_image);
    }

    private void loadProductData() {
        if (product == null) return;

        tvName.setText(product.getName());
        tvPrice.setText("$ " + product.getSalePrice());
        tvStock.setText("Stock Total: " + product.getCurrentStock());
        tvCategory.setText(product.getCategory());

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            imgProduct.setImageURI(Uri.parse(product.getImageUri()));
        }

        // Cargar desglose de stock (si existe)
        parseStockBreakdown(product.stockBreakdown);
    }

    private void parseStockBreakdown(String breakdown) {
        stockMap.clear();
        if (breakdown != null && !breakdown.isEmpty()) {
            String[] parts = breakdown.split(";");
            for (String part : parts) {
                String[] pair = part.split(":");
                if (pair.length == 2) {
                    try {
                        stockMap.put(pair[0], Integer.parseInt(pair[1]));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveStockToProduct() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        product.stockBreakdown = sb.toString();
    }

    // --- DIÁLOGO DE VENTA ---
    private void showSellDialog() {
        if (product == null || product.getCurrentStock() <= 0) {
            Toast.makeText(this, "Sin stock disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Realizar Venta 🧾");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);

        // Selector de Color
        final Spinner spinnerColors = new Spinner(this);
        List<String> colorOptions = new ArrayList<>();
        if (!stockMap.isEmpty()) {
            for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
                if (entry.getValue() > 0) {
                    colorOptions.add(entry.getKey() + " (Disp: " + entry.getValue() + ")");
                }
            }
        } else {
            colorOptions.add("Estándar (Disp: " + product.getCurrentStock() + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colorOptions);
        spinnerColors.setAdapter(adapter);
        container.addView(spinnerColors);

        // Cantidad
        final EditText inputQuantity = new EditText(this);
        inputQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputQuantity.setText("1");
        inputQuantity.setHint("Cantidad");
        container.addView(inputQuantity);

        builder.setView(container);

        // BOTONES
        builder.setPositiveButton("VENDER Y TICKET 🧾", (dialog, which) -> processSale(inputQuantity, spinnerColors, true));
        builder.setNeutralButton("SOLO VENDER 💾", (dialog, which) -> processSale(inputQuantity, spinnerColors, false));
        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    private void processSale(EditText inputQty, Spinner spinner, boolean sendTicket) {
        String qtyStr = inputQty.getText().toString().trim();
        if (qtyStr.isEmpty()) qtyStr = "1";
        int quantityToSell = Integer.parseInt(qtyStr);

        String selectedColorName = null;

        if (!stockMap.isEmpty() && spinner.getSelectedItem() != null) {
            String selection = spinner.getSelectedItem().toString();
            selectedColorName = selection.substring(0, selection.indexOf(" (Disp:")).trim();
        }

        if (quantityToSell <= 0) return;
        if (quantityToSell > product.getCurrentStock()) {
            Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show();
            return;
        }

        // RESTAR STOCK
        product.currentStock = product.currentStock - quantityToSell;
        if (selectedColorName != null && stockMap.containsKey(selectedColorName)) {
            int currentSpecific = stockMap.get(selectedColorName);
            stockMap.put(selectedColorName, Math.max(0, currentSpecific - quantityToSell));
            saveStockToProduct();
        }

        // GUARDAR CAMBIOS
        productViewModel.update(product);

        // REGISTRAR TRANSACCIÓN
        double totalAmount = quantityToSell * product.getSalePrice();
        String desc = "Venta: " + product.getName() + (selectedColorName != null ? " (" + selectedColorName + ")" : "");

        TransactionEntity sale = new TransactionEntity(
                TransactionType.INCOME,
                totalAmount,
                totalAmount,
                quantityToSell,
                product.getId(),
                desc,
                System.currentTimeMillis(),
                "",
                "Cliente Mostrador",
                "COMPLETED"
        );
        transactionViewModel.insert(sale);

        loadProductData(); // Refrescar pantalla

        if (sendTicket) sendWhatsAppTicket(quantityToSell, totalAmount, selectedColorName);
        else Toast.makeText(this, "Venta registrada", Toast.LENGTH_SHORT).show();
    }

    private void sendWhatsAppTicket(int quantity, double total, String color) {
        String ticket = "🧾 *TICKET FERSE STORE*\n\n" +
                "📦 *" + product.getName() + "*\n" +
                (color != null ? "🎨 Color: " + color + "\n" : "") +
                "🔢 Cantidad: " + quantity + "\n" +
                "💰 *TOTAL: $" + total + "*\n\n" +
                "¡Gracias por tu compra! 😊";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, ticket);
        try {
            startActivity(Intent.createChooser(intent, "Enviar Ticket..."));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }
}