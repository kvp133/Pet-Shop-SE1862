package com.example.petshopapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.petshopapplication.Adapter.OrderAdapter;
import com.example.petshopapplication.databinding.FragmentProcessingTablayoutBinding;
import com.example.petshopapplication.databinding.FragmentShippingTablayoutBinding;
import com.example.petshopapplication.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShippingTablayoutFragment extends Fragment {
    private String TAG = "ShippingTablayoutFragment";
    private FragmentShippingTablayoutBinding binding;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderItems;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private boolean isInventory = true;
    public ShippingTablayoutFragment(boolean isInventory) {
        this.isInventory = isInventory;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShippingTablayoutBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        orderItems = new ArrayList<>();
        adapter = new OrderAdapter(orderItems, true, "Shipping", isInventory);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("orders");
        if (isInventory) {
            initOrdersProcessingInventory();
        } else {
            initOrdersProcessing();
        }
        return view;
    }

    private void initOrdersProcessingInventory() {
        Log.d(TAG, "Start - load orders from Firebase");

        Query query = reference;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Start - onDataChange");
                orderItems.clear();

                if (snapshot.exists()) {
                    Log.d(TAG, "Data found in Firebase");

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if (order != null && "Shipping".equals(order.getStatus())) {
                            Log.d(TAG, "Order ID: " + order.getId() + ", Status: " + order.getStatus());
                            orderItems.add(order);
                        } else {
                            Log.e(TAG, "Order data is null for snapshot: " + dataSnapshot.getKey());
                        }
                    }
                    Log.d(TAG, "Total orders retrieved: " + orderItems.size());
                    adapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "No data found in Firebase");
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders: " + error.getMessage());
                Toast.makeText(getContext(), "Không tải được danh sách đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "End - load orders from Firebase");
    }

    private void initOrdersProcessing() {
        Log.d(TAG, "Start - load processing orders from Firebase");

        // Get current User
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not logged in.");
            return; // Stop if user is not logged in
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading processing orders for user ID: " + userId);

        // Get all orders of current user - status = "processing"
        Query query = reference.orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Start - onDataChange");
                orderItems.clear(); // Clear old data

                // Check snapshot has data
                if (snapshot.exists()) {
                    Log.d(TAG, "Data found in Firebase");

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if (order != null && "Shipping".equals(order.getStatus())) { // Filter orders with status "Processing"
                            Log.d(TAG, "Order ID: " + order.getId() + ", Status: " + order.getStatus());
                            orderItems.add(order);
                        }
                    }

                    Log.d(TAG, "Total processing orders retrieved: " + orderItems.size());
                    adapter.notifyDataSetChanged(); // Update RecyclerView with new data

                } else {
                    Log.d(TAG, "No processing data found in Firebase for user ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load processing orders: " + error.getMessage());
            }
        });
        Log.d(TAG, "End - load processing orders from Firebase");
    }

}