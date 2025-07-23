package com.example.petshopapplication.model;

import java.io.Serializable;

// Thêm "implements Serializable" để có thể gửi đối tượng qua Intent
public class UAddress implements Serializable {
    private String addressId;
    private String userId;
    private String fullName;
    private String phone;
    private String city;
    private String district;
    private String ward;
    private String address; // Địa chỉ chi tiết (số nhà, tên đường)
    private boolean isDefault;

    // Các trường mới cho API GHN
    private int provinceId;
    private int districtId;
    private String wardCode;

    public UAddress() {
        // Cần constructor rỗng cho Firebase
    }

    // Constructor đầy đủ
    public UAddress(String addressId, String userId, String fullName, String phone, String city, String district, String ward, String address, boolean isDefault, int provinceId, int districtId, String wardCode) {
        this.addressId = addressId;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.isDefault = isDefault;
        this.provinceId = provinceId;
        this.districtId = districtId;
        this.wardCode = wardCode;
    }

    // Getters and Setters
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public int getProvinceId() { return provinceId; }
    public void setProvinceId(int provinceId) { this.provinceId = provinceId; }
    public int getDistrictId() { return districtId; }
    public void setDistrictId(int districtId) { this.districtId = districtId; }
    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
}