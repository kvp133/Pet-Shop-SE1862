package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.databinding.ActivityUserPanelBinding;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserPanel extends AppCompatActivity {
    private ActivityUserPanelBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityUserPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        //get current user
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        //display user profile
        binding.tvEmail.setText(user.getEmail());
        fetchUserData();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnHomeLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        binding.btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListOrderActivity.class);
            startActivity(intent);
        });

        binding.btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListUserFeedbackActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserData() {
        databaseReference = database.getReference(getString(R.string.tbl_user_name));
        Query query = databaseReference.orderByChild("id").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userData = null;
                if (snapshot.exists()) {
                    //Get user data from database
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userData = dataSnapshot.getValue(User.class);
                    }
                    if (userData != null) {
                        binding.tvFullName.setText(userData.getFullName());

                        if (userData.getAvatar() != null) {
                            Glide.with(UserPanel.this)
                                    .load(userData.getAvatar())
                                    .placeholder(R.drawable.icon) // Optional placeholder image
                                    .into(binding.imgProfilePicture);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}