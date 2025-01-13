package com.gd.weatherapp.databse;

public class WeatherData {
    private String cityName;

    private String temp;
    private String humidity;
    private String maxTemp;
    private String minTemp;

    private String pressure;
    private String wind;
    private String description;
    private String icon;

    private String sunrise;
    private String sunset;


    public WeatherData(String cityName, String temp, String humidity,String maxTemp,String minTemp, String pressure, String wind, String description, String icon) {
        this.cityName = cityName;
        this.temp = temp;
        this.humidity = humidity;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.pressure = pressure;
        this.wind = wind;
        this.description = description;
        this.icon = icon;
    }

    public WeatherData(String cityName, String temp, String humidity, String maxTemp, String minTemp, String pressure, String wind, String description, String icon, String sunrise, String sunset) {
        this.cityName = cityName;
        this.temp = temp;
        this.humidity = humidity;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.pressure = pressure;
        this.wind = wind;
        this.description = description;
        this.icon = icon;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    // Getters for each field
    public String getCityName() { return cityName; }

    public String getTemp() { return temp; }
    public String getHumidity() { return humidity; }

    public String getMaxTemp() {
        return maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public String getPressure() { return pressure; }
    public String getWind() { return wind; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }
}
