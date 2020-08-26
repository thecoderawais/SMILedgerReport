package com.example.ledgerreport.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {

    private final String FAV_PREFS_KEY = "MY_FAVOURITIES_KEY";
    private final Context context;

    public MySharedPreference(Context context) {
        this.context = context;
    }

    public void saveCompanyCode(String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(FAV_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void saveCompanyName(String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(FAV_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getCompanyCode(String key) {
        SharedPreferences prefs = context.getSharedPreferences(FAV_PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

    public String getCompanyName(String key) {
        SharedPreferences prefs = context.getSharedPreferences(FAV_PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

}
