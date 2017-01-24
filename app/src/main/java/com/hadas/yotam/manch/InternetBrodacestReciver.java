package com.hadas.yotam.manch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

/**
 * Created by Yotam on 04/01/2017.
 */

public class InternetBrodacestReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            AppConstants.setInternetConnection(true,InternetBrodacestReciver.class.getName(),context);
        else
            AppConstants.setInternetConnection(false,InternetBrodacestReciver.class.getName(),context);
        }
    }
}
