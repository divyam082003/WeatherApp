package com.gd.weatherapp.scheduler;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gd.weatherapp.R;
import com.gd.weatherapp.api_pojo.OpenWeatherMap;
import com.gd.weatherapp.databse.WeatherDatabaseHelper;
import com.gd.weatherapp.retrofit.RetrofitWeather;
import com.gd.weatherapp.retrofit.WeatherApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherWorker extends Worker {

    private WeatherDatabaseHelper dbHelper;
    private Context context;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        dbHelper = new WeatherDatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        double lat = getLatFromPreferences();
        double lon = getLonFromPreferences();

        fetchWeatherDataAndSaveToDb(lat, lon);

        return Result.success();
    }

    private void fetchWeatherDataAndSaveToDb(double lat, double lon) {
        WeatherApi apiService = RetrofitWeather.getclient().create(WeatherApi.class);
        Call<OpenWeatherMap> call = apiService.getWeatherWithLocation(lat, lon);
        call.enqueue(new Callback<OpenWeatherMap>() {
            @Override
            public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String cityName = response.body().getName() + ", " + response.body().getSys().getCountry();
                    String temperature = String.valueOf(response.body().getMain().getTemp());
                    String description = response.body().getWeather().get(0).getDescription();
                    String humidity = String.valueOf(response.body().getMain().getHumidity());
                    String minTemp = String.valueOf(response.body().getMain().getTempMin());
                    String maxTemp = String.valueOf(response.body().getMain().getTempMax());
                    String pressure = String.valueOf(response.body().getMain().getPressure());
                    String windSpeed = String.valueOf(response.body().getWind().getSpeed());
                    String iconCode = response.body().getWeather().get(0).getIcon();
                    String sunrise = convertUnixToLocalTime(response.body().getSys().getSunrise(), response.body().getTimezone());
                    String sunset = convertUnixToLocalTime(response.body().getSys().getSunset(), response.body().getTimezone());

                    dbHelper.resetLastUsed();

                    if (dbHelper.cityExists(cityName)) {
                        dbHelper.updateWeatherData(cityName, temperature, humidity, maxTemp, minTemp, pressure, windSpeed, description, iconCode,sunrise,sunset);
                    } else {
                        dbHelper.insertWeatherData(cityName, temperature, humidity, maxTemp, minTemp, pressure, windSpeed, description, iconCode,sunrise,sunset);
                    }

                    showNotification(cityName,temperature,description);
                }
            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {

            }
        });
    }

    private String convertUnixToLocalTime(long unixTime, int timezoneOffset) {
        // Convert the UNIX time (seconds) to milliseconds
        Date date = new Date((unixTime + timezoneOffset) * 1000L);
        // Format the time into a readable format
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // The date is now in UTC
        return sdf.format(date);
    }

    private void showNotification(String cityName, String temperature, String description) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "WeatherUpdateChannel";
            String descriptionText = "Channel for weather updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WEATHER_UPDATE_CHANNEL", name, importance);
            channel.setDescription(descriptionText);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WEATHER_UPDATE_CHANNEL")
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("Weather Update")
                .setContentText(cityName + ": " + temperature + "°C, " + description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

        }else{
            notificationManagerCompat.notify(1, builder.build());
        }

    }

    private double getLatFromPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("LocPrefs", Context.MODE_PRIVATE);
        return Double.longBitsToDouble(prefs.getLong("lat", Double.doubleToLongBits(0.0)));
    }

    private double getLonFromPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("LocPrefs", Context.MODE_PRIVATE);
        return Double.longBitsToDouble(prefs.getLong("lon", Double.doubleToLongBits(0.0)));
    }


}
