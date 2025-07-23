package com.example.petshopapplication.API_model.ghn;
import com.google.gson.annotations.SerializedName;
public class CalculateFeeRequest {
    @SerializedName("service_type_id")
    private int serviceTypeId;
    @SerializedName("to_district_id")
    private int toDistrictId;
    @SerializedName("to_ward_code")
    private String toWardCode;
    @SerializedName("height")
    private int height;
    @SerializedName("length")
    private int length;
    @SerializedName("weight")
    private int weight;
    @SerializedName("width")
    private int width;
    @SerializedName("insurance_value")
    private int insuranceValue;

    // Constructor, getters and setters
    public CalculateFeeRequest(int serviceTypeId, int toDistrictId, String toWardCode, int height, int length, int weight, int width, int insuranceValue) {
        this.serviceTypeId = serviceTypeId;
        this.toDistrictId = toDistrictId;
        this.toWardCode = toWardCode;
        this.height = height;
        this.length = length;
        this.weight = weight;
        this.width = width;
        this.insuranceValue = insuranceValue;
    }
}