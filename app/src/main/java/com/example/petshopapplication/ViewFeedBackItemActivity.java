package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.FeedBackListAdapter;
import com.example.petshopapplication.Adapter.UserFeedBackListAdapter;
import com.example.petshopapplication.databinding.ActivityListFeedbackBinding;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewFeedBackItemActivity extends AppCompatActivity {

    private ActivityListFeedbackBinding binding;  // ViewBinding reference
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private UserFeedBackListAdapter feedbackAdapter;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private List<FeedBack> feedbackList;
    private String userId;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewBinding
        binding = ActivityListFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.tbl_feedback_name));

        // Set up RecyclerView
        binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(this));
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        //get current user
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        feedbackList = new ArrayList<>();
        binding.rcvFeedback.setAdapter(feedbackAdapter);

        // Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(getString(R.string.tbl_feedback_name));

        // Fetch feedbacks from Firebase
        getIntend();
        getProductId(orderId);
    }

    private void fetchFeedbacks(String productId) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                    if (feedback != null && !feedback.isDeleted()
                            && feedback.getProductId().equals(productId)
                            && feedback.getOrderId().equals(orderId)) {
                        feedbackList.add(feedback);
                    }
                    fetchUserData(feedbackList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFeedBackItemActivity.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProductId(String orderId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(getString(R.string.tbl_order_name));
        List<String> productIds = new ArrayList<>();
        Query query = ordersRef.orderByChild("id").equalTo(orderId);
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Get user data from database
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        DataSnapshot orderDetailsSnapshot = orderSnapshot.child("orderDetails");

                        // Duyệt qua từng mục trong orderDetails
                        for (DataSnapshot detailSnapshot : orderDetailsSnapshot.getChildren()) {
                            String productId = detailSnapshot.child("productId").getValue(String.class);
                            if (productId != null) {
                                productIds.add(productId);
                            }
                        }
                    }
                    if (!productIds.isEmpty()) {
                        for (String productId : productIds) {
                            fetchFeedbacks(productId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchUserData(List<FeedBack> feedbackItems) {
        databaseReference = database.getReference(getString(R.string.tbl_user_name));
        for (FeedBack feedBack : feedbackItems) {
            //Reference to the user table
            databaseReference = database.getReference(getString(R.string.tbl_user_name));
            //Get user data by user Id in feed back
            Query query = databaseReference.orderByChild("id").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        //Get user data from database
                        User user = null;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(User.class);
                        }
                        if (user != null) {
                            binding.tvFeedbackRatingTotal.setVisibility(View.GONE);
                            binding.rbFeedbackAverageRating.setVisibility(View.GONE);
                            binding.tvFeedbackRatingValue.setVisibility(View.GONE);
                            binding.tvFeedbackTitle.setText("My Feedbacks");
                            feedbackAdapter = new UserFeedBackListAdapter(feedbackItems, user);
                            binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(ViewFeedBackItemActivity.this, RecyclerView.VERTICAL, true));
                            binding.rcvFeedback.setAdapter(feedbackAdapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getIntend() {
        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null) {
            Log.e("ViewFeedBackItemActivity", "Null. Please pass a valid order ID.");
            // Hiển thị thông báo lỗi hoặc kết thúc activity nếu cần
            finish();
        }
    }
}
