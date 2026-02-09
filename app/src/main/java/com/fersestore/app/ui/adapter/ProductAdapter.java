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
import com.fersestore.app.ui.view.ProductDetailActivity;

import java.io.File;
import java.util.List;

// IMPORTS PARA EL FORMATO DE MONEDA
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductEntity> productList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProductEntity product);
    }

    public ProductAdapter(List<ProductEntity> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void setProductList(List<ProductEntity> newProducts) {
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
        ProductEntity product = productList.get(position);

        holder.tvName.setText(product.name);

        // --- INICIO MAGIA DEL PRECIO (FORMATO LIMPIO) ---
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols);
        holder.tvPrice.setText("$ " + decimalFormat.format(product.salePrice));
        // --- FIN MAGIA DEL PRECIO ---

        holder.tvStock.setText("Stock: " + product.currentStock);

        // --- LÓGICA PARA CARGAR IMAGEN ---
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

        // --- CLICK PARA IR AL DETALLE (CORREGIDO) ---
        holder.itemView.setOnClickListener(v -> {
            // 1. Creamos el Intent para ir a la pantalla de detalle
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);

            // 2. Metemos el producto entero con la clave que espera el detalle
            intent.putExtra("product_data", product);

            // 3. ¡Arrancamos!
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
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