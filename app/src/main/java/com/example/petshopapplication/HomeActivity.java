package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.CategoryAdapter;
import com.example.petshopapplication.Adapter.FeedBackAdapter;
import com.example.petshopapplication.Adapter.ProductAdapter;
import com.example.petshopapplication.databinding.ActivityHomeBinding;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    RecyclerView.Adapter productAdapter, categoryAdapter, feedbackAdapter;

    //Authentication with firebase
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //welcome name
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        database = FirebaseDatabase.getInstance();
        //initNewProduct();
        initCategory();
        initFeedback();

        binding.btnHomeSearch.setOnClickListener(v -> {
            String searchText = binding.tvSearch.getText().toString().trim();
            if(!searchText.isEmpty()) {
                Intent intent = new Intent(HomeActivity.this, ListProductActivity.class);
                intent.putExtra("searchText", searchText);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });

        binding.btnHomeCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        binding.tvViewListProduct.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ListProductActivity.class);
            startActivity(intent);
        });
    }

    private void initNewProduct(List<Category> categoryItems){
        reference = database.getReference(getString(R.string.tbl_product_name));
        //Display progress bar
        binding.prgHomeNewProduct.setVisibility(View.VISIBLE);

        List<Product> productItems = new ArrayList<>();
        Query query = reference.orderByChild("createdAt");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        boolean isDeleted = Boolean.TRUE.equals(dataSnapshot.child("deleted").getValue(Boolean.class));
                        Product product = dataSnapshot.getValue(Product.class);
                        if(!isDeleted && productItems.size() < 10) {
                            productItems.add(product);
                        }
                    }
                    productAdapter = new ProductAdapter(productItems, categoryItems);
                    binding.rcvNewProduct.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rcvNewProduct.setAdapter(productAdapter);
                    binding.prgHomeNewProduct.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void initFeedback() {
        reference = database.getReference(getString(R.string.tbl_feedback_name));
        //Display progress bar
        binding.prgFeedback.setVisibility(View.VISIBLE);

        List<FeedBack> feedbackItems = new ArrayList<>();

        Query query = reference.orderByChild("rating");
        query.limitToLast(4).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        feedbackItems.add(dataSnapshot.getValue(FeedBack.class));
                    }
                    fetchUserData(feedbackItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchUserData(List<FeedBack> feedbackItems) {
        reference = database.getReference(getString(R.string.tbl_user_name));
        List<User> userItems = new ArrayList<>();
        for(FeedBack feedBack : feedbackItems) {
            //Reference to the user table
            reference = database.getReference(getString(R.string.tbl_user_name));
            //Get user data by user Id in feed back
            Query query = reference.orderByChild("id").equalTo(feedBack.getUserId());
            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        //Get user data from database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            userItems.add(user);
                        }
                        if(userItems.size() > 0) {
                            feedbackAdapter = new FeedBackAdapter(feedbackItems, userItems);
                            binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.VERTICAL, true));
                            binding.rcvFeedback.setAdapter(feedbackAdapter);

                            binding.btnHomeUserPanel.setOnClickListener(v -> {
                                Intent intent = new Intent(HomeActivity.this, UserPanel.class);
                                intent.putExtra("userId", userItems.get(0).getId());
                                startActivity(intent);
                            });
                        }
                        binding.prgFeedback.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void initCategory() {
        reference = database.getReference(getString(R.string.tbl_category_name));
        //Display progress bar
        binding.prgHomeCategory.setVisibility(View.VISIBLE);

        List<Category> categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("deleted").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                    }

                    if(categoryItems.size() > 1) {
                        categoryAdapter = new CategoryAdapter(categoryItems);
                        binding.rcvHomeCategory.setLayoutManager(new GridLayoutManager(HomeActivity.this, 3));
                        binding.rcvHomeCategory.setAdapter(categoryAdapter);
                    }
                    binding.prgHomeCategory.setVisibility(View.GONE);
                    initNewProduct(categoryItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}