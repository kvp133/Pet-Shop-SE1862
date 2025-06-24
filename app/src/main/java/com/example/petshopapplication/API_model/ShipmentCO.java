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
public class ShipmentCO {
    private String rate;
    @SerializedName("address_from")
    private AddressCO addressFrom;
    @SerializedName("address_to")
    private AddressCO addressTo;
    private ParcelCO parcel;
}
