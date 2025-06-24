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
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.Color;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder>{

    List<Color> colorItems;
    Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ColorAdapter.OnColorClickEventListener onColorClickEventListener;

    public ColorAdapter(List<Color> colorItems,OnColorClickEventListener onColorClickEventListener) {
        this.colorItems = colorItems;
        this.onColorClickEventListener = onColorClickEventListener;

    }

    public interface OnColorClickEventListener {
        void onColorClick(Color color);
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {

        if (position == selectedPosition) {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_corners));
        } else {
            holder.itemView.setBackground(context.getDrawable(R.drawable.rounded_button_outline_orange));
        }
        Color color = colorItems.get(position);
        //Set color view
        holder.tv_color_name.setText(color.getName());

        //Set color image
        Glide.with(context)
                .load(color.getImageUrl())
                .into(holder.imv_color_image);
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = position;
            notifyItemChanged(selectedPosition);

            onColorClickEventListener.onColorClick(color);
        });
    }

    @Override
    public int getItemCount() {
        return colorItems.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder{
        ImageView imv_color_image;
        TextView tv_color_name;
        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_color_image = itemView.findViewById(R.id.imv_color_image);
            tv_color_name = itemView.findViewById(R.id.tv_color_name);
        }
    }
}
