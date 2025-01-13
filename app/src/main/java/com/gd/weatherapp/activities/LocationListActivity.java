package com.gd.weatherapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gd.weatherapp.R;
import com.gd.weatherapp.adapter.CityWeatherItem;
import com.gd.weatherapp.api_pojo.OpenWeatherMap;
import com.gd.weatherapp.databinding.ActivityLocationListBinding;
import com.gd.weatherapp.databse.CitiesWeatherDatabaseHelper;
import com.gd.weatherapp.databse.WeatherData;
import com.gd.weatherapp.retrofit.RetrofitWeather;
import com.gd.weatherapp.retrofit.WeatherApi;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationListActivity extends AppCompatActivity {

    private CitiesWeatherDatabaseHelper dbHelper;

    private WeatherApi apiService;

    LocationManager locationManager;
    LocationListener locationListener;

    WeatherData currentWeatherData;

    private ItemAdapter<CityWeatherItem> itemAdapter;
    private FastAdapter<CityWeatherItem> fastAdapter;

    private SharedPreferences sharedPreferences;

    ActivityLocationListBinding locationListBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationListBinding = ActivityLocationListBinding.inflate(getLayoutInflater());
        setContentView(locationListBinding.getRoot());

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        applySavedTheme();

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        locationListBinding.locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationListBinding.locationsRecyclerView.setAdapter(fastAdapter);

        apiService = RetrofitWeather.getclient().create(WeatherApi.class);
        dbHelper = new CitiesWeatherDatabaseHelper(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                getWeatherData(latitude, longitude);
            }
        };

        locationListBinding.currentLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentWeatherData !=null){
                    shareWeatherData(currentWeatherData);
                }
            }
        });


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
        }

        loadSavedCities();

        locationListBinding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!dbHelper.cityExists(query)) {
                    dbHelper.addCity(query);
                        fetchWeatherData(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void loadSavedCities() {
        List<String> savedCities = dbHelper.getCities();
        for (String city : savedCities) {
                fetchWeatherData(city);
        }
    }

    private void fetchWeatherData(String cityName) {
        Call<OpenWeatherMap> call = apiService.getWeatherWithCityName(cityName);
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
                    WeatherData data = new WeatherData(cityName,temperature,humidity,maxTemp,minTemp,pressure,windSpeed,description,iconCode);

                    CityWeatherItem item = new CityWeatherItem(data);
                    itemAdapter.add(item);

                }
            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {
                Toast.makeText(LocationListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getWeatherData(double lat, double lon) {
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
                    currentWeatherData = new WeatherData(cityName,temperature,humidity,maxTemp,minTemp,pressure,windSpeed,description,iconCode);

                    locationListBinding.cityNameCurrentTV.setText(cityName);
                    locationListBinding.tempCurrentTV.setText(temperature + " C");
                    locationListBinding.weatherDescCurrentTV.setText(description);
                    Picasso.get().load("https://openweathermap.org/img/wn/" + iconCode + "@2x.png")
                            .placeholder(R.drawable.cloudy)
                            .into(locationListBinding.weatherIconCurrentIV);

                }

            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {
            }
        });
    }

    public void shareWeatherData(WeatherData weatherData) {
        String shareContent = String.format(
                "Weather in %s:\nTemperature: %s°C\nDescription: %s\nHumidity: %s%%\nMin Temp: %s°C\nMax Temp: %s°C\nPressure: %shPa\nWind Speed: %sm/s",
                weatherData.getCityName(),
                weatherData.getTemp(),
                weatherData.getDescription(),
                weatherData.getHumidity(),
                weatherData.getMinTemp(),
                weatherData.getMaxTemp(),
                weatherData.getPressure(),
                weatherData.getWind()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        // Start the share chooser
        startActivity(Intent.createChooser(shareIntent, "Share Weather Info with"));
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
                locationListBinding.locationListBG.setBackgroundResource(R.drawable.bg_dr);
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
            locationListBinding.locationListBG.setBackgroundResource(R.drawable.bg_dr);
        }
    }

}