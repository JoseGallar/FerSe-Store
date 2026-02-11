package com.fersestore.app.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fersestore.app.R;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductWithVariants;
import com.fersestore.app.ui.view.ProductDetailActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // Ahora la lista maneja el "Paquete" (Producto + Variantes)
    private List<ProductWithVariants> productList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProductEntity product);
    }

    public ProductAdapter(List<ProductWithVariants> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void setProductList(List<ProductWithVariants> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // 1. OBTENER EL PAQUETE COMPLETO
        ProductWithVariants item = productList.get(position);

        // 2. EXTRAER EL PRODUCTO PADRE (Para nombre, precio, foto)
        ProductEntity product = item.product;

        holder.tvName.setText(product.name);

        // --- FORMATO DE PRECIO (Sin decimales, ej: $ 4.500) ---
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        holder.tvPrice.setText("$ " + decimalFormat.format(product.salePrice));

        // --- LÓGICA DE STOCK (CON ALERTAS DE COLOR) ---
        int totalStock = item.getTotalStock();

        if (totalStock == 0) {
            // CASO 1: AGOTADO (Rojo)
            holder.tvStock.setText("¡AGOTADO!");
            holder.tvStock.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Rojo fuerte
            holder.tvStock.setTypeface(null, android.graphics.Typeface.BOLD); // Negrita
        } else if (totalStock <= 3) {
            // CASO 2: STOCK BAJO (Naranja)
            holder.tvStock.setText("Stock: " + totalStock);
            holder.tvStock.setTextColor(android.graphics.Color.parseColor("#FF6D00")); // Naranja Alerta
            holder.tvStock.setTypeface(null, android.graphics.Typeface.BOLD); // Negrita
        } else {
            // CASO 3: NORMAL (Gris)
            holder.tvStock.setText("Stock: " + totalStock);
            holder.tvStock.setTextColor(android.graphics.Color.parseColor("#757575")); // Gris oscuro estándar
            holder.tvStock.setTypeface(null, android.graphics.Typeface.NORMAL); // Letra normal
        }
        // ----------------------------------------------

        // --- CARGAR IMAGEN ---
        holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);

        if (product.imageUri != null && !product.imageUri.isEmpty()) {
            try {
                File imgFile = new File(product.imageUri);
                if (imgFile.exists()) {
                    holder.imgProduct.setImageURI(Uri.fromFile(imgFile));
                } else {
                    holder.imgProduct.setImageURI(Uri.parse(product.imageUri));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // IMPORTANTE: NO OLVIDAR EL LISTENER DE CLICK
        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
    }

    @Override
    public int getItemCount() {
        if (productList == null) return 0;
        return productList.size();
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