package com.example.petshopapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.databinding.ActivityAddFeedbackBinding;
import com.example.petshopapplication.databinding.ActivityListFeedbackBinding;
import com.example.petshopapplication.model.FeedBack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class UpdateFeedbackActivity extends AppCompatActivity {
    private ActivityAddFeedbackBinding binding;
    private Uri selectedImageUri;
    private FeedBack feedback;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference feedbackRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;

    private boolean isImageRemoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewBinding
        binding = ActivityAddFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Get the feedback object passed from FeedBackListAdapter
        feedback = (FeedBack) getIntent().getSerializableExtra("feedback");

        // Handle image selection
        binding.btnFeedbackPick.setOnClickListener(view -> {
            chooseImage();
        });

        // Handle removing the image
        binding.btnFeedbackRemoveImage.setOnClickListener(view -> {
            binding.imvFeedbackImage.setImageResource(R.drawable.icon); // Set placeholder or default image
            selectedImageUri = null; // Clear any new image selection
            isImageRemoved = true; // Mark that the image has been removed
            binding.btnFeedbackRemoveImage.setVisibility(View.GONE); // Hide the remove button
        });

        // Handle feedback submission
        binding.btnFeedbackSubmit.setOnClickListener(view -> {
            updateFeedbackInDatabase();
        });

        // Populate the UI with existing feedback data
        if (feedback != null) {
            binding.edtFeedbackComment.setText(feedback.getContent());
            binding.rbFeedbackRating.setRating(feedback.getRating());
            // Load image using Glide if available
            if (feedback.getImageUrl() != null && !feedback.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(feedback.getImageUrl())
                        .placeholder(R.drawable.icon)
                        .into(binding.imvFeedbackImage);

                // Show "Remove Image" button if an image exists
                binding.btnFeedbackRemoveImage.setVisibility(View.VISIBLE);
            }
        }

        // Handle feedback update submission
        binding.btnFeedbackSubmit.setOnClickListener(view -> {
            updateFeedbackInDatabase();
        });

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
            isImageRemoved = false;
            binding.btnFeedbackRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    private void updateFeedbackInDatabase() {
        // Get updated values from the user input
        String updatedComment = binding.edtFeedbackComment.getText().toString();
        float updatedRating = binding.rbFeedbackRating.getRating();

        // Update feedback object with new data
        feedback.setContent(updatedComment);
        feedback.setRating((int) updatedRating);

        // Check if a new image has been selected
        if (isImageRemoved) {
            // If the image is removed, set the image URL to null or an empty string
            saveFeedbackToDatabase("");
        } else if (selectedImageUri != null) {
            // If a new image is selected, upload it and save feedback
            uploadImageAndSaveFeedback();
        } else {
            // No image change, just update feedback with the current image URL
            saveFeedbackToDatabase(feedback.getImageUrl());
        }
    }

    private void uploadImageAndSaveFeedback() {
        // Create a storage reference for the new image
        StorageReference storageReference = firebaseStorage.getReference().child("feedback_images/" + UUID.randomUUID().toString());
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
            // Get the download URL after upload
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                // Save feedback with new image URL
                saveFeedbackToDatabase(imageUrl);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateFeedbackActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveFeedbackToDatabase(String imageUrl) {
        // Update feedback object with new image URL
        feedback.setImageUrl(imageUrl);

        // Save the updated feedback in Firebase
        feedbackRef = firebaseDatabase.getReference("feedbacks").child(feedback.getId());
        feedbackRef.setValue(feedback).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UpdateFeedbackActivity.this, "Feedback updated successfully", Toast.LENGTH_SHORT).show();
                finish();  // Close activity and go back
            } else {
                Toast.makeText(UpdateFeedbackActivity.this, "Failed to update feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
