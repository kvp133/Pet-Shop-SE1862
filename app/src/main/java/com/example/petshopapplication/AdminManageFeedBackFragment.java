package com.example.petshopapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.ListFeedBackProductAdapter;
import com.example.petshopapplication.Adapter.ListProductCategoryAdapter;
import com.example.petshopapplication.databinding.FragmentAdminManageFeedBackBinding;
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

public class AdminManageFeedBackFragment extends Fragment implements ListProductCategoryAdapter.OnCategoryClickListener {

    private final int LIMIT_PAGE = 3;
    private final int ITEMS_PER_PAGE = 16;
    private FragmentAdminManageFeedBackBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private List<Category> categoryItems;
    private List<Product> productItems;
    private RecyclerView.Adapter productAdapter, categoryAdapter;
    private int currentPage = 1;
    private String searchText;
    private String categoryId;
    private boolean isSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminManageFeedBackBinding.inflate(inflater, container, false);

        database = FirebaseDatabase.getInstance();
        binding.prgLoadMore.setVisibility(View.GONE);

        binding.scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = binding.scrollView.getChildAt(binding.scrollView.getChildCount() - 1);
            int diff = view.getBottom() - (binding.scrollView.getHeight() + binding.scrollView.getScrollY());
            if (diff == 0 && currentPage <= LIMIT_PAGE) {
                currentPage++;
                binding.prgLoadMore.setVisibility(View.VISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadMoreProduct(currentPage, ITEMS_PER_PAGE), 2000);
            }
        });

        binding.btnSearch.setOnClickListener(v -> {
            searchText = binding.tvSearch.getText().toString();
            isSearch = true;
            loadProducts();
        });

        binding.tvAllCategory.setOnClickListener(v -> {
            categoryId = null;
            loadProducts();
        });

        initCategory();
        return binding.getRoot();
    }

    private void initCategory() {
        reference = database.getReference(getString(R.string.tbl_category_name));

        categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("isDeleted");
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                    }

                    if (!categoryItems.isEmpty()) {
                        categoryAdapter = new ListProductCategoryAdapter(categoryItems, AdminManageFeedBackFragment.this);
                        binding.rcvListProductCategory.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
                        binding.rcvListProductCategory.setAdapter(categoryAdapter);
                    }
                    loadProducts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        reference = database.getReference(getString(R.string.tbl_product_name));
        binding.prgListProduct.setVisibility(View.VISIBLE);
        productItems = new ArrayList<>();
        Query query = categoryId != null ? reference.orderByChild("categoryId").equalTo(categoryId) : reference.orderByChild("deleted").equalTo(false);
        query.limitToFirst(ITEMS_PER_PAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (!product.isDeleted() && (!isSearch || product.getName().contains(searchText))) {
                            productItems.add(product);
                        }
                    }
                    productAdapter = new ListFeedBackProductAdapter(productItems, categoryItems);
                    binding.rcvListProduct.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    binding.rcvListProduct.setAdapter(productAdapter);
                    binding.prgListProduct.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.prgListProduct.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreProduct(int page, int itemsPerPage) {
        reference = database.getReference(getString(R.string.tbl_product_name));
        int endIndex = page * itemsPerPage;
        Query query = categoryId != null ? reference.orderByChild("categoryId").equalTo(categoryId).limitToFirst(endIndex) : reference.orderByChild("deleted").equalTo(false).limitToFirst(endIndex);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> newProducts = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (isSearch ? product.getName().contains(searchText) : !product.isDeleted()) {
                            newProducts.add(product);
                        }
                    }
                    int startIndex = productItems.size();
                    productItems.addAll(newProducts);
                    productAdapter.notifyItemRangeInserted(startIndex, newProducts.size());
                    binding.prgLoadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.prgLoadMore.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Failed to load more products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        categoryId = category.getId();
        currentPage = 1;
        productItems.clear();
        loadProducts();
    }
}
