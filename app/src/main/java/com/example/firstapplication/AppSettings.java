package com.example.firstapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import java.util.Set;

public class AppSettings

{
    public static final String USER_ID_PREF_KEY = "userid_pref_key";
    private static SharedPreferences sDefaultPreferences;

    public static void setDefaultPreferences() {
        if(sDefaultPreferences == null) {
            sDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(ContextContainer.getContext());
        }

    }



    public static String get(String key)

    {

        return get(key, (String) null);

    }

    public static String get(String key, String defaultValue)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");

        setDefaultPreferences();

        return sDefaultPreferences.getString(key, defaultValue);

    }


    public static boolean getBoolean(String key, boolean defaultValue)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();

        return sDefaultPreferences.getBoolean(key, defaultValue);

    }


    public static String get(String key, Context context)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        if (context == null)

            throw  new NullPointerException("context == null");




        return sDefaultPreferences.getString(key, null);

    }


    public static void set(String key, String value)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();




        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.putString(key, value);

        editor.apply();

    }

    public static void set(String key, boolean value)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();




        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.putBoolean(key, value);

        editor.apply();

    }


    public static void set(String key, int value)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();




        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.putInt(key, value);

        editor.apply();

    }
    public static void set(String key, long value)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();




        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.putLong(key, value);

        editor.apply();

    }
    public static void removeKey(String key)

    {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");




        setDefaultPreferences();




        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.remove(key);

        editor.apply();

    }


    public static void set(String key, Set<String> values) {

        if (TextUtils.isEmpty(key))

            throw new IllegalArgumentException("key");

        setDefaultPreferences();

        SharedPreferences.Editor editor = sDefaultPreferences.edit();

        editor.putStringSet(key, values);

        editor.apply();

    }


}