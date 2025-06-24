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
public class Rate {
    @SerializedName("id")
    private String id;

    @SerializedName("carrier_name")
    private String carrierName;

    @SerializedName("carrier_logo")
    private String carrierLogo;

    @SerializedName("service")
    private String service;

    @SerializedName("expected")
    private String expected;

    @SerializedName("cod_fee")
    private int codFee;

    @SerializedName("total_fee")
    private int totalFee;

    @SerializedName("total_amount")
    private int totalAmount;
}