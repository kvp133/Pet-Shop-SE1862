

package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.Adapter.CartAdapter;
import com.example.petshopapplication.Adapter.ListCategoryAdapter;
import com.example.petshopapplication.databinding.ActivityCategoryListBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.Product;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CategoryListActivity extends AppCompatActivity implements ListCategoryAdapter.OnItemClickedListener {
    FirebaseDatabase database;
    DatabaseReference reference;
    private FirebaseStorage firebaseStorage;



    List<Category> categoryList = new ArrayList<>();
    List<Category> categoryListBeforeSort = new ArrayList<>();
    ActivityCategoryListBinding binding;
    //Category category;

    //Set up dialog cart
    ConstraintLayout layout;
    Dialog dialog;

    //pop up view
    ImageView imv_category;
    TextView tv_category_name;
    Button btn_category_image_pick, btn_category_update, btn_category_delete;
    private Uri selectedImageUri;

    int totalQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_category_list);


        //Handling add category button
        binding.imvAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryListActivity.this, AddCategoryActivity.class);
                startActivity(intent);
            }
        });

        //Handling back button
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryListActivity.this, ViewAdminDashBoardActivity.class);
                startActivity(intent);
            }
        });

        //create dialog of pop up for add to cart
        dialog = new Dialog(CategoryListActivity.this);
        dialog.setContentView(R.layout.pop_up_update_delete_category_item);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //init dialog view items
        imv_category = dialog.findViewById(R.id.imv_category_image);
        tv_category_name = dialog.findViewById(R.id.edt_category_name);
        btn_category_image_pick = dialog.findViewById(R.id.btn_category_image_pick);
        btn_category_update = dialog.findViewById(R.id.btn_category_update);
        btn_category_delete = dialog.findViewById(R.id.btn_category_delete);

        //Initialize firebase
        database = FirebaseDatabase.getInstance();

        initListCategory();
    }

    public void initListCategory() {

        reference = database.getReference(getString(R.string.tbl_category_name));

        reference.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Category category = dataSnapshot.getValue(Category.class);
                        categoryListBeforeSort.add(category);
                    }
                    sortCategoryList(categoryListBeforeSort);
                    //totalQuantity = categoryList.size();
                    RecyclerView rec = findViewById(R.id.rcv_list_category);
                    ListCategoryAdapter adapter = new ListCategoryAdapter(categoryList, CategoryListActivity.this, CategoryListActivity.this);
                    rec.setLayoutManager(new LinearLayoutManager(CategoryListActivity.this));
                    rec.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Handle when user clicked item
    @Override
    public void onItemClicked(Category category) {
        dialog.show();
        //modify category attribute
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();


        // Handle image selection
        btn_category_image_pick.setOnClickListener(view -> {
            chooseImage();
        });


        // Handle category update
        btn_category_update.setOnClickListener(view -> {
            updateCategoryInDatabase(category);

        });

        // Populate the UI with existing feedback data
        if (category != null) {
            tv_category_name.setText(category.getName());
            // Load image using Glide if available
            if (category.getImage() != null) {
                Glide.with(this)
                        .load(category.getImage())
                        .placeholder(R.drawable.icon)
                        .into(imv_category);

            }
        }

        // Handle category update submission
        btn_category_update.setOnClickListener(view -> {
            updateCategoryInDatabase(category);
        });

        //Handle category delete submission
        btn_category_delete.setOnClickListener(view -> {
            deleteCategoryInDatabae(category);
        });
    }

    public void restoreProductOfCategory(Category category){
        //Restore all product relative
        reference = database.getReference(getString(R.string.tbl_product_name));
        reference.orderByChild("categoryId").equalTo(category.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            List<Task<Void>> tasks = new ArrayList<>();
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                                Product product = dataSnapshot.getValue(Product.class);

                                if(product != null){

                                    if(dataSnapshot.child("deleted").getValue(Boolean.class)){

                                        Task<Void> deleteTask = dataSnapshot.getRef().child("deleted")
                                                .setValue(false).addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()){

                                                        Log.e("RESTORE CATEGORY", product.getId() + "Restore product successfully!");

                                                    } else {
                                                        Log.e("RESTORE CATEGORY", "Fail to restore product!");
                                                    }
                                                });
                                        tasks.add(deleteTask);
                                    }
                                }
                            }
                            //All product delete have been update
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                                reloadCategoryListActivity();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onRestoreButtutonClikcked(Category category) {
        //Restore category
        reference = database.getReference(getString(R.string.tbl_category_name));
        reference.orderByChild("id").equalTo(category.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                dataSnapshot.getRef().child("deleted")
                                        .setValue(false).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(CategoryListActivity.this, "Category restore successfully", Toast.LENGTH_SHORT).show();
                                                restoreProductOfCategory(category);
                                                //Exit popup
                                                dialog.dismiss();

                                            } else {
                                                Toast.makeText(CategoryListActivity.this, "Failed to restore category", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
            imv_category.setImageURI(selectedImageUri); // Display the selected image
        }
    }
    public void deleteProductOfCategory(Category category, AllProductDeleteChangeCallback callback){
        //Delete all product of category in Firebase
        reference = database.getReference(getString(R.string.tbl_product_name));
        reference.orderByChild("categoryId").equalTo(category.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            List<Task<Void>> tasks = new ArrayList<>();
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                                Product product = dataSnapshot.getValue(Product.class);

                                if(product != null){
                                    if(!product.isDeleted()){
                                        Task<Void> deleteTask = dataSnapshot.getRef().child("deleted")
                                                .setValue(true).addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()){

                                                        Log.e("DELETE CATEGORY", product.getId() + "Delete product successfully!");

                                                    } else {
                                                        Log.e("DELETE CATEGORY", "Fail to delete product!");
                                                    }
                                                });
                                        tasks.add(deleteTask);
                                    }
                                }
                            }
                            //All product delete have been update
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                                callback.onComplete();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void deleteCategoryInDatabae(Category category){

        deleteProductOfCategory(category, new AllProductDeleteChangeCallback() {
            @Override
            public void onComplete() {
                //Delete category relative
                reference = database.getReference(getString(R.string.tbl_category_name));
                reference.orderByChild("id").equalTo(category.getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        dataSnapshot.getRef().child("deleted")
                                                .setValue(true).addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(CategoryListActivity.this, "Category delete successfully", Toast.LENGTH_SHORT).show();
                                                        //Exit popup
                                                        dialog.dismiss();
                                                        reloadCategoryListActivity();
                                                    } else {
                                                        Toast.makeText(CategoryListActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });


    }

    private void updateCategoryInDatabase(Category category) {
        boolean isDuplicate = false;
        // Get updated values from the user input
        String categoryName = tv_category_name.getText().toString().trim();

        //Check category name is empty or not
        if (categoryName.isEmpty()) {
            Toast.makeText(CategoryListActivity.this, "Please provide a name for category", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check if category name is exceed 20
        if(categoryName.length() > 20){
            Toast.makeText(CategoryListActivity.this, "Max length of category name is 20!", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check category name is existed or not
        for (Category c : categoryList) {
            if (categoryName.equals(c.getName())) {
                Toast.makeText(CategoryListActivity.this, "Category name has exited!", Toast.LENGTH_SHORT).show();
                isDuplicate = true;
                break;
            }
        }

        if(!isDuplicate){
            // Update category object with new data
            category.setName(categoryName);

            // Check if a new image has been selected
            if (selectedImageUri != null) {
                // If a new image is selected, upload it and save category
                uploadImageAndSaveCategory(category);
            } else {
                // No image change, just update feedback with the current image URL
                saveCategoryToDatabase(category, category.getImage());
            }
        }


    }

    private void uploadImageAndSaveCategory(Category category) {
        // Create a storage reference for the new image
        StorageReference storageReference = firebaseStorage.getReference().child("category_image/" + UUID.randomUUID().toString());
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after upload
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Save feedback with new image URL
                        saveCategoryToDatabase(category, imageUrl);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(CategoryListActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCategoryToDatabase(Category category, String imageUrl) {
        // Update Category object with new image URL
        category.setImage(imageUrl);

        // Save the updated category in Firebase
        reference = database.getReference("categories").child(category.getId());
        reference.setValue(category).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CategoryListActivity.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                //Exit popup
                dialog.dismiss();
                reloadCategoryListActivity();
            } else {
                Toast.makeText(CategoryListActivity.this, "Failed to update category", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void sortCategoryList(List<Category> categoryListBeforeSort){
        List<Category> activeCategory = new ArrayList<>();
        List<Category> hiddenCategory = new ArrayList<>();

        for (Category category : categoryListBeforeSort){
            if(category.isDeleted()){
                hiddenCategory.add(category);
            } else if (!category.isDeleted()) {
                activeCategory.add(category);
            }
        }
        categoryList.clear();
        categoryList.addAll(activeCategory);
        categoryList.addAll(hiddenCategory);
    }

    public void reloadCategoryListActivity(){
        categoryList.clear();
        categoryListBeforeSort.clear();
        initListCategory();
    }
    public interface AllProductDeleteChangeCallback{
        void onComplete();
    }



//    public void fetchProductQuantityToCategory(List<Category> categoryList, OnQuantityFetchListener listener) {
//        Map<Category, Long> categoryIntegerMap = new LinkedHashMap<>();
//
//
//        for (Category category : categoryList) {
//            categoryIntegerMap.put(category, getProductQuantityOfCategory(category, listener));
//
//        }
//
//
//
//
//    }
//
//    public Long getProductQuantityOfCategory(Category category, OnQuantityFetchListener listener) {
//        Long quantity = 0L;
//        reference = database.getReference(getString(R.string.tbl_product_name));
//        reference.orderByChild("categoryId").equalTo(category.getId())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            quantity = snapshot.getChildrenCount();
//                            quantity = listener.onQuantityFetch(quantity);
//
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//        return quantity;
//
//    }
//
//    public interface OnQuantityFetchListener {
//        Long onQuantityFetch(Long quantity);
//    }

}
