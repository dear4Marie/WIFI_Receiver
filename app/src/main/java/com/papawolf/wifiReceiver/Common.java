package com.papawolf.wifiReceiver;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by papawolf on 2017-08-28.
 */

public class Common {

    public static long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static void setPreferencesString(Context context, String key, String value) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public static void setPreferencesInt(Context context, String key, Integer value) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    public static void setPreferencesBoolean(Context context, String key, Boolean value) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();

        editor.putBoolean(key, value);
        editor.commit();

        Dlog.d(key + " -> " + value);
    }

    public static String getPreferencesString(Context context, String key) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
        return p.getString(key, "");
    }

    public static Integer getPreferencesInt(Context context, String key) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
        return p.getInt(key, 0);
    }

    public static Boolean getPreferencesBoolean(Context context, String key) {
        SharedPreferences p = context.getSharedPreferences("Setting", context.MODE_PRIVATE);

        Boolean value = p.getBoolean(key, false);

        Dlog.d(key + " -> " + value);

        return value;
    }
}
