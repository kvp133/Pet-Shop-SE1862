package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.AddressShopAdapter;
import com.example.petshopapplication.databinding.ActivityChangeShopAddressBinding;
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChangeShopAddressActivity extends AppCompatActivity {
    private String TAG = "ChangeShopAddressActivity";
    private ActivityChangeShopAddressBinding binding;
    private RecyclerView recyclerViewAddresses;
    private AddressShopAdapter addressShopAdapter;
    private List<UAddress> addressList;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeShopAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        Log.d(TAG, "Order id: " + orderId);

        recyclerViewAddresses = binding.recyclerViewAddresses;
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));

        addressList = new ArrayList<>(); // Khởi tạo list rỗng
        addressShopAdapter = new AddressShopAdapter(addressList);
        recyclerViewAddresses.setAdapter(addressShopAdapter);

        getAddressesFromFirebaseForInventory(); // Tải dữ liệu thật từ Firebase

        binding.ivBack.setOnClickListener(v -> finish());

        binding.linkAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddShopAddressActivity.class);
            startActivity(intent);
        });

        binding.btnSave.setOnClickListener(v -> {
            String selectedAddressId = addressShopAdapter.getManuallySelectedAddressId();
            if (selectedAddressId != null) {
                Intent intent = new Intent(this, PrepareOrderActivity.class);
                intent.putExtra("order_id", orderId);
                intent.putExtra("selected_id", selectedAddressId);
                startActivity(intent);
            } else {
                // Nếu không có địa chỉ nào được chọn thủ công, lấy địa chỉ mặc định
                String defaultAddressId = addressShopAdapter.getSelectedAddressId();
                if(defaultAddressId != null){
                    Intent intent = new Intent(this, PrepareOrderActivity.class);
                    intent.putExtra("order_id", orderId);
                    intent.putExtra("selected_id", defaultAddressId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please select an address.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getAddressesFromFirebaseForInventory() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("addresses");
        Query query = databaseReference.orderByChild("userId").equalTo("Inventory");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear(); // Xóa list cũ trước khi thêm dữ liệu mới
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UAddress address = dataSnapshot.getValue(UAddress.class);
                    if (address != null) {
                        addressList.add(address);
                    }
                }
                addressShopAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to retrieve addresses: " + error.getMessage());
            }
        });
    }
}