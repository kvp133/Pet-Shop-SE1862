package com.example.petshopapplication.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.text.SimpleDateFormat;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Order implements Serializable {
    private String id;
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String shipmentId;
    private String rateId;
    private String carrierLogo;
    private String carrierName;
    private String districtId;
    private String district;
    private String cityId;
    private String city;
    private String wardId;
    private String ward;
    private double totalAmount;
    private List<OrderDetail> orderDetails;
    private Date orderDate;
    private String paymentId;
    private String status;
    private boolean isShipmentCreated = false;

    // Binh - add History list
//    private List<History> history;

//    public Order(boolean isShipmentCreated, String status, String paymentId, Date orderDate, List<OrderDetail> orderDetails, double totalAmount, String ward, String wardId, String city, String cityId, String district, String districtId, String carrierName, String carrierLogo, String rateId, String shipmentId, String userId, String id) {
//        this.isShipmentCreated = isShipmentCreated;
//        this.status = status;
//        this.paymentId = paymentId;
//        this.orderDate = orderDate;
//        this.orderDetails = orderDetails;
//        this.totalAmount = totalAmount;
//        this.ward = ward;
//        this.wardId = wardId;
//        this.city = city;
//        this.cityId = cityId;
//        this.district = district;
//        this.districtId = districtId;
//        this.carrierName = carrierName;
//        this.carrierLogo = carrierLogo;
//        this.rateId = rateId;
//        this.shipmentId = shipmentId;
//        this.userId = userId;
//        this.id = id;
//    }
}
