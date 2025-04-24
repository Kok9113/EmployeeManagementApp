package com.example.employeemanagementapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ListPreference listPreference = findPreference("language");
            if (listPreference != null) {
                listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    String value = (String) newValue;
                    updateLocale(value);
                    return true;
                });
                updateLocale(listPreference.getValue());
            } else {
                Log.e("SettingsFragment", "Language preference not found");
            }

            SwitchPreferenceCompat themeSwitch = findPreference("theme");
            if (themeSwitch != null) {
                themeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                        boolean isLightMode = (boolean) newValue;
                        Log.d("theme: ", String.valueOf(isLightMode));
                        updateTheme(isLightMode);
                        return true;
                    }
                });
            } else {
                Log.e("SettingsFragment", "Theme preference not found");
            }
        }

        private void updateLocale(String lang) {
            Locale locale;
            if (lang.equals("Tiếng Việt")) {
                locale = new Locale("vi"); // Sửa từ "ar" thành "vi"
            } else {
                locale = Locale.ENGLISH;
            }
            if (!locale.equals(Locale.getDefault())) {
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.setLocale(locale);

                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                preferences.edit().putString("selected_language", lang).apply();
                requireActivity().recreate();
            }
        }

        private void updateTheme(boolean isLightMode) {
            if (isLightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
    }
}