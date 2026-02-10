package com.fersestore.app.ui.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductWithVariants; // <--- OJO: Usamos este ahora
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

        SharedPreferences prefs = getSharedPreferences("FerSePrefs", MODE_PRIVATE);
        initialInvestment = prefs.getFloat("INITIAL_INVESTMENT", 0);
        etInitialInvestment.setText(String.format("%.0f", initialInvestment));

        transactionViewModel.getHistory().observe(this, this::calculateFinances);

        // AQUÍ ESTABA EL ERROR: Ahora observamos "ProductWithVariants"
        productViewModel.getAllProducts().observe(this, this::calculateStockValue);

        btnSaveInv.setOnClickListener(v -> {
            String amountStr = etInitialInvestment.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                float amount = Float.parseFloat(amountStr);
                prefs.edit().putFloat("INITIAL_INVESTMENT", amount).apply();
                initialInvestment = amount;
                Toast.makeText(this, "Inversión actualizada", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        btnExpense.setOnClickListener(v -> showExpenseDialog());
    }

    private void calculateFinances(List<TransactionEntity> transactions) {
        double totalIncome = 0;
        double totalExpenses = 0;
        double moneyOnStreet = 0;

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                if (t.type == TransactionType.INCOME) {
                    totalIncome += t.paidAmount;
                    if (t.totalAmount > t.paidAmount) {
                        moneyOnStreet += (t.totalAmount - t.paidAmount);
                    }
                } else if (t.type == TransactionType.EXPENSE) {
                    totalExpenses += t.totalAmount;
                }
            }
        }

        double currentCash = initialInvestment + totalIncome - totalExpenses;
        tvCurrentCash.setText("$ " + String.format("%.0f", currentCash));
        tvMoneyOnStreet.setText("$ " + String.format("%.0f", moneyOnStreet));
    }

    // CORREGIDO: Recibe ProductWithVariants
    private void calculateStockValue(List<ProductWithVariants> products) {
        double totalStockValue = 0;
        if (products != null) {
            for (ProductWithVariants item : products) {
                // Multiplicamos Costo * Stock Total (Suma de variantes)
                totalStockValue += (item.product.costPrice * item.getTotalStock());
            }
        }
        tvStockValue.setText("$ " + String.format("%.0f", totalStockValue));
    }

    private void showExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registrar Gasto 💸");

        final EditText inputDesc = new EditText(this);
        inputDesc.setHint("Descripción (Ej: Bolsas)");
        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Monto ($)");
        inputAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputDesc);
        layout.addView(inputAmount);
        builder.setView(layout);

        builder.setPositiveButton("REGISTRAR", (dialog, which) -> {
            String desc = inputDesc.getText().toString().trim();
            String amountStr = inputAmount.getText().toString().trim();
            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                TransactionEntity expense = new TransactionEntity(
                        TransactionType.EXPENSE, amount, amount, 1, 0,
                        desc, System.currentTimeMillis(), "", "Proveedor", "COMPLETED"
                );
                transactionViewModel.insert(expense);
                Toast.makeText(this, "Gasto registrado", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }
}