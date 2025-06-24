package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderingActivity extends AppCompatActivity {
    private DatabaseReference db;
    private String orderId;

    private TextView tvRecipientName, tvAddressDetail, tvTotalPrice, tvPaymentMethod;
    private TextView tvCarrierName;
    private ImageView imvShipmentLogo;
    Button continueShoppingButton;
    Button viewOrdersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);
        db = FirebaseDatabase.getInstance().getReference();
        orderId = getIntent().getStringExtra("orderId");

        tvCarrierName = findViewById(R.id.tv_shipping_method);
        tvRecipientName = findViewById(R.id.tv_recipient_name);
        tvAddressDetail = findViewById(R.id.tv_address_detail);
        tvTotalPrice = findViewById(R.id.txt_total_price);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        imvShipmentLogo = findViewById(R.id.imv_shipment_logo);

        loadOrderDetails(orderId);

        continueShoppingButton = findViewById(R.id.button_continue_shopping);
        continueShoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OrderingActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // End current activity
            }
        });

        viewOrdersButton = findViewById(R.id.button_view_orders);
        viewOrdersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to ListOrderActivity
                Intent intent = new Intent(OrderingActivity.this, ListOrderActivity.class);
                startActivity(intent);
                finish(); // End current activity
            }
        });
    }

    private void loadOrderDetails(String orderId) {
        Log.d("OrderingActivity", "Start loading order details for orderId: " + orderId);

        DatabaseReference orderRef = db.child("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("OrderingActivity", "Order found: " + orderId);

                    String recipientFullName = dataSnapshot.child("fullName").getValue(String.class);
                    String shipmentLogoUrl = dataSnapshot.child("carrierLogo").getValue(String.class);

                    String carrierName = dataSnapshot.child("carrierName").getValue(String.class);
                    String city = dataSnapshot.child("city").getValue(String.class);
                    String district = dataSnapshot.child("district").getValue(String.class);
                    String ward = dataSnapshot.child("ward").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                    String addressDetail = (ward != null ? ward : "") + "," + (district != null ? district : "") + "," + (city != null ? city : "");
                    String recipientName = (recipientFullName != null ? recipientFullName : "") + " | " + (phoneNumber != null ? phoneNumber : "");

                    Log.d("OrderingActivity", "Shipping method: " + carrierName);
                    Log.d("OrderingActivity", "Recipient name: " + recipientName);
                    Log.d("OrderingActivity", "Address: " + addressDetail);

                    DatabaseReference paymentRef = db.child("payments");
                    Query paymentQuery = paymentRef.orderByChild("orderId").equalTo(orderId); // Create Query object
                    paymentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot paymentSnapshot : dataSnapshot.getChildren()) {

                                    Double totalAmount = paymentSnapshot.child("amount").getValue(Double.class);
                                    NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                                    numberFormat.setMinimumFractionDigits(0); // Set minimum fraction digits to 0 (if needed)

                                    String formattedTotalPrice = totalAmount != null ? numberFormat.format(totalAmount) : "N/A";

                                    tvTotalPrice.setText("Tổng tiền thanh toán: " + formattedTotalPrice + " VND");

                                    String paymentMethod = paymentSnapshot.child("paymentMethod").getValue(String.class);

                                    tvCarrierName.setText(carrierName != null ? carrierName : "Unknown");
                                    tvRecipientName.setText(recipientName);
                                    tvAddressDetail.setText(addressDetail);
                                    tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "Unknown");

                                    if (shipmentLogoUrl != null) {
                                        Glide.with(OrderingActivity.this).load(shipmentLogoUrl).into(imvShipmentLogo);
                                        Log.d("OrderingActivity", "Loaded shipment logo: " + shipmentLogoUrl);
                                    }
                                }
                            } else {
                                Toast.makeText(OrderingActivity.this, "Không tìm thấy thông tin thanh toán!", Toast.LENGTH_SHORT).show();
                                Log.d("OrderingActivity", "Payment information not found for orderId: " + orderId);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(OrderingActivity.this, "Lỗi tải thông tin thanh toán!", Toast.LENGTH_SHORT).show();
                            Log.e("OrderingActivity", "Error loading payment information: " + databaseError.getMessage());
                        }
                    });

                } else {
                    Toast.makeText(OrderingActivity.this, "Đơn hàng không tồn tại!", Toast.LENGTH_SHORT).show();
                    Log.d("OrderingActivity", "Order not found: " + orderId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OrderingActivity.this, "Lỗi tải dữ liệu đơn hàng!", Toast.LENGTH_SHORT).show();
                Log.e("OrderingActivity", "Error loading order data: " + databaseError.getMessage());
            }
        });
    }
}
