package com.fersestore.app.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.DebtEntity;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.viewmodel.DebtViewModel;
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private TransactionViewModel transactionViewModel;
    private DebtViewModel debtViewModel;

    private ProductWithVariants currentPackage;
    private int productId;

    private TextView tvName, tvCategory, tvPrice, tvStock;
    private LinearLayout llVariantsContainer;
    private ImageView imgDetail;

    private Uri nuevaImagenUri = null;
    private ImageView imgPreviewEnDialogo;

    private final ActivityResultLauncher<Intent> launcherEditarImagen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    nuevaImagenUri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(nuevaImagenUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (imgPreviewEnDialogo != null) {
                        imgPreviewEnDialogo.setImageURI(nuevaImagenUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        debtViewModel = new ViewModelProvider(this).get(DebtViewModel.class);

        ProductEntity basicProduct = (ProductEntity) getIntent().getSerializableExtra("product_data");
        if (basicProduct != null) {
            productId = basicProduct.id;
        } else {
            finish();
            return;
        }

        initViews();

        productViewModel.getProductById(productId).observe(this, productWithVariants -> {
            if (productWithVariants != null) {
                currentPackage = productWithVariants;
                updateUI();
            } else {
                finish();
            }
        });
    }

    private void initViews() {
        imgDetail = findViewById(R.id.img_detail);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvName = findViewById(R.id.tv_detail_name);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvStock = findViewById(R.id.tv_detail_stock);
        llVariantsContainer = findViewById(R.id.ll_variants_container);

        findViewById(R.id.btn_action_sell).setOnClickListener(v -> prepararVenta());
        findViewById(R.id.btn_action_restock).setOnClickListener(v -> prepararReposicion());
    }

    private void updateUI() {
        ProductEntity p = currentPackage.product;
        tvName.setText(p.name);
        tvCategory.setText(p.category.toUpperCase());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        tvPrice.setText("$ " + df.format(p.salePrice));

        if (p.imageUri != null && !p.imageUri.isEmpty()) {
            try {
                imgDetail.setImageURI(Uri.parse(p.imageUri));
            } catch (Exception e) {
                imgDetail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgDetail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        tvStock.setText(currentPackage.getTotalStock() + " u.");
        renderizarListaVariantes();
    }

    private void renderizarListaVariantes() {
        llVariantsContainer.removeAllViews();

        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        int colorTextoSeguro = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE : Color.BLACK;

        if (currentPackage.variants == null || currentPackage.variants.isEmpty()) {
            TextView emptyMsg = new TextView(this);
            emptyMsg.setText("Sin variantes registradas");
            emptyMsg.setPadding(20, 20, 20, 20);
            emptyMsg.setTextColor(colorTextoSeguro);
            llVariantsContainer.addView(emptyMsg);
            return;
        }

        for (ProductVariantEntity v : currentPackage.variants) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(10, 15, 10, 15);

            TextView tvColor = new TextView(this);
            tvColor.setText("🎨 " + v.color);
            tvColor.setTextSize(16);
            tvColor.setTextColor(colorTextoSeguro);
            LinearLayout.LayoutParams paramsColor = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvColor.setLayoutParams(paramsColor);

            TextView tvQty = new TextView(this);
            tvQty.setTextSize(16);
            tvQty.setTypeface(null, android.graphics.Typeface.BOLD);
            if (v.stock == 0) {
                tvQty.setText("AGOTADO");
                tvQty.setTextColor(Color.RED);
            } else {
                tvQty.setText(v.stock + " u.");
                tvQty.setTextColor(v.stock < 3 ? Color.parseColor("#FF9800") : Color.parseColor("#2E7D32"));
            }

            ImageButton btnDelete = new ImageButton(this);
            btnDelete.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            btnDelete.setBackgroundColor(Color.TRANSPARENT);
            btnDelete.setColorFilter(Color.RED);
            btnDelete.setPadding(20, 10, 10, 10);

            btnDelete.setOnClickListener(view -> confirmarEliminarVariante(v));

            row.addView(tvColor);
            row.addView(tvQty);
            row.addView(btnDelete);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.LTGRAY);

            llVariantsContainer.addView(row);
            llVariantsContainer.addView(divider);
        }
    }

    private void confirmarEliminarVariante(ProductVariantEntity variante) {
        new AlertDialog.Builder(this)
                .setTitle("¿Borrar " + variante.color + "?")
                .setMessage("Se eliminará esta variante y su stock.\n(No afecta a ventas pasadas)")
                .setPositiveButton("BORRAR", (dialog, which) -> {
                    productViewModel.deleteVariant(variante);
                    Toast.makeText(this, "Variante eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- MÉTODOS DE VENTA (Con lógica de Deuda) ---

    private void prepararVenta() {
        if (currentPackage.variants == null || currentPackage.variants.isEmpty()) {
            Toast.makeText(this, "No hay stock para vender", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> opciones = new ArrayList<>();
        final List<ProductVariantEntity> variantesDisponibles = new ArrayList<>();

        for (ProductVariantEntity v : currentPackage.variants) {
            if (v.stock > 0) {
                opciones.add(v.color + " (Quedan: " + v.stock + ")");
                variantesDisponibles.add(v);
            }
        }

        if (opciones.isEmpty()) {
            Toast.makeText(this, "Sin stock disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("¿Qué vendiste?")
                .setItems(opciones.toArray(new String[0]), (dialog, which) -> {
                    ProductVariantEntity elegida = variantesDisponibles.get(which);
                    confirmarVenta(elegida);
                }).show();
    }

    private void confirmarVenta(ProductVariantEntity variante) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vender " + variante.color);
        builder.setMessage("Stock disponible: " + variante.stock);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // 1. Campo Cantidad
        final EditText inputCantidad = new EditText(this);
        inputCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputCantidad.setHint("Cantidad (Ej: 1)");
        layout.addView(inputCantidad);

        // 2. Campo Pago
        final EditText inputPago = new EditText(this);
        inputPago.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputPago.setHint("Pago del Cliente ($)");
        layout.addView(inputPago);

        // 3. Campo Nombre (Siempre visible para tu comodidad)
        final EditText inputCliente = new EditText(this);
        inputCliente.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        inputCliente.setHint("Nombre Cliente (Opcional)");
        layout.addView(inputCliente);

        builder.setView(layout);

        builder.setPositiveButton("CONFIRMAR", (dialog, which) -> {
            String cantStr = inputCantidad.getText().toString().trim();
            if (cantStr.isEmpty()) return;

            int cantidad = Integer.parseInt(cantStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "⛔ La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cantidad > variante.stock) {
                Toast.makeText(this, "⛔ ¡No hay suficiente stock!", Toast.LENGTH_SHORT).show();
                return;
            }

            double precioUnitario = currentPackage.product.salePrice;
            double costoUnitario = currentPackage.product.costPrice;
            double totalVenta = precioUnitario * cantidad;

            // --- VALIDACIÓN DEL PAGO ---
            double montoPagado;
            String pagoStr = inputPago.getText().toString().trim();

            if (pagoStr.isEmpty()) {
                // Si lo deja vacío, asume que paga TODO
                montoPagado = totalVenta;
            } else {
                try {
                    montoPagado = Double.parseDouble(pagoStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "⛔ Número inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // REGLA 1: No se puede pagar negativo
            if (montoPagado < 0) {
                Toast.makeText(this, "⛔ El pago no puede ser negativo (Min $0)", Toast.LENGTH_LONG).show();
                return;
            }

            // REGLA 2: No se puede pagar más del total
            if (montoPagado > totalVenta) {
                Toast.makeText(this, "⛔ Error: Paga más del total ($" + totalVenta + ")", Toast.LENGTH_LONG).show();
                return;
            }

            // --- FIN VALIDACIONES, EJECUTAMOS ---

            // 1. DESCONTAR STOCK
            variante.stock -= cantidad;
            productViewModel.updateVariant(variante);

            // 2. REGISTRAR EL DINERO (Solo si pagó algo real, mayor a 0)
            // REGLA 3: Si pone $0, NO se crea transacción en la caja (solo deuda)
            if (montoPagado > 0) {
                double gananciaProporcional = montoPagado - (costoUnitario * cantidad);

                TransactionEntity venta = new TransactionEntity(
                        TransactionType.INCOME,
                        montoPagado,
                        montoPagado,
                        gananciaProporcional,
                        cantidad,
                        currentPackage.product.id,
                        "Venta: " + currentPackage.product.name + " (" + variante.color + ")",
                        System.currentTimeMillis(),
                        "",
                        "Cliente",
                        "COMPLETED"
                );
                transactionViewModel.insert(venta);
            }

            // 3. REGISTRAR DEUDA (Si pagó de menos o pagó $0)
            if (montoPagado < totalVenta) {
                String nombreIngresado = inputCliente.getText().toString().trim();
                String nombreFinal = nombreIngresado.isEmpty() ? "Cliente Desconocido" : nombreIngresado;

                DebtEntity deuda = new DebtEntity(
                        nombreFinal,
                        currentPackage.product.name,
                        variante.color,
                        totalVenta,
                        montoPagado,
                        currentPackage.product.id,
                        variante.id,
                        cantidad,
                        System.currentTimeMillis()
                );
                debtViewModel.insert(deuda);

                if (montoPagado == 0) {
                    Toast.makeText(this, "⚠️ Todo al fiado a: " + nombreFinal, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "⚠️ Deuda parcial guardada a: " + nombreFinal, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "¡Venta Exitosa!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // --- MÉTODOS DE REPOSICIÓN ---

    private void prepararReposicion() {
        List<String> opciones = new ArrayList<>();
        final List<ProductVariantEntity> variantesReales = new ArrayList<>();

        if (currentPackage.variants != null) {
            for (ProductVariantEntity v : currentPackage.variants) {
                variantesReales.add(v);
                if (v.stock == 0) {
                    opciones.add(v.color + " (¡AGOTADO!)");
                } else {
                    opciones.add(v.color + " (Actual: " + v.stock + ")");
                }
            }
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                opciones
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                ProductVariantEntity v = variantesReales.get(position);

                if (v.stock == 0) {
                    textView.setTextColor(Color.RED);
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                    textView.setText("🚨 " + textView.getText());
                } else {
                    textView.setTextColor(new TextView(getContext()).getTextColors());
                    textView.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                return textView;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Seleccioná para reponer")
                .setAdapter(adapter, (dialog, which) -> {
                    ProductVariantEntity varianteElegida = variantesReales.get(which);
                    mostrarDialogoSumarStock(varianteElegida);
                })
                .setNeutralButton("➕ CREAR NUEVO COLOR", (dialog, which) -> {
                    mostrarDialogoNuevoColor();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoSumarStock(ProductVariantEntity variante) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reponer " + variante.color);
        builder.setMessage("Stock actual: " + variante.stock);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Cantidad que llegó");
        builder.setView(input);

        builder.setPositiveButton("AGREGAR", (dialog, which) -> {
            String cantStr = input.getText().toString();
            if (!cantStr.isEmpty()) {
                int cantidad = Integer.parseInt(cantStr);
                variante.stock += cantidad;
                productViewModel.updateVariant(variante);
                Toast.makeText(this, "Stock actualizado", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void mostrarDialogoNuevoColor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva Variante");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputColor = new EditText(this);
        inputColor.setHint("Nombre (Ej: Arcoiris, Turquesa)");
        layout.addView(inputColor);

        final EditText inputCantidad = new EditText(this);
        inputCantidad.setHint("Cantidad que llegó");
        inputCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputCantidad);

        builder.setView(layout);

        builder.setPositiveButton("CREAR Y GUARDAR", (dialog, which) -> {
            String nuevoColor = inputColor.getText().toString().trim();
            String cantStr = inputCantidad.getText().toString().trim();

            if (!nuevoColor.isEmpty() && !cantStr.isEmpty()) {
                int cantidad = Integer.parseInt(cantStr);
                ProductVariantEntity nuevaVariante = new ProductVariantEntity("Único", nuevoColor, cantidad);
                nuevaVariante.productId = productId;
                productViewModel.insertNewVariant(nuevaVariante);
                Toast.makeText(this, "¡Variante agregada!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    // --- MÉTODOS DE MENÚ Y EDICIÓN (LOS QUE FALTABAN) ---

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_manage) {
            View menuItemView = findViewById(R.id.action_manage);
            PopupMenu popup = new PopupMenu(this, menuItemView);
            popup.getMenu().add(0, 1, 0, "✏️ Editar Nombre/Foto");
            popup.getMenu().add(0, 2, 0, "🗑️ Eliminar Producto");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    mostrarDialogoEditar();
                    return true;
                } else if (menuItem.getItemId() == 2) {
                    mostrarDialogoEliminar();
                    return true;
                }
                return false;
            });
            popup.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoEditar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Producto");

        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean esModoOscuro = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        int colorTexto = esModoOscuro ? Color.WHITE : Color.BLACK;
        int colorEtiqueta = esModoOscuro ? Color.parseColor("#B0BEC5") : Color.parseColor("#757575");
        int colorBorde = esModoOscuro ? Color.WHITE : Color.GRAY;

        LinearLayout imageContainer = new LinearLayout(this);
        imageContainer.setOrientation(LinearLayout.VERTICAL);
        imageContainer.setGravity(Gravity.CENTER);
        imageContainer.setPadding(0, 0, 0, 30);

        imgPreviewEnDialogo = new ImageView(this);
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(250, 250);
        imgPreviewEnDialogo.setLayoutParams(paramsImg);
        imgPreviewEnDialogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgPreviewEnDialogo.setBackgroundColor(Color.LTGRAY);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            imgPreviewEnDialogo.setClipToOutline(true);
        }

        if (currentPackage.product.imageUri != null && !currentPackage.product.imageUri.isEmpty()) {
            try {
                imgPreviewEnDialogo.setImageURI(Uri.parse(currentPackage.product.imageUri));
            } catch (Exception e) {
                imgPreviewEnDialogo.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgPreviewEnDialogo.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        imgPreviewEnDialogo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            launcherEditarImagen.launch(intent);
        });

        TextView lblFoto = new TextView(this);
        lblFoto.setText("Tocá la foto para cambiarla");
        lblFoto.setTextSize(12);
        lblFoto.setTextColor(colorEtiqueta);
        lblFoto.setGravity(Gravity.CENTER);
        lblFoto.setPadding(0, 10, 0, 0);

        imageContainer.addView(imgPreviewEnDialogo);
        imageContainer.addView(lblFoto);
        mainLayout.addView(imageContainer);

        mainLayout.addView(crearTituloCampo("NOMBRE DEL PRODUCTO", colorEtiqueta));
        final EditText inputNombre = new EditText(this);
        inputNombre.setText(currentPackage.product.name);
        inputNombre.setTextColor(colorTexto);
        inputNombre.setBackground(crearBordePersonalizado(colorBorde));
        inputNombre.setPadding(20, 20, 20, 20);
        mainLayout.addView(inputNombre);

        mainLayout.addView(crearEspacio());

        LinearLayout preciosLayout = new LinearLayout(this);
        preciosLayout.setOrientation(LinearLayout.HORIZONTAL);
        preciosLayout.setWeightSum(2);

        LinearLayout colCosto = new LinearLayout(this);
        colCosto.setOrientation(LinearLayout.VERTICAL);
        colCosto.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        colCosto.setPadding(0, 0, 15, 0);

        colCosto.addView(crearTituloCampo("COSTO", colorEtiqueta));
        final EditText inputCosto = new EditText(this);
        inputCosto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputCosto.setText(String.format("%.0f", currentPackage.product.costPrice));
        inputCosto.setTextColor(colorTexto);
        inputCosto.setBackground(crearBordePersonalizado(colorBorde));
        inputCosto.setPadding(20, 20, 20, 20);
        colCosto.addView(inputCosto);

        LinearLayout colVenta = new LinearLayout(this);
        colVenta.setOrientation(LinearLayout.VERTICAL);
        colVenta.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        colVenta.setPadding(15, 0, 0, 0);

        colVenta.addView(crearTituloCampo("PRECIO VENTA", colorEtiqueta));
        final EditText inputVenta = new EditText(this);
        inputVenta.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputVenta.setText(String.format("%.0f", currentPackage.product.salePrice));
        inputVenta.setTextColor(colorTexto);
        inputVenta.setBackground(crearBordePersonalizado(colorBorde));
        inputVenta.setPadding(20, 20, 20, 20);
        colVenta.addView(inputVenta);

        preciosLayout.addView(colCosto);
        preciosLayout.addView(colVenta);
        mainLayout.addView(preciosLayout);

        mainLayout.addView(crearEspacio());

        mainLayout.addView(crearTituloCampo("CATEGORÍA", colorEtiqueta));
        Spinner spinnerCategoria = new Spinner(this);
        spinnerCategoria.setBackground(crearBordePersonalizado(colorBorde));
        spinnerCategoria.setPadding(10, 10, 10, 10);

        String[] categorias = {"Remeras", "Pantalones", "Buzos", "Accesorios", "Calzado", "Otros"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categorias
        );
        spinnerCategoria.setAdapter(adapter);

        if (currentPackage.product.category != null) {
            for (int i = 0; i < categorias.length; i++) {
                if (currentPackage.product.category.equalsIgnoreCase(categorias[i])) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }
        }
        mainLayout.addView(spinnerCategoria);

        scrollView.addView(mainLayout);
        builder.setView(scrollView);

        builder.setPositiveButton("GUARDAR CAMBIOS", (dialog, which) -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            String costoStr = inputCosto.getText().toString().trim();
            String ventaStr = inputVenta.getText().toString().trim();
            String nuevaCategoria = spinnerCategoria.getSelectedItem().toString();

            if (!nuevoNombre.isEmpty() && !costoStr.isEmpty() && !ventaStr.isEmpty()) {
                currentPackage.product.name = nuevoNombre;
                currentPackage.product.category = nuevaCategoria;

                try {
                    currentPackage.product.costPrice = Double.parseDouble(costoStr.replace(",", "."));
                    currentPackage.product.salePrice = Double.parseDouble(ventaStr.replace(",", "."));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Error en números", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (nuevaImagenUri != null) {
                    currentPackage.product.imageUri = nuevaImagenUri.toString();
                }

                productViewModel.update(currentPackage.product);
                Toast.makeText(this, "Producto Actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar Producto?")
                .setMessage("Se borrará el producto y su stock actual.\n\n✅ Tu historial de ventas y dinero NO se verá afectado.")
                .setPositiveButton("ELIMINAR DEFINITIVAMENTE", (dialog, which) -> {
                    productViewModel.delete(currentPackage.product);
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- FUNCIONES AUXILIARES DE DISEÑO (LAS QUE FALTABAN) ---

    private TextView crearTituloCampo(String texto, int color) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(12);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(color);
        tv.setPadding(4, 0, 0, 8);
        return tv;
    }

    private GradientDrawable crearBordePersonalizado(int colorBorde) {
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(2, colorBorde);
        border.setCornerRadius(12);
        return border;
    }

    private View crearEspacio() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40));
        return v;
    }
}