package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.AddressUpdateActivity;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.UAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<UAddress> UAddressList;
    private Context context;
    private OnAddressClickListener listener;
    private static final String TAG = "AddressAdapter";

    public AddressAdapter(List<UAddress> UAddressList, Context context, OnAddressClickListener listener) {
        this.UAddressList = UAddressList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        UAddress UAddress = UAddressList.get(position);
        holder.fullNameTextView.setText(UAddress.getFullName());
        holder.phoneTextView.setText(UAddress.getPhone());
        holder.addressTextView.setText(UAddress.getWard() + ", " + UAddress.getDistrict() + ", " + UAddress.getCity());

        Log.d(TAG, "Binding address: " + UAddress.getFullName() + " - " + UAddress.getPhone());


        if (UAddress.isDefault()) {
            holder.defaultTextView.setVisibility(View.VISIBLE); // Hiện TextView nếu là mặc định
        } else {
            holder.defaultTextView.setVisibility(View.GONE); // Ẩn TextView nếu không phải
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddressUpdateActivity.class);
            intent.putExtra("addressId", UAddress.getAddressId());
            context.startActivity(intent);
            Log.d(TAG, "Opening AddressUpdateActivity for addressId: " + UAddress.getAddressId());
        });

        holder.radioButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(UAddress);
                Log.d(TAG, "Selected address: " + UAddress.getFullName() + " - " + UAddress.getPhone());
            } else {
                Log.d(TAG, "Listener is null, address selection failed.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return UAddressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, phoneTextView, addressTextView,defaultTextView;
        RadioButton radioButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullNameTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            radioButton = itemView.findViewById(R.id.radioButton);
            defaultTextView = itemView.findViewById(R.id.defaultTextView);
        }
    }

    public interface OnAddressClickListener {
        void onAddressSelected(UAddress UAddress);
    }
}
