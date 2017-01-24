package com.hadas.yotam.manch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.HashMap;

/**
 * Created by Yotam on 27/12/2016.
 */

public abstract class UtilitiesSharedPreference {
    public static final String SHARED_PREFERENCE_NAME="SHARED_PREFERENCE_NAME";
    public static final String SHARED_ORDER_NAME="SHARED_ORDER_NAME";
    public static final String SHARED_ORDER_PHONE="SHARED_ORDER_PHONE";
    public static final String SHARED_ORDER_ADDRESS="SHARED_ORDER_ADDRESS";
    public static final String SHARED_ORDER_DATA_EXIST="SHARED_ORDER_DATA_EXIST";

    public static void setUserOrderDetails(Context context,String name,int phone,String address){
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME,context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_ORDER_NAME,name);
        editor.putInt(SHARED_ORDER_PHONE,phone);
        editor.putString(SHARED_ORDER_ADDRESS,address);
        editor.putBoolean(SHARED_ORDER_DATA_EXIST,true);
        editor.commit();
    }
    public static Bundle getUserOrderDetails(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME,context.MODE_PRIVATE);
        Boolean exist = sharedPreferences.getBoolean(SHARED_ORDER_DATA_EXIST,false);
        if(!exist)
            return null;
        Bundle bundle = new Bundle();
        bundle.putInt(SHARED_ORDER_PHONE,sharedPreferences.getInt(SHARED_ORDER_PHONE,0));
        bundle.putString(SHARED_ORDER_NAME,sharedPreferences.getString(SHARED_ORDER_NAME,"אין שם"));
        bundle.putString(SHARED_ORDER_ADDRESS,sharedPreferences.getString(SHARED_ORDER_ADDRESS,"אין כתובת"));
        return bundle;
    }
}
