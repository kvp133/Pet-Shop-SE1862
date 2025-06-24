package com.example.petshopapplication.API_model;

import java.util.List;
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
public class ShipmentSearchResponse {
    private int code;
    private String status;
    private List<ShipmentData> data;
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class ShipmentData {
        private String id;

        @SerializedName("order_id")
        private String orderId;

        @SerializedName("carrier_name")
        private String carrierName;

        @SerializedName("carrier_logo")
        private String carrierLogo;

        @SerializedName("carrier_code")
        private String carrierCode;

        @SerializedName("service_name")
        private String serviceName;

        @SerializedName("address_from")
        private Address addressFrom;

        @SerializedName("address_to")
        private Address addressTo;

        private Parcel parcel;
        private List<History> history;
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class Address {
        private String name;
        private String phone;
        private String email;
        private String street;
        private String district;

        @SerializedName("district_code")
        private int districtCode;

        private String city;

        @SerializedName("city_code")
        private int cityCode;

        private String ward;

        @SerializedName("ward_code")
        private int wardCode;
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class Parcel {
        private String name;
        private int quantity;
        private double width;
        private double height;
        private double length;
        private double weight;
        private double cweight;
        private String metadata;

        @SerializedName("cod_amount")
        private int codAmount;
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class History {
        private int status;
        private String message;
        private String statusText;
        private String statusDesc;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("updated_time")
        private long updatedTime;
    }
}
