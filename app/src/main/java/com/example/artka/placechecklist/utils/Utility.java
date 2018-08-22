package com.example.artka.placechecklist.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public final class Utility {

    private Utility() {}

    public static void saveChecklist(Context context, String key, ArrayList<String> strings) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < strings.size(); i++) {
            jsonArray.put(strings.get(i));
        }

        if (!strings.isEmpty()) {
            editor.putString(key, jsonArray.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    public static ArrayList<String> getCheckList(Context context, String key) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String json = sharedPreferences.getString(key, null);
        ArrayList<String> urls = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String url = jsonArray.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public static void saveCheckBoxes(Context context, String key, ArrayList<Boolean> booleans) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < booleans.size(); i++) {
            jsonArray.put(booleans.get(i));
        }

        if (!booleans.isEmpty()) {
            editor.putString("B" + key, jsonArray.toString());
        } else {
            editor.putString("B" + key, null);
        }
        editor.commit();
    }

    public static ArrayList<Boolean> getCheckBoxList(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("B" + key, null);
        ArrayList<Boolean> urls = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String url = jsonArray.optString(i);
                    urls.add(Boolean.valueOf(url));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public static void saveGeofenceStatus(Context context, String key, Boolean value) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(value);


        if (value != null) {
            editor.putString("G" + key, jsonArray.toString());
        } else {
            editor.putString("G" + key, null);
        }
        editor.commit();
    }

    public static Boolean getGeofenceStatus(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("G" + key, null);
        Boolean url = null;
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                url = jsonArray.optBoolean(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static void saveImageUrl(Context context, String key, String value) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(value);


        if (!value.isEmpty()) {
            editor.putString("Q" + key, jsonArray.toString());
        } else {
            editor.putString("Q" + key, null);
        }
        editor.commit();
    }

    public static String getImageUrl(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("Q" + key, null);
        String url = new String();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                url = jsonArray.optString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return url;
    }


}
