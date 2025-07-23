package com.example.petshopapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ListProductAdapter;
import com.example.petshopapplication.Adapter.ListProductCategoryAdapter;
import com.example.petshopapplication.databinding.ActivityListProductBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListProductActivity extends AppCompatActivity implements ListProductCategoryAdapter.OnCategoryClickListener {

    private ActivityListProductBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView.Adapter productAdapter, categoryAdapter;
    private NestedScrollView scrollView;
    private List<Category> categoryItems;
    private List<Product> productItems;
    private int currentPage = 1;
    private final int LIMIT_PAGE = 3;
    private final int ITEMS_PER_PAGE = 16;
    private String searchText;
    private String categoryId;
    private boolean isSearch;
    private boolean isLoading = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupScrollListener();
        setupClickListeners();
        getIntentExtra();


        // Add entrance animation
        animateEntrance();

        binding.tvSearch.setText(searchText);
        initCategory();
    }

    private void initializeViews() {
        database = FirebaseDatabase.getInstance();
        binding.loadMoreCard.setVisibility(View.GONE);
        binding.loadingCard.setVisibility(View.GONE);
        scrollView = binding.scrollView;
    }

    private void animateEntrance() {
        // Animate header card slide in from top
        binding.headerCard.setTranslationY(-200f);
        binding.headerCard.setAlpha(0f);
        binding.headerCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate FAB with bounce effect
        binding.fabFilter.setScaleX(0f);
        binding.fabFilter.setScaleY(0f);
        binding.fabFilter.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void setupScrollListener() {
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    if (!isLoading && currentPage < LIMIT_PAGE) {
                        loadMoreProducts();
                    }
                }

                // Hide/show FAB based on scroll direction
                if (scrollY > oldScrollY + 12 && binding.fabFilter.isShown()) {
                    binding.fabFilter.hide();
                } else if (scrollY < oldScrollY - 12 && !binding.fabFilter.isShown()) {
                    binding.fabFilter.show();
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> {
            searchText = binding.tvSearch.getText().toString().trim();
            if (!searchText.isEmpty()) {
                isSearch = true;
                currentPage = 1;
                showLoadingWithAnimation();
                initListProduct(categoryItems);
            } else {
                Toast.makeText(this, "Please enter search term", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCart.setOnClickListener(v -> {
            // Add click animation
            animateButtonClick(binding.btnCart);
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        binding.tvAllCategory.setOnClickListener(v -> {
            animateButtonClick(binding.tvAllCategory);
            categoryId = null;
            currentPage = 1;
            isSearch = false;
            initListProduct(categoryItems);
        });

        binding.imvGoBack.setOnClickListener(v -> {
            animateButtonClick(binding.imvGoBack);
            onBackPressed();
        });

        binding.fabFilter.setOnClickListener(v -> {
            // Add pulse animation
            ObjectAnimator pulse = ObjectAnimator.ofFloat(binding.fabFilter, "scaleX", 1f, 1.1f, 1f);
            pulse.setDuration(200);
            pulse.start();

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(binding.fabFilter, "scaleY", 1f, 1.1f, 1f);
            pulseY.setDuration(200);
            pulseY.start();

            // TODO: Open filter dialog
            Toast.makeText(this, "Filter options coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void showLoadingWithAnimation() {
        binding.loadingCard.setAlpha(0f);
        binding.loadingCard.setVisibility(View.VISIBLE);
        binding.loadingCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void hideLoadingWithAnimation() {
        binding.loadingCard.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> binding.loadingCard.setVisibility(View.GONE))
                .start();
    }

    private void initCategory() {
        reference = database.getReference(getString(R.string.tbl_category_name));
        categoryItems = new ArrayList<>();

        Query query = reference.orderByChild("deleted");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    categoryItems.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Category category = dataSnapshot.getValue(Category.class);
                        if (category != null && !category.isDeleted()) {
                            categoryItems.add(category);
                        }
                    }

                    if (categoryItems.size() > 0) {
                        setupCategoryAdapter();
                    }
                    initListProduct(categoryItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryAdapter() {
        categoryAdapter = new ListProductCategoryAdapter(categoryItems, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        binding.rcvListProductCategory.setLayoutManager(layoutManager);
        binding.rcvListProductCategory.setAdapter(categoryAdapter);

        // Add stagger animation to category items
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        LayoutAnimationController controller = new LayoutAnimationController(slideIn, 0.1f);
        binding.rcvListProductCategory.setLayoutAnimation(controller);
    }

    private void loadMoreProducts() {
        if (isLoading) return;

        isLoading = true;
        currentPage++;

        // Show load more animation
        binding.loadMoreCard.setVisibility(View.VISIBLE);
        binding.loadMoreCard.setAlpha(0f);
        binding.loadMoreCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadMoreProduct(categoryItems, currentPage, ITEMS_PER_PAGE);
        }, 1500); // Slightly longer delay for better UX
    }

    private void loadMoreProduct(List<Category> categoryItems, int page, int itemsPerPage) {
        reference = database.getReference(getString(R.string.tbl_product_name));

        int endIndex = page * itemsPerPage;
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
                        if (product != null && !product.isDeleted()) {
                            if (isSearch) {
                                if (product.getName().toLowerCase().contains(searchText.toLowerCase())) {
                                    newProducts.add(product);
                                }
                            } else {
                                newProducts.add(product);
                            }
                        }
                    }

                    int startIndex = productItems.size();
                    productItems.addAll(newProducts.subList(startIndex, Math.min(newProducts.size(), startIndex + ITEMS_PER_PAGE)));

                    if (productAdapter != null) {
                        productAdapter.notifyItemRangeInserted(startIndex, Math.min(ITEMS_PER_PAGE, newProducts.size() - startIndex));
                    }
                }

                // Hide load more animation
                binding.loadMoreCard.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            binding.loadMoreCard.setVisibility(View.GONE);
                            isLoading = false;
                        })
                        .start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListProductActivity.this, "Failed to load more products", Toast.LENGTH_SHORT).show();
                isLoading = false;
                binding.loadMoreCard.setVisibility(View.GONE);
            }
        });
    }

    private void initListProduct(List<Category> categoryItems) {
        reference = database.getReference(getString(R.string.tbl_product_name));

        if (!isLoading) {
            showLoadingWithAnimation();
        }

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
                        if (product != null && !product.isDeleted()) {
                            if (isSearch) {
                                if (product.getName().toLowerCase().contains(searchText.toLowerCase())) {
                                    productItems.add(product);
                                }
                            } else {
                                productItems.add(product);
                            }
                        }
                    }

                    setupProductAdapter();
                }

                hideLoadingWithAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingWithAnimation();
                Toast.makeText(ListProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductAdapter() {
        productAdapter = new ListProductAdapter(productItems, categoryItems);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rcvListProduct.setLayoutManager(gridLayoutManager);
        binding.rcvListProduct.setAdapter(productAdapter);
        binding.rcvListProduct.setNestedScrollingEnabled(false);

        // Add stagger animation to product items
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(400);
        LayoutAnimationController controller = new LayoutAnimationController(slideUp, 0.1f);
        binding.rcvListProduct.setLayoutAnimation(controller);
    }

    private void getIntentExtra() {
        categoryId = getIntent().getStringExtra("categoryId");
        searchText = getIntent().getStringExtra("searchText");
        isSearch = getIntent().getBooleanExtra("isSearch", false);

        if (searchText == null) {
            searchText = "";
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        // Add selection animation
        animateButtonClick(binding.rcvListProductCategory);

        categoryId = category.getId();
        currentPage = 1;
        isSearch = false;
        productItems.clear();
        initListProduct(categoryItems);
        binding.loadMoreCard.setVisibility(View.GONE);

        // Scroll to top smoothly
        binding.scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void onBackPressed() {
        // Add exit animation
        binding.headerCard.animate()
                .translationY(-200f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(super::onBackPressed)
                .start();
    }
}