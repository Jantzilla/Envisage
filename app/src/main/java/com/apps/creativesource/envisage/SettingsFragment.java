package com.apps.creativesource.envisage;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.pref_settings);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("notifications")) {
            Map<String, Object> notifications = new HashMap<>();
            notifications.put("notifications", sharedPreferences.getBoolean(key, true));
        }
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
