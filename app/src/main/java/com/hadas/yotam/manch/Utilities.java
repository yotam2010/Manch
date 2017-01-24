package com.hadas.yotam.manch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Yotam on 12/12/2016.
 */

public abstract class Utilities {
    public static Boolean internetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }
    public static void signOut(Context context, final FirebaseAuth firebaseAuth){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.log_out_confirmation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }
    public static String reformatNumberToPrice(int price){
        //1200
        String priceString = String.valueOf(price);
        StringBuilder stringBuilder=new StringBuilder();
        //length = 4
        while (priceString.length()>3){
            //builder = 120
            stringBuilder.insert(0,priceString.substring(priceString.length()-3));
            //builder = 120,
            stringBuilder.insert(0,",");
            priceString=priceString.substring(0,priceString.length()-3);
        }
        stringBuilder.insert(0,priceString);

        return stringBuilder.toString();
    }

    public static void openGPS(final MapsActivity context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("GPS כבוי, האם ברצונך לעבור למסך הגדרות?");
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.finish();
            }
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
