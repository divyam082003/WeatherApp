package com.gd.weatherapp.databse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CitiesWeatherDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "city.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CITIES = "cities";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CITY_NAME = "city_name";

    public CitiesWeatherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CITIES_TABLE = "CREATE TABLE " + TABLE_CITIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CITIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
        onCreate(db);
    }

    public boolean cityExists(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CITIES + " WHERE " + COLUMN_CITY_NAME + " = ?", new String[]{cityName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }


    public void addCity(String cityName) {
        if (!cityExists(cityName)) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_CITY_NAME, cityName);
            db.insert(TABLE_CITIES, null, values);
            db.close();
        }
    }


    public List<String> getCities() {
        List<String> cities = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_CITY_NAME + " FROM " + TABLE_CITIES + " ORDER BY " + COLUMN_ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                cities.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cities;
    }
}
