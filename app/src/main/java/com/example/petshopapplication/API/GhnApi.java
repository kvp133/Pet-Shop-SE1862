package com.example.petshopapplication.API;

import com.example.petshopapplication.API_model.ghn.CalculateFeeRequest;
import com.example.petshopapplication.API_model.ghn.FeeResponse;
import com.example.petshopapplication.API_model.ghn.GhnDistrict;
import com.example.petshopapplication.API_model.ghn.GhnProvince;
import com.example.petshopapplication.API_model.ghn.GhnResponse;
import com.example.petshopapplication.API_model.ghn.GhnWard;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GhnApi {

    // Lấy danh sách Tỉnh/Thành phố
    @GET("master-data/province")
    Call<GhnResponse<List<GhnProvince>>> getProvinces(
            @Header("Token") String token
    );

    // Lấy danh sách Quận/Huyện
    @GET("master-data/district")
    Call<GhnResponse<List<GhnDistrict>>> getDistricts(
            @Header("Token") String token,
            @Query("province_id") int provinceId
    );

    // Lấy danh sách Phường/Xã
    @GET("master-data/ward")
    Call<GhnResponse<List<GhnWard>>> getWards(
            @Header("Token") String token,
            @Query("district_id") int districtId
    );

    // Tính phí vận chuyển
    @POST("v2/shipping-order/fee")
    Call<GhnResponse<FeeResponse>> calculateFee(
            @Header("Token") String token,
            @Header("ShopId") String shopId,
            @Body CalculateFeeRequest request
    );
}