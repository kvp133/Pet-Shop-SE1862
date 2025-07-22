package com.example.petshopapplication;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class PetShopApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d("PetShopApplication", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e("PetShopApplication", "Error initializing Firebase", e);
        }
    }
} 