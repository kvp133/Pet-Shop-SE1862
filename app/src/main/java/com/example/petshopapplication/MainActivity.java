package com.example.petshopapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.petshopapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Kiểm tra nếu Activity này được khởi tạo lần đầu tiên, hiển thị Fragment ban đầu
//        if (savedInstanceState == null) {
//            // Đặt Fragment cần hiển thị sau khi đăng nhập
//            replaceFragment(new ProcessingTablayoutFragment());
//        }
    }

    // Phương thức để thay thế Fragment
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}