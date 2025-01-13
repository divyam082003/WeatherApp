package com.gd.weatherapp.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.gd.weatherapp.R;
import com.gd.weatherapp.databinding.ActivitySettingBinding;
import com.gd.weatherapp.scheduler.WeatherWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding settingBinding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(settingBinding.getRoot());

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        applySavedTheme();
        setupThemeToggleListener();
        setupRefreshIntervalSpinner();
        setupAutoThemeCheckBox();
    }
    public  void applySavedTheme() {
        boolean isAutoThemeOn = sharedPreferences.getBoolean("isAutoThemeOn", false);
        if (isAutoThemeOn) {
            applyAutoTheme();
            settingBinding.themeToggleSwitch.setEnabled(false);
            settingBinding.autoThemeCheckBox.setChecked(true);
        } else {
            settingBinding.themeToggleSwitch.setEnabled(true);
            boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
            if (isDarkModeOn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                settingBinding.themeToggleSwitch.setChecked(true);
                settingBinding.themeToggleText.setText("Enable Light Mode");
                settingBinding.modeIconIV.setImageResource(R.drawable.light_icon);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                settingBinding.themeToggleSwitch.setChecked(false);
                settingBinding.themeToggleText.setText("Enable Dark Mode");
                settingBinding.modeIconIV.setImageResource(R.drawable.dark_icon);
            }
        }
    }

    private void setupThemeToggleListener() {
        settingBinding.themeToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isAutoThemeOn = sharedPreferences.getBoolean("isAutoThemeOn", false);
            if (!isAutoThemeOn){
                if (isChecked && currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                    enableDarkMode();
                } else if (!isChecked && currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
                    enableLightMode();
                }
            }
        });
    }

    private void setupAutoThemeCheckBox() {
        settingBinding.autoThemeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                settingBinding.themeToggleSwitch.setEnabled(false);
                editor.putBoolean("isAutoThemeOn", true);
                editor.apply();
                applyAutoTheme();
            } else {
                settingBinding.themeToggleSwitch.setEnabled(true);
                editor.putBoolean("isAutoThemeOn", false);
                editor.apply();
            }
        });
    }
    private void applyAutoTheme() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isDayTime = (hour >= 6 && hour < 18);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // Apply light mode only if it's day and the current mode is dark
        if (isDayTime && currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            settingBinding.themeToggleSwitch.setChecked(false);
            enableLightMode();
            editor.putBoolean("isDarkModeOn", false); // Save mode preference
            editor.apply();
        }
        // Apply dark mode only if it's night and the current mode is light
        else if (!isDayTime && currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            settingBinding.themeToggleSwitch.setChecked(true);
            enableDarkMode();
            editor.putBoolean("isDarkModeOn", true); // Save mode preference
            editor.apply();
        }
    }

    private void enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        editor.putBoolean("isDarkModeOn", true);
        settingBinding.themeToggleText.setText("Enable Light Mode");
        settingBinding.modeIconIV.setImageResource(R.drawable.light_icon);
        editor.apply();
    }
    private void enableLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        editor.putBoolean("isDarkModeOn", false);
        settingBinding.themeToggleText.setText("Enable Dark Mode");
        settingBinding.modeIconIV.setImageResource(R.drawable.dark_icon);
        editor.apply();
    }
    private void setupRefreshIntervalSpinner() {
        Spinner spinner = settingBinding.refreshWeatherSpinner;
        String[] options = {"1 Day", "1 Hour"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String savedInterval = sharedPreferences.getString("refreshInterval", "1 Day");
        int position = adapter.getPosition(savedInterval);
        spinner.setSelection(position);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = (String) parentView.getItemAtPosition(position);
                editor.putString("refreshInterval", selectedOption);
                editor.apply();
                scheduleWeatherWorker(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }
    private void scheduleWeatherWorker(String interval) {
        long repeatInterval;
        if (interval.equals("1 Hour")) {
            repeatInterval = 1 * 60 ;
        } else {
            repeatInterval = 24 * 60 ;
        }

        WorkManager workManager = WorkManager.getInstance(this);

        workManager.cancelUniqueWork("WeatherWorker");

        PeriodicWorkRequest weatherRequest = new PeriodicWorkRequest.Builder(WeatherWorker.class,repeatInterval, TimeUnit.MINUTES)
                .build();

        workManager.enqueueUniquePeriodicWork("WeatherWorker", ExistingPeriodicWorkPolicy.REPLACE, weatherRequest);
    }
}

