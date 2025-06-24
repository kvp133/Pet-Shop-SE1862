package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class AddProductVariantActivity extends AppCompatActivity implements  ManageSizeAdapter.OnSizeClickEventListener,ColorAdapter.OnColorClickEventListener
{
    Dialog dialog_dimension;
    Button btnShowAddSize;
    ActivityAddProductVariantBinding binding;
    PopUpAddVariantSizeColorBinding binding2;
    FirebaseDatabase database;
    private Uri selectedImageUri; // To store the selected image URI

    DatabaseReference reference;
    Dialog dialog;
    private FirebaseStorage firebaseStorage;
    Dialog dialog2;
    private Dimension currentDimension = null;
    List<Variant> variants = new ArrayList<>();
    String productName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product_variant);

        binding = ActivityAddProductVariantBinding.inflate(getLayoutInflater());
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
            //your codes here

        }
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
         model = (Product) getIntent().getSerializableExtra("product");
        System.out.println(ObjectPrinter.print(model));

        if(model == null)
        {
           finish();
        }
        binding.addPvName.setText(model.getName().toString());
        binding.addPvButtonAddSizeColor.setOnClickListener(view -> showAddSize());
        binding.addPvDimension.setOnClickListener(view -> showAddDimension());
        binding.addPvButton.setOnClickListener(view->addVariant());
        initVariants();
    }
    Product model;
    private void addVariant()
    {
        if(currentDimension == null)
        {
            Toast.makeText(AddProductVariantActivity.this, "Please set dimension", Toast.LENGTH_SHORT).show();

        }
        if(variants.isEmpty())
        {
            Variant base = new Variant();
            base.setDimension(currentDimension);
            base.setSize(null);
        }
        else {
            for(Variant variant:variants)
            {
                variant.setPrice(model.getBasePrice());
                variant.setDimension(currentDimension);
            }
            model.setListVariant(variants);
        }
        DatabaseReference productRef = database.getReference("products");

        String feedbackId = "product-" + productRef.push().getKey(); // Generate a unique ID
        model.setId(feedbackId);


        productRef.child(feedbackId).setValue(model)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product submitted successfully!", Toast.LENGTH_SHORT).show();
                })

                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit product.", Toast.LENGTH_SHORT).show());
    }
    private void initVariants()
    {
        System.out.println(ObjectPrinter.print(variants));
        if(!variants.isEmpty())
        {
            binding.addPvStock.setEnabled(false);
            binding.addPvStock.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.GONE);
            binding.recyclerView2.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.recyclerView2.setVisibility(View.GONE);
            binding.noItemsTextView.setVisibility(View.VISIBLE);

        }
        RecyclerView recyclerView = findViewById(R.id.recycler_view_2);

        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create sample data for the adapter
        List<ItemModel> itemList = new ArrayList<>();
        for(Variant variant : variants)
        {
            for(Color color : variant.getListColor())
            {
                itemList.add(new ItemModel(variant.getSize().getName(),
                        R.drawable.arrow,color.getName(),color  .getStock()));
            }
        }
        // Initialize the custom adapter and set it to the RecyclerView
        VariantAdapter adapter = new VariantAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Save the selected image URI
            binding2.addProductUploadImage.setImageURI(selectedImageUri); // Display the selected image
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityIfNeeded(intent, 100);
    }

    private void showAddDimension() {
        dialog_dimension = new Dialog(AddProductVariantActivity.this);


        dialog_dimension.setContentView(R.layout.pop_up_add_variant_dimnesion);
        PopUpAddVariantDimnesionBinding bindingDimension =
                PopUpAddVariantDimnesionBinding.inflate(getLayoutInflater());
        dialog_dimension.setContentView(bindingDimension.getRoot());
        dialog_dimension.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button btnSubmit = dialog_dimension.findViewById(R.id.btn_submit_dimension);
        if(currentDimension!=null)
        {
            bindingDimension.editHeight.setText(String.valueOf(currentDimension.getHeight()));
            bindingDimension.editWidth.setText(String.valueOf(currentDimension.getWidth()));
            bindingDimension.editLength.setText(String.valueOf(currentDimension.getLength()));
            bindingDimension.editWeight.setText(String.valueOf(currentDimension.getWeight()));
        }
        btnSubmit.setOnClickListener(viewCurrent ->
        {
            currentDimension = new Dimension();

            currentDimension.setHeight(Integer.parseInt(bindingDimension.editHeight.getText().toString()));
            currentDimension.setLength(Integer.parseInt(bindingDimension.editLength.getText().toString()));
            currentDimension.setWidth(Integer.parseInt(bindingDimension.editWidth.getText().toString()));
            currentDimension.setWeight(Integer.parseInt(bindingDimension.editWeight.getText().toString()));
            Toast.makeText(AddProductVariantActivity.this, "Add successfully", Toast.LENGTH_SHORT).show();
            dialog_dimension.dismiss();
        });
        dialog_dimension.show();

    }
    private void showAddSize(){
        dialog = new Dialog(AddProductVariantActivity.this);
        dialog.setContentView(R.layout.pop_up_add_variant_size_color);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

// Create binding BEFORE showing the dialog
        binding2 = PopUpAddVariantSizeColorBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding2.getRoot());

        dialog.show();
        initSize(dialog);
        initColor(dialog);
        binding2.addProductUploadImage.setOnClickListener(view -> chooseImage());


        binding2.btnSubmit.setOnClickListener(view -> {
            //reference =  database.getReference("list");
            if (currentColor == null || currentSize == null) {
                Toast.makeText(AddProductVariantActivity.this, "Please select color or size.", Toast.LENGTH_SHORT).show();
            } else if (binding2.editTextText.getText().toString().equals("0")) {
                Toast.makeText(AddProductVariantActivity.this, "Please enter stock size.", Toast.LENGTH_SHORT).show();
            } else {
                List<Variant> found = variants.stream().filter(x -> x.getSize().getName().equals(currentSize.getName())).collect(Collectors.toList());
                Color newColor = currentColor;
                CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

                if (found.isEmpty()) {
                    Variant newva = new Variant();
                    newva.setId(UUID.randomUUID().toString());
                    newva.setSize(currentSize);
                    newva.setDeleted(false);
                    newva.setDimension(null);
                    newva.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));
                    newColor.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));

                    if (selectedImageUri != null) {
                        binding2.btnSubmit.setEnabled(false);

                        // Upload the image to Firebase Storage
                        StorageReference storageReference = firebaseStorage.getReference().child("product_images/" + UUID.randomUUID().toString());
                        storageReference.putFile(selectedImageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        newColor.setImageUrl(imageUrl);
                                        newva.setListColor(List.of(newColor));
                                        newva.setPrice(0);
                                        newva.setDeliveringQuantity(0);
                                        variants.add(newva);
                                        binding2.btnSubmit.setEnabled(true);
                                        latch.countDown(); // Decrease latch count
                                    });
                                }).addOnFailureListener(e -> latch.countDown()); // Handle failure and decrease latch count
                    } else {
                        newColor.setImageUrl(null);
                        newva.setListColor(List.of(newColor));
                        newva.setPrice(0);
                        newva.setDeliveringQuantity(0);
                        variants.add(newva);
                        latch.countDown(); // Decrease latch count immediately
                    }

                } else {
                    Variant updateVar = found.get(0);
                    List<Color> colors = new ArrayList<>(updateVar.getListColor());
                    if (colors.stream().anyMatch(x -> x.getId().equals(currentColor.getId()))) {
                        Color updateColor = colors.stream().filter(x -> x.getId().equals(currentColor.getId())).findFirst().get();
                        int colorIndex = colors.indexOf(updateColor);
                        updateVar.setStock(updateVar.getStock() - updateColor.getStock() + Integer.parseInt(binding2.editTextText.getText().toString()));

                        if (selectedImageUri != null) {
                            binding2.btnSubmit.setEnabled(false);
                            StorageReference storageReference = firebaseStorage.getReference().child("product_images/" + UUID.randomUUID().toString());
                            storageReference.putFile(selectedImageUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString();
                                            updateColor.setImageUrl(imageUrl);
                                            updateColor.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));
                                            colors.set(colorIndex, updateColor);
                                            updateVar.setListColor(colors);
                                            binding2.btnSubmit.setEnabled(true);
                                            latch.countDown(); // Decrease latch count
                                        });
                                    }).addOnFailureListener(e -> latch.countDown()); // Handle failure and decrease latch count
                        } else {
                            newColor.setImageUrl(null);
                            updateColor.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));
                            colors.set(colorIndex, updateColor);
                            updateVar.setListColor(colors);
                            latch.countDown(); // Decrease latch count immediately
                        }

                    } else {
                        if (selectedImageUri != null) {
                            binding2.btnSubmit.setEnabled(false);
                            StorageReference storageReference = firebaseStorage.getReference().child("product_images/" + UUID.randomUUID().toString());
                            storageReference.putFile(selectedImageUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString();
                                            currentColor.setImageUrl(imageUrl);
                                            colors.add(currentColor);
                                            updateVar.setListColor(colors);
                                            currentColor.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));
                                            updateVar.setStock(updateVar.getStock() + currentColor.getStock());
                                            binding2.btnSubmit.setEnabled(true);
                                            latch.countDown(); // Decrease latch count
                                        });
                                    }).addOnFailureListener(e -> latch.countDown()); // Handle failure and decrease latch count
                        } else {
                            newColor.setImageUrl(null);
                            colors.add(currentColor);
                            updateVar.setListColor(colors);
                            currentColor.setStock(Integer.parseInt(binding2.editTextText.getText().toString()));
                            updateVar.setStock(updateVar.getStock() + currentColor.getStock());
                            latch.countDown(); // Decrease latch count immediately
                        }
                    }

                    int index = variants.indexOf(updateVar);
                    if (index != -1) {
                        variants.set(index, updateVar);
                    }
                }
                Toast.makeText(AddProductVariantActivity.this, "Submitting. Please wait...", Toast.LENGTH_SHORT).show();

                // Wait for all asynchronous operations to complete before dismissing the dialog
                new Thread(() -> {
                    try {
                        latch.await(); // Wait until latch count reaches zero
                        runOnUiThread(() -> {
                            Toast.makeText(AddProductVariantActivity.this, "Submit color and size success", Toast.LENGTH_SHORT).show();
                            currentColor = null;
                            currentSize = null;
                            dialog.dismiss();
                            initVariants();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        binding2.btnAddSize.setOnClickListener(view -> {
            dialog2 = new Dialog(AddProductVariantActivity.this);
            dialog2.setContentView(R.layout.pop_up_add_variant_size);
            dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog2.show();
            Button btnConfirm = dialog2.findViewById(R.id.btn_submit_name);
            btnConfirm.setOnClickListener(view2->
            {
                EditText txtName = dialog2.findViewById(R.id.edit_size);
                if(!txtName.getText().equals("")) {
                    DatabaseReference ref = database.getReference("Size");

                    String id = "size-" + ref.push().getKey(); // Generate a unique ID
                    Size size = new Size();
                    size.setId(id);
                    size.setName(txtName.getText().toString());
                    ref.child(id).setValue(size)
                            .addOnSuccessListener(aVoid ->
                                    {
                                        dialog2.dismiss();
                                        initSize(dialog);
                                        Toast.makeText(AddProductVariantActivity.this, "Size submitted successfully!", Toast.LENGTH_SHORT).show();

                                    })
                            .addOnFailureListener(e ->
                                    {
                                        dialog2.dismiss();
                                        initSize(dialog);
                                        Toast.makeText(AddProductVariantActivity.this, "Failed to submit size.", Toast.LENGTH_SHORT).show();
                                    });

                }
                else
                {
                    dialog2.dismiss();
                    initSize(dialog);
                }
            });

        });
    }
    RecyclerView colorCartRecyclerView, sizeCartRecyclerView;
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
    private void initSize(Dialog dialog)
    {
        List<Size> sizeItems = new ArrayList<>();
        reference =  database.getReference("Size");
        Query query = reference.orderByChild("id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        sizeItems.add(dataSnapshot.getValue(Size.class));
                    }
                    ManageSizeAdapter sizeAdapter = new ManageSizeAdapter(sizeItems, AddProductVariantActivity.this);
                    sizeCartRecyclerView = dialog.findViewById(R.id.rcv_size);
                    sizeCartRecyclerView.setLayoutManager(new GridLayoutManager(AddProductVariantActivity.this, 2));
                    sizeCartRecyclerView.setAdapter(sizeAdapter);

            }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    Size currentSize = null;
    @Override
    public void onSizeClickEvent(Size size) {
        currentSize = size;
        if (currentColor != null) onColorClick(currentColor);

    }

    private void initColor(Dialog dialog)
    {
        List<Color> colorItems = new ArrayList<>();
        Color color = new Color();
        color.setId("1");
        color.setName("Tuna");
        color.setImageUrl("https://firebasestorage.googleapis.com/v0/b/pet-shop-a220c.appspot.com/o/per-food-4.png?alt=media&token=2672b45a-80c6-4011-b28a-6fbd97806498");
        colorItems.add(color);
        Color color2 = new Color();

        color2.setId("2");
        color2.setName("Salmon2");
        color2.setImageUrl("https://firebasestorage.googleapis.com/v0/b/pet-shop-a220c.appspot.com/o/per-food-4.png?alt=media&token=2672b45a-80c6-4011-b28a-6fbd97806498");
        colorItems.add(color2);

        ColorAdapter colorAdapter = new ColorAdapter(colorItems,AddProductVariantActivity.this);
        colorCartRecyclerView = dialog.findViewById(R.id.rcv_color);
        colorCartRecyclerView.setLayoutManager(new GridLayoutManager(AddProductVariantActivity.this, 1));
        colorCartRecyclerView.setAdapter(colorAdapter);
    }
    private Color currentColor = null;
    @Override
    public void onColorClick(Color color) {
        currentColor = color;
        if(currentSize !=null)
        {
            List<Variant> variantss = variants.stream().filter(x->x.getSize().getName().equals(currentSize.getName())).collect(Collectors.toList());
            if(!variantss.isEmpty()) {
                List<Color> colors = variantss.get(0).getListColor();
                if (colors.stream().anyMatch(x -> Objects.equals(x.getId(), currentColor.getId()))) {
                    EditText ed = dialog.findViewById(R.id.editTextText);
                    Color currentco = colors.stream().filter(x -> Objects.equals(x.getId(), currentColor.getId())).findFirst().get();
                    ed.setText(String.valueOf(colors.stream().filter(x -> Objects.equals(x.getId(), currentColor.getId())).findFirst().get().getStock()));
                    if (currentco.getImageUrl() != null)
                    {
                        selectedImageUri = Uri.parse(currentco.getImageUrl());

                        binding2.addProductUploadImage.setImageBitmap(getBitmapFromURL(String.valueOf(selectedImageUri)));
                    }
                    else
                        resetImage();
                } else resetQuantity();
            } else resetQuantity();
        } else resetQuantity();
    }

    public void resetQuantity() {
        EditText ed = dialog.findViewById(R.id.editTextText);
        ed.setText("0");
        resetImage();
    }
    public void resetImage() {
        selectedImageUri = null;
        binding2.addProductUploadImage.setImageURI(null);
        binding2.addProductUploadImage.setImageBitmap(null);
        binding2.addProductUploadImage.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.upload_img));
    }
}