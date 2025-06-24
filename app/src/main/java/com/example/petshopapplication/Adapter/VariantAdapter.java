package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ItemModel;
import com.example.petshopapplication.R;

import java.util.List;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    private Context context;
    private List<ItemModel> items;

    public VariantAdapter(Context context, List<ItemModel> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_variants_color_size, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = items.get(position);
        holder.sizeNameTextView.setText(item.getSizeName());
        holder.colorNameTextView.setText(item.getColorName());
        holder.stockTextView.setText(String.valueOf(item.getStock()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sizeNameTextView;
        TextView stockTextView;
        TextView colorNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sizeNameTextView = itemView.findViewById(R.id.size_name_text_view);
            colorNameTextView = itemView.findViewById(R.id.color_name_text_view);
            stockTextView = itemView.findViewById(R.id.stock_text_view);
        }
    }
}
