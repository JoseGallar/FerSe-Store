package com.fersestore.app.ui.view;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;
import com.fersestore.app.ui.adapter.TransactionAdapter;
import com.fersestore.app.ui.viewmodel.TransactionViewModel;

import java.util.List;

public class FinancialActivity extends AppCompatActivity {

    private TransactionViewModel transactionViewModel;
    private TextView tvIncome, tvExpense, tvBalance;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Finanzas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Vincular vistas
        tvIncome = findViewById(R.id.tv_total_income);
        tvExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        RecyclerView recyclerView = findViewById(R.id.rv_transactions);

        // Configurar lista
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        // ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observar datos
        transactionViewModel.getHistory().observe(this, transactions -> {
            adapter.setTransactions(transactions);
            calculateTotals(transactions);
        });
    }

    private void calculateTotals(List<TransactionEntity> transactions) {
        double income = 0;
        double expense = 0;

        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                if (t.type == TransactionType.INCOME) {
                    income += t.totalAmount;
                } else {
                    expense += t.totalAmount;
                }
            }
        }

        double balance = income - expense;

        tvIncome.setText(String.format("$ %.2f", income));
        tvExpense.setText(String.format("$ %.2f", expense));
        tvBalance.setText(String.format("$ %.2f", balance));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}