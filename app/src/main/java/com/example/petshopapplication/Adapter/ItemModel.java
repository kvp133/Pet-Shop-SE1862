package com.example.petshopapplication.Adapter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemModel {
    private String sizeName;
    private int colorImage; // Drawable resource ID
    private String colorName;
    private int stock;
    public ItemModel(String sizeName, int colorImage, String colorName,int stock) {
        this.sizeName = sizeName;
        this.colorImage = colorImage;
        this.colorName = colorName;
                this.stock = stock;
    }
}
