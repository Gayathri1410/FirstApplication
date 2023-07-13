package com.example.firstapplication;

    public class DefaultSharedPrefAdapter implements ISharedPrefAdapter {

        @Override

        public String getString(String key) {
            return AppSettings.get(key);
        }

        @Override

        public void setString(String key, String value) {
            AppSettings.set(key, value);
            return;
        }

        @Override

        public boolean getBoolean(String key) {
            return AppSettings.getBoolean(key,false);
        }

        @Override

        public void setBoolean(String key, boolean value) {
            AppSettings.set(key, value);
        }

        @Override

        public void removeKey(String key) {
            AppSettings.removeKey(key);
            return;
        }

    }
