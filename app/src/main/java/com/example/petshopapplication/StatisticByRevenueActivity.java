package com.example.petshopapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityStatisticByRevenueBinding;
import com.example.petshopapplication.model.Order;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatisticByRevenueActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ordersRef;
    LineChart revenueLineChart;
    final Handler handler = new Handler(Looper.getMainLooper());
    ActivityStatisticByRevenueBinding binding;

    // To store revenue by month
    Map<Integer, Double> revenueByMonth = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticByRevenueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imvGoBack.setOnClickListener(v -> finish());

        revenueLineChart = findViewById(R.id.revenueLineChart);
        database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");

        // Fetch orders data and calculate revenue by month
        fetchOrdersAndCalculateRevenue();
    }

    private void fetchOrdersAndCalculateRevenue() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ordersSnapshot) {
                if (ordersSnapshot.exists()) {
                    // Loop through each order in the snapshot
                    for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && order.getTotalAmount() > 0) {
                            // Access `orderDate` snapshot to get the `month` field directly
                            DataSnapshot orderDateSnapshot = orderSnapshot.child("orderDate");
                            Integer month = orderDateSnapshot.child("month").getValue(Integer.class); // Get the month as an integer

                            if (month != null) { // Ensure month is not null
                                double totalAmount = order.getTotalAmount(); // Get the total amount from the order
                                // Add revenue to the corresponding month in the map
                                revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0.0) + totalAmount);
                            }
                        }
                    }
                    // Display the revenue statistics on the chart after data is accumulated
                    displayRevenueByMonth();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.getMessage());
            }
        });
    }


    private void displayRevenueByMonth() {
        ArrayList<Entry> revenueEntries = new ArrayList<>();
        // Add revenue entries for each month (1-12)
        for (int i = 1; i <= 12; i++) {
            double revenue = revenueByMonth.getOrDefault(i, 0.0); // Get revenue for each month, default to 0 if no data
            revenueEntries.add(new Entry(i, (float) revenue)); // Cast to float for chart data
            Log.d("RevenueEntry", "Month: " + i + ", Revenue: " + revenue);
        }

        handler.post(() -> {
            // Set up line chart with data

            LineDataSet lineDataSet = new LineDataSet(revenueEntries, "Revenue by Month");
            lineDataSet.setColor(Color.BLUE);
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setDrawFilled(true);

            LineData lineData = new LineData(lineDataSet);
            revenueLineChart.setData(lineData);
            revenueLineChart.getDescription().setEnabled(false);
            revenueLineChart.animateX(1000);

            // Format the X-axis to show months only (1-12)
            revenueLineChart.getXAxis().setValueFormatter((value, axis) -> {
                if (value >= 1 && value <= 12) {
                    return String.valueOf((int) value); // Convert to integer to remove decimal part
                }
                return "";
            });

            // Set X-axis granularity to 1 to make sure only integer months appear
            revenueLineChart.getXAxis().setGranularity(1f);
            revenueLineChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

            // Set minimum and maximum Y-axis values
            revenueLineChart.getAxisLeft().setAxisMinimum(0f);
            revenueLineChart.getAxisLeft().setGranularity(5000f); // Set step size to 5000 for Y-axis

            // Format the left Y-axis to show revenue without decimal places
            revenueLineChart.getAxisLeft().setValueFormatter((value, axis) -> {
                return String.format("%.0f", value); // Display Y-axis values without decimal
            });

            // Format the right Y-axis to show revenue without decimal places
            revenueLineChart.getAxisRight().setValueFormatter((value, axis) -> {
                return String.format("%.0f", value); // Display Y-axis values without decimal
            });

            // Set right Y-axis to be the same as the left axis
            revenueLineChart.getAxisRight().setEnabled(false);

            revenueLineChart.invalidate(); // Refresh chart
        });
    }
}
