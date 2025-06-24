package com.example.petshopapplication.API;

import com.example.petshopapplication.model.History;

import java.util.List;

public interface FirebaseDataCallback {
    void onDataLoaded(int totalWeight, int totalWidth, int totalHeight, int totalLength);
}
