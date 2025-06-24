package com.example.petshopapplication.API_model;
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
public class ParcelCO {
    private String cod;
    private String weight;
    private String width;
    private String height;
    private String length;
    private String metadata;
}
