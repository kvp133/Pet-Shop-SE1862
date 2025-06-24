package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Color;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Size;
import com.example.petshopapplication.model.Variant;
import com.example.petshopapplication.utils.Validate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailHolder> {

    private List<OrderDetail> orderDetailList;
    private Context context;

    public OrderDetailAdapter(List<OrderDetail> orderDetailList) {
        this.orderDetailList = orderDetailList;
    }

    @NonNull
    @Override
    public OrderDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_order_detail_item, parent, false);
        return new OrderDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);
        Log.d("OrderDetailAdapter", "Binding item at position " + position);
        Log.d("OrderDetailAdapter", "OrderDetail Info - Product ID: " + orderDetail.getProductId()
                + ", Quantity: " + orderDetail.getQuantity()
                + ", Purchased Price: " + orderDetail.getPurchased());

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");
        Query query = productsRef.orderByChild("id").equalTo(orderDetail.getProductId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("OrderDetailAdapter", "Product data found in Firebase for Product ID: " + orderDetail.getProductId());

                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            Log.d("OrderDetailAdapter", "Product Info - ID: " + product.getId() + ", Name: " + product.getName());
                            holder.txt_product_name.setText(product.getName());
                            Glide.with(context).load(product.getBaseImageURL()).into(holder.imv_product_image);
                            String formattedOldPrice = Validate.formatVND(product.getBasePrice());
                            String size, color;
                            Size sizeModel;
                            List<Variant> variants = product.getListVariant();
                            for (int i = 0; i < variants.size(); i++) {
                                if (variants.get(i).getId().equals(orderDetail.getVariantId())) {
                                    formattedOldPrice = Validate.formatVND(variants.get(i).getPrice());
                                    sizeModel = variants.get(i).getSize();
//                                    size = variants.get(i).getSize().getName();
                                    List<Color> colors = variants.get(i).getListColor();

                                    if (sizeModel != null && colors != null && !colors.isEmpty()) {
                                        // Have both size & color
                                        for (Color colorItem : colors) {
                                            if (colorItem.getId().equals(orderDetail.getColorId())) {
                                                holder.txt_product_detail.setText(sizeModel.getName() + " - " + colorItem.getName());
                                                break;
                                            }
                                        }
                                    } else if (sizeModel == null && colors != null && !colors.isEmpty()) {
                                        // Have Color, not Size
                                        for (Color colorItem : colors) {
                                            if (colorItem.getId().equals(orderDetail.getColorId())) {
                                                holder.txt_product_detail.setText(colorItem.getName());
                                                break;
                                            }
                                        }
                                    } else if (sizeModel != null && (colors == null || colors.isEmpty())) {
                                        // Have Size, not Color
                                        holder.txt_product_detail.setText(sizeModel.getName());
                                    } else {
                                        // No Size, No Color
                                        holder.txt_product_detail.setText("");
                                    }
                                    break;
                                }
                            }
                            holder.tv_old_price.setText(formattedOldPrice);
                            Log.d("OrderDetailAdapter", "Product: " + product.toString());
                            Log.d("OrderDetailAdapter", "Old Price: " + formattedOldPrice);

                        }
                    }
                } else {
                    Log.d("OrderDetailAdapter", "Product not found for productId: " + orderDetail.getProductId());
                    holder.txt_product_name.setText("Unknown Product");
                    holder.imv_product_image.setImageResource(R.drawable.product_image); // Placeholder if product not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("OrderDetailAdapter", "Failed to load product details: " + databaseError.getMessage());
            }
        });

        holder.txt_quantity.setText("x" + orderDetail.getQuantity());
        Log.d("OrderDetailAdapter", "Set quantity: x" + orderDetail.getQuantity());

        // Format price
        String formattedPrice = Validate.formatVND(orderDetail.getPurchased());

        holder.tv_old_price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        holder.txt_price.setText(formattedPrice);
    }


    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    static class OrderDetailHolder extends RecyclerView.ViewHolder {
        TextView txt_product_name, tv_old_price, txt_quantity, txt_price, txt_product_detail;
        ImageView imv_product_image;

        public OrderDetailHolder(@NonNull View itemView) {
            super(itemView);
            txt_product_name = itemView.findViewById(R.id.txt_product_name_detail);
            txt_product_detail = itemView.findViewById(R.id.txt_product_detail_detail);
            tv_old_price = itemView.findViewById(R.id.txt_old_price_detail);
            txt_quantity = itemView.findViewById(R.id.txt_quantity_detail);
            txt_price = itemView.findViewById(R.id.txt_price_detail);
            imv_product_image = itemView.findViewById(R.id.imv_product_image_detail);
        }
    }
}
