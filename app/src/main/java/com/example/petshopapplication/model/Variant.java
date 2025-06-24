package com.example.petshopapplication.model;


import java.io.Serializable;
import java.util.List;

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
public class Variant implements Serializable {
    private String id;
    private Size size;
    private List<Color> listColor;
    private Dimension dimension;
    private double price;
    private int stock;
    private int deliveringQuantity;
    private boolean isDeleted;
    private String createdAt;
}
