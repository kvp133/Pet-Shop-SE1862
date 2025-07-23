package com.example.petshopapplication.API_model.ghn;
import com.google.gson.annotations.SerializedName;
public class GhnDistrict {
    @SerializedName("DistrictID")
    private int districtID;
    @SerializedName("DistrictName")
    private String districtName;
    public int getDistrictID() { return districtID; }
    public String getDistrictName() { return districtName; }
    @Override
    public String toString() { return districtName; }
}