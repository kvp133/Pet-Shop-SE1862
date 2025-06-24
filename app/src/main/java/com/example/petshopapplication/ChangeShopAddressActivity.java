package com.example.petshopapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.AddressShopAdapter;
import com.example.petshopapplication.databinding.ActivityChangeShopAddressBinding;
import com.example.petshopapplication.model.UAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import lombok.NonNull;


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
        Log.d(TAG, "Get Address From FB - Start: ");
        addressList = getAddressesFromFirebaseForInventory();
        Log.d(TAG, "Get Address From FB - End: ");

//        Log.d(TAG, "Address List: ");
//        addressList = getAddressesFromPreferencesFAKE();
//        Log.d(TAG, "Address List: " + addressList.toString());
        addressShopAdapter = new AddressShopAdapter(addressList);
        recyclerViewAddresses.setAdapter(addressShopAdapter);

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.linkAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddShopAddressActivity.class);
            startActivity(intent);
        });

        binding.btnSave.setOnClickListener(v -> {
            String selectedAddressId = addressShopAdapter.getManuallySelectedAddressId(); // Lấy ID của địa chỉ được chọn

            if (selectedAddressId != null) {
                Log.d(TAG, "selectedAddressId to PrepareActivity: " + selectedAddressId);
                Intent intent = new Intent(this, PrepareOrderActivity.class);
                intent.putExtra("order_id", orderId);
                intent.putExtra("selected_id", selectedAddressId); // Truyền `selected_id`
                startActivity(intent);
            } else {
                Log.d(TAG, "No address selected. Please select an address.");
                // Hiển thị thông báo nếu không có địa chỉ nào được
                Intent intent = new Intent(this, PrepareOrderActivity.class);
                intent.putExtra("order_id", orderId);
                intent.putExtra("selected_id", selectedAddressId); // Truyền `selected_id`
                startActivity(intent);
            }
        });


    }

    private void saveSelectedAddressAsSelected() {
        String selectedAddressId = addressShopAdapter.getManuallySelectedAddressId();
        if (selectedAddressId != null) {
            for (UAddress address : addressList) {
                if (address.getAddressId().equals(selectedAddressId)) {
                    Log.d(TAG, "Address id : " + address.getAddressId() + " will be update");
                    break;
                }
            }
        }
    }

    private List<UAddress> getAddressesFromPreferencesFAKE() {
        List<UAddress> addresses = new ArrayList<>();

        // Tạo dữ liệu giả để kiểm tra hiển thị
        addresses.add(new UAddress("1", "Nguyen Van A", "0123456789", "Ho Chi Minh City", "1", "District 1", "101", "Ward 1", "1001", true, "user123"));
        addresses.add(new UAddress("2", "Tran Thi B", "0987654321", "Ha Noi", "2", "District 2", "102", "Ward 2", "1002", false, "user124"));
        addresses.add(new UAddress("3", "Le Van C", "0912345678", "Da Nang", "3", "District 3", "103", "Ward 3", "1003", false, "user125"));
        addresses.add(new UAddress("4", "Pham Thi D", "0909876543", "Can Tho", "4", "District 4", "104", "Ward 4", "1004", true, "user126"));
        addresses.add(new UAddress("5", "Do Van E", "0932123456", "Hue", "5", "District 5", "105", "Ward 5", "1005", false, "user127"));
        addresses.add(new UAddress("1", "Nguyen Van A", "0123456789", "Ho Chi Minh City", "1", "District 1", "101", "Ward 1", "1001", true, "user123"));
        addresses.add(new UAddress("2", "Tran Thi B", "0987654321", "Ha Noi", "2", "District 2", "102", "Ward 2", "1002", false, "user124"));
        addresses.add(new UAddress("3", "Le Van C", "0912345678", "Da Nang", "3", "District 3", "103", "Ward 3", "1003", false, "user125"));
        addresses.add(new UAddress("4", "Pham Thi D", "0909876543", "Can Tho", "4", "District 4", "104", "Ward 4", "1004", true, "user126"));
        addresses.add(new UAddress("5", "Do Van E", "0932123456", "Hue", "5", "District 5", "105", "Ward 5", "1005", false, "user127"));
        addresses.add(new UAddress("1", "Nguyen Van A", "0123456789", "Ho Chi Minh City", "1", "District 1", "101", "Ward 1", "1001", true, "user123"));
        addresses.add(new UAddress("2", "Tran Thi B", "0987654321", "Ha Noi", "2", "District 2", "102", "Ward 2", "1002", false, "user124"));
        addresses.add(new UAddress("3", "Le Van C", "0912345678", "Da Nang", "3", "District 3", "103", "Ward 3", "1003", false, "user125"));
        addresses.add(new UAddress("4", "Pham Thi D", "0909876543", "Can Tho", "4", "District 4", "104", "Ward 4", "1004", true, "user126"));
        addresses.add(new UAddress("5", "Do Van E", "0932123456", "Hue", "5", "District 5", "105", "Ward 5", "1005", false, "user127"));
        addresses.add(new UAddress("1", "Nguyen Van A", "0123456789", "Ho Chi Minh City", "1", "District 1", "101", "Ward 1", "1001", true, "user123"));
        addresses.add(new UAddress("2", "Tran Thi B", "0987654321", "Ha Noi", "2", "District 2", "102", "Ward 2", "1002", false, "user124"));
        addresses.add(new UAddress("3", "Le Van C", "0912345678", "Da Nang", "3", "District 3", "103", "Ward 3", "1003", false, "user125"));
        addresses.add(new UAddress("4", "Pham Thi D", "0909876543", "Can Tho", "4", "District 4", "104", "Ward 4", "1004", true, "user126"));
        addresses.add(new UAddress("5", "Do Van E", "0932123456", "Hue", "5", "District 5", "105", "Ward 5", "1005", false, "user127"));

        return addresses;
    }

    private List<UAddress> getAddressesFromFirebaseForInventory() {
        List<UAddress> addresses = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child("addresses").orderByChild("userId").equalTo("Inventory");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                addresses.clear(); // Clear list before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UAddress address = dataSnapshot.getValue(UAddress.class);
                    if (address != null) {
                        addresses.add(address);
                    }
                }

                Log.d(TAG, "Addresses for Inventory: " + addresses.toString());
                addressShopAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to retrieve addresses: " + error.getMessage());
            }
        });
        return addresses;
    }
}