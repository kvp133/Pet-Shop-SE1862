package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.example.petshopapplication.databinding.ActivityAddProductVariantBinding;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AddProductVariantActivity extends AppCompatActivity implements ManageSizeAdapter.OnSizeClickEventListener, ColorAdapter.OnColorClickEventListener {
    private Dialog dialog_dimension;
    private Dialog dialog;
    private Dialog dialog2;
    private ActivityAddProductVariantBinding binding;
    private PopUpAddVariantSizeColorBinding binding2;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private Uri selectedImageUri;
    private Dimension currentDimension = null;
    private Size currentSize = null;
    private Color currentColor = null;
    private List<Variant> variants = new ArrayList<>();
    private Product model;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new VariantAdapter(this, new ArrayList<>()));

        // Get Product from Intent
        model = (Product) getIntent().getSerializableExtra("product");
        if (model == null) {
            Toast.makeText(this, "Thiếu dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Set product name
        binding.addPvName.setText(model.getName());

        // Set click listeners
        binding.imvGoBack.setOnClickListener(v -> finish());
        binding.addPvButtonAddSizeColor.setOnClickListener(v -> showAddSize());
        binding.addPvDimension.setOnClickListener(v -> showAddDimension());
        binding.addPvButton.setOnClickListener(v -> addVariant());

        initVariants();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dismiss dialogs to prevent memory leaks
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        if (dialog2 != null && dialog2.isShowing()) dialog2.dismiss();
        if (dialog_dimension != null && dialog_dimension.isShowing()) dialog_dimension.dismiss();
    }

    private void addVariant() {
        if (currentDimension == null) {
            Toast.makeText(this, "Vui lòng chọn kích thước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (variants.isEmpty()) {
            Variant base = new Variant();
            base.setId(UUID.randomUUID().toString());
            base.setDimension(currentDimension);
            base.setSize(null);
            base.setPrice(model.getBasePrice());
            base.setListColor(new ArrayList<>());
            base.setStock(0);
            base.setDeliveringQuantity(0);
            variants.add(base);
        } else {
            for (Variant variant : variants) {
                variant.setPrice(model.getBasePrice());
                variant.setDimension(currentDimension);
            }
            model.setListVariant(variants);
        }

        DatabaseReference productRef = database.getReference("products");
        // Bạn đang tạo một productId mới ở đây. Nếu bạn đã có product.id từ AddProductActivity, bạn nên dùng nó.
        // product.setId(productId);
        // productRef.child(productId).setValue(model)
        // Nếu model.getId() đã được set từ AddProductActivity, chỉ cần dùng model.getId()
        productRef.child(model.getId()).setValue(model) // Sử dụng ID đã có của model
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    // --- THÊM PHẦN CHUYỂN HƯỚNG VỀ ĐÂY ---
                    Intent intent = new Intent(AddProductVariantActivity.this, AdminDashBoardFragment.class); // Thay bằng Activity bạn muốn quay về
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa các Activity cũ trên stack
                    startActivity(intent);
                    finish(); // Đóng AddProductVariantActivity
                    // ------------------------------------
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lưu sản phẩm thất bại.", Toast.LENGTH_SHORT).show());
    }

    private void initVariants() {
        if (!variants.isEmpty()) {
            binding.addPvStock.setEnabled(false);
            binding.addPvStock.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.GONE);
            binding.recyclerView2.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView2.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.VISIBLE);
        }

        List<ItemModel> itemList = new ArrayList<>();
        for (Variant variant : variants) {
            if (variant.getListColor() != null) {
                for (Color color : variant.getListColor()) {
                    itemList.add(new ItemModel(
                            variant.getSize() != null ? variant.getSize().getName() : "",
                            R.drawable.arrow,
                            color.getName(),
                            color.getStock()
                    ));
                }
            }
        }

        VariantAdapter adapter = new VariantAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            selectedImageUri = data.getData();
//            binding2.addProductUploadImage.setImageURI(selectedImageUri);
//        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    private boolean validateDimensionInputs(PopUpAddVariantDimnesionBinding binding) {
        try {
            int height = Integer.parseInt(binding.editHeight.getText().toString());
            int width = Integer.parseInt(binding.editWidth.getText().toString());
            int length = Integer.parseInt(binding.editLength.getText().toString());
            int weight = Integer.parseInt(binding.editWeight.getText().toString());
            return height > 0 && width > 0 && length > 0 && weight > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAddDimension() {
        dialog_dimension = new Dialog(this);
        PopUpAddVariantDimnesionBinding bindingDimension = PopUpAddVariantDimnesionBinding.inflate(getLayoutInflater());
        dialog_dimension.setContentView(bindingDimension.getRoot());
        dialog_dimension.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (currentDimension != null) {
            bindingDimension.editHeight.setText(String.valueOf(currentDimension.getHeight()));
            bindingDimension.editWidth.setText(String.valueOf(currentDimension.getWidth()));
            bindingDimension.editLength.setText(String.valueOf(currentDimension.getLength()));
            bindingDimension.editWeight.setText(String.valueOf(currentDimension.getWeight()));
        }

        Button btnSubmit = bindingDimension.btnSubmitDimension;
        btnSubmit.setOnClickListener(v -> {
            if (!validateDimensionInputs(bindingDimension)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ và đúng định dạng kích thước", Toast.LENGTH_SHORT).show();
                return;
            }

            currentDimension = new Dimension();
            currentDimension.setHeight(Integer.parseInt(bindingDimension.editHeight.getText().toString()));
            currentDimension.setLength(Integer.parseInt(bindingDimension.editLength.getText().toString()));
            currentDimension.setWidth(Integer.parseInt(bindingDimension.editWidth.getText().toString()));
            currentDimension.setWeight(Integer.parseInt(bindingDimension.editWeight.getText().toString()));
            Toast.makeText(this, "Thêm kích thước thành công", Toast.LENGTH_SHORT).show();
            dialog_dimension.dismiss();
        });

        dialog_dimension.show();
    }

    private void showAddSize() {
        dialog = new Dialog(this);
        binding2 = PopUpAddVariantSizeColorBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding2.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        initSize(dialog);
        initColor(dialog);
//        binding2.addProductUploadImage.setOnClickListener(v -> chooseImage());

        // --- THÊM CÁC LISTENER CHO NÚT TĂNG/GIẢM SỐ LƯỢNG VÀO ĐÂY ---
        binding2.btnPlus.setOnClickListener(v -> {
            String currentText = binding2.editTextText.getText().toString();
            int currentQuantity = 0;
            try {
                currentQuantity = Integer.parseInt(currentText);
            } catch (NumberFormatException e) {
                // Xử lý nếu có lỗi parse, đặt về 0
                currentQuantity = 0;
            }
            currentQuantity++;
            binding2.editTextText.setText(String.valueOf(currentQuantity));
        });

        binding2.btnMinus.setOnClickListener(v -> {
            String currentText = binding2.editTextText.getText().toString();
            int currentQuantity = 0;
            try {
                currentQuantity = Integer.parseInt(currentText);
            } catch (NumberFormatException e) {
                // Xử lý nếu có lỗi parse, đặt về 0
                currentQuantity = 0;
            }
            if (currentQuantity > 0) { // Đảm bảo số lượng không âm
                currentQuantity--;
            }
            binding2.editTextText.setText(String.valueOf(currentQuantity));
        });
        // ------------------------------------------------------------------

        binding2.btnSubmit.setOnClickListener(v -> {
            if (currentColor == null || currentSize == null) {
                Toast.makeText(this, "Vui lòng chọn màu sắc và kích thước", Toast.LENGTH_SHORT).show();
                return;
            }

            String stockText = binding2.editTextText.getText().toString();
            if (stockText.isEmpty() || stockText.equals("0")) {
                Toast.makeText(this, "Vui lòng nhập số lượng tồn kho hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng tồn kho không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Variant> found = variants.stream()
                    .filter(x -> x.getSize() != null && x.getSize().getName().equals(currentSize.getName()))
                    .collect(Collectors.toList());

            if (found.isEmpty()) {
                Variant newVariant = new Variant();
                newVariant.setId(UUID.randomUUID().toString());
                newVariant.setSize(currentSize);
                newVariant.setDeleted(false);
                newVariant.setDimension(null);
                newVariant.setStock(stock);
                newVariant.setPrice(0);
                newVariant.setDeliveringQuantity(0);

                Color newColor = new Color();
                newColor.setId(currentColor.getId());
                newColor.setName(currentColor.getName());
                newColor.setStock(stock);

                handleImageUpload(newVariant, newColor);
            } else {
                Variant updateVariant = found.get(0);
                List<Color> colors = new ArrayList<>(updateVariant.getListColor() != null ? updateVariant.getListColor() : new ArrayList<>());
                Color existingColor = colors.stream()
                        .filter(c -> c.getId().equals(currentColor.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingColor != null) {
                    int colorIndex = colors.indexOf(existingColor);
                    updateVariant.setStock(updateVariant.getStock() - existingColor.getStock() + stock);
                    existingColor.setStock(stock);
                    handleImageUpload(updateVariant, existingColor, colors, colorIndex);
                } else {
                    Color newColor = new Color();
                    newColor.setId(currentColor.getId());
                    newColor.setName(currentColor.getName());
                    newColor.setStock(stock);
                    colors.add(newColor);
                    updateVariant.setStock(updateVariant.getStock() + stock);
                    handleImageUpload(updateVariant, newColor, colors, -1);
                }
            }
        });

        binding2.btnAddSize.setOnClickListener(v -> {
            dialog2 = new Dialog(this);
            dialog2.setContentView(R.layout.pop_up_add_variant_size);
            dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog2.show();

            Button btnConfirm = dialog2.findViewById(R.id.btn_submit_name);
            btnConfirm.setOnClickListener(v2 -> {
                EditText txtName = dialog2.findViewById(R.id.edit_size);
                String sizeName = txtName.getText().toString().trim();
                if (sizeName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên kích thước", Toast.LENGTH_SHORT).show();
                    dialog2.dismiss();
                    return;
                }

                DatabaseReference sizeRef = database.getReference("Size");
                String id = "size-" + sizeRef.push().getKey();
                Size size = new Size();
                size.setId(id);
                size.setName(sizeName);

                sizeRef.child(id).setValue(size)
                        .addOnSuccessListener(aVoid -> {
                            dialog2.dismiss();
                            initSize(dialog);
                            Toast.makeText(this, "Thêm kích thước thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            dialog2.dismiss();
                            initSize(dialog);
                            Toast.makeText(this, "Thêm kích thước thất bại", Toast.LENGTH_SHORT).show();
                        });
            });
        });

        dialog.show();
    }

    private void handleImageUpload(Variant variant, Color color, List<Color> colors, int colorIndex) {
        if (selectedImageUri != null) {
            binding2.btnSubmit.setEnabled(false);
            StorageReference storageReference = firebaseStorage.getReference().child("product_images/" + UUID.randomUUID().toString());
            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        color.setImageUrl(uri.toString());
                        if (colorIndex >= 0) {
                            colors.set(colorIndex, color);
                        } else {
                            variant.setListColor(colors.isEmpty() ? List.of(color) : colors);
                        }
                        updateVariantList(variant);
                        binding2.btnSubmit.setEnabled(true);
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        resetDialog();
                    }))
                    .addOnFailureListener(e -> {
                        binding2.btnSubmit.setEnabled(true);
                        Toast.makeText(this, "Tải hình ảnh thất bại", Toast.LENGTH_SHORT).show();
                    });
        } else {
            color.setImageUrl(null);
            if (colorIndex >= 0) {
                colors.set(colorIndex, color);
            } else {
                variant.setListColor(colors.isEmpty() ? List.of(color) : colors);
            }
            updateVariantList(variant);
            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
            resetDialog();
        }
    }

    private void handleImageUpload(Variant variant, Color color) {
        handleImageUpload(variant, color, new ArrayList<>(), -1);
    }

    private void updateVariantList(Variant variant) {
        int index = variants.indexOf(variant);
        if (index >= 0) {
            variants.set(index, variant);
        } else {
            variants.add(variant);
        }
        initVariants();
    }

    private void resetDialog() {
        currentColor = null;
        currentSize = null;
        selectedImageUri = null;
        binding2.editTextText.setText("0");
//        binding2.addProductUploadImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.upload_img));
        dialog.dismiss();
    }

    private void initSize(Dialog dialog) {
        List<Size> sizeItems = new ArrayList<>();
        DatabaseReference sizeRef = database.getReference("Size");
        Query query = sizeRef.orderByChild("id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Size size = dataSnapshot.getValue(Size.class);
                        if (size != null) sizeItems.add(size);
                    }
                    ManageSizeAdapter sizeAdapter = new ManageSizeAdapter(sizeItems, AddProductVariantActivity.this);
                    RecyclerView sizeCartRecyclerView = dialog.findViewById(R.id.rcv_size);
                    sizeCartRecyclerView.setLayoutManager(new GridLayoutManager(AddProductVariantActivity.this, 2));
                    sizeCartRecyclerView.setAdapter(sizeAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductVariantActivity.this, "Lỗi khi tải kích thước", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initColor(Dialog dialog) {
        List<Color> colorItems = new ArrayList<>();
        DatabaseReference colorRef = database.getReference("Color");
        colorRef.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Color color = dataSnapshot.getValue(Color.class);
                        if (color != null) colorItems.add(color);
                    }
                    ColorAdapter colorAdapter = new ColorAdapter(colorItems, AddProductVariantActivity.this);
                    RecyclerView colorCartRecyclerView = dialog.findViewById(R.id.rcv_color);
                    colorCartRecyclerView.setLayoutManager(new GridLayoutManager(AddProductVariantActivity.this, 1));
                    colorCartRecyclerView.setAdapter(colorAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductVariantActivity.this, "Lỗi khi tải màu sắc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSizeClickEvent(Size size) {
        currentSize = size;
        if (currentColor != null) onColorClick(currentColor);
    }

    @Override
    public void onColorClick(Color color) {
        currentColor = color;
        if (currentSize != null) {
            List<Variant> variantss = variants.stream()
                    .filter(x -> x.getSize() != null && x.getSize().getName().equals(currentSize.getName()))
                    .collect(Collectors.toList());
            if (!variantss.isEmpty()) {
                List<Color> colors = variantss.get(0).getListColor();
                if (colors != null && colors.stream().anyMatch(c -> c.getId().equals(currentColor.getId()))) {
                    Color currentCo = colors.stream()
                            .filter(c -> c.getId().equals(currentColor.getId()))
                            .findFirst()
                            .orElse(null);
                    if (currentCo != null) {
                        binding2.editTextText.setText(String.valueOf(currentCo.getStock()));
//                        if (currentCo.getImageUrl() != null) {
//                            selectedImageUri = Uri.parse(currentCo.getImageUrl());
//                            loadImageAsync(currentCo.getImageUrl());
//                        } else {
//                            resetImage();
//                        }
                    }
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

    private void resetQuantity() {
        binding2.editTextText.setText("0");
//        resetImage();
    }

//    private void resetImage() {
//        selectedImageUri = null;
//        binding2.addProductUploadImage.setImageURI(null);
//        binding2.addProductUploadImage.setImageBitmap(null);
//        binding2.addProductUploadImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.upload_img));
//    }

//    private void loadImageAsync(String url) {
//        Executor executor = Executors.newSingleThreadExecutor();
//        executor.execute(() -> {
//            Bitmap bitmap = getBitmapFromURL(url);
//            runOnUiThread(() -> binding2.addProductUploadImage.setImageBitmap(bitmap));
//        });
//    }

//    public static Bitmap getBitmapFromURL(String src) {
//        try {
//            URL url = new URL(src);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            return BitmapFactory.decodeStream(input);
//        } catch (Exception e) {
//            Log.e("BitmapError", "Lỗi tải hình ảnh: " + e.getMessage());
//            return null;
//        }
//    }
}