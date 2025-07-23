package com.example.petshopapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable; // Đảm bảo import này nếu sử dụng getDrawable
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Variant;
import com.example.petshopapplication.model.Color; // Vẫn cần import Color/Variant cho interface

import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ProductImageHolder>{

    private Product productItem;
    private Context context;
    private OnProductImageClickListener onProductImageClickListener;
    private int selectedPosition = 0; // Luôn chọn vị trí 0 (ảnh base)

    // Danh sách chỉ chứa duy nhất URL của ảnh base
    private List<String> imageUrls;

    public ProductImageAdapter(Product productItem, OnProductImageClickListener onProductImageClickListener) {
        this.productItem = productItem;
        this.onProductImageClickListener = onProductImageClickListener;
        this.imageUrls = new ArrayList<>();
        // Chỉ thêm baseImageURL vào danh sách
        if (productItem.getBaseImageURL() != null && !productItem.getBaseImageURL().isEmpty()) {
            this.imageUrls.add(productItem.getBaseImageURL());
        }
        // Lưu ý: Nếu listVariant rỗng, thì productItem.getBaseImageURL() sẽ là ảnh duy nhất.
        // Nếu listVariant CÓ dữ liệu, nhưng bạn CHỈ muốn hiển thị baseImageURL, thì code này đáp ứng.
    }

    public interface OnProductImageClickListener {
        // Trong trường hợp này, variant và color sẽ luôn là null khi click vào thumbnail
        // vì thumbnail chỉ hiển thị base image.
        void onProductImageClick(Product product, Variant variant, Color color);
    }

    @NonNull
    @Override
    public ProductImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // Sử dụng layout view_holder_product_image cho mỗi thumbnail
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_product_image, parent, false);
        return new ProductImageHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductImageHolder holder, @SuppressLint("RecyclerView") int position) {
        // Lấy URL ảnh (sẽ luôn là ảnh base vì danh sách chỉ có 1 phần tử)
        String currentImageUrl = imageUrls.get(position);

        // Logic highlight (có thể không cần thiết nếu chỉ có 1 ảnh)
        if (position == selectedPosition) {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_button_outline_orange));
        } else {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rectangle_button_border_gray));
        }

        // Tải ảnh sử dụng Glide
        Glide.with(context)
                .load(currentImageUrl)
                .into(holder.imv_product_image);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = position; // Vị trí này sẽ luôn là 0
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);

            // Khi click vào thumbnail, luôn gửi base image (variant và color là null)
            onProductImageClickListener.onProductImageClick(productItem, null, null);
        });

        // Trigger onProductImageClick cho ảnh đầu tiên khi nó được bind (đảm bảo màn hình chính hiển thị đúng)
        // Đây là cách để thiết lập ảnh ban đầu trên màn hình chi tiết sản phẩm.
        // Bạn cũng có thể gọi onProductImageClick từ Activity sau khi adapter được set.
        if (position == 0) { // Nếu đây là phần tử đầu tiên (và duy nhất)
            if (onProductImageClickListener != null) {
                // Đảm bảo không gọi quá nhiều lần nếu RecyclerView tự động bind lại
                // Có thể dùng một flag hoặc gọi từ Activity như đã đề xuất trước đó
                // Tuy nhiên, với 1 item duy nhất thì gọi ở đây cũng không quá tệ
                onProductImageClickListener.onProductImageClick(productItem, null, null);
            }
        }
    }

    @Override
    public int getItemCount() {
        // Luôn trả về 1 nếu có ảnh base, hoặc 0 nếu không có ảnh base
        return imageUrls.size();
    }

    public class ProductImageHolder extends RecyclerView.ViewHolder {

        ImageView imv_product_image;

        public ProductImageHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID này đúng là ImageView trong view_holder_product_image.xml
            imv_product_image = itemView.findViewById(R.id.imv_product_image); // Hoặc imv_product_image_thumbnail nếu bạn đổi ID
        }
    }
}