package com.hadas.yotam.manch;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Yotam on 04/01/2017.
 */

public abstract class AppConstants {
    private static Boolean isStoreOpen=false;
    private static Boolean internetConnection=false;
    public static final int MINIMUM_ORDER=60;
    public static final int VIEW_REVEAL_ANIMATION_TIME=10;
    public static final int VIEW_REVEAL_ANIMATION_DURATION=3;
    public static final String TRACK_FRAGMENT_EMPTY_KEY="TRACK_FRAGMENT_EMPTY_KEY";
    public static final String TRACK_FRAGMENT_DESTROYED_ORDERS="TRACK_FRAGMENT_DESTROYED_ORDERS";

    public static Boolean getIsStoreOpen() {
        return isStoreOpen;
    }

    public static void setIsStoreOpen(Boolean isStoreOpen, String className) {
        if(FirebaseServiceListener.class.getName().equals(className))
            AppConstants.isStoreOpen = isStoreOpen;
    }

    public static Boolean getInternetConnection() {
        return internetConnection;
    }

    public static void setInternetConnection(Boolean internetConnection,String className,Context context) {
        if(className.equals(InternetBrodacestReciver.class.getName())) {
            AppConstants.internetConnection = internetConnection;
            EventBus.getDefault().postSticky(new Boolean(internetConnection));
        }
    }

    public static void resetDatabaseConnection(){
        isStoreOpen=false;
        internetConnection=false;
    }
}
