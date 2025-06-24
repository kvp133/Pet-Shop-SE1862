package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.Adapter.FeedBackListAdapter;
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

public class ListFeedbackActivity extends AppCompatActivity {

    private ActivityListFeedbackBinding binding;  // ViewBinding reference
    private FeedBackListAdapter feedbackAdapter;
    private List<FeedBack> feedbackList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    User currentUser;
    private String productId;
    private String userId;
    String role = "a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewBinding
        binding = ActivityListFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up RecyclerView
        binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(this));

        feedbackList = new ArrayList<>();
        binding.rcvFeedback.setAdapter(feedbackAdapter);
        binding.btnBack.setOnClickListener(v -> {
            setIntent(new Intent());
            finish();
        });

        //get current user
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        // Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(getString(R.string.tbl_feedback_name));

        //logout
        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Fetch feedbacks from Firebase
        getIntend();
        fetchFeedbacks();
    }

    private void fetchFeedbacks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                int totalRating = 0;
                int feedbackCount = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                    if (feedback != null && !feedback.isDeleted() && feedback.getProductId().equals(productId)) {
                        feedbackList.add(feedback); // Add feedback to the list
                        totalRating += feedback.getRating(); // Sum up ratings
                        feedbackCount++;
                    }
                    if (feedback != null && role != null && feedback.isDeleted() && feedback.getProductId().equals(productId)){
                        feedbackList.add(feedback);
                    }
                }

                if (feedbackCount > 0) {
                    double averageRating = (double) totalRating / feedbackCount;
                    String formattedRating = String.format("%.1f", averageRating);
                    binding.tvFeedbackRatingValue.setText(formattedRating);
                    binding.rbFeedbackAverageRating.setRating((float) averageRating);
                }

                fetchUserData(feedbackList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListFeedbackActivity.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserData(List<FeedBack> feedbackItems) {
        databaseReference = database.getReference(getString(R.string.tbl_user_name));
        List<User> userItems = new ArrayList<>();
        for(FeedBack feedBack : feedbackItems) {
            //Reference to the user table
            databaseReference = database.getReference(getString(R.string.tbl_user_name));
            //Get user data by user Id in feed back
            Query query = databaseReference.orderByChild("id").equalTo(feedBack.getUserId());
            query.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        //Get user data from database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            userItems.add(user);
                            if (user.getId().equals(userId)) currentUser = user;
                        }
                        if(!userItems.isEmpty()) {
                            feedbackAdapter = new FeedBackListAdapter(feedbackItems, userItems, currentUser);
                            binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(ListFeedbackActivity.this, RecyclerView.VERTICAL, true));
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
        productId = getIntent().getStringExtra("productId");
    }
}