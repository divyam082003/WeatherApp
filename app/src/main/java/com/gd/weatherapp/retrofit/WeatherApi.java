package com.gd.weatherapp.retrofit;

import com.gd.weatherapp.api_pojo.OpenWeatherMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather?&appid=f497c7d13b522a34cacc76b8a1821338&units=metric")
    Call<OpenWeatherMap>getWeatherWithLocation(@Query("lat")double lat, @Query("lon")double lon);

    @GET("weather?&appid=f497c7d13b522a34cacc76b8a1821338&units=metric")
    Call<OpenWeatherMap>getWeatherWithCityName(@Query("q")String city);


}
