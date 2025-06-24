package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.R;
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter

public class AddressShopAdapter extends RecyclerView.Adapter<AddressShopAdapter.AddressViewHolder> {
    private String TAG = "AddressShopAdapter";
    private List<UAddress> addressList;
    private Context context;
    private String selectedAddressId; // ID of default address
    private String manuallySelectedAddressId; // ID check by user

    public AddressShopAdapter(List<UAddress> addressList) {
        this.addressList = addressList;

        // Find the default address ID
        for (UAddress address : addressList) {
            if (address.isDefault()) {
                selectedAddressId = address.getAddressId();
                Log.d(TAG, "Default selectedAddressId initialized to: " + selectedAddressId);
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_address_shop_item, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        UAddress address = addressList.get(position);

        // Information for each address
        holder.tvNamePhone.setText(address.getFullName() + " - " + address.getPhone());
        holder.tvAddress.setText(address.getWard() + " - " + address.getDistrict() + " - " + address.getCity());
        holder.tvSupport.setText("");

        Log.d(TAG, "onBindViewHolder - selectedAddressId: " + selectedAddressId + ", manuallySelectedAddressId: " + manuallySelectedAddressId);
        Log.d(TAG, "Current address ID: " + address.getAddressId() + ", isDefault: " + address.isDefault());

        holder.switchDefault.setOnCheckedChangeListener(null);
        holder.switchDefault.setChecked(address.isDefault());
        holder.tvDefaultTag.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

        // Check checked - ivCheck
        boolean isChecked = manuallySelectedAddressId != null
                ? address.getAddressId().equals(manuallySelectedAddressId)
                : address.getAddressId().equals(selectedAddressId);

        if (address.isDefault() && manuallySelectedAddressId == null) {
            holder.ivCheck.setVisibility(View.VISIBLE);
            Log.d(TAG, "Setting ivCheck visible for default address ID: " + address.getAddressId());
        } else {
            holder.ivCheck.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            Log.d(TAG, "Setting ivCheck " + (isChecked ? "visible" : "invisible") + " for address ID: " + address.getAddressId());
        }

        // Do Not accept if change the default to NOT DEFAULT
        holder.switchDefault.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            if (isChecked1 && !address.isDefault()) {
                Log.d(TAG, "Switch toggled ON for address ID: " + address.getAddressId());
                setDefaultAddress(address);
            } else if (!isChecked1 && address.isDefault()) {
                Log.d(TAG, "Attempt to turn off default switch for address ID: " + address.getAddressId() + " - resetting switch to ON.");
                holder.switchDefault.setChecked(true);
            }
        });

        // Update manuallySelectedAddressId when an item is clicked
        holder.itemView.setOnClickListener(v -> {
            manuallySelectedAddressId = address.getAddressId(); // save the manually selected address ID
            Log.d(TAG, "Item clicked - manuallySelectedAddressId set to: " + manuallySelectedAddressId);
            notifyDataSetChanged(); // Cập nhật lại giao diện RecyclerView
        });
    }

    // Set the default address
    private void setDefaultAddress(UAddress selectedAddress) {
        selectedAddressId = selectedAddress.getAddressId();
        Log.d(TAG, "Setting default address - selectedAddressId updated to: " + selectedAddressId);

        // Do not clear the manuallySelectedAddressId when setting the default address
        for (UAddress addr : addressList) {
            addr.setDefault(addr.getAddressId().equals(selectedAddressId));
        }
        notifyDataSetChanged();

        // Update in Firebase
        updateDefaultAddressOnFirebase(selectedAddress);
    }

    private void updateDefaultAddressOnFirebase(UAddress selectedAddress) {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        // Find All Address and update default
        addressRef.orderByChild("userId").equalTo("Inventory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UAddress address = snapshot.getValue(UAddress.class);
                    if (address != null) {
                        boolean isSelectedAddress = address.getAddressId().equals(selectedAddress.getAddressId());
                        snapshot.getRef().child("default").setValue(isSelectedAddress);
                        Log.d(TAG, "Firebase update - Address ID: " + address.getAddressId() + ", isDefault set to: " + isSelectedAddress);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Error updating default address");
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamePhone, tvAddress, tvSupport, tvDefaultTag, btnEditAddress;
        ImageView ivCheck;
        Switch switchDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamePhone = itemView.findViewById(R.id.tvNamePhone);
            tvDefaultTag = itemView.findViewById(R.id.tvDefaultTag);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvSupport = itemView.findViewById(R.id.tvSupport);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            switchDefault = itemView.findViewById(R.id.switchDefault);
            btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
        }
    }
}
