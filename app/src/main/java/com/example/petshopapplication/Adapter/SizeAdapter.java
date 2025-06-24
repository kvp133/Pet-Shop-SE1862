package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.ProductDetailActivity;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Size;
import com.example.petshopapplication.model.Variant;

import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder>{

    List<Size> sizeItems;

    Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnSizeClickEventListener onSizeClickEventListener;
    Product product;
    public SizeAdapter(List<Size> sizeItems, OnSizeClickEventListener onSizeClickEventListener,Product product) {
        this.sizeItems = sizeItems;
        this.onSizeClickEventListener = onSizeClickEventListener;
        this.product = product;
    }

    public interface OnSizeClickEventListener{
        void onSizeClickEvent(Size size, Product product);

    }

    @NonNull
    @Override
    public SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_size, parent, false);
        return new SizeViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder holder, int position) {
        if (position == selectedPosition) {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_corners));
        } else {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_button_outline_orange));
        }


        Size size = sizeItems.get(position);
        //bind data
        holder.tv_size.setText(size.getName());



        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = position;
            notifyItemChanged(selectedPosition);
            onSizeClickEventListener.onSizeClickEvent(size,product);
        });

    }

    @Override
    public int getItemCount() {
        return sizeItems.size();
    }

    public class SizeViewHolder extends RecyclerView.ViewHolder {
        TextView tv_size;
        public SizeViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_size = itemView.findViewById(R.id.tv_size);
        }
    }
}
