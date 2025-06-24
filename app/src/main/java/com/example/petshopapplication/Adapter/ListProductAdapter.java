package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import com.example.petshopapplication.ProductDetailActivity;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListProductAdapter extends RecyclerView.Adapter<ListProductAdapter.ProductHolder>{

    List<Product> productItems;
    List<Category> categoryItems;
    Context context;
    FirebaseDatabase database;
    DatabaseReference reference;

    public ListProductAdapter(List<Product> productItems, List<Category> categoryItems) {
        this.productItems = productItems;
        this.categoryItems = categoryItems;
    }


    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_list_product, parent, false);
        return new ProductHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        Product product = productItems.get(position);

        //Check length of product name
        if(product.getName().length() > 40) {
            holder.tv_product_name.setText(product.getName().substring(0, 30) + "...");
        } else {
            holder.tv_product_name.setText(product.getName());
        }

        double oldPrice = product.getBasePrice();
        String imageUrl = product.getBaseImageURL();
        //Check if product have variants
        if(!product.getListVariant().isEmpty()) {
            oldPrice = product.getListVariant().get(0).getPrice();
            //check if product have color variants
            if(product.getListVariant().get(0).getListColor() != null && !product.getListVariant().get(0).getListColor().isEmpty()) {
                imageUrl = product.getListVariant().get(0).getListColor().get(0).getImageUrl();
            }
        }

        //check if product is discounted
        if(product.getDiscount() > 0) {
            holder.tv_discount.setText(String.valueOf("-" + product.getDiscount()) + "%");
            holder.tv_old_price.setText(String.format("%,.0fđ", oldPrice));
            holder.tv_old_price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tv_new_price.setText(String.format("%,.0f$", oldPrice * (1 - product.getDiscount()/100.0)));

        } else {
            holder.tv_discount.setVisibility(View.GONE);
            holder.tv_old_price.setVisibility(View.GONE);
            holder.tv_new_price.setText(String.format("%,.0fđ", oldPrice));
        }

        //Set category
        holder.tv_category.setText(getCategoryById(product.getCategoryId()).getName());

        Glide.with(context)
                .load(imageUrl)
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.imv_product_image);

        holder.itemView.setOnClickListener(v -> {
            // Open product detail activity with product id
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });

        System.out.println(product);
        fetchFeedback(product, holder);
    }

    private Category getCategoryById(String categoryId) {
        for (Category category : categoryItems) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;  // Return null if category not found
    }



    public Category getCategory(String categoryId) {
        for (Category category : categoryItems) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }

    private void fetchFeedback(Product product, @NonNull ProductHolder holder) {
        List<FeedBack> feedbackItems = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("feedbacks");

        Query query = reference.orderByChild("productId").equalTo(product.getId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                feedbackItems.clear();
                int totalRating = 0;
                int feedbackCount = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                    if (!feedback.isDeleted()) {
                        System.out.println();
                        feedbackItems.add(feedback);
                        totalRating += feedback.getRating();
                        feedbackCount++;
                    }
                    if (feedbackCount > 0){
                        double averageRating = (double) totalRating / feedbackCount;
                        String formattedRating = String.format("%.1f", averageRating);
                        holder.tv_rating.setText(formattedRating);
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return productItems.size();
    }


    public class ProductHolder extends RecyclerView.ViewHolder {
        ImageView imv_product_image;
        TextView tv_product_name, tv_rating, tv_old_price, tv_new_price, tv_discount, tv_category;
        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            imv_product_image = itemView.findViewById(R.id.imv_product_image);
            tv_product_name = itemView.findViewById(R.id.tv_product_name);
            tv_new_price = itemView.findViewById(R.id.tv_new_price);
            tv_old_price = itemView.findViewById(R.id.tv_old_price);
            tv_discount = itemView.findViewById(R.id.tv_discount);
            tv_rating = itemView.findViewById(R.id.tv_rating);
            tv_category = itemView.findViewById(R.id.tv_category);
        }
    }
}
