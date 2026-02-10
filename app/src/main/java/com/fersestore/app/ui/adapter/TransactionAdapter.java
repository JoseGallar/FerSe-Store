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

    public TransactionAdapter(List<TransactionEntity> transactionList) {
        this.transactionList = transactionList;
    }

    public void setTransactionList(List<TransactionEntity> newTransactions) {
        this.transactionList = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // AQUÍ ESTÁ EL CAMBIO: Usamos tu nuevo diseño 'item_transaction'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = transactionList.get(position);

        // 1. Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(transaction.timestamp)));

        // 2. Descripción
        holder.tvDesc.setText(transaction.description);

        // 3. Obtenemos la tarjeta completa para pintarla
        androidx.cardview.widget.CardView card = (androidx.cardview.widget.CardView) holder.itemView;

        // 4. LÓGICA DE COLORES BEBÉ (PASTEL) 🎨
        if (TransactionType.INCOME.equals(transaction.type)) {
            // === ES INGRESO (VERDE) ===

            // Fondo Verde Bebé
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));

            // Icono Verde Oscuro
            holder.imgIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.imgIcon.setColorFilter(Color.parseColor("#2E7D32"));
            // Quitamos el fondo del círculo del icono porque ya pintamos toda la tarjeta
            holder.imgIcon.setBackground(null);

            // Texto Monto
            holder.tvAmount.setText("+ $ " + String.format("%.0f", transaction.totalAmount));
            holder.tvAmount.setTextColor(Color.parseColor("#1B5E20")); // Verde muy oscuro para que se lea

        } else {
            // === ES GASTO (ROJO) ===

            // Fondo Rojo Bebé
            card.setCardBackgroundColor(Color.parseColor("#FFEBEE"));

            // Icono Rojo Oscuro
            holder.imgIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.imgIcon.setColorFilter(Color.parseColor("#C62828"));
            holder.imgIcon.setBackground(null);

            // Texto Monto
            holder.tvAmount.setText("- $ " + String.format("%.0f", transaction.totalAmount));
            holder.tvAmount.setTextColor(Color.parseColor("#B71C1C")); // Rojo muy oscuro
        }
    }

    // Función auxiliar para hacer el circulito de color detrás del icono
    private GradientDrawable crearCirculo(int colorFondo) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(colorFondo);
        return shape;
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
            // Vinculamos con los IDs del archivo item_transaction.xml
            tvDesc = itemView.findViewById(R.id.tv_trans_desc);
            tvDate = itemView.findViewById(R.id.tv_trans_date);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            imgIcon = itemView.findViewById(R.id.img_icon_type);
        }
    }
}