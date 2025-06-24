package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.AddressAdapter;
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddressSelectionActivity extends AppCompatActivity implements AddressAdapter.OnAddressClickListener {
    private static final int REQUEST_CODE_ADD_ADDRESS = 1;
    private static final int REQUEST_CODE_UPDATE_ADDRESS = 2;
    private static final String TAG = "AddressSelectionActivity";

    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<UAddress> UAddressList;
    private DatabaseReference addressRef;
    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_manage);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = findViewById(R.id.address_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        UAddressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(UAddressList, this, this);
        recyclerView.setAdapter(addressAdapter);

        addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        // Lấy tất cả địa chỉ của người dùng
        fetchUserAddresses(user.getUid());

        LinearLayout addAddressLayout = findViewById(R.id.add_address_layout);
        addAddressLayout.setOnClickListener(v -> {
            Intent intent = new Intent(AddressSelectionActivity.this, AddressAddActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_ADDRESS);
        });
    }

    @Override
    public void onAddressSelected(UAddress selectedAddress) {
        Log.d(TAG, "Selected address1: " + selectedAddress.toString());
        proceedToPayment(selectedAddress);
    }

    private void proceedToPayment(UAddress selectedAddress) {
        if (selectedAddress != null) {
            Intent intent = new Intent();
            intent.putExtra("selectedAddress", selectedAddress); // Chuyển địa chỉ đã chọn
            setResult(RESULT_OK, intent); // Gửi kết quả về activity trước đó
            finish(); // Kết thúc Activity này
        } else {
            Toast.makeText(this, "Vui lòng chọn địa chỉ!", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_ADD_ADDRESS || requestCode == REQUEST_CODE_UPDATE_ADDRESS) && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Triggered for requestCode " + requestCode);
            fetchUserAddresses(user.getUid()); // Tải lại danh sách địa chỉ sau khi thêm hoặc cập nhật
        }
    }

    private void fetchUserAddresses(String userId) {
        Log.d(TAG, "fetchUserAddresses: Fetching addresses for userId " + userId);
        addressRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UAddressList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UAddress UAddress = snapshot.getValue(UAddress.class);
                    if (UAddress != null) {
                        UAddressList.add(UAddress);
                    }
                }
                addressAdapter.notifyDataSetChanged(); // Cập nhật adapter sau khi thay đổi dữ liệu
                Log.d(TAG, "Fetched addresses: " + UAddressList.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddressSelectionActivity.this, "Error fetching addresses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
