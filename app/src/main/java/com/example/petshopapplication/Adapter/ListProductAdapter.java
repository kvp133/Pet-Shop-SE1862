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
import com.example.petshopapplication.model.Variant;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListProductAdapter extends RecyclerView.Adapter<ListProductAdapter.ProductHolder> {

    private List<Product> productItems;
    private List<Category> categoryItems;
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference reference;

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

        // Product name trimming
        if (product.getName().length() > 40) {
            holder.tv_product_name.setText(product.getName().substring(0, 30) + "...");
        } else {
            holder.tv_product_name.setText(product.getName());
        }

        double basePrice = product.getBasePrice();
        String imageUrl = product.getBaseImageURL();

        // If variants exist, use first variant's price
        List<Variant> variants = product.getListVariant();
        if (variants != null && !variants.isEmpty()) {
            basePrice = variants.get(0).getPrice();
        }

        // Handle discount
        if (product.getDiscount() > 0) {
            holder.discountContainer.setVisibility(View.VISIBLE);
            holder.tv_discount.setVisibility(View.VISIBLE);
            holder.tv_discount.setText("-" + product.getDiscount() + "%");

            holder.tv_old_price.setVisibility(View.VISIBLE);
            holder.tv_old_price.setText(String.format("%,.0fđ", basePrice));
            holder.tv_old_price.setPaintFlags(
                    holder.tv_old_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );

            double newPrice = basePrice * (1 - product.getDiscount() / 100.0);
            holder.tv_new_price.setText(String.format("%,.0fđ", newPrice));
        } else {
            holder.discountContainer.setVisibility(View.GONE);
            holder.tv_old_price.setVisibility(View.GONE);
            holder.tv_new_price.setText(String.format("%,.0fđ", basePrice));
        }

        // Category
        Category cat = getCategoryById(product.getCategoryId());
        holder.tv_category.setText(cat != null ? cat.getName() : "");

        // Image
        Glide.with(context)
                .load(imageUrl)
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.imv_product_image);

        // Item click opens detail
      //  holder.itemView.setOnClickListener(v -> {
     //       Intent intent = new Intent(context, ProductDetailActivity.class);
      //      intent.putExtra("productId", product.getId());
      //      context.startActivity(intent);
     //   });

        // Add-to-cart button click opens detail
        holder.btn_add_to_cart.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });

        // Fetch and display feedback
        fetchFeedback(product, holder);
    }

    @Override
    public int getItemCount() {
        return productItems.size();
    }

    private Category getCategoryById(String categoryId) {
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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRating = 0;
                int feedbackCount = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                    if (feedback != null && !feedback.isDeleted()) {
                        feedbackItems.add(feedback);
                        totalRating += feedback.getRating();
                        feedbackCount++;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public class ProductHolder extends RecyclerView.ViewHolder {
        ImageView imv_product_image;
        TextView tv_product_name, tv_rating, tv_old_price, tv_new_price, tv_discount, tv_category;
        MaterialCardView discountContainer;
        MaterialButton btn_add_to_cart;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            imv_product_image = itemView.findViewById(R.id.imv_product_image);
            tv_product_name = itemView.findViewById(R.id.tv_product_name);
            tv_new_price = itemView.findViewById(R.id.tv_new_price);
            tv_old_price = itemView.findViewById(R.id.tv_old_price);
            tv_discount = itemView.findViewById(R.id.tv_discount);
            tv_rating = itemView.findViewById(R.id.tv_rating);
            tv_category = itemView.findViewById(R.id.tv_category);
            discountContainer = itemView.findViewById(R.id.discount_container);
            btn_add_to_cart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
