package com.example.petshopapplication.model;

import java.io.Serializable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Cart implements Serializable {
    private String cartId;
    private String userId;
    private String productId;
    private int quantity;
    private String selectedColorId;
    private String selectedVariantId;
    private Boolean isChecked;

}
