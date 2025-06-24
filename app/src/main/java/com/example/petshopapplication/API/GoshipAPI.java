package com.example.petshopapplication.API;

import com.example.petshopapplication.API_model.CityResponse;
import com.example.petshopapplication.API_model.CreateOrderRequest;
import com.example.petshopapplication.API_model.CreateOrderResponse;
import com.example.petshopapplication.API_model.DistrictResponse;
import com.example.petshopapplication.API_model.RateRequest;
import com.example.petshopapplication.API_model.RateResponse;
import com.example.petshopapplication.API_model.ShipmentSearchResponse;
import com.example.petshopapplication.API_model.WardResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoshipAPI {
    @GET("/api/v2/cities")
    Call<CityResponse> getCities(@Header("Accept") String accept,
                                 @Header("Content-Type") String contentType,
                                 @Header("Authorization") String authToken);

    @GET("/api/v2/cities/{cityId}/districts")
    Call<DistrictResponse> getDistricts(@Path("cityId") String cityId,
                                        @Header("Accept") String accept,
                                        @Header("Content-Type") String contentType,
                                        @Header("Authorization") String authToken);

    @GET("/api/v2/districts/{districtId}/wards")
    Call<WardResponse> getWards(@Path("districtId") String districtId,
                                @Header("Accept") String accept,
                                @Header("Content-Type") String contentType,
                                @Header("Authorization") String authToken);

    @POST("/api/v2/rates")
    Call<RateResponse> getRates(
            @Header("Accept") String accept,
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authToken,
            @Body RateRequest rateRequest
    );

    @POST("/api/v2/shipments")
    Call<CreateOrderResponse> createOrder(
            @Header("Accept") String accept,
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authToken,
            @Body CreateOrderRequest createOrderRequest
    );

    @GET("/api/v2/shipments/search")
    Call<ShipmentSearchResponse> searchShipment(
            @Header("Accept") String accept,
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authToken,
            @Query("code") String code
    );
}
