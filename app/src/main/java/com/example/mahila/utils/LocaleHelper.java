package com.example.mahila.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            languageCode = "en";
        }
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return context.createConfigurationContext(config);
    }
}
