package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.Adapter.OrderDetailAdapter;
import com.example.petshopapplication.databinding.ActivityPrepareOrderBinding;
import com.example.petshopapplication.databinding.ActivityViewDetailOrderBinding;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.utils.Validate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewDetailOrderActivity extends AppCompatActivity {
    private static final String TAG = "ViewDetailOrderActivity";
    private List<OrderDetail> orderDetailList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private OrderDetailAdapter orderDetailAdapter;
    private ActivityViewDetailOrderBinding binding;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_detail_order);

        binding = ActivityViewDetailOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderId = getIntent().getStringExtra("order_id");
        Log.d(TAG, "Order ID: " + orderId);


        orderDetailList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(orderDetailList);

        binding.rcvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvOrderDetails.setAdapter(orderDetailAdapter);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("orders");

        loadOrderDetailById();

        // Set click listener for back button
        binding.ivBack.setOnClickListener(v -> {
            finish();
        });


        // Set click listener for back button
        binding.btnOut.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadOrderDetailById() {
        Log.d(TAG, "Start - load orders from Firebase");

        Query query = reference.orderByChild("id").equalTo(orderId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Start - onDataChange");
                orderDetailList.clear();

                if (snapshot.exists()) {
                    Log.d(TAG, "Data found in Firebase");

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if (order != null) {
                            Log.d(TAG, "Order ID: " + order.getId() + ", Status: " + order.getStatus());
//                            Glide.with(ViewDetailOrderActivity.this)
//                                    .load(order.getCarrierLogo())
//                                    .into(binding.imgShipmentLogo);
                            String userId = order.getUserId();
                            loadUserDetails(userId);
                            String city = order.getCity();
                            String district = order.getDistrict();
                            String ward = order.getWard();
                            String phoneNumber = getString(R.string.petshop_phone_nunmber);

                            String addressDetail = ward + "\n" + district + "\n" + city;
                            binding.tvOrderStatus.setText("Order " + order.getStatus());
                            binding.tvAddressDetail.setText(addressDetail);
                            binding.tvShippingMethod.setText(order.getCarrierName());
                            if (order.getStatus().equals("Processing") || order.getStatus().equals("Canceled")) {
                                binding.tvOrderCode.setVisibility(View.GONE);
                                binding.tvOrderCodeLabel.setVisibility(View.GONE);
                            } else {
                                binding.tvOrderCode.setText(order.getShipmentId());
                            }
                            Glide.with(ViewDetailOrderActivity.this)
                                    .load(order.getCarrierLogo())
                                    .into(binding.imvShipmentLogo);

                            binding.tvOrderCode.setText(order.getShipmentId());

                            binding.tvRecipientName.setText("");

//                            binding.tvPaymentMethod.setText(order.getPaymentId());

                            // Lấy paymentId và truy vấn payment method
                            String paymentId = order.getPaymentId();
                            loadPaymentMethod(paymentId);

//                            binding.tvShipmentBrand.setText(order.getCarrierName());
                            binding.txtTotalPrice.setText(String.format("Total: %s", Validate.formatVND(order.getTotalAmount())));
                            // Tính tổng số sản phẩm trong order
                            int totalQuantity = 0;
                            List<OrderDetail> orderDetails = order.getOrderDetails();
                            if (orderDetails != null) {
                                for (OrderDetail detail : orderDetails) {
                                    totalQuantity += detail.getQuantity();
                                }
                            }

                            // Cập nhật TextView cho tổng số sản phẩm
//                            binding.tvProductCount.setText("Total: x" + totalQuantity + " products");

                            // Cập nhật danh sách chi tiết sản phẩm
                            orderDetailList.clear();
                            orderDetailList.addAll(order.getOrderDetails());
                        } else {
                            Log.e(TAG, "Order data is null for snapshot: " + dataSnapshot.getKey());
                        }

                    }

                    Log.e(TAG, "orderDetailList ngay sau: " + orderDetailList.toString());

                    orderDetailAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "No data found in Firebase for fixed order ID: " + orderId);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders: " + error.getMessage());
                Toast.makeText(ViewDetailOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPaymentMethod(String paymentId) {
        DatabaseReference paymentReference = database.getReference("payments").child(paymentId);
        paymentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy payment method
                    String paymentMethod = snapshot.child("paymentMethod").getValue(String.class);
                    if (paymentMethod != null) {
                        binding.tvPaymentMethod.setText(paymentMethod);
                    } else {
                        Log.e(TAG, "Payment method is null for paymentId: " + paymentId);
                    }

                    // Lấy total amount
                    Double totalAmount = snapshot.child("amount").getValue(Double.class);
                    if (totalAmount != null) {
                        binding.txtTotalPrice.setText(String.format("Total: %s", Validate.formatVND(totalAmount)));
                    } else {
                        Log.e(TAG, "Total amount is null for paymentId: " + paymentId);
                    }
                } else {
                    Log.e(TAG, "No payment data found for paymentId: " + paymentId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load payment method: " + error.getMessage());
            }
        });
    }

    private void loadUserDetails(String userId) {
        DatabaseReference userReference = database.getReference("users");
        Log.d(TAG, "Searching for user with ID: " + userId);

        userReference.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String fullName = userSnapshot.child("fullName").getValue(String.class);
                    String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);

                    if (fullName != null && phoneNumber != null) {
                        binding.tvRecipientName.setText(String.format("%s - %s", fullName, phoneNumber));
                        userFound = true;
                        Log.d(TAG, "User found: " + fullName + " - " + phoneNumber);
                        break;
                    } else {
                        Log.e(TAG, "User details are missing for userId: " + userId);
                    }
                }

                if (!userFound) {
                    Log.e(TAG, "No user data found for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user details: " + error.getMessage());
            }
        });
    }


}