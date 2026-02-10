package com.fersestore.app.ui.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.adapter.TransactionAdapter;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class FinancialActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;

    // Vistas del diseño
    private TextView tvBalance;       // El Balance grande
    private TextView tvTotalIncome;   // Ventas (Verde)
    private TextView tvTotalExpense;  // Gastos (Rojo)

    private List<TransactionEntity> currentTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial); // Tu XML

        // 1. VINCULAMOS CON TUS IDs EXACTOS (Los que están en tu XML)
        tvBalance = findViewById(R.id.tv_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        RecyclerView recyclerView = findViewById(R.id.rv_transactions); // ID corregido
        View btnGanancias = findViewById(R.id.btn_ver_ganancias);

        // 2. Configuramos la lista
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 3. Obtenemos los datos
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getHistory().observe(this, transactions -> {
            this.currentTransactions = transactions;

            // Actualizamos la lista visual
            adapter.setTransactionList(transactions);

            // Calculamos los números de las tarjetas de arriba
            actualizarTablero(transactions);
        });

        // 4. Acción del botón GANANCIAS
        btnGanancias.setOnClickListener(v -> mostrarReporteGanancias());
    }

    // --- CÁLCULO VISUAL (Lo que se ve en las tarjetas) ---
    private void actualizarTablero(List<TransactionEntity> transactions) {
        double ingresos = 0;
        double gastos = 0;

        for (TransactionEntity t : transactions) {
            // Usamos .equals porque TransactionType ahora es String
            if (TransactionType.INCOME.equals(t.type)) {
                ingresos += t.totalAmount;
            } else if (TransactionType.EXPENSE.equals(t.type)) {
                gastos += t.totalAmount;
            }
        }

        double balance = ingresos - gastos;

        // Actualizamos los textos
        tvTotalIncome.setText("$ " + String.format("%.0f", ingresos));
        tvTotalExpense.setText("$ " + String.format("%.0f", gastos));
        tvBalance.setText("$ " + String.format("%.0f", balance));
    }

    // --- REPORTE DE GANANCIA REAL (Botón Verde) ---
    private void mostrarReporteGanancias() {
        double gananciaNetaTotal = 0;
        double ventasBrutas = 0;
        double gastosOperativos = 0;

        for (TransactionEntity t : currentTransactions) {
            if (TransactionType.INCOME.equals(t.type)) {
                // Sumamos la columna 'profit' (Venta - Costo)
                gananciaNetaTotal += t.profit;
                ventasBrutas += t.totalAmount;
            } else if (TransactionType.EXPENSE.equals(t.type)) {
                // Restamos gastos
                gananciaNetaTotal -= t.totalAmount;
                gastosOperativos += t.totalAmount;
            }
        }

        // Mostramos el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📊 Rentabilidad Real");

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Ventas Totales: $ ").append(String.format("%.0f", ventasBrutas)).append("\n");
        mensaje.append("Gastos: -$ ").append(String.format("%.0f", gastosOperativos)).append("\n");
        mensaje.append("--------------------------------\n");
        mensaje.append("GANANCIA LIMPIA: $ ").append(String.format("%.0f", gananciaNetaTotal));

        builder.setMessage(mensaje.toString());
        builder.setPositiveButton("CERRAR", null);
        builder.show();
    }
}