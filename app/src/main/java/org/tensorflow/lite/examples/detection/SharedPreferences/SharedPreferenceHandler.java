package org.tensorflow.lite.examples.detection.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceHandler {
    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setStringValues(Context ctx, String key, String DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(key, DataToSave);
        editor.commit();
    }

    public static String getStringValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, "");
    }

    public static void setIntValues(Context ctx, String key, int DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(key, DataToSave);
        editor.commit();
    }

    public static int getIntValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getInt(key, 0);
    }

    public static void setBooleanValue(Context context, String key, boolean value){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBooleanValue(Context context, String key){
        boolean value = getSharedPreferences(context).getBoolean(key, false);
        return value;
    }
}
