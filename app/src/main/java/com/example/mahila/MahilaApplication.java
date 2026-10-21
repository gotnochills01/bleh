package com.example.mahila;

import android.app.Application;

import com.example.mahila.utils.LocaleHelper;

public class MahilaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.applyPersistedLanguage(this);
    }
}
