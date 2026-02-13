package com.example.minitfg.network;

import com.example.minitfg.network.models.AuthResponse;
import com.example.minitfg.network.models.LoginRequest;
import com.example.minitfg.network.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import com.example.minitfg.network.models.SaveScoreResponse;
import com.example.minitfg.network.models.Score;
import com.example.minitfg.network.models.ScoreRequest;
import java.util.List;
import retrofit2.http.Query;

public interface ApiService {
    @POST("login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("me")
    Call<AuthResponse> validateToken(@Header("Authorization") String token);

    @POST("scores")
    Call<SaveScoreResponse> saveScore(@Header("Authorization") String token, @Body ScoreRequest request);

    @POST("forgot-password")
    Call<Void> forgotPassword(@Body LoginRequest request);

    @GET("scores")
    Call<List<Score>> getScores(
        @Query("subject") String subject, 
        @Query("period") String period,
        @Query("userId") Integer userId
    );
}
