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
public class District {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("city_id")
    private String cityId;
}
