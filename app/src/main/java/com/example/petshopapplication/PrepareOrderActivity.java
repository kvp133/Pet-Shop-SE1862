package com.example.petshopapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.API.GhnApi;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.Adapter.OrderDetailAdapter;
import com.example.petshopapplication.databinding.ActivityPrepareOrderBinding;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.UAddress;
import com.example.petshopapplication.utils.Validate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PrepareOrderActivity extends AppCompatActivity {
    private static final String TAG = "PrepareOrderActivity";
    private ActivityPrepareOrderBinding binding;

    private List<OrderDetail> orderDetailList;
    private OrderDetailAdapter orderDetailAdapter;
    private Order currentOrder;
    private UAddress shopAddress;

    private FirebaseDatabase database;
    private String orderId;
    private String selectedShopAddressId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrepareOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        selectedShopAddressId = getIntent().getStringExtra("selected_id");

        database = FirebaseDatabase.getInstance();
        initRecyclerView();
        loadOrderDetails();

        if (selectedShopAddressId != null) {
            loadShopAddressById(selectedShopAddressId);
        } else {
            loadDefaultShopAddress();
        }

        setupListeners();
    }

    private void initRecyclerView() {
        orderDetailList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(orderDetailList);
        binding.rcvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvOrderDetails.setAdapter(orderDetailAdapter);
    }

    private void setupListeners() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> handleConfirmOrder());
        binding.tvChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeShopAddressActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });
    }

    private void loadOrderDetails() {
        if (orderId == null) return;
        DatabaseReference orderRef = database.getReference("orders").child(orderId);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentOrder = snapshot.getValue(Order.class);
                    if (currentOrder != null) {
                        updateOrderUI(currentOrder);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PrepareOrderActivity.this, "Failed to load order.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderUI(Order order) {
        Glide.with(this).load(order.getCarrierLogo()).into(binding.imgShipmentLogo);
        binding.tvShipmentBrand.setText(order.getCarrierName());
        loadPaymentAmount(order.getPaymentId());

        int totalQuantity = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                totalQuantity += detail.getQuantity();
            }
            orderDetailList.clear();
            orderDetailList.addAll(order.getOrderDetails());
            orderDetailAdapter.notifyDataSetChanged();
        }
        binding.tvProductCount.setText("Total: x" + totalQuantity + " products");
    }

    private void loadDefaultShopAddress() {
        Query query = database.getReference("addresses").orderByChild("userId").equalTo("Inventory");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UAddress address = dataSnapshot.getValue(UAddress.class);
                    if (address != null && address.isDefault()) {
                        shopAddress = address;
                        updateAddressUI(address);
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadShopAddressById(String addressId) {
        DatabaseReference addressRef = database.getReference("addresses").child(addressId);
        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopAddress = snapshot.getValue(UAddress.class);
                    updateAddressUI(shopAddress);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateAddressUI(UAddress address) {
        if (address == null) return;
        String addressDetail = address.getPhone() + "\n" + address.getWard() + "\n" + address.getDistrict() + "\n" + address.getCity();
        binding.tvAddressDetail.setText(addressDetail);
    }

    private void loadPaymentAmount(String paymentId) {
        if (paymentId == null) return;
        DatabaseReference paymentRef = database.getReference("payments").child(paymentId);
        paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double totalAmount = snapshot.child("amount").getValue(Double.class);
                    if (totalAmount != null) {
                        binding.tvTotalPrice.setText(String.format("Total: %s", Validate.formatVND(totalAmount)));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleConfirmOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận chuẩn bị đơn hàng")
                .setMessage("Bạn đã chuẩn bị xong đơn hàng này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Đây là nơi sẽ gọi API tạo đơn hàng của GHN trong tương lai.
                    // Hiện tại, chúng ta chỉ cập nhật trạng thái trên Firebase.
                    updateOrderStatusToShipping();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateOrderStatusToShipping() {
        if (currentOrder == null || shopAddress == null) {
            Toast.makeText(this, "Thông tin đơn hàng hoặc địa chỉ shop bị thiếu.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference orderRef = database.getReference("orders").child(currentOrder.getId());
        // Trong tương lai, bạn sẽ nhận shipmentId từ API GHN và lưu vào đây.
        // Tạm thời, chúng ta sẽ tạo một mã giả.
        String fakeShipmentId = "GHN_" + currentOrder.getId().substring(0, 8).toUpperCase();

        orderRef.child("status").setValue("Shipping");
        orderRef.child("shipmentId").setValue(fakeShipmentId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đơn hàng đã được xác nhận và chuyển sang trạng thái Vận chuyển.", Toast.LENGTH_LONG).show();

                // Chuyển sang màn hình thành công
                Intent intent = new Intent(PrepareOrderActivity.this, CreateOrderSuccessActivity.class);
                intent.putExtra("successShipmentId", fakeShipmentId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật trạng thái đơn hàng thất bại.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}