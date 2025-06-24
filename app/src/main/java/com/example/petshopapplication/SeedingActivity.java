package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.model.FeedBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeedingActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private String productId;
    private String orderId = "0";
    private String userId = "CnuZxJdbenOp1snTnBu9k8eBlZF3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        getIntend();

        // Handle feedback submission
        uploadFeedback(productId);
    }

    private void uploadFeedback(String productId) {
        final String comment = "Absolute amazing product ever seen in my life";
        final int rating = 5;
        submitFeedback(userId, productId, orderId, comment, rating, null);
    }

    private void submitFeedback(String userId, String productId, String orderId, String comment, int rating, @Nullable String imageUrl) {
        DatabaseReference feedbackRef = firebaseDatabase.getReference(getString(R.string.tbl_feedback_name));

        String feedbackId = "feedback-" + feedbackRef.push().getKey(); // Generate a unique ID

        // Create a feedback object
        FeedBack feedback = FeedBack.builder()
                .id(feedbackId)
                .userId(userId)
                .productId(productId)
                .orderId(orderId)
                .rating(rating)
                .imageUrl(imageUrl)
                .content(comment)
                .createdAt(String.valueOf(new Date()))
                .build();

        // Store the feedback in the database
        feedbackRef.child(feedbackId).setValue(feedback)
                .addOnSuccessListener(aVoid -> Toast.makeText(SeedingActivity.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SeedingActivity.this, "Failed to submit feedback.", Toast.LENGTH_SHORT).show());
        finish();
    }

    private void getIntend() {
        productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Log.e("SeedingActivity", "Null. Please pass a valid order ID.");
            // Hiển thị thông báo lỗi hoặc kết thúc activity nếu cần
            finish();
        }
    }


}