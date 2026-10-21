package com.example.mahila;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = new TokenManager(newBase).getLanguage();
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
