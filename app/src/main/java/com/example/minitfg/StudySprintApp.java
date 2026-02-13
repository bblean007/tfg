package com.example.minitfg;

import android.app.Application;

public class StudySprintApp extends Application {
    private static StudySprintApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static StudySprintApp getInstance() {
        return instance;
    }
}
