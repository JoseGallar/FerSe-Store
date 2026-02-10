package com.fersestore.app.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.ui.viewmodel.ProductViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private ImageView imgPreview;
    private Uri selectedImageUri;
    private Uri cameraUri;

    private EditText etName, etCost, etPrice;
    private Spinner spinnerCategory;

    // Variables para el sistema de stock
    private LinearLayout llVariantsContainer;
    private TextView tvTotalStock;
    private List<View> variantRows = new ArrayList<>(); // Para guardar referencia de las filas

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgPreview.setImageURI(selectedImageUri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedImageUri = cameraUri;
                    imgPreview.setImageURI(cameraUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        etName = findViewById(R.id.et_prod_name);
        etCost = findViewById(R.id.et_prod_cost);
        etPrice = findViewById(R.id.et_prod_price);
        spinnerCategory = findViewById(R.id.spinner_category);
        imgPreview = findViewById(R.id.img_preview);
        Button btnSelectImg = findViewById(R.id.btn_select_img);
        Button btnSave = findViewById(R.id.btn_save_prod);

        // Stock Nuevo
        llVariantsContainer = findViewById(R.id.ll_variants_container);
        tvTotalStock = findViewById(R.id.tv_total_stock);
        Button btnAddVariant = findViewById(R.id.btn_add_variant);

        // Configurar Spinner
        String[] categories = {"Remeras", "Pantalones", "Accesorios", "Abrigos", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        btnSelectImg.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveProduct());

        // Botón para agregar color personalizado
        btnAddVariant.setOnClickListener(v -> addVariantRow("", false));

        // --- INICIALIZAR COLORES COMUNES ---
        addVariantRow("Negro", true);
        addVariantRow("Rojo", true);
        addVariantRow("Amarillo", true);
        addVariantRow("Verde", true);
    }

    // Método para agregar una fila de color
    private void addVariantRow(String colorName, boolean isFixed) {
        View rowView = getLayoutInflater().inflate(R.layout.item_stock_variant, llVariantsContainer, false);

        EditText etColor = rowView.findViewById(R.id.et_variant_name);
        EditText etQty = rowView.findViewById(R.id.et_variant_qty);

        if (isFixed) {
            etColor.setText(colorName);
            // etColor.setEnabled(false); // Descomentar si querés que NO se pueda cambiar el nombre "Negro"
        } else {
            etColor.setHint("Ej: Arcoiris");
            etColor.requestFocus();
        }

        // Listener para sumar automáticamente cuando escribís
        etQty.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        llVariantsContainer.addView(rowView);
        variantRows.add(rowView);
    }

    // Calcula la suma de todas las filas
    private void calculateTotal() {
        int total = 0;
        for (View row : variantRows) {
            EditText etQty = row.findViewById(R.id.et_variant_qty);
            String qtyStr = etQty.getText().toString();
            if (!qtyStr.isEmpty()) {
                try {
                    total += Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
        }
        tvTotalStock.setText(String.valueOf(total));
    }

    private void showImageSourceDialog() {
        String[] options = {"📷 Tomar Foto", "🖼️ Elegir de Galería"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) checkCameraPermission();
            else openGallery();
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva Foto");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara");
        cameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String costStr = etCost.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || costStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Completá los datos básicos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. PREPARAR LA LISTA DE VARIANTES (LOS HIJOS)
        List<ProductVariantEntity> variants = new ArrayList<>();
        int totalCalculatedStock = 0;

        for (View row : variantRows) {
            EditText etColor = row.findViewById(R.id.et_variant_name);
            EditText etQty = row.findViewById(R.id.et_variant_qty);

            String colorName = etColor.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();

            if (!colorName.isEmpty() && !qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    // Creamos el objeto variante real
                    // (Size lo dejamos vacío por ahora o podés poner "Único")
                    variants.add(new ProductVariantEntity("Único", colorName, qty));
                    totalCalculatedStock += qty;
                }
            }
        }

        // Si no cargó ninguna variante, obligamos a crear al menos una "Estándar"
        if (variants.isEmpty()) {
            // Si el usuario puso stock total manual pero no variantes, creamos una variante genérica
            String manualStockStr = tvTotalStock.getText().toString();
            int manualStock = manualStockStr.isEmpty() ? 0 : Integer.parseInt(manualStockStr);
            if(manualStock > 0) {
                variants.add(new ProductVariantEntity("Único", "Estándar", manualStock));
            } else {
                Toast.makeText(this, "Cargá al menos un color/cantidad", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String finalImagePath = "";
        if (selectedImageUri != null) {
            finalImagePath = saveImageToInternalStorage(selectedImageUri);
        }

        // 2. CREAR EL PRODUCTO PADRE (SIN STOCK, ESO LO TIENEN LOS HIJOS)
        ProductEntity newProduct = new ProductEntity(
                name,
                category,
                Double.parseDouble(costStr),
                Double.parseDouble(priceStr),
                finalImagePath
        );

        // 3. GUARDAR TODO JUNTO USANDO EL NUEVO MÉTODO DEL VIEWMODEL
        productViewModel.insert(newProduct, variants);

        Toast.makeText(this, "Producto Guardado", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outStream);
            outStream.flush();
            outStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}