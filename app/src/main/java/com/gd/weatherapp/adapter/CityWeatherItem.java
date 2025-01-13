package com.gd.weatherapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gd.weatherapp.R;
import com.gd.weatherapp.activities.LocationListActivity;
import com.gd.weatherapp.databse.WeatherData;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CityWeatherItem extends AbstractItem<CityWeatherItem,CityWeatherItem.ViewHolder> {

    private WeatherData weatherData;

    public CityWeatherItem(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    @Override
    public int getType() {
        return R.id.listCardView;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.location_list_item; // Your layout file
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

   @Override
    public void bindView(CityWeatherItem.ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        // Bind the weather data to the views
        holder.cityNameListTV.setText(weatherData.getCityName());
        holder.tempListTV.setText(String.format("%s°C", weatherData.getTemp()));
        holder.weatherDescListTV.setText(weatherData.getDescription());

        
        Picasso.get().load("https://openweathermap.org/img/wn/" + weatherData.getIcon() + "@2x.png")
                .placeholder(R.drawable.cloudy)
                .into(holder.weatherIconListIV);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getContext() instanceof LocationListActivity) {
                    ((LocationListActivity) view.getContext()).shareWeatherData(weatherData);
                }
            }
        });
    }
    @Override
    public void unbindView(CityWeatherItem.ViewHolder holder) {
        super.unbindView(holder);

        holder.cityNameListTV.setText(null);
        holder.tempListTV.setText(null);
        holder.weatherDescListTV.setText(null);
        holder.weatherIconListIV.setImageDrawable(null);
    }


    public static class ViewHolder extends FastAdapter.ViewHolder<CityWeatherItem> {
        TextView cityNameListTV, tempListTV, weatherDescListTV;
        ImageView weatherIconListIV;

        public ViewHolder(View itemView) {
            super(itemView);
            cityNameListTV = itemView.findViewById(R.id.cityNameListTV);
            tempListTV = itemView.findViewById(R.id.tempListTV);
            weatherDescListTV = itemView.findViewById(R.id.weatherDescListTV);
            weatherIconListIV = itemView.findViewById(R.id.weatherIconListIV);
        }

        @Override
        public void bindView(CityWeatherItem item, List<Object> payloads) {

        }

        @Override
        public void unbindView(CityWeatherItem item) {

        }

    }
}
