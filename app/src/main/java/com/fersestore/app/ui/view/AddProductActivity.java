package com.fersestore.app.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.ui.viewmodel.ProductViewModel;

public class AddProductActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private ImageView imgPreview;
    private Uri selectedImageUri;

    // Elementos de la pantalla
    private EditText etName, etCost, etPrice, etStock;
    private Spinner spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Vincular vistas
        etName = findViewById(R.id.et_prod_name);
        etCost = findViewById(R.id.et_prod_cost);
        etPrice = findViewById(R.id.et_prod_price);
        etStock = findViewById(R.id.et_prod_stock);
        spinnerCategory = findViewById(R.id.spinner_category);
        imgPreview = findViewById(R.id.img_preview);
        Button btnSelectImg = findViewById(R.id.btn_select_img);
        Button btnSave = findViewById(R.id.btn_save_prod);

        // Configurar Spinner de Categorías
        String[] categories = {"Remeras", "Pantalones", "Accesorios", "Abrigos", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // Selector de Imagen
        ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgPreview.setImageURI(selectedImageUri);
                    }
                }
        );

        btnSelectImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePicker.launch(intent);
        });

        // Guardar Producto
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String costStr = etCost.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || costStr.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Completá todos los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        double cost = Double.parseDouble(costStr);
        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);
        String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        // AQUÍ ESTABA EL ERROR: Ahora creamos un ProductEntity
        ProductEntity newProduct = new ProductEntity(
                name,
                category,
                cost,
                price,
                stock,
                imageUriString,
                "" // Stock desglosado vacío al inicio
        );

        productViewModel.insert(newProduct);
        Toast.makeText(this, "Producto Guardado", Toast.LENGTH_SHORT).show();
        finish();
    }
}