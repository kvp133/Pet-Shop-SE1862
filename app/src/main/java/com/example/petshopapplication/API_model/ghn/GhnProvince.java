package com.example.petshopapplication.API_model.ghn;
import com.google.gson.annotations.SerializedName;
public class GhnProvince {
    @SerializedName("ProvinceID")
    private int provinceID;
    @SerializedName("ProvinceName")
    private String provinceName;
    public int getProvinceID() { return provinceID; }
    public String getProvinceName() { return provinceName; }
    @Override
    public String toString() { return provinceName; }
}