package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;


import com.example.petshopapplication.Adapter.ViewPagerOrderManageAdapter;
import com.example.petshopapplication.databinding.ActivityListOrderManageBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ListOrderManageActivity extends AppCompatActivity {
    private String TAG = "ListOrderManageActivity";
    ActivityListOrderManageBinding binding;
    private String[] tabTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListOrderManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tabTitles = getResources().getStringArray(R.array.tab_order_manage_inventory_titles);
        initTablayouts();

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

    public static void updateOrderHistory(String orderId, int status, String message, String statusText, String statusDesc) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId).child("history");
        String updatedAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

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