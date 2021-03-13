package com.muniryenigul.kam.interfaces;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {
    @GET
    Call<String> getPrices(@Url String url);
}