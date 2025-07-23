package com.example.petshopapplication.API_model.ghn;
import com.google.gson.annotations.SerializedName;
public class FeeResponse {
    @SerializedName("total")
    private long total;
    public long getTotal() { return total; }
}