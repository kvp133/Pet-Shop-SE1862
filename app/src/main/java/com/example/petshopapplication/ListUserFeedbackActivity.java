package com.example.petshopapplication;

import android.app.Activity;
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

public class ListUserFeedbackActivity extends AppCompatActivity {

    private ActivityListFeedbackBinding binding;  // ViewBinding reference
    private UserFeedBackListAdapter feedbackAdapter;
    private List<FeedBack> feedbackList;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewBinding
        binding = ActivityListFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up RecyclerView
        binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(this));

        //get userId
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        // Set title for feedback list
        binding.tvFeedbackTitle.setText("My Feedbacks");
        binding.tvFeedbackRatingValue.setVisibility(View.GONE);
        binding.rbFeedbackAverageRating.setVisibility(View.GONE);
        binding.tvFeedbackRatingTotal.setVisibility(View.GONE);

        feedbackList = new ArrayList<>();
        binding.rcvFeedback.setAdapter(feedbackAdapter);
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(getString(R.string.tbl_feedback_name));

        // Fetch feedbacks from Firebase
        fetchFeedbacks();
    }

    private void fetchFeedbacks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                    if (feedback != null && !feedback.isDeleted() && feedback.getUserId().equals(userId)) {
                        feedbackList.add(feedback); // Add feedback to the list
                    }
                }
                fetchUserData(feedbackList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListUserFeedbackActivity.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
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
                    User user = null;
                    if (snapshot.exists()) {
                        //Get user data from database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(User.class);
                        }
                        if (user != null) {
                            feedbackAdapter = new UserFeedBackListAdapter(feedbackItems, user);
                            binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(ListUserFeedbackActivity.this, RecyclerView.VERTICAL, true));
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
}
