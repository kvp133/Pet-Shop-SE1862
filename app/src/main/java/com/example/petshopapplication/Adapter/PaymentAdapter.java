package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log; // Import Log class
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Cart;
import com.example.petshopapplication.model.Color;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Variant;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentHolder> {
    private List<Cart> selectedCartItems; // Danh sách sản phẩm đã chọn
    private List<Product> productList; // Danh sách tất cả sản phẩm
    private Context context;
    private static final String TAG = "PaymentAdapter"; // Tạo một TAG cho log

    public PaymentAdapter(List<Product> productList, List<Cart> selectedCartItems, Context context) {
        this.productList = productList;
        this.selectedCartItems = selectedCartItems;
        this.context = context; // Gán context từ activity
    }

    @NonNull
    @Override
    public PaymentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_item_product, parent, false);
        return new PaymentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentHolder holder, int position) {
        Cart cart = selectedCartItems.get(position);
        Log.d(TAG, "Cart: " + cart.getSelectedColorId());
        Product product = getProductById(cart.getProductId());

        if (product != null) {
            // Hiển thị tên sản phẩm
            if (holder.tv_item_name != null) {
                holder.tv_item_name.setText(product.getName());
                Log.d(TAG, "Tên sản phẩm: " + product.getName()); // Ghi log tên sản phẩm
            }

                holder.tv_item_quantity.setText(String.valueOf(cart.getQuantity())); // Chuyển đổi số lượng thành chuỗi
                Log.d(TAG, "Số lượng: " + cart.getQuantity()); // Ghi log số lượng


            // Lấy variant đã chọn của sản phẩm
            Variant selectedVariant = null;
            for (Variant variant : product.getListVariant()) {
                if (variant.getId().equals(cart.getSelectedVariantId())) {
                    selectedVariant = variant;
                    break;
                }
            }

            if (selectedVariant != null) {
                // Hiển thị giá cũ và giá mới
                double oldPrice = selectedVariant.getPrice();
                if (holder.tv_item_old_price != null) {
                    holder.tv_item_old_price.setText(String.format("%.0fđ", oldPrice)); // Giá cũ
                    holder.tv_item_old_price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

                if (product.getDiscount() > 0) {
                    double newPrice = oldPrice * (1 - product.getDiscount() / 100.0); // Giá mới
                    if (holder.tv_item_new_price != null) {
                        holder.tv_item_new_price.setText(String.format("%.0fđ", newPrice));
                        Log.d(TAG, "Giá mới sau giảm giá: " + newPrice); // Ghi log giá mới
                    }
                } else {
                    if (holder.tv_item_new_price != null) {
                        holder.tv_item_new_price.setText(String.format("%.0fđ", oldPrice)); // Hiển thị giá gốc
                        Log.d(TAG, "Giá gốc: " + oldPrice); // Ghi log giá gốc
                    }
                }

                // Hiển thị màu sắc và kích thước đã chọn
                String selectedColor = null;
                String selectedSize = selectedVariant.getSize() != null ? selectedVariant.getSize().getName() : null;
                if (cart.getSelectedColorId() == null) {
                    selectedColor="";
                } else {
                    for (Color color : selectedVariant.getListColor()) {
                        if (color.getId().equals(cart.getSelectedColorId())) {
                            selectedColor = color.getName();
                            break;
                        }
                    }
                }

                StringBuilder itemTypeBuilder = new StringBuilder();
                if (selectedColor != null) {
                    itemTypeBuilder.append(selectedColor);
                }
                if (selectedSize != null) {
                    if (itemTypeBuilder.length() > 0) {
                        itemTypeBuilder.append(", ");
                    }
                    itemTypeBuilder.append(selectedSize);
                }

                if (holder.tv_item_type != null) {
                    if (itemTypeBuilder.length() > 0) {
                        holder.tv_item_type.setText(itemTypeBuilder.toString()); // Hiển thị màu sắc và kích thước
                    } else {
                        holder.tv_item_type.setVisibility(View.INVISIBLE); // Hiển thị thông báo nếu không có thông tin
                    }
                    Log.d(TAG, "Màu sắc và kích thước: " + itemTypeBuilder.toString()); // Ghi log thông tin màu sắc và kích thước
                }

                Glide.with(context)
                        .load(product.getBaseImageURL())
                        .into(holder.imv_item);
            }
        }
    }

    public Product getProductById(String productId) {
        for (Product product : productList) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return selectedCartItems.size();
    }

    public static class PaymentHolder extends RecyclerView.ViewHolder {
        ImageView imv_item;
        TextView tv_item_name, tv_item_old_price, tv_item_new_price, tv_item_quantity, tv_item_type;

        public PaymentHolder(@NonNull View itemView) {
            super(itemView);
            imv_item = itemView.findViewById(R.id.imv_item);
            tv_item_name = itemView.findViewById(R.id.tv_item_name);
            tv_item_old_price = itemView.findViewById(R.id.tv_item_old_price);
            tv_item_new_price = itemView.findViewById(R.id.tv_item_new_price);
            tv_item_quantity = itemView.findViewById(R.id.tv_item_quantity); // Check the ID here
            tv_item_type = itemView.findViewById(R.id.tv_item_type);
        }
    }
}
