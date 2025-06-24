package com.example.petshopapplication.API_model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CreateOrderResponse {
    private int code;
    private String status;
    private String id;
    private String cod;
    private String fee;
    @SerializedName("tracking_number")
    private String trackingNumber;
    private String carrier;
    @SerializedName("carrier_short_name")
    private String carrierShortName;
    @SerializedName("created_at")
    private String createdAt;
}
