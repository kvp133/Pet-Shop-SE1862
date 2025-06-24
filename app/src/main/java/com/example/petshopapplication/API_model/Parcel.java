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
public class Parcel {
    @SerializedName("cod")
    private int cod;

    @SerializedName("amount")
    private int amount;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    @SerializedName("length")
    private int length;

    @SerializedName("weight")
    private int weight;
}