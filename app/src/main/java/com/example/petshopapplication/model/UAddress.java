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
public class UAddress implements Serializable {
    private String addressId;
    private String fullName;
    private String phone;
    private String city;
    private String cityId;
    private String district;
    private String districtId;
    private String ward;
    private String wardId;
    private boolean isDefault;
    private String userId;

}
