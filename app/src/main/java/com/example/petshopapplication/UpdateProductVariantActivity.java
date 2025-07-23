package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ColorAdapter;
import com.example.petshopapplication.Adapter.ItemModel;
import com.example.petshopapplication.Adapter.ManageSizeAdapter;
import com.example.petshopapplication.Adapter.VariantAdapter;
import com.example.petshopapplication.databinding.ActivityUpdateProductVariantBinding;
import com.example.petshopapplication.databinding.PopUpAddVariantDimnesionBinding;
import com.example.petshopapplication.databinding.PopUpAddVariantSizeColorBinding;
import com.example.petshopapplication.model.Color;
import com.example.petshopapplication.model.Dimension;
import com.example.petshopapplication.model.ObjectPrinter;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Size;
import com.example.petshopapplication.model.Variant;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class UpdateProductVariantActivity extends AppCompatActivity implements ManageSizeAdapter.OnSizeClickEventListener, ColorAdapter.OnColorClickEventListener {

    private Dialog dialog_dimension;
    private Dialog dialog; // Dialog for adding size/color
    private Dialog dialog2; // Dialog for adding new size
    private ActivityUpdateProductVariantBinding binding;
    private PopUpAddVariantSizeColorBinding binding2; // Binding for PopUpAddVariantSizeColor dialog
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    //    private Uri selectedImageUri;
    private Dimension currentDimension = null;
    private List<Variant> variants = new ArrayList<>();
    private Product model;
    private String oldProductId;
    private Size currentSize = null;
    private Color currentColor = null;

    // Executor for background tasks and Handler for UI updates
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityUpdateProductVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Retrieve product data from Intent
        model = (Product) getIntent().getSerializableExtra("product");
        oldProductId = getIntent().getStringExtra("product_old");

        if (model == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu sản phẩm để cập nhật.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("UpdateProductVariant", "Old Product ID: " + (oldProductId != null ? oldProductId : "N/A"));
        Log.d("UpdateProductVariant", "Product Name: " + model.getName());

        // Set product name and base price
        binding.addPvName.setText(model.getName());
        binding.addPvImportPrice.setText(String.valueOf(model.getBasePrice()));

        // Initialize variants list from the product model
        variants = model.getListVariant();
        if (variants == null) {
            variants = new ArrayList<>(); // Ensure variants list is not null
        }
        // If variants exist, set the initial dimension from the first variant
        if (!variants.isEmpty() && variants.get(0).getDimension() != null) {
            currentDimension = variants.get(0).getDimension();
        }

        // Set up click listeners for UI elements
        binding.imvGoBack.setOnClickListener(v -> finish());
        binding.addPvButtonAddSize.setOnClickListener(view -> showAddSize());
        binding.addPvDimension.setOnClickListener(view -> showAddDimension());
        binding.addPvButton.setOnClickListener(view -> addVariant());

        // Initialize the RecyclerView displaying current variants
        initVariants();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dismiss any active dialogs to prevent WindowLeaked exceptions
        if (dialog_dimension != null && dialog_dimension.isShowing()) {
            dialog_dimension.dismiss();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (dialog2 != null && dialog2.isShowing()) {
            dialog2.dismiss();
        }
    }

    private void addVariant() {
        if (currentDimension == null) {
            Toast.makeText(this, "Vui lòng chọn kích thước (Dimension) cho sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If no size/color variants were added, create a base variant with just dimension
        if (variants.isEmpty()) {
            Variant baseVariant = new Variant();
            baseVariant.setId(UUID.randomUUID().toString());
            baseVariant.setDimension(currentDimension);
            baseVariant.setSize(null); // No specific size
            baseVariant.setListColor(new ArrayList<>()); // No specific colors
            baseVariant.setStock(0); // Default stock
            baseVariant.setPrice(model.getBasePrice());
            baseVariant.setDeliveringQuantity(0);
            baseVariant.setDeleted(false);
            variants.add(baseVariant);
        } else {
            // Apply current dimension and base price to all existing variants
            for (Variant variant : variants) {
                variant.setDimension(currentDimension);
                variant.setPrice(model.getBasePrice());
            }
        }
        model.setListVariant(variants); // Update the product model with the modified variants list

        DatabaseReference productRef = database.getReference("products");
        String newProductId = "product-" + productRef.push().getKey(); // Generate a new unique ID for the updated product
        model.setId(newProductId);

        // Save the new/updated product to Firebase
        productRef.child(newProductId).setValue(model)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();

                    // Mark the old product as deleted (soft delete)
                    if (oldProductId != null && !oldProductId.isEmpty()) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("deleted", true);
                        productRef.child(oldProductId).updateChildren(updates)
                                .addOnSuccessListener(task -> Log.d("Firebase", "Đã đánh dấu sản phẩm cũ là bị xóa."))
                                .addOnFailureListener(e -> Log.e("Firebase", "Không thể đánh dấu sản phẩm cũ là bị xóa: " + e.getMessage()));
                    }

                    finish(); // Go back to previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật sản phẩm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseUpdate", "Error updating product: " + e.getMessage());
                });
    }

    private void initVariants() {
        Log.d("UpdateProductVariant", "Initializing variants for RecyclerView.");

        // Adjust visibility of stock input and RecyclerView based on variant presence
        if (variants != null && !variants.isEmpty()) {
            binding.addPvStock.setEnabled(false);
            binding.addPvStock.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.GONE);
            binding.recyclerView2.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView2.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.VISIBLE);
            binding.addPvStock.setEnabled(true);
            binding.addPvStock.setVisibility(View.VISIBLE);
        }

        // Set up RecyclerView
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(this));

        // Prepare data for the VariantAdapter
        List<ItemModel> itemList = new ArrayList<>();
        if (variants != null) {
            for (Variant variant : variants) {
                if (variant.getListColor() != null) {
                    for (Color color : variant.getListColor()) {
                        itemList.add(new ItemModel(
                                variant.getSize() != null ? variant.getSize().getName() : "Không kích thước",
                                R.drawable.arrow, // Placeholder image for now
                                color.getName(),
                                color.getStock()
                        ));
                    }
                }
            }
        }
        // Initialize and set the adapter
        VariantAdapter adapter = new VariantAdapter(this, itemList);
        binding.recyclerView2.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            selectedImageUri = data.getData();
//            if (binding2 != null) {
//                binding2.addProductUploadImage.setImageURI(selectedImageUri);
//            }
//        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityIfNeeded(intent, 100);
    }

    private void showAddDimension() {
        dialog_dimension = new Dialog(UpdateProductVariantActivity.this);
        PopUpAddVariantDimnesionBinding bindingDimension = PopUpAddVariantDimnesionBinding.inflate(getLayoutInflater());
        dialog_dimension.setContentView(bindingDimension.getRoot());
        dialog_dimension.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_dimension.setCancelable(true);

        // Pre-fill dimension fields if a dimension already exists
        if (currentDimension != null) {
            bindingDimension.editHeight.setText(String.valueOf(currentDimension.getHeight()));
            bindingDimension.editWidth.setText(String.valueOf(currentDimension.getWidth()));
            bindingDimension.editLength.setText(String.valueOf(currentDimension.getLength()));
            bindingDimension.editWeight.setText(String.valueOf(currentDimension.getWeight()));
        }

        bindingDimension.btnSubmitDimension.setOnClickListener(view -> {
            try {
                int height = Integer.parseInt(bindingDimension.editHeight.getText().toString());
                int length = Integer.parseInt(bindingDimension.editLength.getText().toString());
                int width = Integer.parseInt(bindingDimension.editWidth.getText().toString());
                int weight = Integer.parseInt(bindingDimension.editWeight.getText().toString());

                if (height <= 0 || length <= 0 || width <= 0 || weight <= 0) {
                    Toast.makeText(UpdateProductVariantActivity.this, "Kích thước và trọng lượng phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentDimension = new Dimension(height, length, width, weight);
                Toast.makeText(UpdateProductVariantActivity.this, "Đã lưu kích thước sản phẩm.", Toast.LENGTH_SHORT).show();
                dialog_dimension.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(UpdateProductVariantActivity.this, "Vui lòng nhập số hợp lệ cho các trường kích thước.", Toast.LENGTH_SHORT).show();
                Log.e("DimensionInput", "Parsing error: " + e.getMessage());
            }
        });
        dialog_dimension.show();
    }

    private void showAddSize() {
        dialog = new Dialog(UpdateProductVariantActivity.this);
        binding2 = PopUpAddVariantSizeColorBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding2.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        resetQuantity(); // This also calls resetImage()

        dialog.show();

        initSize(dialog);
        initColor(dialog);

//        binding2.addProductUploadImage.setOnClickListener(view -> chooseImage());

        binding2.btnSubmit.setOnClickListener(view -> {
            if (currentSize == null || currentColor == null) {
                Toast.makeText(UpdateProductVariantActivity.this, "Vui lòng chọn kích thước và màu sắc.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (binding2.editTextText.getText().toString().isEmpty()) {
                Toast.makeText(UpdateProductVariantActivity.this, "Vui lòng nhập số lượng tồn kho.", Toast.LENGTH_SHORT).show();
                return;
            }
            int stockQuantity = Integer.parseInt(binding2.editTextText.getText().toString());
            if (stockQuantity < 0) {
                Toast.makeText(UpdateProductVariantActivity.this, "Số lượng tồn kho không thể âm.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(UpdateProductVariantActivity.this, "Đang xử lý. Vui lòng đợi...", Toast.LENGTH_SHORT).show();
            binding2.btnSubmit.setEnabled(false);

            CountDownLatch latch = new CountDownLatch(1);

//            executor.execute(() -> {
//                String imageUrl = null;
//                boolean isImageUploaded = false;
//
//                try {
//                    if (selectedImageUri != null) {
//                        if (!selectedImageUri.toString().startsWith("http")) { // New local URI, needs upload
//                            StorageReference storageReference = firebaseStorage.getReference().child("product_images/" + UUID.randomUUID().toString());
//                            storageReference.putFile(selectedImageUri)
//                                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
//                                        handler.post(() -> {
//                                            handleVariantUpdateLogic(uri.toString(), stockQuantity);
//                                            latch.countDown();
//                                        });
//                                    }))
//                                    .addOnFailureListener(e -> {
//                                        Log.e("FirebaseStorage", "Image upload failed: " + e.getMessage());
//                                        handler.post(() -> {
//                                            Toast.makeText(UpdateProductVariantActivity.this, "Tải ảnh lên thất bại.", Toast.LENGTH_SHORT).show();
//                                            binding2.btnSubmit.setEnabled(true);
//                                            latch.countDown();
//                                        });
//                                    });
//                            isImageUploaded = true;
//                        } else { // Already a Firebase URL
//                            imageUrl = selectedImageUri.toString();
//                        }
//                    }
//
//                    if (!isImageUploaded) { // If no image upload happened (either no image or already Firebase URL)
//                        handleVariantUpdateLogic(imageUrl, stockQuantity);
//                        latch.countDown(); // Release latch immediately
//                    }
//
//                    latch.await();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    Log.e("UpdateProductVariant", "Thread interrupted during variant update: " + e.getMessage());
//                    handler.post(() -> {
//                        Toast.makeText(UpdateProductVariantActivity.this, "Đã xảy ra lỗi nội bộ.", Toast.LENGTH_SHORT).show();
//                        binding2.btnSubmit.setEnabled(true);
//                    });
//                }
//            });
        });

        binding2.btnAddSize.setOnClickListener(view -> {
            dialog2 = new Dialog(UpdateProductVariantActivity.this);
            dialog2.setContentView(R.layout.pop_up_add_variant_size);
            dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog2.setCancelable(true);
            dialog2.show();

            Button btnConfirm = dialog2.findViewById(R.id.btn_submit_name);
            EditText txtName = dialog2.findViewById(R.id.edit_size);
            // XÓA DÒNG NÀY: Button btnCancel = dialog2.findViewById(R.id.btn_cancel); // Không tồn tại trong XML
            // XÓA KHỐI NÀY: if (btnCancel != null) { btnCancel.setOnClickListener(v -> dialog2.dismiss()); } // Không tồn tại

            btnConfirm.setOnClickListener(v -> {
                String sizeName = txtName.getText().toString().trim();
                if (!sizeName.isEmpty()) {
                    DatabaseReference ref = database.getReference("Size");
                    String id = "size-" + ref.push().getKey();
                    Size newSize = new Size(id, sizeName);
                    ref.child(id).setValue(newSize)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UpdateProductVariantActivity.this, "Thêm kích thước thành công!", Toast.LENGTH_SHORT).show();
                                dialog2.dismiss();
                                initSize(dialog);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UpdateProductVariantActivity.this, "Thêm kích thước thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("FirebaseAddSize", "Error adding size: " + e.getMessage());
                                dialog2.dismiss();
                            });
                } else {
                    Toast.makeText(UpdateProductVariantActivity.this, "Vui lòng nhập tên kích thước.", Toast.LENGTH_SHORT).show();
                }
            });

            // Nếu bạn muốn nút hủy, bạn cần thêm nó vào pop_up_add_variant_size.xml
            // Nếu không, người dùng chỉ có thể dùng nút back hoặc Add để đóng dialog
        });
    }

    /**
     * Logic to update or add a variant based on selected size, color, and stock.
     * This method is called from a background thread.
     */
    private void handleVariantUpdateLogic(String imageUrl, int stockQuantity) {
        // SỬ DỤNG LOMBOK BUILDER ĐỂ KHỞI TẠO ĐỐI TƯỢNG COLOR
        Color newColorData = Color.builder()
                .id(currentColor.getId())
                .name(currentColor.getName())
                .imageUrl(imageUrl)
                .stock(stockQuantity)
                .deliveringQuantity(0) // Giá trị mặc định
                .createdAt(String.valueOf(System.currentTimeMillis())) // Giá trị mặc định
                .isDeleted(false) // Giá trị mặc định
                .build();

        Variant existingVariantForSize = variants.stream()
                .filter(v -> v.getSize() != null && v.getSize().getId().equals(currentSize.getId()))
                .findFirst()
                .orElse(null);

        if (existingVariantForSize == null) {
            Variant newVariant = new Variant();
            newVariant.setId(UUID.randomUUID().toString());
            newVariant.setSize(currentSize);
            newVariant.setDimension(currentDimension);
            newVariant.setPrice(model.getBasePrice());
            newVariant.setDeliveringQuantity(0);
            newVariant.setDeleted(false);
            newVariant.setListColor(new ArrayList<>(List.of(newColorData)));
            newVariant.setStock(stockQuantity);
            variants.add(newVariant);
            Log.d("VariantUpdate", "Created new variant for size: " + currentSize.getName());
        } else {
            List<Color> colorsInExistingVariant = new ArrayList<>(existingVariantForSize.getListColor() != null ? existingVariantForSize.getListColor() : new ArrayList<>());
            boolean colorFound = false;

            for (int i = 0; i < colorsInExistingVariant.size(); i++) {
                Color c = colorsInExistingVariant.get(i);
                if (c.getId().equals(newColorData.getId())) {
                    colorsInExistingVariant.set(i, newColorData);
                    colorFound = true;
                    break;
                }
            }

            if (!colorFound) {
                colorsInExistingVariant.add(newColorData);
            }

            existingVariantForSize.setListColor(colorsInExistingVariant);
            int totalStockForVariant = colorsInExistingVariant.stream().mapToInt(Color::getStock).sum();
            existingVariantForSize.setStock(totalStockForVariant);

            Log.d("VariantUpdate", "Updated variant for size: " + currentSize.getName() + ", new total stock: " + totalStockForVariant);
        }

        handler.post(() -> {
            Toast.makeText(UpdateProductVariantActivity.this, "Đã cập nhật biến thể sản phẩm.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            initVariants();
            binding2.btnSubmit.setEnabled(true);
            resetQuantity();
        });
    }


    private void initSize(Dialog dialog) {
        List<Size> sizeItems = new ArrayList<>();
        DatabaseReference sizeRef = database.getReference("Size");

        sizeRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sizeItems.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Size size = dataSnapshot.getValue(Size.class);
                    if (size != null) {
                        sizeItems.add(size);
                    }
                }
                RecyclerView rcvSize = dialog.findViewById(R.id.rcv_size);
                if (rcvSize != null) {
                    ManageSizeAdapter sizeAdapter = new ManageSizeAdapter(sizeItems, UpdateProductVariantActivity.this);
                    rcvSize.setLayoutManager(new GridLayoutManager(UpdateProductVariantActivity.this, 2));
                    rcvSize.setAdapter(sizeAdapter);
                } else {
                    Log.e("initSize", "RecyclerView with ID rcv_size not found in dialog layout.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseSize", "Failed to load sizes: " + error.getMessage());
                Toast.makeText(UpdateProductVariantActivity.this, "Không thể tải danh sách kích thước.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSizeClickEvent(Size size) {
        currentSize = size;
        updateAddVariantPopupFields();
    }

    private void initColor(Dialog dialog) {
        List<Color> colorItems = new ArrayList<>();
        DatabaseReference colorRef = database.getReference("Colors");

        colorRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                colorItems.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Color color = dataSnapshot.getValue(Color.class);
                    if (color != null) {
                        colorItems.add(color);
                    }
                }

                // Nếu không có màu sắc nào trong Firebase, thêm một số màu mẫu
                if (colorItems.isEmpty()) {
                    Log.w("initColor", "No colors found in Firebase, adding sample colors.");
                    // SỬ DỤNG LOMBOK BUILDER ĐỂ KHỞI TẠO ĐỐI TƯỢNG COLOR MẪU
                    Color sampleColor1 = Color.builder()
                            .id("color-sample-1")
                            .name("Red")
                            .imageUrl("https://firebasestorage.googleapis.com/v0/b/pet-shop-a220c.appspot.com/o/sample_red.png?alt=media")
                            .stock(0)
                            .deliveringQuantity(0)
                            .isDeleted(false)
                            .createdAt(String.valueOf(System.currentTimeMillis()))
                            .build();

                    Color sampleColor2 = Color.builder()
                            .id("color-sample-2")
                            .name("Blue")
                            .imageUrl("https://firebasestorage.googleapis.com/v0/b/pet-shop-a220c.appspot.com/o/sample_blue.png?alt=media")
                            .stock(0)
                            .deliveringQuantity(0)
                            .isDeleted(false)
                            .createdAt(String.valueOf(System.currentTimeMillis()))
                            .build();

                    colorItems.add(sampleColor1);
                    colorItems.add(sampleColor2);
                }

                RecyclerView rcvColor = dialog.findViewById(R.id.rcv_color);
                if (rcvColor != null) {
                    ColorAdapter colorAdapter = new ColorAdapter(colorItems, UpdateProductVariantActivity.this);
                    rcvColor.setLayoutManager(new GridLayoutManager(UpdateProductVariantActivity.this, 1));
                    rcvColor.setAdapter(colorAdapter);
                } else {
                    Log.e("initColor", "RecyclerView with ID rcv_color not found in dialog layout.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseColor", "Failed to load colors: " + error.getMessage());
                Toast.makeText(UpdateProductVariantActivity.this, "Không thể tải danh sách màu sắc.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onColorClick(Color color) {
        currentColor = color;
        updateAddVariantPopupFields();
    }

    private void updateAddVariantPopupFields() {
        if (currentSize != null && currentColor != null && binding2 != null) {
            Variant matchingVariant = variants.stream()
                    .filter(v -> v.getSize() != null && v.getSize().getId().equals(currentSize.getId()))
                    .findFirst()
                    .orElse(null);

            if (matchingVariant != null && matchingVariant.getListColor() != null) {
                Color existingColorInVariant = matchingVariant.getListColor().stream()
                        .filter(c -> c.getId().equals(currentColor.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingColorInVariant != null) {
                    binding2.editTextText.setText(String.valueOf(existingColorInVariant.getStock()));
//                    if (existingColorInVariant.getImageUrl() != null && !existingColorInVariant.getImageUrl().isEmpty()) {
//                        selectedImageUri = Uri.parse(existingColorInVariant.getImageUrl());
//                        executor.execute(() -> {
//                            Bitmap bitmap = getBitmapFromURL(selectedImageUri.toString());
//                            handler.post(() -> {
//                                if (bitmap != null) {
//                                    binding2.addProductUploadImage.setImageBitmap(bitmap);
//                                } else {
//                                    resetImage();
//                                }
//                            });
//                        });
//                    } else {
//                        resetImage();
//                    }
                } else {
                    resetQuantity();
                }
            } else {
                resetQuantity();
            }
        } else {
            resetQuantity();
        }
    }

    public void resetQuantity() {
        if (binding2 != null) {
            binding2.editTextText.setText("0");
//            resetImage();
        }
    }

//    public void resetImage() {
//        if (binding2 != null) {
//            selectedImageUri = null;
//            binding2.addProductUploadImage.setImageURI(null);
//            binding2.addProductUploadImage.setImageBitmap(null);
//            binding2.addProductUploadImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.upload_img));
//        }
//    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (MalformedURLException e) {
            Log.e("BitmapLoad", "Malformed URL: " + src + ", Error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("BitmapLoad", "IO Error loading bitmap from URL: " + src + ", Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e("BitmapLoad", "Generic error loading bitmap from URL: " + src + ", Error: " + e.getMessage());
            return null;
        }
    }
}