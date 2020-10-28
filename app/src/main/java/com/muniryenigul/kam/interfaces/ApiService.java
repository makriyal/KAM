package com.muniryenigul.kam.interfaces;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {

    @GET
    Call<String> getPrices(@Url String url);


//    @GET(".")
//    Call<String> getStringResponse();
//    //p=Products&q=
//    @GET("index.php")
//    Call<String> get1001(
//            @Query("p") String p,
//            @Query("q") String q);
//
//    @GET
//    Call<String> get1001Alt(
//            @Url String url);
//
//    @GET("ara/")
//    Call<String> getAlt(
//            @Query("search_performed") String search_performed,
//            @Query("q") String q);

}