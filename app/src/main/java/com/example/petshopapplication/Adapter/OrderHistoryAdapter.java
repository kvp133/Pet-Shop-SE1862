package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.History;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    private final List<History> historyList;
    private final Context context;

    public OrderHistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_item_order_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = historyList.get(position);
        // Format `updatedAt`: "dd/MM/yyyy HH:mm" => "dd 'Th'MM - yyyy" + "HH:mm"
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(history.getUpdatedAt());

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd 'Th'MM - yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String formattedDate = dayFormat.format(date) + "\n" + timeFormat.format(date);
            holder.tvDate.setText(formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            holder.tvDate.setText(history.getUpdatedAt());
        }        holder.tvStatus.setText(history.getStatusText());
        holder.tvDetail.setText(history.getStatusDesc());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDetail = itemView.findViewById(R.id.tvDetail);
        }
    }
}
