package com.example.petshopapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.petshopapplication.databinding.FragmentAdminDashBoardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashBoardFragment extends Fragment {

    private BarChart trendingBarChart;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FragmentAdminDashBoardBinding binding;

    private final List<String> productLabels = new ArrayList<>();
    private final Map<String, String> productNames = new HashMap<>();
    private TextView totalUserText, totalProductText, totalCategoryText, totalOrderText;

    private int[] customColors = {
            Color.parseColor("#FF5722"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#009688"),
            Color.parseColor("#FFEB3B")
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminDashBoardBinding.inflate(inflater, container, false);

        // Initialize Firebase and BarChart
        trendingBarChart = binding.trendingProductBarChart;
        database = FirebaseDatabase.getInstance();

        // Reference to statistics TextViews
        totalUserText = binding.getRoot().findViewById(R.id.totalUserText);
        totalProductText = binding.getRoot().findViewById(R.id.totalProductText);
        totalCategoryText = binding.getRoot().findViewById(R.id.totalCategoryText);
        totalOrderText = binding.getRoot().findViewById(R.id.totalOrderText);

        loadStatistics();
        loadTop5BestSellers();

        return binding.getRoot();
    }

    private void loadStatistics() {
        // Example: Load total users
        reference = database.getReference("users");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalUsers = task.getResult().getChildrenCount();
                totalUserText.setText("Total Users: " + totalUsers);
                Log.w("k","Total User is: "+totalUsers);
            }
        });

        // Example: Load total products
        reference = database.getReference("products");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalProducts = task.getResult().getChildrenCount();
                totalProductText.setText("Total Products: " + totalProducts);
            }
        });

        // Example: Load total categories
        reference = database.getReference("categories");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalCategories = task.getResult().getChildrenCount();
                totalCategoryText.setText("Total Categories: " + totalCategories);
            }
        });

        // Example: Load total orders
        reference = database.getReference("orders");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalOrders = task.getResult().getChildrenCount();
                totalOrderText.setText("Total Orders: " + totalOrders);
            }
        });
    }

    private void loadTop5BestSellers() {
        reference = database.getReference("orders");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Integer> productOrderCount = new HashMap<>();
                Map<String, Integer> productQuantityCount = new HashMap<>();

                // Fetch order details and quantity sold
                for (DataSnapshot orderSnapshot : task.getResult().getChildren()) {
                    for (DataSnapshot detailSnapshot : orderSnapshot.child("orderDetails").getChildren()) {
                        String productId = detailSnapshot.child("productId").getValue(String.class);
                        int quantity = detailSnapshot.child("quantity").getValue(Integer.class);

                        productOrderCount.put(productId, productOrderCount.getOrDefault(productId, 0) + 1);
                        productQuantityCount.put(productId, productQuantityCount.getOrDefault(productId, 0) + quantity);
                    }
                }

                // Query product names from Firebase
                DatabaseReference productReference = database.getReference("products");
                productReference.get().addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful() && productTask.getResult() != null) {
                        // Map to store product names with productId as key
                        Map<String, String> productNames = new HashMap<>();

                        // Fetch product names from Firebase
                        for (DataSnapshot productSnapshot : productTask.getResult().getChildren()) {
                            String productId = productSnapshot.child("id").getValue(String.class);
                            String productName = productSnapshot.child("name").getValue(String.class);

                            // Ensure product name is not null before adding it to the map
                            if (productId != null && productName != null) {
                                productNames.put(productId, productName);
                            } else {
                                Log.w("TrendingProductActivity", "Missing data for productId: " + productId);
                            }
                        }

                        // Sort products by order count
                        List<Map.Entry<String, Integer>> sortedProductOrderList = new ArrayList<>(productOrderCount.entrySet());
                        sortedProductOrderList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                        List<BarEntry> entries = new ArrayList<>();
                        List<String> productLabels = new ArrayList<>();

                        // Add top 5 products
                        for (int i = 0; i < Math.min(5, sortedProductOrderList.size()); i++) {
                            String productId = sortedProductOrderList.get(i).getKey();
                            String productName = productNames.get(productId);

                            // Check if productName is null before adding to the chart
                            if (productName != null) {
                                int totalQuantity = productQuantityCount.get(productId);
                                entries.add(new BarEntry(i, totalQuantity));
                                productLabels.add(productName); // Add actual product name to the list
                            } else {
                                Log.w("TrendingProductActivity", "Product name is null for productId: " + productId);
                            }
                        }

                        // Show the bar chart with the data
                        showBarChart(entries, productLabels, productNames); // Pass productNames to showBarChart method
                    } else {
                        Toast.makeText(getContext(), "Failed to load product names", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to load order data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBarChart(List<BarEntry> entries, List<String> productLabels, Map<String, String> productNames) {
        // Create and customize the BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Quantity Sold");
        dataSet.setColors(customColors);  // Set custom colors for the bars
        dataSet.setValueTextSize(14f);  // Set value text size inside the bars

        // Set up the BarData with the dataSet
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // Set custom bar width (optional)

        // Configure the BarChart (make it horizontal)
        trendingBarChart.setData(barData);
        trendingBarChart.setFitBars(true); // Fit bars to the height of the chart
        trendingBarChart.invalidate(); // Refresh the chart

        // Configure the X-axis (Product names will be on the X-axis)
        XAxis xAxis = trendingBarChart.getXAxis();
        xAxis.setValueFormatter(new ProductAxisValueFormatter(productLabels));  // Use custom formatter for product labels
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // Position at the bottom
        xAxis.setGranularity(1f); // Ensure labels are evenly spaced
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false); // Optional: Disable grid lines for cleaner look
        xAxis.setLabelRotationAngle(45f); // Rotate labels if needed

        // Configure the Y-axis (Quantities will be on the Y-axis)
        YAxis yAxisLeft = trendingBarChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0); // Start y-axis from zero
        yAxisLeft.setGranularity(1f); // Set granularity to whole numbers for quantity
        yAxisLeft.setDrawGridLines(true);  // Show grid lines for better readability

        trendingBarChart.getAxisRight().setEnabled(false); // Disable the right Y-axis

        // Set chart description and animations
        trendingBarChart.getDescription().setEnabled(false); // Disable chart description
        trendingBarChart.animateX(1000); // X-axis animation duration (1 second)
        trendingBarChart.animateY(1000); // Y-axis animation duration (1 second)

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Avoid memory leaks
    }
}
