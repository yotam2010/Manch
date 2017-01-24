package com.hadas.yotam.manch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Yotam on 04/01/2017.
 */

public class FirebaseServiceListener extends Service{
    public static Boolean isRunning=false;
    private Boolean isRunner=false;
    DatabaseReference mDatabaseReference;
    ValueEventListener mValueEventListener;
    @Override
    public void onCreate() {
        super.onCreate();
        if(isRunning)
            this.stopSelf();
        else {
            isRunning = true;
            isRunner=true;
            initializeFirebase();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isRunner)
            isRunning=false;
        if(mDatabaseReference!=null && mValueEventListener!=null)
            mDatabaseReference.removeEventListener(mValueEventListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mDatabaseReference!=null&&mValueEventListener!=null)
            mDatabaseReference.addValueEventListener(mValueEventListener);
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeFirebase(){
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.STORE_STATUS);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.child(FirebaseConstants.STATUS).exists() && dataSnapshot.child(FirebaseConstants.STATUS).getValue(Boolean.class))
                        AppConstants.setIsStoreOpen(true,FirebaseServiceListener.class.getName());
                    else if (!dataSnapshot.child(FirebaseConstants.STATUS).exists() || !dataSnapshot.child(FirebaseConstants.STATUS).getValue(Boolean.class))
                        AppConstants.setIsStoreOpen(false,FirebaseServiceListener.class.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

}
