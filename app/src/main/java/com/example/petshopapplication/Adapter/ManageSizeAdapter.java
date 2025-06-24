package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Size;

import java.util.List;

public class ManageSizeAdapter extends RecyclerView.Adapter<ManageSizeAdapter.SizeViewHolder2>{

    List<Size> sizeItems;

    Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnSizeClickEventListener onSizeClickEventListener;

    public ManageSizeAdapter(List<Size> sizeItems, OnSizeClickEventListener onSizeClickEventListener) {
        this.sizeItems = sizeItems;
        this.onSizeClickEventListener = onSizeClickEventListener;
    }

    public interface OnSizeClickEventListener{
        void onSizeClickEvent(Size size);
    }

    @NonNull
    @Override
    public SizeViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_size, parent, false);
        return new SizeViewHolder2(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder2 holder, int position) {
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
            onSizeClickEventListener.onSizeClickEvent(size);
        });

    }

    @Override
    public int getItemCount() {
        return sizeItems.size();
    }

    public class SizeViewHolder2 extends RecyclerView.ViewHolder {
        TextView tv_size;
        public SizeViewHolder2(@NonNull View itemView) {
            super(itemView);
            tv_size = itemView.findViewById(R.id.tv_size);
        }
    }
}