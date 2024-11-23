package com.items.mp3player.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefData {

    SharedPreferences mySharedPref ;
    public SharedPrefData(Context context) {
        if (context != null) {
            mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
        }

    }

    public void SaveInt(String key,int state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt(key,state);
        editor.commit();
    }
    // this method will load the Night Mode State
    public int LoadInt (String key){
       int  state = mySharedPref.getInt(key,-1);
        return  state;
    }
    public void SaveBoolean(String key, Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean(key,state);
        editor.commit();
    }
    // this method will load the Night Mode State
    public Boolean LoadBoolean(String key){
        Boolean  state = mySharedPref.getBoolean(key,false);
        return  state;
    }
    public void SaveString(String key,String state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putString(key,state);
        editor.commit();
    }

    public String LoadString (String key){
        String  state = mySharedPref.getString(key,"");
        return  state;
    }
    public void clear(){
        mySharedPref.edit().clear().apply();
    }
}
