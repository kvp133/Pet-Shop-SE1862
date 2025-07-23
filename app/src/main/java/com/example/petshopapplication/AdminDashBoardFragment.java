package com.example.petshopapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.petshopapplication.databinding.FragmentAdminDashBoardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.Entry; // Import Entry cho IValueFormatter
import com.github.mikephil.charting.data.PieEntry; // Import PieEntry vẫn cần
import com.github.mikephil.charting.formatter.IAxisValueFormatter; // CHÍNH XÁC: IAxisValueFormatter cho BarChart
import com.github.mikephil.charting.formatter.IValueFormatter; // CHÍNH XÁC: IValueFormatter cho Pie Chart (định dạng phần trăm)
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler; // Cần cho IValueFormatter

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashBoardFragment extends Fragment {

    private BarChart trendingBarChart;
    private PieChart categoryPieChart;
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

        trendingBarChart = binding.trendingProductBarChart;
        categoryPieChart = binding.categoryProductPieChart;
        database = FirebaseDatabase.getInstance();

        // CHÍNH XÁC: Truy cập TextView qua View Binding
        totalUserText = binding.totalUserText;
        totalProductText = binding.totalProductText;
        totalCategoryText = binding.totalCategoryText;
        totalOrderText = binding.totalOrderText;

        loadStatistics();
        loadTop5BestSellers();
        loadProductCategoryDistribution();

        return binding.getRoot();
    }

    private void loadStatistics() {
        reference = database.getReference("users");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalUsers = task.getResult().getChildrenCount();
                totalUserText.setText("Tổng số người dùng:\n" + totalUsers);
                Log.w("AdminDashBoard", "Total User is: " + totalUsers);
            } else {
                Log.e("AdminDashBoard", "Failed to load total users", task.getException());
            }
        });

        reference = database.getReference("products");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalProducts = task.getResult().getChildrenCount();
                totalProductText.setText("Tổng số sản phẩm:\n" + totalProducts);
            } else {
                Log.e("AdminDashBoard", "Failed to load total products", task.getException());
            }
        });

        reference = database.getReference("categories");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalCategories = task.getResult().getChildrenCount();
                totalCategoryText.setText("Tổng số danh mục:\n" + totalCategories);
            } else {
                Log.e("AdminDashBoard", "Failed to load total categories", task.getException());
            }
        });

        reference = database.getReference("orders");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long totalOrders = task.getResult().getChildrenCount();
                totalOrderText.setText("Tổng số đơn hàng:\n" + totalOrders);
            } else {
                Log.e("AdminDashBoard", "Failed to load total orders", task.getException());
            }
        });
    }

    private void loadTop5BestSellers() {
        reference = database.getReference("orders");
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Integer> productOrderCount = new HashMap<>();
                Map<String, Integer> productQuantityCount = new HashMap<>();

                for (DataSnapshot orderSnapshot : task.getResult().getChildren()) {
                    for (DataSnapshot detailSnapshot : orderSnapshot.child("orderDetails").getChildren()) {
                        String productId = detailSnapshot.child("productId").getValue(String.class);
                        if (detailSnapshot.child("quantity").exists() && detailSnapshot.child("quantity").getValue() instanceof Long) {
                            int quantity = ((Long) detailSnapshot.child("quantity").getValue()).intValue();

                            if (productId != null) {
                                productOrderCount.put(productId, productOrderCount.getOrDefault(productId, 0) + 1);
                                productQuantityCount.put(productId, productQuantityCount.getOrDefault(productId, 0) + quantity);
                            }
                        } else {
                            Log.w("AdminDashBoard", "Missing quantity or invalid type for order detail: " + detailSnapshot.getKey());
                        }
                    }
                }

                DatabaseReference productReference = database.getReference("products");
                productReference.get().addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful() && productTask.getResult() != null) {
                        Map<String, String> productNamesForChart = new HashMap<>();

                        for (DataSnapshot productSnapshot : productTask.getResult().getChildren()) {
                            String productId = productSnapshot.child("id").getValue(String.class);
                            String productName = productSnapshot.child("name").getValue(String.class);

                            if (productId != null && productName != null) {
                                productNamesForChart.put(productId, productName);
                            } else {
                                Log.w("AdminDashBoard", "Missing product ID or Name for product snapshot: " + productSnapshot.getKey());
                            }
                        }

                        List<Map.Entry<String, Integer>> sortedProductOrderList = new ArrayList<>(productOrderCount.entrySet());
                        sortedProductOrderList.sort((a, b) -> {
                            Integer quantityA = productQuantityCount.getOrDefault(a.getKey(), 0);
                            Integer quantityB = productQuantityCount.getOrDefault(b.getKey(), 0);
                            return quantityB.compareTo(quantityA);
                        });


                        List<BarEntry> entries = new ArrayList<>();
                        List<String> labelsForBarChart = new ArrayList<>();

                        for (int i = 0; i < Math.min(5, sortedProductOrderList.size()); i++) {
                            String productId = sortedProductOrderList.get(i).getKey();
                            String productName = productNamesForChart.get(productId);

                            if (productName != null && productQuantityCount.containsKey(productId)) {
                                int totalQuantity = productQuantityCount.get(productId);
                                entries.add(new BarEntry(i, totalQuantity));
                                labelsForBarChart.add(productName);
                            } else {
                                Log.w("AdminDashBoard", "Product name or quantity is null for productId: " + productId);
                            }
                        }
                        showBarChart(entries, labelsForBarChart);
                    } else {
                        Toast.makeText(getContext(), "Failed to load product names for Bar Chart", Toast.LENGTH_SHORT).show();
                        Log.e("AdminDashBoard", "Failed to load product names for Bar Chart", productTask.getException());
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to load order data for Bar Chart", Toast.LENGTH_SHORT).show();
                Log.e("AdminDashBoard", "Failed to load order data for Bar Chart", task.getException());
            }
        });
    }

    private void loadProductCategoryDistribution() {
        Map<String, Integer> categoryProductCount = new HashMap<>();
        Map<String, String> categoryNames = new HashMap<>();

        DatabaseReference categoriesRef = database.getReference("categories");
        categoriesRef.get().addOnCompleteListener(categoryTask -> {
            if (categoryTask.isSuccessful() && categoryTask.getResult() != null) {
                for (DataSnapshot categorySnapshot : categoryTask.getResult().getChildren()) {
                    String categoryId = categorySnapshot.child("id").getValue(String.class);
                    String categoryName = categorySnapshot.child("name").getValue(String.class);
                    if (categoryId != null && categoryName != null) {
                        categoryNames.put(categoryId, categoryName);
                        categoryProductCount.put(categoryId, 0);
                    }
                }

                DatabaseReference productsRef = database.getReference("products");
                productsRef.get().addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful() && productTask.getResult() != null) {
                        for (DataSnapshot productSnapshot : productTask.getResult().getChildren()) {
                            String categoryId = productSnapshot.child("categoryId").getValue(String.class);
                            if (categoryId != null && categoryProductCount.containsKey(categoryId)) {
                                categoryProductCount.put(categoryId, categoryProductCount.get(categoryId) + 1);
                            }
                        }
                        showPieChart(categoryProductCount, categoryNames);
                    } else {
                        Toast.makeText(getContext(), "Failed to load products for Pie Chart", Toast.LENGTH_SHORT).show();
                        Log.e("AdminDashBoard", "Failed to load products for Pie Chart", productTask.getException());
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to load categories for Pie Chart", Toast.LENGTH_SHORT).show();
                Log.e("AdminDashBoard", "Failed to load categories for Pie Chart", categoryTask.getException());
            }
        });
    }

    private void showPieChart(Map<String, Integer> categoryProductCount, Map<String, String> categoryNames) {
        List<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());

        float totalValueSum = 0f; // Khởi tạo tổng giá trị
        for (Map.Entry<String, Integer> entry : categoryProductCount.entrySet()) {
            String categoryId = entry.getKey();
            int count = entry.getValue();
            String categoryName = categoryNames.get(categoryId);

            if (categoryName != null && count > 0) {
                pieEntries.add(new PieEntry(count, categoryName));
                totalValueSum += count; // Cộng dồn vào tổng
            }
        }

        if (pieEntries.isEmpty()) {
            categoryPieChart.clear();
            categoryPieChart.setNoDataText("Không có dữ liệu sản phẩm theo danh mục.");
            categoryPieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Product Categories");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        // CHÍNH XÁC: Sử dụng IValueFormatter tùy chỉnh để tự tính phần trăm
        final float finalTotalValueSum = totalValueSum; // Cần final hoặc effectively final cho anonymous inner class
        pieData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (finalTotalValueSum == 0) return "0%"; // Tránh chia cho 0
                // Value ở đây là giá trị gốc (số lượng), không phải phần trăm
                return String.format("%.1f%%", (value / finalTotalValueSum) * 100);
            }
        });

        categoryPieChart.setData(pieData);

        // RẤT QUAN TRỌNG: Tắt tính năng phần trăm tự động của PieChart
        // vì chúng ta đã tự tính trong formatter.
        categoryPieChart.setUsePercentValues(false); // Đặt thành FALSE

        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setExtraOffsets(5f, 10f, 5f, 5f);

        categoryPieChart.setDragDecelerationFrictionCoef(0.95f);

        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setHoleColor(Color.WHITE);

        categoryPieChart.setTransparentCircleColor(Color.WHITE);
        categoryPieChart.setTransparentCircleAlpha(110);

        categoryPieChart.setHoleRadius(58f);
        categoryPieChart.setTransparentCircleRadius(61f);

        categoryPieChart.setDrawCenterText(true);
        categoryPieChart.setCenterText("Tỷ lệ SP\nTheo DM");
        categoryPieChart.setCenterTextSize(16f);
        categoryPieChart.setCenterTextColor(Color.DKGRAY);

        categoryPieChart.animateY(1400);
        categoryPieChart.setEntryLabelColor(Color.BLACK);
        categoryPieChart.setEntryLabelTextSize(10f);

        categoryPieChart.invalidate();
    }


    private void showBarChart(List<BarEntry> entries, List<String> productLabels) {
        BarDataSet dataSet = new BarDataSet(entries, "Số lượng đã bán");
        dataSet.setColors(customColors);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        trendingBarChart.setData(barData);
        trendingBarChart.setFitBars(true);
        trendingBarChart.invalidate();

        XAxis xAxis = trendingBarChart.getXAxis();
        // CHÍNH XÁC: Sử dụng ProductAxisValueFormatter đã được cung cấp (IAxisValueFormatter)
        xAxis.setValueFormatter(new ProductAxisValueFormatter(productLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(45f);

        YAxis yAxisLeft = trendingBarChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setDrawGridLines(true);

        trendingBarChart.getAxisRight().setEnabled(false);

        trendingBarChart.getDescription().setEnabled(false);
        trendingBarChart.animateX(1000);
        trendingBarChart.animateY(1000);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}