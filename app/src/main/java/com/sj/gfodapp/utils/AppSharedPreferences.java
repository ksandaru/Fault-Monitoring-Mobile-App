package com.sj.gfodapp.utils;

import android.content.SharedPreferences;

public class AppSharedPreferences {

    public static void saveData(SharedPreferences sharedPre, String key,String value) {
        SharedPreferences sharedPreferences=sharedPre;//getSharedPreferences("MyPref",0);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(key,value);
        editor.apply();
    }

    public static String getData(SharedPreferences sharedPre, String key){
        SharedPreferences sharedPreferences=sharedPre;//getSharedPreferences("MyPref",0);
        if(sharedPreferences.contains(key)){
            String data=sharedPreferences.getString(key,null);
            return data;
        }
        else{
            return null;
        }
    }

    public static void removeData(SharedPreferences sharedPre, String key) {
        SharedPreferences sharedPreferences=sharedPre;//getSharedPreferences("MyPref",0);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

}
