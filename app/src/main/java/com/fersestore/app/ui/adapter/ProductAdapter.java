package com.fersestore.app.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductWithVariants;

import java.io.File;
import java.util.List;
import java.util.Objects; // Importante para comparar Strings nulos

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProductEntity product);
    }

    public ProductAdapter(List<ProductWithVariants> initialList, OnItemClickListener listener) {
        this.listener = listener;
        mDiffer.submitList(initialList);
    }

    // --- CORRECCIÓN AQUÍ ---
    private final DiffUtil.ItemCallback<ProductWithVariants> diffCallback = new DiffUtil.ItemCallback<ProductWithVariants>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductWithVariants oldItem, @NonNull ProductWithVariants newItem) {
            return oldItem.product.id == newItem.product.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductWithVariants oldItem, @NonNull ProductWithVariants newItem) {
            // AHORA COMPARAMOS TAMBIÉN LA IMAGEN (imageUri)
            return oldItem.getTotalStock() == newItem.getTotalStock()
                    && oldItem.product.salePrice == newItem.product.salePrice
                    && oldItem.product.name.equals(newItem.product.name)
                    && Objects.equals(oldItem.product.imageUri, newItem.product.imageUri); // <--- ESTO FALTABA
        }
    };
    // -----------------------

    private final AsyncListDiffer<ProductWithVariants> mDiffer = new AsyncListDiffer<>(this, diffCallback);

    public void setProductList(List<ProductWithVariants> newProducts) {
        mDiffer.submitList(newProducts);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductWithVariants pkg = mDiffer.getCurrentList().get(position);
        ProductEntity product = pkg.product;

        holder.tvName.setText(product.name);
        holder.tvPrice.setText("$ " + String.format("%.0f", product.salePrice));

        int totalStock = pkg.getTotalStock();
        holder.tvStock.setText("Stock: " + totalStock);

        // --- LÓGICA DE COLORES CORREGIDA ---
        if (totalStock == 0) {
            // AGOTADO: Rojo Total
            holder.tvStock.setTextColor(android.graphics.Color.RED);
            holder.tvStock.setText("SIN STOCK");
            holder.tvStock.setAlpha(1.0f); // Opacidad completa

        } else if (totalStock <= 3) {
            // ALERTA (3, 2, 1): Naranja
            holder.tvStock.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            holder.tvStock.setAlpha(1.0f); // Opacidad completa

        } else {
            // NORMAL (4 o más):
            // TRUCO: Copiamos el color del Título (tvName) que ya es dinámico (Blanco/Negro)
            holder.tvStock.setTextColor(holder.tvName.getCurrentTextColor());

            // Y le bajamos la opacidad al 60% para que se vea "como un gris suave"
            // Esto hace que en Modo Noche se vea gris claro (legible) y en Día gris oscuro.
            holder.tvStock.setAlpha(0.6f);
        }

        // GLIDE: Carga la imagen optimizada
        if (product.imageUri != null && !product.imageUri.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.imageUri)
                    .override(300, 300)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView imgProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            imgProduct = itemView.findViewById(R.id.img_product);
        }
    }
}