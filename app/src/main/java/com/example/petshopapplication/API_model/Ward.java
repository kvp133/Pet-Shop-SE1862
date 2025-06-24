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
public class Ward {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("district_id")
    private String districtId;
}