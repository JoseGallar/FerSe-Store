package com.fersestore.app.ui.view;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.DebtEntity;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.viewmodel.DebtViewModel;
import com.fersestore.app.ui.viewmodel.ProductViewModel;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DebtsActivity extends AppCompatActivity {

    private DebtViewModel debtViewModel;
    private TransactionViewModel transactionViewModel;
    private ProductViewModel productViewModel;
    private DebtAdapter adapter;

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debts);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("📋 Lista de Deudores");

        recyclerView = findViewById(R.id.rv_debts);
        layoutEmpty = findViewById(R.id.layout_empty_debts);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DebtAdapter();
        recyclerView.setAdapter(adapter);

        debtViewModel = new ViewModelProvider(this).get(DebtViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        debtViewModel.getAllDebts().observe(this, debts -> {
            adapter.setDebts(debts);
            if (debts == null || debts.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- ADAPTADOR ---
    private class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {
        private List<DebtEntity> debts = new ArrayList<>();

        void setDebts(List<DebtEntity> debts) {
            this.debts = debts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
            return new DebtViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
            DebtEntity debt = debts.get(position);

            // LÓGICA NOMBRE OPCIONAL: Si es null o vacío, mostramos "Cliente Desconocido"
            String nombre = (debt.clientName == null || debt.clientName.trim().isEmpty())
                    ? "Cliente Desconocido"
                    : debt.clientName;

            holder.tvClient.setText(nombre);
            holder.tvProduct.setText(debt.quantity + "x " + debt.productName + " (" + debt.variantInfo + ")");
            holder.tvRemaining.setText("Debe: $" + String.format("%.0f", debt.remainingAmount));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(debt.timestamp)));

            holder.btnPay.setOnClickListener(v -> mostrarDialogoCobrar(debt));
            holder.btnCancel.setOnClickListener(v -> mostrarDialogoCancelar(debt));
        }

        @Override
        public int getItemCount() { return debts.size(); }

        class DebtViewHolder extends RecyclerView.ViewHolder {
            TextView tvClient, tvProduct, tvRemaining, tvDate;
            Button btnPay, btnCancel;

            DebtViewHolder(View itemView) {
                super(itemView);
                tvClient = itemView.findViewById(R.id.tv_client_name); // Asegurate que en XML sea id/tv_client_name
                tvProduct = itemView.findViewById(R.id.tv_product_info);
                tvRemaining = itemView.findViewById(R.id.tv_remaining);
                tvDate = itemView.findViewById(R.id.tv_date);
                btnPay = itemView.findViewById(R.id.btn_pay_debt);
                btnCancel = itemView.findViewById(R.id.btn_cancel_debt);
            }
        }
    }

    // --- COBRAR DEUDA ---
    private void mostrarDialogoCobrar(DebtEntity debt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cobrar Deuda");
        // Le agregué formato para que no se vean tantos decimales feos en el mensaje
        builder.setMessage("Debe: $" + String.format("%.0f", debt.remainingAmount) + "\n¿Cuánto paga ahora?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Monto a cobrar");
        builder.setView(input);

        builder.setPositiveButton("COBRAR", (dialog, which) -> {
            String montoStr = input.getText().toString().trim();
            if (!montoStr.isEmpty()) {
                double pago;
                try {
                    pago = Double.parseDouble(montoStr);
                } catch (NumberFormatException e) {
                    return;
                }

                // --- VALIDACIÓN NUEVA ---
                // Si es 0 o negativo, NO hacemos nada y avisamos.
                if (pago <= 0) {
                    Toast.makeText(this, "⛔ El cobro debe ser mayor a $0", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ------------------------

                if (pago > debt.remainingAmount) {
                    Toast.makeText(this, "¡No puede pagar más de lo que debe!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Si llega acá, es porque pagó algo real. Registramos.
                TransactionEntity ingreso = new TransactionEntity(
                        TransactionType.INCOME,
                        pago, pago, 0, 0, -1,
                        "Pago Deuda: " + debt.clientName,
                        System.currentTimeMillis(), "", "Caja", "COMPLETED"
                );
                transactionViewModel.insert(ingreso);

                debt.paidAmount += pago;
                debt.remainingAmount -= pago;

                if (debt.remainingAmount <= 0) {
                    debtViewModel.delete(debt);
                    Toast.makeText(this, "¡Deuda saldada! 🎉", Toast.LENGTH_SHORT).show();
                } else {
                    debtViewModel.update(debt);
                    Toast.makeText(this, "Pago registrado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    // --- CANCELAR Y DEVOLVER (SOLUCIÓN DEFINITIVA) ---
    private void mostrarDialogoCancelar(DebtEntity debt) {
        new AlertDialog.Builder(this)
                .setTitle("¿Cancelar Deuda?")
                .setMessage("Se devolverá el stock.\n" +
                        (debt.paidAmount > 0 ? "⚠️ Se descontarán $" + debt.paidAmount + " de la Caja (Devolución)." : ""))
                .setPositiveButton("CONFIRMAR", (d, w) -> {

                    // 1. CORRECCIÓN DINERO: Si ya pagó algo, sacarlo de la caja
                    if (debt.paidAmount > 0) {
                        TransactionEntity devolucion = new TransactionEntity(
                                TransactionType.EXPENSE, // GASTO = Salida de dinero
                                debt.paidAmount,
                                debt.paidAmount,
                                0, 0, -1,
                                "Devolución: " + debt.clientName, // Para que sepas de quién fue
                                System.currentTimeMillis(),
                                "",
                                "Caja",
                                "COMPLETED"
                        );
                        transactionViewModel.insert(devolucion);
                        Toast.makeText(this, "💸 Se devolvieron $" + debt.paidAmount + " de la caja", Toast.LENGTH_SHORT).show();
                    }

                    // 2. CORRECCIÓN STOCK: Usamos el método SQL directo (NO SUMA LOCA)
                    // Al usar la query directa a la base de datos, evitamos errores de memoria
                    productViewModel.devolverStock(debt.variantId, debt.quantity);

                    // 3. Borrar la deuda
                    debtViewModel.delete(debt);

                    Toast.makeText(this, "✅ Operación cancelada correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Volver", null)
                .show();
    }
}