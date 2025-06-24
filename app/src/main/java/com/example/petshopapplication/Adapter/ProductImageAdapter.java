package com.example.petshopapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Color;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ProductImageHolder>{

    Product productItem;
    Context context;
    List<Color> colorItems;
    Map<Color, Variant> colorVariantMap;
    OnProductImageClickListener onProductImageClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public ProductImageAdapter(Product productItem, OnProductImageClickListener onProductImageClickListener) {
        this.productItem = productItem;
        this.onProductImageClickListener = onProductImageClickListener;
        colorItems = new ArrayList<>();
        colorVariantMap = new HashMap<>();
        setColorItems();
    }

    public interface OnProductImageClickListener {
        void onProductImageClick(Product product, Variant variant, Color color);
    }



    public void setColorItems() {
        for(Variant variant : productItem.getListVariant()) {
            //Check list color is null
            if(variant.getListColor() == null || variant.getListColor().isEmpty()) {
                return;
            }
            for (Color color : variant.getListColor()) {
                colorItems.add(color);
                colorVariantMap.put(color, variant);
            }
        }
    }



    @NonNull
    @Override
    public ProductImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_product_image, parent, false);
        return new ProductImageHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductImageHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position == selectedPosition) {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_button_outline_orange));
        } else {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rectangle_button_border_gray));
        }

        Color color = colorItems.get(position);
        Glide.with(context)
                .load(color.getImageUrl())
               .into(holder.imv_product_image);
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = position;
            notifyItemChanged(selectedPosition);
            Variant variant = colorVariantMap.get(color);
            onProductImageClickListener.onProductImageClick(productItem, variant, color);
        });
    }

    @Override
    public int getItemCount() {
        return colorItems.size();
    }

    public class ProductImageHolder extends RecyclerView.ViewHolder {

        ImageView imv_product_image;

        public ProductImageHolder(@NonNull View itemView) {
            super(itemView);
            imv_product_image = itemView.findViewById(R.id.imv_product_image);
        }
    }
}
