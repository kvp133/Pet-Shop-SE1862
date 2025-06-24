package com.example.petshopapplication.model;

import java.io.Serializable;

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
public class Color implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private int stock;
    private int deliveringQuantity;
    private boolean isDeleted;
    private String createdAt;
}
