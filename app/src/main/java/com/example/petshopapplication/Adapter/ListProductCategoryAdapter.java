package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Category;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ListProductCategoryAdapter extends RecyclerView.Adapter<ListProductCategoryAdapter.CategoryHolder>{

    List<Category> categoryItems;
    OnCategoryClickListener onCategoryClickListener;
    Context context;

    public ListProductCategoryAdapter(List<Category> categoryItems, OnCategoryClickListener onCategoryClickListener) {
        this.categoryItems = categoryItems;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }


    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_category_list_product, parent, false);
        return new ListProductCategoryAdapter.CategoryHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        Category category = categoryItems.get(position);
        holder.txt_cate_name.setText(category.getName());

        Glide.with(context)
                .load(category.getImage())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.imv_cate_image);
        holder.itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryItems.size() > 6 ? 6 : categoryItems.size();
    }

    class CategoryHolder extends RecyclerView.ViewHolder{

        ImageView imv_cate_image;
        TextView txt_cate_name;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            imv_cate_image = itemView.findViewById(R.id.imv_cate_image);
            txt_cate_name = itemView.findViewById(R.id.txt_cate_name);

        }
    }
}
