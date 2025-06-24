package com.example.petshopapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.petshopapplication.Adapter.CategoryAdapter;
import com.example.petshopapplication.databinding.ActivityAddFeedbackBinding;
import com.example.petshopapplication.databinding.ActivityAddProductBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.ObjectPrinter;
import com.example.petshopapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class AddProductActivity extends AppCompatActivity {
    private ActivityAddProductBinding binding;
    private Uri selectedImageUri; // To store the selected image URI
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    private Category selectedCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        setContentView(R.layout.activity_add_product);

        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        initCategory();

        // Handle image selection
        binding.addProductButton.setOnClickListener(view -> addProduct());

        // Handle feedback submission
        binding.addProductUploadImage.setOnClickListener(view -> chooseImage());
    }
    private void initCategory() {
        reference = firebaseDatabase.getReference(getString(R.string.tbl_category_name));
        //Display progress bar
        binding.prgHomeCategory2.setVisibility(View.VISIBLE);

        List<Category> categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("deleted").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                        System.out.println(categoryItems.size());
                    }
                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_item, categoryItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.addProductCategory.setAdapter(adapter);
                    binding.addProductCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                             selectedCategory = (Category) parent.getSelectedItem();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.prgHomeCategory2.setVisibility(View.INVISIBLE);

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
            binding.addProductUploadImage.setImageURI(selectedImageUri); // Display the selected image
        }
    }
    @SuppressLint("NewApi")
    private void addProduct() {
        String productName= binding.addProductName.getText().toString();
        String price = binding.addProductBasePrice.getText().toString();
        String discount = binding.addProductDiscount.getText().toString();
        String description = binding.addProductDescription.getText().toString();
        if (productName.isEmpty()
            || price.isEmpty() || discount.isEmpty()
            || description.isEmpty()||selectedCategory == null)
        {
            Toast.makeText(this, "Please enter enough information", Toast.LENGTH_SHORT).show();
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
                            submitProduct(productName,description, imageUrl,discount,price, LocalDateTime.now().toString(),false);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddProductActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
        } else {
            // If no image is selected, submit feedback without image
            submitProduct(productName, description,null,discount,price, LocalDateTime.now().toString(),false);
        }

    }
    private void submitProduct(String name,String description, String image,String discount,String price, String createdAt, boolean isDeleted)
    {
        DatabaseReference productref = firebaseDatabase.getReference("products");
        String productId = "product-" + productref.push().getKey(); // Generate a unique ID

        Product newProduct = new Product();
        newProduct.setId(productId);
        newProduct.setDeleted(false);
        newProduct.setName(name);
        newProduct.setCategoryId(selectedCategory.getId());
        newProduct.setDiscount(Integer.parseInt(discount));
        newProduct.setBasePrice(Double.parseDouble(price));
        newProduct.setListVariant(new ArrayList<>());
        newProduct.setBaseImageURL(image);
        newProduct.setDescription(description);
        System.out.println(ObjectPrinter.print(newProduct));
        Intent intent= new Intent(this, AddProductVariantActivity.class);
        intent.putExtra("product",newProduct);
        startActivity(intent);
    }

}
