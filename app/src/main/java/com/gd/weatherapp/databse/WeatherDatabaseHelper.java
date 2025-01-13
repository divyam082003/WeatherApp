package com.gd.weatherapp.databse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "weather";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CITY_NAME = "city_name";
    private static final String COLUMN_TEMP = "temperature";
    private static final String COLUMN_HUMIDITY = "humidity";

    private static final String COLUMN_MAX_TEMP = "maxTemp";
    private static final String COLUMN_MIN_TEMP = "minTemp";

    private static final String COLUMN_PRESSURE = "pressure";
    private static final String COLUMN_WIND = "wind";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_ICON = "icon";

    private static final String COLUMN_SUNRISE = "sunrise";
    private static final String COLUMN_SUNSET = "sunset";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_LAST_USED = "last_used";

    public WeatherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CITY_NAME + " TEXT, "
                + COLUMN_TEMP + " TEXT, "
                + COLUMN_HUMIDITY + " TEXT, "
                + COLUMN_MAX_TEMP + " TEXT, "
                + COLUMN_MIN_TEMP + " TEXT, "
                + COLUMN_PRESSURE + " TEXT, "
                + COLUMN_WIND + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_ICON + " TEXT,"
                + COLUMN_SUNRISE + " TEXT,"
                + COLUMN_SUNSET + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_LAST_USED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertWeatherData(String cityName, String temperature, String humidity,String maxTemp,String minTemp, String pressure, String wind, String description, String icon,String sunrise,String sunset) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, cityName);
        values.put(COLUMN_TEMP, temperature);
        values.put(COLUMN_HUMIDITY, humidity);
        values.put(COLUMN_MAX_TEMP, maxTemp);
        values.put(COLUMN_MIN_TEMP, minTemp);
        values.put(COLUMN_PRESSURE, pressure);
        values.put(COLUMN_WIND, wind);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_SUNRISE, sunrise);
        values.put(COLUMN_SUNSET, sunset);
        values.put(COLUMN_LAST_USED, 0); // Set default last_used
        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    public boolean cityExists(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                COLUMN_CITY_NAME + "=?", new String[]{cityName},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public void updateWeatherData(String cityName, String temperature, String humidity,String maxTemp,String minTemp, String pressure, String wind, String description, String icon,String sunrise,String sunset) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEMP, temperature);
        values.put(COLUMN_HUMIDITY, humidity);
        values.put(COLUMN_MAX_TEMP, maxTemp);
        values.put(COLUMN_MIN_TEMP, minTemp);
        values.put(COLUMN_PRESSURE, pressure);
        values.put(COLUMN_WIND, wind);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_SUNRISE, sunrise);
        values.put(COLUMN_SUNSET, sunset);
        values.put(COLUMN_LAST_USED, 1);
        db.update(TABLE_NAME, values, COLUMN_CITY_NAME + "=?", new String[]{cityName});
        db.close();
    }

    public WeatherData getLastWeatherData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_LAST_USED + " = 1 "
                + "ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            WeatherData weatherData = new WeatherData(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HUMIDITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAX_TEMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIN_TEMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRESSURE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WIND)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUNRISE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUNSET))

            );
            cursor.close();
            return weatherData;
        } else {
            return null;
        }
    }

    public void resetLastUsed() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_USED, 0);
        db.update(TABLE_NAME, values, null, null);
        db.close();
    }

}