package com.example.petshopapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityAddProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private Uri selectedImageUri;
    private List<String> categoryList = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupCategoryDropdown();
        setupImagePicker();
        setupAddButton();
    }

    private void setupToolbar() {
        binding.imvGoBack.setOnClickListener(v -> finish());
    }

    private void setupCategoryDropdown() {
        AutoCompleteTextView categoryDropdown = binding.addProductCategory;

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        binding.prgHomeCategory2.setVisibility(View.VISIBLE);

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String categoryName = data.child("name").getValue(String.class);
                    if (categoryName != null) {
                        categoryList.add(categoryName);
                    }
                }
                categoryAdapter = new ArrayAdapter<String>(
                        AddProductActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryList
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.black)); // hoặc R.color.your_color
                        return view;
                    }
                };
                categoryDropdown.setAdapter(categoryAdapter);
                categoryDropdown.setThreshold(1);
                binding.prgHomeCategory2.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
                binding.prgHomeCategory2.setVisibility(View.GONE);
            }
        });
    }

    private void setupImagePicker() {
        binding.addProductUploadImage.setOnClickListener(v -> pickImageFromGallery());
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            binding.addProductUploadImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void setupAddButton() {
        binding.addProductButton.setOnClickListener(v -> {
            String name = binding.addProductName.getText().toString().trim();
            String category = binding.addProductCategory.getText().toString().trim();
            String price = binding.addProductBasePrice.getText().toString().trim();
            String discount = binding.addProductDiscount.getText().toString().trim();
            String description = binding.addProductDescription.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.prgHomeCategory2.setVisibility(View.VISIBLE);

            // TODO: Push product to Firebase or server
            Toast.makeText(this, "Product submitted", Toast.LENGTH_SHORT).show();
            binding.prgHomeCategory2.setVisibility(View.GONE);
        });
    }
}
