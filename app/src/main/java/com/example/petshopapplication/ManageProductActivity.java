package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ListUpdateProductAdapter;
import com.example.petshopapplication.Adapter.ListProductCategoryAdapter;
import com.example.petshopapplication.databinding.ActivityListManageProductBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageProductActivity extends AppCompatActivity implements ListProductCategoryAdapter.OnCategoryClickListener,
        ListUpdateProductAdapter.OnEditClickListener, ListUpdateProductAdapter.OnDeleteClickListener {

    private final int LIMIT_PAGE = 3;
    private final int ITEMS_PER_PAGE = 16;
    ActivityListManageProductBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    ScrollView scrollView;
    List<Category> categoryItems;
    List<Product> productItems;
    private RecyclerView.Adapter productAdapter, categoryAdapter;
    private int currentPage = 1;
    private String searchText;
    private String categoryId;
    private boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListManageProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        binding.prgLoadMore.setVisibility(View.GONE);
        scrollView = binding.scrollView;
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
            int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

            // If diff is zero, reached the bottom of the ScrollView
            if (diff == 0) {
                //increase page by 1
                currentPage = currentPage + 1;
                //display progress bar

                //check if current page reach limit
                if (currentPage <= LIMIT_PAGE) {
                    binding.prgLoadMore.setVisibility(View.VISIBLE);
                    //load more data
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadMoreProduct(categoryItems, currentPage, ITEMS_PER_PAGE);
                    }, 2000);

                }

            }
        });

        getIntentExtra();
        binding.tvSearch.setText(searchText);
        binding.btnSearch.setOnClickListener(v -> {
            searchText = binding.tvSearch.getText().toString();
            isSearch = true;
            initListProduct(categoryItems);
        });


        binding.tvAllCategory.setOnClickListener(v -> {
            categoryId = null;
            initListProduct(categoryItems);
        });

        initCategory();

    }

    private void initCategory() {
        reference = database.getReference(getString(R.string.tbl_category_name));

        categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("deleted");
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                    }

                    if (categoryItems.size() > 1) {
                        categoryAdapter = new ListProductCategoryAdapter(categoryItems, ManageProductActivity.this);
                        binding.rcvListProductCategory.setLayoutManager(new LinearLayoutManager(ManageProductActivity.this, RecyclerView.HORIZONTAL, false));
                        binding.rcvListProductCategory.setAdapter(categoryAdapter);
                    }
                    initListProduct(categoryItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadMoreProduct(List<Category> categoryItems, int page, int itemsPerPage) {
        reference = database.getReference(getString(R.string.tbl_product_name));

        //calculate paging factor
        int endIndex = page * itemsPerPage;
        int startIndex = productItems.size();

        List<Product> newProducts = new ArrayList<>();
        Query query;

        if (categoryId != null && !categoryId.isBlank()) {
            query = reference.orderByChild("categoryId").equalTo(categoryId).limitToFirst(endIndex);
        } else {
            query = reference.orderByChild("deleted").equalTo(false).limitToFirst(endIndex);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        //Check if user searching for product
                        if (isSearch) {
                            if (product.getName().contains(searchText)) {
                                newProducts.add(dataSnapshot.getValue(Product.class));
                            }
                        } else {
                            newProducts.add(dataSnapshot.getValue(Product.class));
                        }
                    }
                    productItems.clear();
                    productItems.addAll(newProducts);
                    productAdapter.notifyItemRangeInserted(startIndex, productItems.size());
                    binding.prgLoadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void initListProduct(List<Category> categoryItems) {
        reference = database.getReference(getString(R.string.tbl_product_name));
        //Display progress bar
        binding.prgListProduct.setVisibility(View.VISIBLE);

        productItems = new ArrayList<>();
        Query query;

        if (categoryId != null && !categoryId.isBlank()) {
            query = reference.orderByChild("categoryId").equalTo(categoryId);
        } else {
            query = reference.orderByChild("deleted").equalTo(false);
        }
        query.limitToFirst(ITEMS_PER_PAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        //Check status of product
                        if (!product.isDeleted()) {
                            //Check if user searching for product
                            if (isSearch) {
                                if (product.getName().contains(searchText)) {
                                    productItems.add(dataSnapshot.getValue(Product.class));
                                }
                            } else {
                                productItems.add(dataSnapshot.getValue(Product.class));
                            }
                        }
                    }
                    productAdapter = new ListUpdateProductAdapter(productItems, categoryItems, ManageProductActivity.this,
                            ManageProductActivity.this);
                    binding.rcvListProduct.setLayoutManager(new GridLayoutManager(ManageProductActivity.this, 2));
                    binding.rcvListProduct.setAdapter(productAdapter);

                    binding.prgListProduct.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getIntentExtra() {
        categoryId = getIntent().getStringExtra("categoryId");
        searchText = getIntent().getStringExtra("searchText");

        isSearch = getIntent().getBooleanExtra("isSearch", false);

        binding.imvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onCategoryClick(Category category) {
        categoryId = category.getId();
        currentPage = 1;
        productItems.clear();
        initListProduct(categoryItems);
        binding.prgLoadMore.setVisibility(View.GONE);
    }

    @Override
    public void onEditClickEvent(Product product) {
        Intent intent = new Intent(this, UpdateProductActivity.class);
        intent.putExtra("id", product.getId());
        startActivity(intent);
    }

    public void onDeleteClickEvent(Product product) {
        // Create an AlertDialog builder
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with delete if user confirms
                    DatabaseReference productRef = database.getReference("products");
                    Query query = productRef.orderByChild("id").equalTo(product.getId());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String id = "";
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    id = child.getKey();
                                }

                                // Update isDeleted field to true
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("deleted", true);

                                productRef.child(id).updateChildren(updates)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Restart activity to refresh data after deletion
                                                finish();
                                                startActivity(getIntent());
                                                Toast.makeText(ManageProductActivity.this, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ManageProductActivity.this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ManageProductActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog if user clicks "No"
                    dialog.dismiss();
                })
                .show();
    }

}