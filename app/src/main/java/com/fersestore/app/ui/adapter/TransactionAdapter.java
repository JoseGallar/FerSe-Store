package com.fersestore.app.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.domain.model.TransactionType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionEntity> transactionList;
    private OnItemLongClickListener longClickListener; // <--- La antena nueva

    // Interfaz para comunicarse con la Activity
    public interface OnItemLongClickListener {
        void onItemLongClick(TransactionEntity transaction);
    }

    public TransactionAdapter(List<TransactionEntity> transactionList) {
        this.transactionList = transactionList;
    }

    // Método para conectar la antena
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setTransactionList(List<TransactionEntity> newTransactions) {
        this.transactionList = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = transactionList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(transaction.timestamp)));
        holder.tvDesc.setText(transaction.description);

        // Obtenemos la tarjeta para el click largo
        androidx.cardview.widget.CardView card = (androidx.cardview.widget.CardView) holder.itemView;

        // --- DETECTAR CLICK LARGO ---
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(transaction);
                return true; // "Consumimos" el evento para que no haga click normal después
            }
            return false;
        });
        // -----------------------------

        if (TransactionType.INCOME.equals(transaction.type)) {
            // VERDE (Ingreso)
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.imgIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.imgIcon.setColorFilter(Color.parseColor("#2E7D32"));
            holder.imgIcon.setBackground(null);
            holder.tvAmount.setText("+ $ " + String.format("%.0f", transaction.totalAmount));
            holder.tvAmount.setTextColor(Color.parseColor("#1B5E20"));
        } else {
            // ROJO (Gasto)
            card.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.imgIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.imgIcon.setColorFilter(Color.parseColor("#C62828"));
            holder.imgIcon.setBackground(null);
            holder.tvAmount.setText("- $ " + String.format("%.0f", transaction.totalAmount));
            holder.tvAmount.setTextColor(Color.parseColor("#B71C1C"));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvDate, tvAmount;
        ImageView imgIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tv_trans_desc);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            imgIcon = itemView.findViewById(R.id.img_icon_type);
        }
    }
}