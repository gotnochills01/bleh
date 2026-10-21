package com.example.mahila.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.mahila.api.TokenManager;

import java.util.Locale;

public class LocaleHelper {
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_KN = "kn";

    public static String normalizeLanguageCode(String languageCode) {
        if (languageCode == null) {
            return LANGUAGE_EN;
        }

        String normalized = languageCode.trim();
        if (normalized.isEmpty()) {
            return LANGUAGE_EN;
        }

        if (normalized.equalsIgnoreCase(LANGUAGE_KN) || normalized.toLowerCase(Locale.getDefault()).startsWith(LANGUAGE_KN)) {
            return LANGUAGE_KN;
        }

        return LANGUAGE_EN;
    }

    public static Locale getLocale(String languageCode) {
        return new Locale(normalizeLanguageCode(languageCode));
    }

    public static Context setLocale(Context context, String languageCode) {
        if (context == null) {
            return null;
        }

        String normalizedCode = normalizeLanguageCode(languageCode);
        Locale locale = getLocale(normalizedCode);
        Locale.setDefault(locale);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedCode));

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(new LocaleList(locale));
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

    public static void applyPersistedLanguage(Context context) {
        if (context == null) {
            return;
        }
        String languageCode = new TokenManager(context).getLanguage();
        setLocale(context, languageCode);
    }
}
