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
public class Shipment {
    @SerializedName("address_from")
    private Address addressFrom;

    @SerializedName("address_to")
    private Address addressTo;

    @SerializedName("parcel")
    private Parcel parcel;
}