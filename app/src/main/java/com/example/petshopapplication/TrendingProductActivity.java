package com.example.petshopapplication;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityPieChartBinding;
import com.example.petshopapplication.databinding.ActivityTrendingProductBinding;
import com.example.petshopapplication.model.Product;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

public class TrendingProductActivity extends AppCompatActivity {

    BarChart trendingBarChart;
    FirebaseDatabase database;
    DatabaseReference reference;
    final Handler handler = new Handler(Looper.getMainLooper());
    ActivityTrendingProductBinding binding;
    private List<String> productLabels = new ArrayList<>();
    private Map<String, String> productNames = new HashMap<>();

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trending_product);
        binding = ActivityTrendingProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        trendingBarChart = binding.trendingProductBarChart;
        database = FirebaseDatabase.getInstance();


        loadTop5BestSellers();
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
                        Toast.makeText(TrendingProductActivity.this, "Failed to load product names", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(TrendingProductActivity.this, "Failed to load order data", Toast.LENGTH_SHORT).show();
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

        // Now pass productNames to addShowDetailButtons method
        addShowDetailButtons(productLabels, productNames);
    }




    private void addShowDetailButtons(List<String> productLabels, Map<String, String> productNames) {
        // Create a container to hold the buttons
        LinearLayout buttonContainer = binding.buttonContainer;

        // Remove any existing buttons (in case of a re-render)
        buttonContainer.removeAllViews();

        // Log the product labels to verify the list
        Log.d("TrendingProductActivity", "Product Labels size: " + productLabels.size());

        for (int i = 0; i < productLabels.size(); i++) {
            String productName = productLabels.get(i);
            String productId = getProductIdFromName(productName, productNames); // Pass productNames to the method

            Log.d("TrendingProductActivity", "Product ID get: " + productId);

            if (productId != null) {
                // Create a new button for each product
                Button detailButton = new Button(this);
                detailButton.setText("Show Detail - " + productName);
                detailButton.setTag(productId);

                // Set button width and height explicitly
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                detailButton.setLayoutParams(params);
                detailButton.setPadding(16, 16, 16, 16);  // Add padding for better appearance

                // Set button click listener
                detailButton.setOnClickListener(v -> {
                    String clickedProductId = (String) v.getTag();
                    Intent intent = new Intent(TrendingProductActivity.this, AdminViewProductDetailActivity.class);
                    intent.putExtra("PRODUCT_ID", clickedProductId);
                    startActivity(intent);
                });

                // Add the button to the container
                buttonContainer.addView(detailButton);
            }
        }
    }

    // Method to retrieve productId from product name (now accepts productNames map)
    private String getProductIdFromName(String productName, Map<String, String> productNames) {
        Log.d("TrendingProductActivity", "Searching for productId for: " + productName);
        for (Map.Entry<String, String> entry : productNames.entrySet()) {
            Log.d("TrendingProductActivity", "Checking entry: " + entry.getKey() + " -> " + entry.getValue());
            if (entry.getValue().equalsIgnoreCase(productName.trim())) {
                return entry.getKey();
            }
        }
        return null;
    }
}