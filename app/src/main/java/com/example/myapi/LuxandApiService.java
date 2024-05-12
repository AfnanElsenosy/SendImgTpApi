package com.example.myapi;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;

public interface LuxandApiService {
    @Headers({
            "token: c4da8898b06a4c2b95d26c62caccd1fd",
            "Content-Type: application/octet-stream",


    })
    @Multipart
    @POST("photo/detect")
    Call<ApiResponse> detectFace(@Body RequestBody image);
}