package com.example.petshopapplication.model;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
    private String id;
    private String name;
    private String image;
    private String createdAt;
    private boolean deleted;
    @NonNull
    @Override
    public String toString() {
        return name;
    }


}
