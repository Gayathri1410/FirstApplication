package com.example.firstapplication;

/**

 * Custom Wrapper for reading/updating values in shared preferences.

 * Reason for introduction: As we are moving towards common c++ code which has level db,

 * we need to migrate all used shared preferences to level db.

 * As it is common code, we plan to have different implementations for it in kaizala and non-kaizala apps.

 * Add in methods as and when required.

 */

public interface ISharedPrefAdapter {




    String getString(String key);




    void setString(String key, String value);




    boolean getBoolean(String key);




    void setBoolean(String key, boolean value);




    void removeKey(String key);




}