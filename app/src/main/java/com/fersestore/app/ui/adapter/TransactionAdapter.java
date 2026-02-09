package com.fersestore.app.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionEntity> transactions = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity currentTransaction = transactions.get(position);

        // 1. Mostrar Descripción
        holder.tvDescription.setText(currentTransaction.description);

        // 2. Mostrar Fecha
        holder.tvDate.setText(dateFormat.format(new Date(currentTransaction.timestamp)));

        // 3. Mostrar Monto y Color (AQUÍ ESTABA EL ERROR)
        // Antes usábamos .amount, ahora usamos .totalAmount
        if (currentTransaction.type == TransactionType.INCOME) {
            holder.tvAmount.setText("+ $ " + currentTransaction.totalAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else if (currentTransaction.type == TransactionType.EXPENSE) {
            holder.tvAmount.setText("- $ " + currentTransaction.totalAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Rojo
        } else {
            // INVERSION
            holder.tvAmount.setText("Inv: $ " + currentTransaction.totalAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#FF9800")); // Naranja
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDescription;
        private TextView tvAmount;
        private TextView tvDate;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_desc);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}