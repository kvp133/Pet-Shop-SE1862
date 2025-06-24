package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;


import com.example.petshopapplication.API.GoshipAPI;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.ShipmentSearchResponse;
import com.example.petshopapplication.Adapter.ViewPagerOrderManageAdapter;
import com.example.petshopapplication.databinding.ActivityListOrderManageBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListOrderManageActivity extends AppCompatActivity {
    private String TAG = "ListOrderManageActivity";
    ActivityListOrderManageBinding binding;
    private String[] tabTitles;
    private String AUTH_TOKEN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_order_manage);

        binding = ActivityListOrderManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tabTitles = getResources().getStringArray(R.array.tab_order_manage_inventory_titles);
        initTablayouts();

        // FAKE REQUEST to DEMO History status:
//        updateOrderHistory("74d40f01-33b9-48c4-94c5-ec8b274e1558", 901, "Đơn hàng đang chờ lấy hàng", "Chờ lấy hàng", "Chờ HVC điều phối đơn cho bưu tá");
//        updateOrderHistory("74d40f01-33b9-48c4-94c5-ec8b274e1558", 903, "Bưu tá đã nhận hàng từ shop", "Đã lấy hàng", "Bưu tá đã nhận hàng từ shop");
//        updateOrderHistory("74d40f01-33b9-48c4-94c5-ec8b274e1558", 904, "Hàng đang trên đường đi giao", "Giao hàng", "Hàng đang được đi giao cho khách");
//        updateOrderHistory("74d40f01-33b9-48c4-94c5-ec8b274e1558", 905, "Đơn hàng đã giao thành công", "Giao thành công", "Đã giao hàng cho khách thành công");

//        updateOrderHistory("", 905, "", "", "");
//        searchShipment("GSL7KJRM96");

        // Back btn:
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void initTablayouts() {
        // Find TabLayout and ViewPager2 in the layout
        TabLayout tabLayout = binding.tabLayout;
        ViewPager2 viewPager = binding.viewPager;

        // Create ViewPagerAdapter and set it to ViewPager2
        ViewPagerOrderManageAdapter viewPagerAdapter = new ViewPagerOrderManageAdapter(this, true);
        viewPager.setAdapter(viewPagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Check if the position is valid
            if (position >= 0 && position < tabTitles.length) {
                tab.setText(tabTitles[position]);
            } else {
                tab.setText("Tab " + position); // default name if out of range
            }
        }).attach();
    }

    // Phương thức gọi API searchShipment
    private void searchShipment(String code) {
        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);
        AUTH_TOKEN = "Bearer " + getResources().getString(R.string.goship_api_token);
        // Gọi API với mã vận chuyển (code)
        Call<ShipmentSearchResponse> call = api.searchShipment(
                "application/json",
                "application/json",
                AUTH_TOKEN,
                code
        );

        // Xử lý phản hồi từ API
        call.enqueue(new Callback<ShipmentSearchResponse>() {
            @Override
            public void onResponse(Call<ShipmentSearchResponse> call, Response<ShipmentSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShipmentSearchResponse shipmentResponse = response.body();

                    Log.d(TAG, "RESPONSE - BODY: " + response.body().toString());

                    // Xử lý dữ liệu từ shipmentResponse ở đây
                    Log.d(TAG, "Shipment found: " + shipmentResponse.getData().get(0).getId());
                    Log.d(TAG, "Shipment found: " + shipmentResponse.getData().get(0).toString());
                    String carrierLogo = shipmentResponse.getData().get(0).getServiceName();
                    Log.d(TAG, "Shipment carrierLogo: " + carrierLogo);
                    // Thực hiện các hành động khác với dữ liệu từ shipmentResponse nếu cần
                } else {
                    Log.e(TAG, "Không tìm thấy thông tin vận chuyển, mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ShipmentSearchResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi khi gọi API: " + t.getMessage());
            }
        });
    }

    public static void updateOrderHistory(String orderId, int status, String message, String statusText, String statusDesc) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId).child("history");
        String updatedAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        long updatedTime = System.currentTimeMillis() / 1000L;

        // Tạo object History mới
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("status", status);
        historyEntry.put("message", message);
        historyEntry.put("statusText", statusText);
        historyEntry.put("statusDesc", statusDesc);
        historyEntry.put("updatedAt", updatedAt);

        // Thêm vào danh sách history trong Firebase
        orderRef.push().setValue(historyEntry)
                .addOnSuccessListener(aVoid -> Log.d("UpdateHistory", "Order history updated successfully."))
                .addOnFailureListener(e -> Log.e("UpdateHistory", "Failed to update order history: " + e.getMessage()));
    }

}