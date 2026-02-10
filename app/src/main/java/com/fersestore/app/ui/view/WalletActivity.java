package com.fersestore.app.ui.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.util.List;

public class WalletActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private ProductViewModel productViewModel;

    private EditText etInitialInvestment;
    private TextView tvCurrentCash, tvMoneyOnStreet, tvStockValue;
    private double initialInvestment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Mi Billetera 💼");

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        etInitialInvestment = findViewById(R.id.et_initial_investment);
        tvCurrentCash = findViewById(R.id.tv_current_cash);
        tvMoneyOnStreet = findViewById(R.id.tv_money_on_street);
        tvStockValue = findViewById(R.id.tv_stock_value);
        Button btnSaveInv = findViewById(R.id.btn_save_investment);
        Button btnExpense = findViewById(R.id.btn_record_expense);

        // Cargar inversión inicial guardada
        SharedPreferences prefs = getSharedPreferences("FerSePrefs", MODE_PRIVATE);
        initialInvestment = prefs.getFloat("INITIAL_INVESTMENT", 0);
        etInitialInvestment.setText(String.format("%.0f", initialInvestment));

        // Observamos las transacciones para calcular caja
        transactionViewModel.getHistory().observe(this, this::calculateFinances);

        // Observamos los productos para calcular valor de mercadería
        productViewModel.getAllProducts().observe(this, this::calculateStockValue);

        // Botón Guardar Inversión Inicial
        btnSaveInv.setOnClickListener(v -> {
            String amountStr = etInitialInvestment.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                float amount = Float.parseFloat(amountStr);
                prefs.edit().putFloat("INITIAL_INVESTMENT", amount).apply();
                initialInvestment = amount;
                Toast.makeText(this, "Inversión actualizada", Toast.LENGTH_SHORT).show();
                recreate(); // Recargamos para ver cambios
            }
        });

        // Botón Registrar Gasto
        btnExpense.setOnClickListener(v -> showExpenseDialog());
    }

    // --- CÁLCULOS DE CAJA ---
    private void calculateFinances(List<TransactionEntity> transactions) {
        double totalIncome = 0;
        double totalExpenses = 0;
        double moneyOnStreet = 0;

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                // IMPORTANTE: Usamos .equals() para comparar Strings
                if (TransactionType.INCOME.equals(t.type)) {
                    totalIncome += t.paidAmount;
                    // Si pagó menos del total, hay plata en la calle (fiado)
                    if (t.totalAmount > t.paidAmount) {
                        moneyOnStreet += (t.totalAmount - t.paidAmount);
                    }
                } else if (TransactionType.EXPENSE.equals(t.type)) {
                    totalExpenses += t.totalAmount;
                }
            }
        }

        // Fórmula: Inversión Inicial + Ingresos (Ventas) - Gastos
        double currentCash = initialInvestment + totalIncome - totalExpenses;

        tvCurrentCash.setText("$ " + String.format("%.0f", currentCash));
        tvMoneyOnStreet.setText("$ " + String.format("%.0f", moneyOnStreet));
    }

    // --- CÁLCULO DE VALOR DE MERCADERÍA ---
    private void calculateStockValue(List<ProductWithVariants> products) {
        double totalStockValue = 0;
        if (products != null) {
            for (ProductWithVariants item : products) {
                // Costo * Cantidad Total de todas las variantes
                totalStockValue += (item.product.costPrice * item.getTotalStock());
            }
        }
        tvStockValue.setText("$ " + String.format("%.0f", totalStockValue));
    }

    // --- DIÁLOGO PARA REGISTRAR GASTOS ---
    private void showExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registrar Gasto 💸");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        final EditText inputDesc = new EditText(this);
        inputDesc.setHint("Descripción (Ej: Bolsas, Luz)");
        layout.addView(inputDesc);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Monto ($)");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputAmount);

        builder.setView(layout);

        builder.setPositiveButton("REGISTRAR GASTO", (dialog, which) -> {
            String desc = inputDesc.getText().toString().trim();
            String amountStr = inputAmount.getText().toString().trim();

            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);

                // CREAMOS EL GASTO CON EL NUEVO FORMATO
                TransactionEntity gasto = new TransactionEntity(
                        TransactionType.EXPENSE, // Tipo: GASTO
                        amount,                  // Total
                        amount,                  // Pagado
                        0,                       // Profit: 0 (Los gastos no tienen ganancia)
                        1,                       // Cantidad
                        -1,                      // ID Producto (-1 es genérico)
                        desc,                    // Descripción
                        System.currentTimeMillis(),
                        null,
                        "Negocio",
                        "COMPLETED"
                );

                transactionViewModel.insert(gasto);
                Toast.makeText(this, "Gasto registrado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}