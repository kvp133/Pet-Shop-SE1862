package com.example.petshopapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.petshopapplication.Adapter.ViewPagerOrderManageAdapter;
import com.example.petshopapplication.databinding.ActivityListOrderBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.FirebaseDatabase;

public class ListOrderActivity extends AppCompatActivity {
    ActivityListOrderBinding binding;
    private String[] tabTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy danh sách các tiêu đề cho tab từ file strings.xml
        tabTitles = getResources().getStringArray(R.array.tab_order_manage_titles);

        // Khởi tạo TabLayouts
        initTablayouts();

        // Xử lý sự kiện nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void initTablayouts() {
        TabLayout tabLayout = binding.tabLayout;
        ViewPager2 viewPager = binding.viewPager;

        // isInventory được đặt là false vì đây là màn hình của người dùng (user)
        ViewPagerOrderManageAdapter viewPagerAdapter = new ViewPagerOrderManageAdapter(this, false);
        viewPager.setAdapter(viewPagerAdapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position >= 0 && position < tabTitles.length) {
                tab.setText(tabTitles[position]);
            } else {
                tab.setText("Tab " + position);
            }
        }).attach();
    }
}