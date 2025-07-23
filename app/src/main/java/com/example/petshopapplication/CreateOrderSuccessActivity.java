package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.databinding.ActivityCreateOrderSuccessBinding;
import com.example.petshopapplication.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CreateOrderSuccessActivity extends AppCompatActivity {
    private ActivityCreateOrderSuccessBinding binding;
    private String TAG = "CreateOrderSuccessActivity";
    private String shipmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy shipmentId từ Intent
        shipmentId = getIntent().getStringExtra("successShipmentId");
        if (shipmentId == null || shipmentId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã vận đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvShipmentCode.setText(shipmentId);
        loadOrderInfo(shipmentId);

        binding.btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(CreateOrderSuccessActivity.this, ListOrderManageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    // Tải thông tin đơn hàng từ Firebase dựa trên shipmentId
    private void loadOrderInfo(String shipmentId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        Query query = ordersRef.orderByChild("shipmentId").equalTo(shipmentId).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            // Cập nhật giao diện với thông tin từ Firebase
                            binding.tvShipmentTitle.setText(order.getCarrierName() + " - Shipment Code");
                            Glide.with(CreateOrderSuccessActivity.this)
                                    .load(order.getCarrierLogo())
                                    .into(binding.imgShipmentLogo);

                            // (Giả sử bạn đã lưu địa chỉ cửa hàng vào Firebase, nếu chưa thì có thể lấy từ strings.xml)
                            binding.addressFrom.setText(getResources().getString(R.string.petshop_address_ward_description) + ", " + getResources().getString(R.string.petshop_address_district_description) + ", " + getResources().getString(R.string.petshop_address_city_description));

                            String addressToStr = order.getWard() + ", " + order.getDistrict() + ", " + order.getCity();
                            binding.addressTo.setText(addressToStr);
                            binding.orderStatus.setText("Chờ đơn vị vận chuyển đến lấy hàng");

                            binding.getRoot().setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "Không tìm thấy đơn hàng với mã vận đơn: " + shipmentId);
                    Toast.makeText(CreateOrderSuccessActivity.this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi tải thông tin đơn hàng: " + databaseError.getMessage());
            }
        });
    }
}