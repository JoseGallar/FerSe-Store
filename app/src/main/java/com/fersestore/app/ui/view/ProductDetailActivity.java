package com.fersestore.app.ui.view;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        // Recuperar el producto enviado desde la lista
        if (getIntent().hasExtra("product_id")) {
            int id = getIntent().getIntExtra("product_id", -1);
            // Acá deberíamos buscar por ID, pero por simplicidad pasamos el objeto o sus datos.
            // Si pasaste el objeto serializable sería mejor, pero asumamos que buscamos o recibimos datos.
            // NOTA: Para simplificar, voy a asumir que pasaste los datos por Intent como Strings/Ints
            // O mejor aún, si tu Adapter pasaba el objeto "ProductEntity" (Serializable), lo recuperamos así:
            currentProduct = (ProductEntity) getIntent().getSerializableExtra("product_data");
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show();
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
        TextView tvVariants = findViewById(R.id.tv_detail_variants); // El detalle de colores

        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnEdit = findViewById(R.id.btn_edit);

        // 1. Cargar Textos Básicos
        tvName.setText(currentProduct.getName());
        tvCategory.setText(currentProduct.getCategory().toUpperCase());
        tvStock.setText(currentProduct.getStock() + " unidades");

        // 2. Mostrar el Detalle de Variantes (Colores)
        // Usamos el campo 'description' donde guardamos "Rojo: 5, Verde: 2"
        if (currentProduct.getDescription() != null && !currentProduct.getDescription().isEmpty()) {
            tvVariants.setText(currentProduct.getDescription());
        } else {
            tvVariants.setText("No hay variantes registradas (Solo stock total).");
        }

        // 3. Formato de Precio "Limpio" (Igual que en la lista)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols);
        tvPrice.setText("$ " + decimalFormat.format(currentProduct.getPrice()));

        // 4. Cargar Imagen (Lógica blindada)
        if (currentProduct.getImageUri() != null && !currentProduct.getImageUri().isEmpty()) {
            try {
                File imgFile = new File(currentProduct.getImageUri());
                if (imgFile.exists()) {
                    imgDetail.setImageURI(Uri.fromFile(imgFile));
                } else {
                    imgDetail.setImageURI(Uri.parse(currentProduct.getImageUri()));
                }
            } catch (Exception e) {
                imgDetail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // 5. Botones
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Próximamente: Editar", Toast.LENGTH_SHORT).show();
            // Acá iría el Intent para abrir AddProductActivity con los datos cargados
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de borrar " + currentProduct.getName() + "?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> {
                    productViewModel.delete(currentProduct);
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la pantalla y vuelve a la lista
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}