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
    public static final String LANGUAGE_HI = "hi";
    public static final String LANGUAGE_TA = "ta";
    public static final String LANGUAGE_TE = "te";
    public static final String LANGUAGE_ML = "ml";

    public static String normalizeLanguageCode(String languageCode) {
        if (languageCode == null) {
            return LANGUAGE_EN;
        }

        String normalized = languageCode.trim();
        if (normalized.isEmpty()) {
            return LANGUAGE_EN;
        }

        String lower = normalized.toLowerCase(Locale.US);
        if (lower.startsWith(LANGUAGE_KN)) {
            return LANGUAGE_KN;
        }
        if (lower.startsWith(LANGUAGE_HI)) {
            return LANGUAGE_HI;
        }
        if (lower.startsWith(LANGUAGE_TA)) {
            return LANGUAGE_TA;
        }
        if (lower.startsWith(LANGUAGE_TE)) {
            return LANGUAGE_TE;
        }
        if (lower.startsWith(LANGUAGE_ML)) {
            return LANGUAGE_ML;
        }
        if (lower.startsWith(LANGUAGE_EN)) {
            return LANGUAGE_EN;
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

        TokenManager tokenManager = new TokenManager(context);
        tokenManager.setLanguage(normalizedCode);
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
