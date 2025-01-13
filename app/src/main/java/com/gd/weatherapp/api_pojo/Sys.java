
package com.gd.weatherapp.api_pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Sys {

    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("sunrise")
    @Expose
    private long sunrise;
    @SerializedName("sunset")
    @Expose
    private long sunset;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = Long.parseLong(sunrise);
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = Long.parseLong(sunset);
    }

}
