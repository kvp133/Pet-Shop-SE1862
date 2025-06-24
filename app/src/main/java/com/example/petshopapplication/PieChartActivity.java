package com.example.petshopapplication;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityPieChartBinding;
import com.example.petshopapplication.model.Product;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference, productRef, categoryRef;
    PieChart pieChart;
    final Handler handler = new Handler(Looper.getMainLooper());
    ActivityPieChartBinding binding;

    Map<String, String> idMapping = new HashMap<>(); // Mapping from category ID to name
    Map<String, Integer> categoryCountMap = new HashMap<>();

    private int[] customColors = {
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#FFEB3B")  // Yellow
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pie_chart);
        binding = ActivityPieChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pieChart = findViewById(R.id.pieChart);
        database = FirebaseDatabase.getInstance();

        // Fetch and display top best-selling products
        //getTopBestSellingProducts();

        productRef = database.getReference("products");
        categoryRef = database.getReference("categories");

        // Fetch and display product count by category with category names
        displayProductCountByCategoryWithNames();
    }

    private void displayProductCountByCategoryWithNames() {
        // Step 1: Fetch all categories to create a mapping
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                for (DataSnapshot categorySnapshotChild : categorySnapshot.getChildren()) {
                    String categoryId = categorySnapshotChild.child("id").getValue(String.class);
                    String categoryName = categorySnapshotChild.child("name").getValue(String.class);
                    if (categoryId != null && categoryName != null) {
                        idMapping.put(categoryId, categoryName); // Map category ID to category name
                    }
                }
                // Now fetch all products to count them by the mapped IDs
                fetchProductsAndCountByCategory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.getMessage());
            }
        });
    }

    private void fetchProductsAndCountByCategory() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnapshot) {
                if (productSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot : productSnapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product != null && !product.isDeleted()) {
                            String categoryId = product.getCategoryId();
                            if (idMapping.containsKey(categoryId)) {
                                categoryCountMap.put(categoryId, categoryCountMap.getOrDefault(categoryId, 0) + 1);
                            }
                        }
                    }
                    // After counting, fetch the category names and display in the PieChart
                    fetchCategoryNamesAndDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.getMessage());
            }
        });
    }


    private void fetchCategoryNamesAndDisplay() {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
            String categoryId = entry.getKey();
            int productCount = entry.getValue();
            String categoryName = idMapping.getOrDefault(categoryId, "Unknown");
            Log.d("PieChartEntry", "Category ID: " + categoryId + ", Name: " + categoryName + ", Count: " + productCount);
            pieEntries.add(new PieEntry(productCount, categoryName));
        }

        handler.post(() -> {
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            // Use custom colors instead of COLORFUL_COLORS
            pieDataSet.setColors(getPieChartColors(pieEntries.size()));
            pieDataSet.setValueTextSize(12f);

            PieData pieData = new PieData(pieDataSet);
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.setCenterText("Product by Category");
            pieChart.setCenterTextSize(20f);
            pieChart.animateY(2000);
            pieChart.invalidate();

            // Log results for verification
            for (PieEntry entry : pieEntries) {
                Log.d("CategoryCount", "Category: " + entry.getLabel() + ", Count: " + entry.getValue());
            }
        });
    }

    // Method to return a list of colors based on the number of entries
    private List<Integer> getPieChartColors(int numberOfEntries) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < numberOfEntries; i++) {
            colors.add(customColors[i % customColors.length]);
        }
        return colors;
    }


}
