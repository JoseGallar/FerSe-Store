package com.fersestore.app.ui.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.adapter.TransactionAdapter;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinancialActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;

    // Vistas
    private TextView tvBalance, tvTotalIncome, tvTotalExpense;
    private Button btnFilterAll, btnFilterMonth, btnFilterToday;

    // Listas de datos
    private List<TransactionEntity> allTransactions = new ArrayList<>(); // Historial COMPLETO
    private List<TransactionEntity> filteredTransactions = new ArrayList<>(); // Lo que se ve en pantalla

    // Filtro actual (0=Todo, 1=Mes, 2=Hoy) - Empezamos con HOY por defecto
    private int currentFilterMode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial);

        // 1. VINCULACIÓN DE VISTAS
        tvBalance = findViewById(R.id.tv_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);

        // Botones de Filtro
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterMonth = findViewById(R.id.btn_filter_month);
        btnFilterToday = findViewById(R.id.btn_filter_today);

        // Botones de Acción
        Button btnRegistrarGasto = findViewById(R.id.btn_registrar_gasto);
        View btnGanancias = findViewById(R.id.btn_ver_ganancias);
        RecyclerView recyclerView = findViewById(R.id.rv_transactions);

        // 2. CONFIGURAR LISTA
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>());

        // --- AQUÍ CONECTAMOS EL BORRADO ---
        adapter.setOnItemLongClickListener(transaction -> {
            mostrarDialogoBorrar(transaction);
        });
        // ----------------------------------

        recyclerView.setAdapter(adapter);

        // 3. OBTENER DATOS
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getHistory().observe(this, transactions -> {
            this.allTransactions = transactions;
            // Cada vez que llega un dato nuevo, reaplicamos el filtro actual
            aplicarFiltro(currentFilterMode);
        });

        // 4. ACCIONES DE BOTONES (FILTROS)
        btnFilterAll.setOnClickListener(v -> aplicarFiltro(0));
        btnFilterMonth.setOnClickListener(v -> aplicarFiltro(1));
        btnFilterToday.setOnClickListener(v -> aplicarFiltro(2));

        // 5. ACCIONES DE BOTONES (OPERATIVOS)
        btnGanancias.setOnClickListener(v -> mostrarReporteGanancias());
        btnRegistrarGasto.setOnClickListener(v -> mostrarDialogoRegistrarGasto());
    }

    // --- LÓGICA DE FILTROS 📅 ---
    private void aplicarFiltro(int modo) {
        currentFilterMode = modo;
        filteredTransactions.clear();

        // Obtenemos fechas de corte
        Calendar cal = Calendar.getInstance();

        // Reseteamos horas para comparar solo fechas (00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startOfToday = cal.getTimeInMillis();

        // Para inicio de mes
        cal.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = cal.getTimeInMillis();

        for (TransactionEntity t : allTransactions) {
            boolean pasaElFiltro = false;

            if (modo == 0) {
                pasaElFiltro = true; // TODO: Pasan todos
            } else if (modo == 1) {
                // MES: Si la fecha es mayor o igual al 1 del mes
                if (t.timestamp >= startOfMonth) pasaElFiltro = true;
            } else if (modo == 2) {
                // HOY: Si la fecha es mayor o igual a las 00:00 de hoy
                if (t.timestamp >= startOfToday) pasaElFiltro = true;
            }

            if (pasaElFiltro) {
                filteredTransactions.add(t);
            }
        }

        // Actualizamos la pantalla con la lista filtrada
        adapter.setTransactionList(filteredTransactions);
        actualizarTablero(filteredTransactions);
        actualizarBotonesFiltro(modo);
    }

    // --- ESTÉTICA DE LOS BOTONES ---
    private void actualizarBotonesFiltro(int modo) {
        // Colores: Azul (#1976D2) para activo, Gris (#CFD8DC) para inactivo
        int colorActivo = Color.parseColor("#1976D2");
        int colorInactivo = Color.parseColor("#CFD8DC");
        int textoBlanco = Color.WHITE;
        int textoGris = Color.parseColor("#546E7A");

        // Reseteamos todos a gris
        btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(colorInactivo));
        btnFilterAll.setTextColor(textoGris);
        btnFilterMonth.setBackgroundTintList(ColorStateList.valueOf(colorInactivo));
        btnFilterMonth.setTextColor(textoGris);
        btnFilterToday.setBackgroundTintList(ColorStateList.valueOf(colorInactivo));
        btnFilterToday.setTextColor(textoGris);

        // Pintamos el activo de azul
        if (modo == 0) {
            btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(colorActivo));
            btnFilterAll.setTextColor(textoBlanco);
        } else if (modo == 1) {
            btnFilterMonth.setBackgroundTintList(ColorStateList.valueOf(colorActivo));
            btnFilterMonth.setTextColor(textoBlanco);
        } else {
            btnFilterToday.setBackgroundTintList(ColorStateList.valueOf(colorActivo));
            btnFilterToday.setTextColor(textoBlanco);
        }
    }

    // --- CÁLCULOS VISUALES ---
    private void actualizarTablero(List<TransactionEntity> transactions) {
        double ingresos = 0;
        double gastos = 0;

        for (TransactionEntity t : transactions) {
            if (TransactionType.INCOME.equals(t.type)) {
                ingresos += t.totalAmount;
            } else if (TransactionType.EXPENSE.equals(t.type)) {
                gastos += t.totalAmount;
            }
        }

        double balance = ingresos - gastos;

        tvTotalIncome.setText("$ " + String.format("%.0f", ingresos));
        tvTotalExpense.setText("$ " + String.format("%.0f", gastos));
        tvBalance.setText("$ " + String.format("%.0f", balance));
    }

    // --- REPORTE DE GANANCIA (Botón Verde) ---
    private void mostrarReporteGanancias() {
        double gananciaNetaTotal = 0;
        double ventasBrutas = 0;
        double gastosOperativos = 0;

        // Calculamos usando la lista FILTRADA (lo que estás viendo en pantalla)
        for (TransactionEntity t : filteredTransactions) {
            if (TransactionType.INCOME.equals(t.type)) {
                gananciaNetaTotal += t.profit;
                ventasBrutas += t.totalAmount;
            } else if (TransactionType.EXPENSE.equals(t.type)) {
                gananciaNetaTotal -= t.totalAmount;
                gastosOperativos += t.totalAmount;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // --- AQUÍ ESTABA EL DETALLE ---
        // Preguntamos qué filtro está activo para poner el título correcto
        String titulo;
        if (currentFilterMode == 0) {
            titulo = "Historico (Todo)";
        } else if (currentFilterMode == 1) {
            titulo = "Este mes";
        } else {
            titulo = "Hoy";
        }
        // -----------------------------

        builder.setTitle("📊 Balance: " + titulo);

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Ventas Totales: $ ").append(String.format("%.0f", ventasBrutas)).append("\n");
        mensaje.append("--------------------------------\n");
        mensaje.append("Ganancia (Prod): $ ").append(String.format("%.0f", gananciaNetaTotal + gastosOperativos)).append("\n");
        mensaje.append("Gastos: -$ ").append(String.format("%.0f", gastosOperativos)).append("\n");
        mensaje.append("--------------------------------\n");
        mensaje.append("GANANCIA FINAL: $ ").append(String.format("%.0f", gananciaNetaTotal));

        builder.setMessage(mensaje.toString());
        builder.setPositiveButton("CERRAR", null);
        builder.show();
    }

    // --- DIÁLOGO PARA BORRAR ---
    private void mostrarDialogoBorrar(TransactionEntity transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Eliminar Movimiento?");
        builder.setMessage("Se borrará: " + transaction.description + "\nEl dinero volverá a su estado anterior.");

        builder.setPositiveButton("ELIMINAR", (dialog, which) -> {
            // 1. Borramos de la base de datos
            transactionViewModel.delete(transaction);

            // 2. Avisamos al usuario
            Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();

            // NOTA: No hace falta recalcular manual.
            // Al borrar, el 'observe' de arriba se dispara solo y actualiza
            // el Balance, las Ganancias y todo lo demás automáticamente.
        });

        builder.setNegativeButton("CANCELAR", null);
        builder.show();
    }

    // --- DIÁLOGO DE GASTO (Igual que antes) ---
    private void mostrarDialogoRegistrarGasto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registrar Gasto 💸");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        final EditText inputDesc = new EditText(this);
        inputDesc.setHint("Descripción (Ej: Bolsas)");
        layout.addView(inputDesc);

        final EditText inputAmount = new EditText(this);
        inputAmount.setHint("Monto ($)");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputAmount);

        builder.setView(layout);

        builder.setPositiveButton("GUARDAR", (dialog, which) -> {
            String desc = inputDesc.getText().toString().trim();
            String amountStr = inputAmount.getText().toString().trim();

            if (!desc.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                TransactionEntity gasto = new TransactionEntity(
                        TransactionType.EXPENSE, amount, amount, 0, 1, -1, desc,
                        System.currentTimeMillis(), null, "Negocio", "COMPLETED"
                );
                transactionViewModel.insert(gasto);
                Toast.makeText(this, "Gasto registrado", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // ---------------------------------------------------------
    // SECCIÓN MENÚ SUPERIOR (3 PUNTITOS)
    // ---------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflamos el archivo xml que acabamos de crear
        getMenuInflater().inflate(R.menu.menu_financial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            compartirResumen(); // <--- Llamamos a la función de compartir
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Lógica para armar el texto y compartir
    private void compartirResumen() {
        double ingresos = 0;
        double gastos = 0;

        // Calculamos sobre lo que se está viendo en pantalla (Filtrado)
        for (TransactionEntity t : filteredTransactions) {
            if (TransactionType.INCOME.equals(t.type)) {
                ingresos += t.totalAmount;
            } else if (TransactionType.EXPENSE.equals(t.type)) {
                gastos += t.totalAmount;
            }
        }
        double balance = ingresos - gastos;

        // Definimos qué texto poner según el filtro
        String periodo = (currentFilterMode == 2) ? "HOY" :
                (currentFilterMode == 1) ? "ESTE MES" : "HISTÓRICO COMPLETO";

        // Armamos el mensaje bonito
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("📊 *Reporte Financiero - FerSe Store*\n");
        mensaje.append("📅 Período: ").append(periodo).append("\n\n");
        mensaje.append("🟢 Ventas: $ ").append(String.format("%.0f", ingresos)).append("\n");
        mensaje.append("🔴 Gastos: $ ").append(String.format("%.0f", gastos)).append("\n");
        mensaje.append("----------------------------\n");
        mensaje.append("💰 *BALANCE: $ ").append(String.format("%.0f", balance)).append("*");

        // Creamos el Intent de Android para compartir
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte FerSe Store");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mensaje.toString());

        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir vía..."));
    }
}