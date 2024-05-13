package com.example.myapi;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LuxandApiService {

    @Multipart
    @POST("/photo/detect")
    Call<ResponseBody> detectFace(
            @Header("token") String token,
            @Part MultipartBody.Part photo
    );

//    @POST("photo/detect")
//    Call<ApiResponse> detectFace(@Body RequestBody image);
}