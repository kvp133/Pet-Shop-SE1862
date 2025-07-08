package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.OrderHistoryAdapter;
import com.example.petshopapplication.Adapter.TimelineDecoration;
import com.example.petshopapplication.databinding.ActivityOrderTrackingBinding;
import com.example.petshopapplication.model.History;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class OrderTrackingActivity extends AppCompatActivity {
    private ActivityOrderTrackingBinding binding;
    private static final String TAG = "OrderTrackingActivity";
    private RecyclerView recyclerViewOrderHistory;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<History> historyList;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");

        recyclerViewOrderHistory = findViewById(R.id.recyclerViewOrderHistory);
        recyclerViewOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(this, historyList);
        recyclerViewOrderHistory.setAdapter(orderHistoryAdapter);

        // Add decoration to the RecyclerView
        recyclerViewOrderHistory.addItemDecoration(new TimelineDecoration(this));

        // Get order history from Firebase
        loadOrderHistory(orderId);

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });
    }

    // fake data
    private void generateFakeOrderHistoryData() {
        historyList.clear();
        historyList.add(new History(900, "Đơn đã được đặt", "Đơn mới", "Đơn hàng đã được tạo thành công", "04/10/2024 14:00"));
        historyList.add(new History(901, "Đang chuẩn bị hàng", "Chuẩn bị hàng", "Người gửi đang chuẩn bị hàng", "04/10/2024 16:30"));
        historyList.add(new History(902, "Đang vận chuyển", "Đang giao hàng", "Đơn hàng đã rời kho phân loại", "05/10/2024 08:00"));
        historyList.add(new History(903, "Đã giao hàng thành công", "Đã giao hàng", "Đơn hàng đã được giao thành công", "06/10/2024 12:45"));

        // Thông báo adapter cập nhật dữ liệu
        orderHistoryAdapter.notifyDataSetChanged();
    }

    private void loadOrderHistory(String orderId) {
        Log.d(TAG, "Start - load history");
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders")
                .child(orderId).child("history");

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: bắt đầu lấy dữ liệu từ Firebase");
                historyList.clear();

                Log.d(TAG, "DataSnapshot: " + dataSnapshot.toString());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Reading child: " + snapshot.getKey() + ", Value: " + snapshot.getValue());

                    History history = snapshot.getValue(History.class);
                    if (history != null) {
                        Log.d(TAG, "Parsed History object: " + history.toString());
                        historyList.add(history);
                    } else {
                        Log.e(TAG, "Lỗi: Không thể parse đối tượng History từ Firebase cho key: " + snapshot.getKey());
                    }
                }
                orderHistoryAdapter.notifyDataSetChanged();
                Log.d(TAG, "notifyDataSetChanged called - Total items: " + historyList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load order history: " + databaseError.getMessage());
            }
        });
        Log.d(TAG, "End - load history");

    }

}