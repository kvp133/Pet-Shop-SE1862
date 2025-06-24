package com.example.petshopapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ListCategoryAdapter;
import com.example.petshopapplication.databinding.ActivityAddCategoryBinding;
import com.example.petshopapplication.databinding.ActivityAddFeedbackBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.FeedBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity {
    private ActivityAddCategoryBinding binding;
    private Uri selectedImageUri; // To store the selected image URI
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Initialize ViewBinding
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //Handle back button
        binding.btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(AddCategoryActivity.this, CategoryListActivity.class);
            startActivity(intent);
        });

        // Handle image selection
        binding.btnCategoryImagePick.setOnClickListener(view -> chooseImage());

        // Handle category submit submission
        binding.btnCategorySubmit.setOnClickListener(view -> uploadCategory());

        //Handle max length of category name (max is 20)
        binding.edtCategoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.edtCategoryName.getText().toString().trim().length() > 20) {
                    Toast.makeText(AddCategoryActivity.this, "Max length of category name is 20!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
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
            binding.imvCategoryImage.setImageURI(selectedImageUri); // Display the selected image
        }
    }

    private void uploadCategory() {
        final String categoryName = binding.edtCategoryName.getText().toString().trim();

        //Check category name is empty or not
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Please provide a name for category", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check if category name is exceed 20
        if(categoryName.length() > 20){
            Toast.makeText(AddCategoryActivity.this, "Max length of category name is 20!", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check category name is existed or not
        getListCategory(categories -> {
            boolean isDuplicate = false;

            for (Category category : categories) {
                if (categoryName.equals(category.getName())) {
                    Toast.makeText(AddCategoryActivity.this, "Category name has existed!", Toast.LENGTH_SHORT).show();
                    isDuplicate = true;
                    break;
                }
            }
            if(!isDuplicate) {
                if (selectedImageUri != null) {
                    // Upload the image to Firebase Storage
                    StorageReference storageReference = firebaseStorage.getReference().child("category_image/" + UUID.randomUUID().toString());
                    storageReference.putFile(selectedImageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Get the image download URL
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    // After image upload, submit the feedback to the database
                                    addCategory(categoryName, imageUrl);
                                });
                            })
                            .addOnFailureListener(e -> Toast.makeText(AddCategoryActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
                } else {
                    // If no image is selected, inform to user
                    Toast.makeText(AddCategoryActivity.this, "Please choose an image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addCategory(String categoryName, String imageUrl) {
        DatabaseReference categoryRef = firebaseDatabase.getReference("categories");

        String categoryId = "category" + categoryRef.push().getKey(); // Generate a unique ID

        // Create a category object
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .image(imageUrl)
                .createdAt(String.valueOf(new Date()))
                .deleted(false)
                .build();

        // Store the category in the database
        categoryRef.child(categoryId).setValue(category)
                .addOnSuccessListener(aVoid -> Toast.makeText(AddCategoryActivity.this, "Category added successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AddCategoryActivity.this, "Failed to add category.", Toast.LENGTH_SHORT).show());
        Intent intent = new Intent(AddCategoryActivity.this, CategoryListActivity.class);
        startActivity(intent);
    }

    private void getListCategory(CategoryListCallback callback) {
        reference = firebaseDatabase.getReference(getString(R.string.tbl_category_name));

        reference.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Category category = dataSnapshot.getValue(Category.class);
                        categories.add(category);
                    }
                }
                callback.onCategoryListLoaded(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCategoryListLoaded(new ArrayList<>());
            }
        });
    }

    public interface CategoryListCallback {
        void onCategoryListLoaded(List<Category> categories);
    }

}
