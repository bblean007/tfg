package com.example.minitfg.network;

import com.example.minitfg.StudySprintApp;
import com.example.minitfg.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Intent;
import com.example.minitfg.LoginActivity;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(StudySprintApp.getInstance());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    
                    String token = sessionManager.getToken();
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }
                    
                    Request request = requestBuilder.build();
                    Response response = chain.proceed(request);

                    // Global handling of 401 Unauthorized
                    if (response.code() == 401) {
                        sessionManager.logout();
                        Intent intent = new Intent(StudySprintApp.getInstance(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        StudySprintApp.getInstance().startActivity(intent);
                    }

                    return response;
                })
                .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
