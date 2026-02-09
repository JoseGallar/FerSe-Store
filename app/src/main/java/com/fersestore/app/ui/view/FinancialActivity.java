package com.fersestore.app.ui.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

public class FinancialActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private EditText etAmount, etDescription;
    private Spinner spinnerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial); // Asegurate de que este layout exista

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Registrar Movimiento 💰");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        spinnerType = findViewById(R.id.spinner_type);
        Button btnSave = findViewById(R.id.btn_save_transaction);

        // Configurar el selector de Tipo (Ingreso / Gasto)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Ingreso (Venta Extra)", "Gasto (Salida)"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Por favor completá los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Decidir si es INGRESO o GASTO según lo que eligió en el spinner
        TransactionType type;
        if (spinnerType.getSelectedItemPosition() == 0) {
            type = TransactionType.INCOME;
        } else {
            type = TransactionType.EXPENSE;
        }

        // AQUÍ ESTABA EL ERROR: Ahora usamos el constructor nuevo con los 10 datos
        TransactionEntity transaction = new TransactionEntity(
                type,
                amount,         // totalAmount (Monto Total)
                amount,         // paidAmount (Asumimos que se pagó todo)
                1,              // Cantidad (1 por defecto)
                0,              // ID Producto (0 porque es un movimiento manual)
                description,    // Descripción
                System.currentTimeMillis(), // Fecha y hora actual
                "",             // Nota vacía
                "Manual",       // Cliente: "Manual" porque lo cargaste vos a mano
                "COMPLETED"     // Estado: Completado
        );

        transactionViewModel.insert(transaction);

        Toast.makeText(this, "Movimiento guardado", Toast.LENGTH_SHORT).show();
        finish(); // Cierra la pantalla y vuelve atrás
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}