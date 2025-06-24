package com.example.petshopapplication.API;

import com.example.petshopapplication.model.History;

import java.util.List;

public interface OrderHistoryCallback {
    void onHistoryLoaded(List<History> historyList, boolean isFinalStatus905);
}
