package com.fersestore.app.ui.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fersestore.app.R;
import java.util.ArrayList;
import java.util.List;

public class CalculatorActivity extends AppCompatActivity {

    private LinearLayout containerItems;
    private EditText etShippingCost, etProfitPercent;
    private TextView tvResults;
    private List<ItemRow> itemRows = new ArrayList<>();

    // Clase auxiliar para guardar los inputs de cada fila
    private class ItemRow {
        EditText etName, etQty, etTotalCost;
        View view;

        ItemRow(EditText name, EditText qty, EditText cost, View v) {
            this.etName = name; this.etQty = qty; this.etTotalCost = cost; this.view = v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Calculadora de Costos");

        containerItems = findViewById(R.id.container_items);
        etShippingCost = findViewById(R.id.et_shipping_cost);
        etProfitPercent = findViewById(R.id.et_profit_percent);
        tvResults = findViewById(R.id.tv_results);

        // Agregamos el primer renglón por defecto
        addItemRow();

        findViewById(R.id.btn_add_item_row).setOnClickListener(v -> addItemRow());
        findViewById(R.id.btn_calculate).setOnClickListener(v -> calculate());
    }

    private void addItemRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL); // Centrar elementos verticalmente
        row.setPadding(0, 0, 0, 16);

        // 1. Nombre (Ej: Vestidos)
        EditText etName = new EditText(this);
        etName.setHint("Producto");
        // Ajustamos pesos para dar espacio al botón X
        LinearLayout.LayoutParams paramsName = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        etName.setLayoutParams(paramsName);

        // 2. Cantidad (Ej: 3)
        EditText etQty = new EditText(this);
        etQty.setHint("Cant");
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams paramsQty = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        etQty.setLayoutParams(paramsQty);
        etQty.setGravity(Gravity.CENTER);

        // 3. Costo Total del Lote (Ej: $30.000)
        EditText etCost = new EditText(this);
        etCost.setHint("$ Total");
        etCost.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams paramsCost = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        etCost.setLayoutParams(paramsCost);
        etCost.setGravity(Gravity.CENTER);

        // 4. LA "X" ROJA (Creada programáticamente) 🔴
        ImageButton btnDelete = new ImageButton(this);
        btnDelete.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnDelete.setBackgroundColor(Color.TRANSPARENT);
        btnDelete.setColorFilter(Color.RED); // Pintamos la X de rojo

        // Tamaño fijo para el botón
        LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(80, 80);
        btnDelete.setLayoutParams(paramsBtn);
        btnDelete.setPadding(10, 0, 0, 0);

        // Agregamos todo a la fila visual
        row.addView(etName);
        row.addView(etQty);
        row.addView(etCost);
        row.addView(btnDelete);

        // Creamos el objeto de datos
        final ItemRow newItemData = new ItemRow(etName, etQty, etCost, row);

        // --- LÓGICA DE BORRADO ---
        btnDelete.setOnClickListener(v -> {
            // 1. Borramos de la pantalla
            containerItems.removeView(row);
            // 2. Borramos de la lista de memoria (para que no falle el cálculo)
            itemRows.remove(newItemData);
        });

        // Agregamos a la pantalla y a la lista
        containerItems.addView(row);
        itemRows.add(newItemData);

        // Foco en el nombre para escribir rápido
        etName.requestFocus();
    }

    private void calculate() {
        String shipStr = etShippingCost.getText().toString().trim();
        double shipping = shipStr.isEmpty() ? 0 : Double.parseDouble(shipStr);

        String profitStr = etProfitPercent.getText().toString().trim();
        double profitPercent = profitStr.isEmpty() ? 50 : Double.parseDouble(profitStr);

        double totalMerchandiseCost = 0;

        // 1. Sumar todo lo que gastaste en mercadería
        for (ItemRow row : itemRows) {
            String costStr = row.etTotalCost.getText().toString().trim();
            if (!costStr.isEmpty()) {
                totalMerchandiseCost += Double.parseDouble(costStr);
            }
        }

        if (totalMerchandiseCost == 0) {
            Toast.makeText(this, "Ingresá los costos de los productos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Aplicar TU FÓRMULA (Prorrateo) y Mostrar Resultados
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("📊 RESULTADOS:\n\n");
        resultBuilder.append("Gasto Mercadería: $ ").append(String.format("%.2f", totalMerchandiseCost)).append("\n");
        resultBuilder.append("Gasto Extra (Envío): $ ").append(String.format("%.2f", shipping)).append("\n");
        resultBuilder.append("----------------------------\n");

        for (ItemRow row : itemRows) {
            String nameRaw = row.etName.getText().toString().trim();
            String qtyStr = row.etQty.getText().toString().trim();
            String costStr = row.etTotalCost.getText().toString().trim();

            // CAMBIO: Si no pusiste nombre, usamos uno genérico
            String name = nameRaw.isEmpty() ? "Producto (Sin nombre)" : nameRaw;

            // CAMBIO: Ahora solo validamos que haya CANTIDAD y COSTO para hacer la cuenta
            if (!qtyStr.isEmpty() && !costStr.isEmpty()) {
                double itemSubtotal = Double.parseDouble(costStr); // Ej: $30.000 (Vestidos)
                int quantity = Integer.parseInt(qtyStr);           // Ej: 3

                if (quantity > 0) { // Evitar división por cero
                    // TU FÓRMULA MAESTRA:
                    // Factor de participación: (Subtotal Item / Total Factura)
                    double participationFactor = itemSubtotal / totalMerchandiseCost;

                    // Cuánto envío le toca a este grupo: (Factor * Total Envío)
                    double allocatedShipping = participationFactor * shipping;

                    // Costo REAL del grupo: (Subtotal + Envío asignado)
                    double realGroupCost = itemSubtotal + allocatedShipping;

                    // Costo UNITARIO Real: (Costo Real Grupo / Cantidad)
                    double unitRealCost = realGroupCost / quantity;

                    // Precio de VENTA SUGERIDO (+50%)
                    double sellingPrice = unitRealCost * (1 + (profitPercent / 100));

                    resultBuilder.append("🔹 ").append(name).append(" (x").append(quantity).append(")\n");
                    resultBuilder.append("   Costo Original: $ ").append(String.format("%.2f", itemSubtotal / quantity)).append(" c/u\n");
                    resultBuilder.append("   + Envío asignado: $ ").append(String.format("%.2f", allocatedShipping / quantity)).append(" c/u\n");
                    resultBuilder.append("   ✅ COSTO REAL: $ ").append(String.format("%.2f", unitRealCost)).append(" c/u\n");
                    resultBuilder.append("   💰 VENDER A: $ ").append(String.format("%.2f", sellingPrice)).append("\n\n");
                }
            }
        }

        tvResults.setText(resultBuilder.toString());
    }
}