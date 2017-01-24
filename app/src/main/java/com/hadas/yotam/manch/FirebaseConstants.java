package com.hadas.yotam.manch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Yotam on 14/12/2016.
 */

public abstract class FirebaseConstants {
    public static final String WORKERS= "Workers";
    public static final String UID= "uid";
    public static final String PRODUCTS= "Products";
    public static final String USERS= "Users";
    public static final String ORDERS= "Orders";
    public static final String TITLE= "title";
    public static final String DESCRIPTION= "description";
    public static final String PRICE= "price";
    public static final String IMAGE= "image";
    public static final String STORE_STATUS= "store_status";
    public static final String STATUS= "status";
    public static final String STATUS_SENT= "נשלח";
    public static final String TIME_STAMP= "time_stamp";
    public static final String RECYCLER_REF= "recycler_ref";
    public static final String ACTIVE= "Active";
    public static final String CANCELLED= "cancelled";
    public static final String COMPLETE= "complete";
    public static final String ORDER_LOCATION= "order_location";
    public static final String LAT= "Lat";
    public static final String LNG= "Lng";
    public static final String KEY= "key";


    public static String MY_UID= null;
}
