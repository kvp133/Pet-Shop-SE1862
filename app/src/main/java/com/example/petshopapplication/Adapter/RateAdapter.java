package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.API_model.Rate;
import com.example.petshopapplication.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.RateViewHolder> {

    private List<Rate> rateList;
    private Context context;
    private OnRateSelectListener listener;
    private int selectedPosition = -1; // Chỉ số của `CheckBox` được chọn
    private String selectedRateID; // Biến lưu ID của rate được chọn
    private String selectedCartierName; // Biến lưu ID của rate được chọn
    private String selectedCartierLogo; // Biến lưu ID của rate được chọn

    // Cập nhật giao diện để nhận ID của phí được chọn
    public interface OnRateSelectListener {
        void onRateSelected(double fee, String rateID, String catierName, String cartierLogo);
    }

    public RateAdapter(List<Rate> rateList, Context context, OnRateSelectListener listener) {
        this.rateList = rateList;
        this.context = context;
        this.listener = listener;
    }

    public void setRateList(List<Rate> rateList) {
        this.rateList = rateList;
        notifyDataSetChanged(); // Thông báo cập nhật danh sách
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_item_rate, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        Rate rate = rateList.get(position);
        holder.carrierNameTextView.setText(rate.getCarrierName());
        holder.feeTextView.setText(String.format("%s VND", NumberFormat.getInstance(Locale.getDefault()).format(rate.getTotalAmount())));

        // Đặt trạng thái của `CheckBox` theo vị trí đã chọn
        holder.selectCheckBox.setChecked(position == selectedPosition);

        // Xử lý sự kiện chọn `CheckBox`
        holder.selectCheckBox.setOnClickListener(v -> {
            if (position != selectedPosition) {
                selectedPosition = position; // Cập nhật vị trí đã chọn
                selectedRateID = rate.getId(); // Lưu ID của rate được chọn
                selectedCartierName = rate.getCarrierName();
                selectedCartierLogo = rate.getCarrierLogo();
                listener.onRateSelected(rate.getTotalAmount(), selectedRateID, selectedCartierName, selectedCartierLogo); // Gửi giá trị phí và ID được chọn
                notifyDataSetChanged(); // Cập nhật lại tất cả các `CheckBox` trong danh sách
            }
        });
    }

    @Override
    public int getItemCount() {
        return rateList != null ? rateList.size() : 0;
    }

    public static class RateViewHolder extends RecyclerView.ViewHolder {
        TextView carrierNameTextView, feeTextView;
        CheckBox selectCheckBox;

        public RateViewHolder(@NonNull View itemView) {
            super(itemView);
            carrierNameTextView = itemView.findViewById(R.id.carrierNameTextView);
            feeTextView = itemView.findViewById(R.id.feeTextView);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);
        }
    }
}
