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

        // --- CORRECCIÓN DEL STOCK ---
        // Usamos el método del paquete que suma los hijos
        holder.tvStock.setText("Stock: " + item.getTotalStock());

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

        // --- CLICK (Ir al detalle) ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            // Pasamos el producto padre (el detalle luego buscará los colores si hace falta)
            intent.putExtra("product_data", product);
            v.getContext().startActivity(intent);
        });
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