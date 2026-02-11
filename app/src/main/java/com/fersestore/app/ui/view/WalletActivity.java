package com.fersestore.app.ui.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WalletActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;

    // Vistas
    private TextView tvTotalInversion, tvTotalCaja, tvMeDeben, tvStockValue;
    private Button btnFilterAll, btnFilterMonth, btnFilterToday;

    // Listas
    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private List<TransactionEntity> filteredTransactions = new ArrayList<>();

    // 0=Todo, 1=Mes, 2=Hoy
    // CAMBIO: Empezamos en 0 (Todo) para que veas datos apenas entras
    private int currentFilterMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Mi Billetera");

        initViews();
        setupRecyclerView();

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getHistory().observe(this, transactions -> {
            this.allTransactions = transactions;
            recalcularTodo();
        });

        // Listeners Filtros
        btnFilterAll.setOnClickListener(v -> cambiarFiltro(0));
        btnFilterMonth.setOnClickListener(v -> cambiarFiltro(1));
        btnFilterToday.setOnClickListener(v -> cambiarFiltro(2));

        // Acciones Botones
        findViewById(R.id.btn_record_expense).setOnClickListener(v -> mostrarDialogoGasto());
        findViewById(R.id.btn_add_investment).setOnClickListener(v -> mostrarDialogoInversion());
    }

    private void initViews() {
        tvTotalInversion = findViewById(R.id.tv_total_inversion);
        tvTotalCaja = findViewById(R.id.tv_current_cash);
        tvMeDeben = findViewById(R.id.tv_money_on_street);
        tvStockValue = findViewById(R.id.tv_stock_value);

        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterMonth = findViewById(R.id.btn_filter_month);
        btnFilterToday = findViewById(R.id.btn_filter_today);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>());

        // BORRAR CON CLICK LARGO
        adapter.setOnItemLongClickListener(transaction -> {
            mostrarDialogoBorrar(transaction);
        });

        rv.setAdapter(adapter);
    }

    // --- CÁLCULOS PRINCIPALES ---
    private void recalcularTodo() {
        double cajaTotal = 0;
        double inversionAcumulada = 0;

        for (TransactionEntity t : allTransactions) {
            if (TransactionType.INCOME.equals(t.type)) {
                cajaTotal += t.totalAmount;

                // Sumamos a la tarjeta naranja si parece ser una inversión
                if (esInversion(t)) {
                    inversionAcumulada += t.totalAmount;
                }
            } else {
                cajaTotal -= t.totalAmount;
            }
        }

        tvTotalCaja.setText(formatoDinero(cajaTotal));
        tvTotalInversion.setText(formatoDinero(inversionAcumulada));

        // Actualizar la lista de abajo
        aplicarFiltroLista(currentFilterMode);
    }

    // Función auxiliar para detectar inversiones (Mejorada)
    private boolean esInversion(TransactionEntity t) {
        if (t.description == null) return false;
        String desc = t.description.toLowerCase();
        // Detecta "inversión", "inversion" (sin acento) o "capital"
        return desc.contains("inversión") || desc.contains("inversion") || desc.contains("capital");
    }

    private String formatoDinero(double cantidad) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "$ " + df.format(cantidad);
    }

    // --- FILTROS DE LISTA ---
    private void cambiarFiltro(int modo) {
        currentFilterMode = modo;

        // Colores botones
        int colorActivo = Color.parseColor("#546E7A");
        int colorInactivo = Color.parseColor("#CFD8DC");
        int textoBlanco = Color.WHITE;
        int textoGris = Color.parseColor("#546E7A");

        btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(modo == 0 ? colorActivo : colorInactivo));
        btnFilterAll.setTextColor(modo == 0 ? textoBlanco : textoGris);

        btnFilterMonth.setBackgroundTintList(ColorStateList.valueOf(modo == 1 ? colorActivo : colorInactivo));
        btnFilterMonth.setTextColor(modo == 1 ? textoBlanco : textoGris);

        btnFilterToday.setBackgroundTintList(ColorStateList.valueOf(modo == 2 ? colorActivo : colorInactivo));
        btnFilterToday.setTextColor(modo == 2 ? textoBlanco : textoGris);

        aplicarFiltroLista(modo);
    }

    private void aplicarFiltroLista(int modo) {
        filteredTransactions.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = cal.getTimeInMillis();

        for (TransactionEntity t : allTransactions) {
            // A. Filtro de Tiempo
            boolean pasaTiempo = false;
            if (modo == 0) pasaTiempo = true; // Todo
            else if (modo == 1 && t.timestamp >= startOfMonth) pasaTiempo = true; // Mes
            else if (modo == 2 && t.timestamp >= startOfToday) pasaTiempo = true; // Hoy

            // B. Filtro de TIPO (Qué mostramos en la lista)
            boolean mostrar = false;

            if (pasaTiempo) {
                if (t.type == TransactionType.EXPENSE) {
                    mostrar = true; // ¡SIEMPRE mostrar Gastos!
                } else if (TransactionType.INCOME.equals(t.type)) {
                    // Mostrar ingresos SOLO si son Inversiones
                    if (esInversion(t)) {
                        mostrar = true;
                    }
                }
            }

            if (mostrar) {
                filteredTransactions.add(t);
            }
        }
        adapter.setTransactionList(filteredTransactions);
    }

    // --- DIÁLOGOS ---
    private void mostrarDialogoInversion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💰 Ingresar Inversión");
        builder.setMessage("Dinero externo (de tu bolsillo) para la caja.");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputMonto = new EditText(this);
        inputMonto.setHint("Monto ($)");
        inputMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputMonto);

        final EditText inputNota = new EditText(this);
        inputNota.setHint("Nota (Ej: Ahorros, Préstamo)");
        layout.addView(inputNota);

        builder.setView(layout);
        builder.setPositiveButton("GUARDAR", (dialog, which) -> {
            String montoStr = inputMonto.getText().toString().trim();
            String notaStr = inputNota.getText().toString().trim();

            // VALIDACIÓN: No guardar vacío ni 0
            if (!montoStr.isEmpty()) {
                double monto = Double.parseDouble(montoStr);

                if (monto > 0) {
                    String desc = notaStr.isEmpty() ? "Inversión Capital" : "Inversión: " + notaStr;

                    TransactionEntity inversion = new TransactionEntity(
                            TransactionType.INCOME,
                            monto, monto, 0, 1, -1,
                            desc,
                            System.currentTimeMillis(), "", "Yo", "COMPLETED"
                    );
                    transactionViewModel.insert(inversion);
                    Toast.makeText(this, "¡Inversión Agregada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void mostrarDialogoGasto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💸 Registrar Gasto");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputDesc = new EditText(this);
        inputDesc.setHint("Descripción (Ej: Luz, Comida)");
        layout.addView(inputDesc);

        final EditText inputMonto = new EditText(this);
        inputMonto.setHint("Monto ($)");
        inputMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputMonto);

        builder.setView(layout);
        builder.setPositiveButton("REGISTRAR", (dialog, which) -> {
            String desc = inputDesc.getText().toString().trim();
            String montoStr = inputMonto.getText().toString().trim();

            // VALIDACIÓN: No guardar vacío ni 0
            if (!montoStr.isEmpty()) {
                double monto = Double.parseDouble(montoStr);

                if (monto > 0) {
                    TransactionEntity gasto = new TransactionEntity(
                            TransactionType.EXPENSE,
                            monto, monto, 0, 1, -1,
                            desc,
                            System.currentTimeMillis(), "", "Negocio", "COMPLETED"
                    );
                    transactionViewModel.insert(gasto);
                    Toast.makeText(this, "Gasto Registrado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    // Diálogo de Borrar
    private void mostrarDialogoBorrar(TransactionEntity t) {
        new AlertDialog.Builder(this)
                .setTitle("¿Borrar Movimiento?")
                .setMessage("Se eliminará: " + t.description + "\n(El dinero volverá a ajustarse)")
                .setPositiveButton("BORRAR", (d, w) -> {
                    transactionViewModel.delete(t);
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- MENÚ COMPARTIR ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_financial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            compartirResumen();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void compartirResumen() {
        String caja = tvTotalCaja.getText().toString();
        String inversion = tvTotalInversion.getText().toString();

        StringBuilder msj = new StringBuilder();
        msj.append("💼 *Estado de Billetera - FerSe Store*\n\n");
        msj.append("💰 Caja Disponible: ").append(caja).append("\n");
        msj.append("📈 Inversión Total: ").append(inversion).append("\n");
        msj.append("\n_Generado por FerSe App_");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, msj.toString());
        startActivity(Intent.createChooser(shareIntent, "Compartir..."));
    }
}