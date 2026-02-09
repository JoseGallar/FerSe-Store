package com.fersestore.app.ui.adapter;

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

import java.util.List;

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

    // ESTA FUNCIÓN FALTABA Y DABA ERROR EN MAINACTIVITY
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

        // Usamos los getters o acceso directo, ambos funcionan ahora
        holder.tvName.setText(product.name);
        holder.tvPrice.setText("$ " + product.salePrice);
        holder.tvStock.setText("Stock: " + product.currentStock);

        if (product.imageUri != null && !product.imageUri.isEmpty()) {
            holder.imgProduct.setImageURI(Uri.parse(product.imageUri));
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView imgProduct; // AQUI DABA ERROR ANTES

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            // Ahora esto funcionará porque actualizamos item_product.xml
            imgProduct = itemView.findViewById(R.id.img_product);
        }
    }
}