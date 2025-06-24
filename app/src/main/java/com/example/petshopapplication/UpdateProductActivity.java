package com.example.petshopapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.databinding.ActivityAddProductBinding;
import com.example.petshopapplication.databinding.ActivityUpdateProductBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UpdateProductActivity extends AppCompatActivity {
    private ActivityUpdateProductBinding binding;
    private Uri selectedImageUri;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    private Category selectedCategory;
    String proid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_update_product);

        binding = ActivityUpdateProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        proid = (String) getIntent().getStringExtra("id");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        initCategory();

        // Handle image selection
        binding.addProductButton.setOnClickListener(view -> addProduct());

        // Handle feedback submission
        binding.addProductUploadImage.setOnClickListener(view -> chooseImage());
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src",src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap","returned");
            return myBitmap;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception",e.getMessage());
            return null;
        }
    }
    Product current = null;
    String old = "";
    private void initProduct()
    {
        reference = firebaseDatabase.getReference("products");

        Query query = reference.orderByChild("id").equalTo(proid);
        try {
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    System.out.println(proid);

                    if (snapshot.exists()) {
                        // Lặp qua các con của snapshot để lấy dữ liệu sản phẩm
                        for (DataSnapshot child : snapshot.getChildren()) {
                            current = child.getValue(Product.class);
                            old = child.getKey();
                        }

                        if (current != null) {
                            binding.addProductName.setText(current.getName());
                            String cate = current.getCategoryId();
                            Category select = categoryItems.stream().filter(x -> x.getId().equals(cate)).findFirst().get();
                            binding.addProductCategory.setSelection(categoryItems.indexOf(select));
                            binding.addProductBasePrice.setText(String.valueOf(current.getBasePrice()));
                            binding.addProductDiscount.setText(String.valueOf(current.getDiscount()));
                            binding.addProductDescription.setText(String.valueOf(current.getDescription()));
                            binding.addProductUploadImage.setImageBitmap(getBitmapFromURL(current.getBaseImageURL()));
                        } else {
                            System.out.println("Failed to convert DataSnapshot to Product.");
                        }

                    } else {
                        System.out.println("No product found with the specified ID.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println(error.getMessage());

                    finish();
                }
            });
        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }

    }
    List<Category> categoryItems = new ArrayList<>();
    private void initCategory() {
        reference = firebaseDatabase.getReference(getString(R.string.tbl_category_name));
        //Display progress bar
        binding.prgHomeCategory2.setVisibility(View.VISIBLE);

         categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("deleted").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                    }
                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(UpdateProductActivity.this, android.R.layout.simple_spinner_item, categoryItems);
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
                    });
                    initProduct();

                }
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
                    .addOnFailureListener(e -> Toast.makeText(UpdateProductActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
        } else {
            // If no image is selected, submit feedback without image
            submitProduct(productName, description,current.getBaseImageURL(),discount,price, LocalDateTime.now().toString(),false);
        }

    }
    private void submitProduct(String name,String description, String image,String discount,String price, String createdAt, boolean isDeleted)
    {
        DatabaseReference productref = firebaseDatabase.getReference("products");
        String productId = "product-" + productref.push().getKey(); // Generate a unique ID
        Intent intent= new Intent(this, UpdateProductVariantActivity.class);
        intent.putExtra("product_old",old);

        current.setId(productId);
        current.setDeleted(false);
        current.setName(name);
        current.setCategoryId(selectedCategory.getId());
        current.setDiscount(Integer.parseInt(discount));
        current.setBasePrice(Double.parseDouble(price));
        current.setBaseImageURL(image);
        current.setDescription(description);
        intent.putExtra("product",current);


        startActivity(intent);

    }

}
