package com.example.minitfg;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MiniTfgApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Manual initialization of Firebase to fix CONFIGURATION_NOT_FOUND error
        // Values taken from google-services.json
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:247061022286:android:c84b86118d907f5adce51f")
                        .setApiKey("AIzaSyAXiVWJD6Kp2hrzmRPpb8ClDQgPQrknDQk")
                        .setProjectId("studyprint-8afd4")
                        .setStorageBucket("studyprint-8afd4.firebasestorage.app")
                        .build();
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
