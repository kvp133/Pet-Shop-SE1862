package com.example.petshopapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityAddFeedbackBinding;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.utils.Validate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddFeedbackActivity extends AppCompatActivity {
    private ActivityAddFeedbackBinding binding;
    private Uri selectedImageUri; // To store the selected image URI
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private String userId;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Initialize ViewBinding
        binding = ActivityAddFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //get current user
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        // Handle image selection
        binding.btnFeedbackPick.setOnClickListener(view -> chooseImage());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        getIntend();

        // Handle feedback submission
        binding.btnFeedbackSubmit.setOnClickListener(v -> getProductId(orderId));
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityIfNeeded(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Save the selected image URI
            binding.imvFeedbackImage.setImageURI(selectedImageUri); // Display the selected image
        }
    }

    private void uploadFeedback(String productId) {
        final String comment = binding.edtFeedbackComment.getText().toString();
        final int rating = (int) binding.rbFeedbackRating.getRating();

        String errorMessage = Validate.isValidFeedback(this, comment);
        if(errorMessage != null && !errorMessage.isBlank()) {
            Toast.makeText(AddFeedbackActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            // Upload the image to Firebase Storage
            StorageReference storageReference = firebaseStorage.getReference().child("feedback_images/" + UUID.randomUUID().toString());
            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the image download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // After image upload, submit the feedback to the database
                            submitFeedback(userId, productId, orderId, comment, rating, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddFeedbackActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
        } else {
            // If no image is selected, submit feedback without image
            submitFeedback(userId, productId, orderId, comment, rating, null);
        }
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
                .addOnSuccessListener(aVoid -> Toast.makeText(AddFeedbackActivity.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AddFeedbackActivity.this, "Failed to submit feedback.", Toast.LENGTH_SHORT).show());
        finish();
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

                        // orderDetails
                        for (DataSnapshot detailSnapshot : orderDetailsSnapshot.getChildren()) {
                            String productId = detailSnapshot.child("productId").getValue(String.class);
                            if (productId != null) {
                                System.out.println("getProductId" + productId);
                                productIds.add(productId);
                            }
                        }
                    }
                    if (!productIds.isEmpty()) {
                        for (String productId : productIds) {
                            uploadFeedback(productId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getIntend() {
        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null) {
            Log.e("AddFeedbackActivity", "Null. Please pass a valid order ID.");
            // Hiển thị thông báo lỗi hoặc kết thúc activity nếu cần
            finish();
        }
    }
}