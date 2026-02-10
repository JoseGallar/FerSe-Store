package com.fersestore.app.ui.view;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.ui.viewmodel.ProductViewModel;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ProductDetailActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private ProductEntity currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        currentProduct = (ProductEntity) getIntent().getSerializableExtra("product_data");

        if (currentProduct == null) {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
    }

    private void setupViews() {
        ImageView imgDetail = findViewById(R.id.img_detail);
        TextView tvCategory = findViewById(R.id.tv_detail_category);
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvPrice = findViewById(R.id.tv_detail_price);
        TextView tvStock = findViewById(R.id.tv_detail_stock);
        TextView tvVariants = findViewById(R.id.tv_detail_variants);

        // 1. Cargar Datos
        tvName.setText(currentProduct.getName());
        tvCategory.setText(currentProduct.getCategory().toUpperCase());
        tvStock.setText(currentProduct.getStock() + " unidades");

        String detalle = currentProduct.getStockBreakdown();
        tvVariants.setText((detalle != null && !detalle.isEmpty()) ? detalle : "Sin detalle de colores.");

        // 2. Precio
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols);
        tvPrice.setText("$ " + decimalFormat.format(currentProduct.getPrice()));

        // 3. Imagen
        if (currentProduct.getImageUri() != null && !currentProduct.getImageUri().isEmpty()) {
            try {
                File imgFile = new File(currentProduct.getImageUri());
                if (imgFile.exists()) imgDetail.setImageURI(Uri.fromFile(imgFile));
                else imgDetail.setImageURI(Uri.parse(currentProduct.getImageUri()));
            } catch (Exception e) {
                imgDetail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // 4. Botones de Gestión (Abajo)
        findViewById(R.id.btn_delete).setOnClickListener(v -> showDeleteConfirmation());
        findViewById(R.id.btn_edit).setOnClickListener(v -> Toast.makeText(this, "Edición próximamente...", Toast.LENGTH_SHORT).show());

        // 5. Botones de Acción (Vender / Reponer)
        findViewById(R.id.btn_action_sell).setOnClickListener(v -> prepararVenta());
        findViewById(R.id.btn_action_restock).setOnClickListener(v -> prepararReposicion());
    }

    // --- LÓGICA DE VENTA ---
    private void prepararVenta() {
        String detalle = currentProduct.getStockBreakdown();
        if (detalle == null || detalle.isEmpty()) {
            Toast.makeText(this, "No hay stock por colores", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] variantes = detalle.split(", ");
        new AlertDialog.Builder(this)
                .setTitle("¿Qué color vendiste?")
                .setItems(variantes, (dialog, which) -> {
                    mostrarDialogoCantidad(variantes[which], which, variantes, true);
                }).show();
    }

    // --- LÓGICA DE REPOSICIÓN ---
    private void prepararReposicion() {
        String detalle = currentProduct.getStockBreakdown();
        if (detalle == null || detalle.isEmpty()) detalle = "";

        String[] variantesExistentes = detalle.isEmpty() ? new String[0] : detalle.split(", ");
        String[] opciones = new String[variantesExistentes.length + 1];
        System.arraycopy(variantesExistentes, 0, opciones, 0, variantesExistentes.length);
        opciones[opciones.length - 1] = "+ AGREGAR NUEVO COLOR/VARIANTE";

        new AlertDialog.Builder(this)
                .setTitle("¿Qué color vas a reponer?")
                .setItems(opciones, (dialog, which) -> {
                    if (which == opciones.length - 1) {
                        mostrarDialogoNuevoColor();
                    } else {
                        mostrarDialogoCantidad(opciones[which], which, variantesExistentes, false);
                    }
                }).show();
    }

    private void mostrarDialogoCantidad(String textoVariante, int posicion, String[] variantes, boolean esVenta) {
        String[] partes = textoVariante.split(": ");
        String color = partes[0];
        int stockActual = Integer.parseInt(partes[1]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((esVenta ? "Vender " : "Reponer ") + color);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Cantidad");
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String valor = input.getText().toString();
            if (!valor.isEmpty()) {
                int cantidad = Integer.parseInt(valor);
                if (esVenta && cantidad > stockActual) {
                    Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show();
                } else {
                    actualizarStock(posicion, variantes, color, stockActual, cantidad, esVenta);
                }
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    private void mostrarDialogoNuevoColor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva Variante (ej: Arcoíris)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputColor = new EditText(this);
        inputColor.setHint("Nombre del color");
        layout.addView(inputColor);

        final EditText inputCant = new EditText(this);
        inputCant.setHint("Cantidad inicial");
        inputCant.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputCant);

        builder.setView(layout);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = inputColor.getText().toString().trim();
            String cantStr = inputCant.getText().toString();
            if (!nombre.isEmpty() && !cantStr.isEmpty()) {
                String detalleActual = currentProduct.getStockBreakdown();
                String nuevoItem = nombre + ": " + cantStr;
                currentProduct.stockBreakdown = (detalleActual == null || detalleActual.isEmpty()) ? nuevoItem : detalleActual + ", " + nuevoItem;
                currentProduct.currentStock += Integer.parseInt(cantStr);
                productViewModel.update(currentProduct);
                setupViews();
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    private void actualizarStock(int posicion, String[] variantes, String color, int actual, int mov, boolean esVenta) {
        int nuevo = esVenta ? (actual - mov) : (actual + mov);
        variantes[posicion] = color + ": " + nuevo;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < variantes.length; i++) {
            sb.append(variantes[i]).append(i == variantes.length - 1 ? "" : ", ");
        }

        currentProduct.stockBreakdown = sb.toString();
        currentProduct.currentStock = esVenta ? (currentProduct.currentStock - mov) : (currentProduct.currentStock + mov);

        productViewModel.update(currentProduct);
        setupViews();
        Toast.makeText(this, "Inventario actualizado", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Seguro que querés borrar \"" + currentProduct.getName() + "\"?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    productViewModel.delete(currentProduct);
                    finish();
                })
                .setNegativeButton("Cancelar", null).show();
    }
}