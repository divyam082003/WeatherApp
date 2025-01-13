package com.gd.weatherapp.activities;

import static androidx.core.location.LocationManagerCompat.requestLocationUpdates;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.android.gms.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gd.weatherapp.R;
import com.gd.weatherapp.api_pojo.OpenWeatherMap;
import com.gd.weatherapp.databinding.ActivityMainBinding;
import com.gd.weatherapp.databse.WeatherData;
import com.gd.weatherapp.databse.WeatherDatabaseHelper;
import com.gd.weatherapp.retrofit.RetrofitWeather;
import com.gd.weatherapp.retrofit.WeatherApi;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 1001;
    ActivityMainBinding mainBinding;
    LocationManager locationManager;
    LocationListener locationListener;
    LocationRequest locationRequest;
    WeatherDatabaseHelper dbHelper;
    double lat,lon;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());



        dbHelper = new WeatherDatabaseHelper(this);

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        applySavedTheme();

        WeatherData lastWeatherData = dbHelper.getLastWeatherData();
        if (lastWeatherData != null) {
            displayWeatherData(lastWeatherData);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Build the location request with high accuracy
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.d("LocationUpdate", "Lat: " + lat + ", Lon: " + lon);
                saveLocationInPreferences(lat, lon);
                getWeatherData(lat,lon);
            }
        };

        mainBinding.addBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationListActivity.class);
                startActivity(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, 1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
        }

    }

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied, proceed to request location updates
                    requestLocationUpdates();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the settings so we won't show a dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    // Location services are enabled, proceed to request location updates
                    requestLocationUpdates();
                } else {

                    Log.d("LocationSettings", "User did not enable location services");
                }
                break;
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Location permission granted");
                checkLocationSettings();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},2);
                    }
                }
            }
        } else {
            Log.d("Permissions", "Location permission denied");
        }

    }
    public void getWeatherData(double lat,double lon) {
        WeatherApi apiService = RetrofitWeather.getclient().create(WeatherApi.class);
        Call<OpenWeatherMap> call = apiService.getWeatherWithLocation(lat,lon);
        call.enqueue(new Callback<OpenWeatherMap>() {
            @Override
            public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String cityName = response.body().getName() + ", " + response.body().getSys().getCountry();
                    String temperature = String.valueOf(response.body().getMain().getTemp());
                    String description = response.body().getWeather().get(0).getDescription();
                    String humidity = String.valueOf(response.body().getMain().getHumidity());
                    String minTemp =  String.valueOf(response.body().getMain().getTempMin());
                    String maxTemp =  String.valueOf(response.body().getMain().getTempMax());
                    String pressure = String.valueOf(response.body().getMain().getPressure());
                    String windSpeed = String.valueOf(response.body().getWind().getSpeed());
                    String iconCode = response.body().getWeather().get(0).getIcon();
                    String sunrise = convertUnixToLocalTime(response.body().getSys().getSunrise(), response.body().getTimezone());
                    String sunset = convertUnixToLocalTime(response.body().getSys().getSunset(), response.body().getTimezone());


                    WeatherData newweatherData = new WeatherData(cityName, temperature,
                            humidity,
                            maxTemp,
                            minTemp,
                            pressure,
                            windSpeed,
                            description,
                            iconCode,sunrise,sunset);

                   displayWeatherData(newweatherData);

                    dbHelper.resetLastUsed();
                    if (dbHelper.cityExists(cityName)) {
                        dbHelper.updateWeatherData(cityName, temperature,
                                humidity,
                                maxTemp,
                                minTemp,
                                pressure,
                                windSpeed,
                                description,
                                iconCode,
                                sunrise,
                                sunset
                        );
                    }
                    else {
                        dbHelper.insertWeatherData(cityName, temperature,
                                humidity,
                                maxTemp,
                                minTemp,
                                pressure,
                                windSpeed,
                                description,
                                iconCode,
                                sunrise,
                                sunset
                        );
                    }


                }

            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {

            }
        });

    }

    private String convertUnixToLocalTime(long unixTime, int timezoneOffset) {
        Date date = new Date((unixTime + timezoneOffset) * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // The date is now in UTC
        return sdf.format(date);
    }
    private void displayWeatherData(WeatherData weatherData) {
        mainBinding.cityNameTV.setText(weatherData.getCityName());
        mainBinding.tempTV.setText(weatherData.getTemp() + "°C");
        mainBinding.weatherConditionTV.setText(weatherData.getDescription());
        mainBinding.humidityValTV.setText(weatherData.getHumidity() + " %");
        mainBinding.maxTempValTV.setText(weatherData.getMaxTemp() + "°C");
        mainBinding.minTempValTV.setText(weatherData.getMinTemp() + "°C");
        mainBinding.pressureValTV.setText(weatherData.getPressure()+"Pa");
        mainBinding.windValTV.setText(weatherData.getWind()+"m/s");
        Picasso.get().load("https://openweathermap.org/img/wn/" + weatherData.getIcon() + "@2x.png")
                .placeholder(R.drawable.cloudy)
                .into(mainBinding.weatherIconIV);
        mainBinding.sunriseTV.setText(weatherData.getSunrise());
        mainBinding.sunsetTV.setText(weatherData.getSunset());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public  void applySavedTheme() {
        boolean isAutoThemeOn = sharedPreferences.getBoolean("isAutoThemeOn", false);
        if (isAutoThemeOn) {
            // Apply auto theme based on the time
            applyAutoTheme();
        } else {
            // Apply theme based on the saved preference
            boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
            if (isDarkModeOn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mainBinding.mainBG.setBackgroundResource(R.drawable.bg_dr);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    private void applyAutoTheme() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isDayTime = (hour >= 6 && hour < 18);

        if (isDayTime) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            mainBinding.mainBG.setBackgroundResource(R.drawable.bg_dr);
        }
    }

    public void saveLocationInPreferences(double lat, double lon) {
        SharedPreferences prefs = getSharedPreferences("LocPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lat", Double.doubleToLongBits(lat));
        editor.putLong("lon", Double.doubleToLongBits(lon));
        editor.apply();
    }

}