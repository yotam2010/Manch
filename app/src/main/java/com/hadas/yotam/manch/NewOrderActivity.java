package com.hadas.yotam.manch;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yotam on 29/12/2016.
 */

public class NewOrderActivity extends AppCompatActivity {
    HashMap<String,Integer[]> mProductsList;
    ListView mSummaryList;
    ArrayAdapter<String> mSummaryAdapter;
    TextView mTotalPriceText;
    TextInputEditText mFullName;
    TextInputEditText mAddress;
    TextInputEditText mPhoneNumber;
    ImageButton mGPSButton;
    BroadcastReceiver mBroadcastReceiver;
    LatLng mLatLng;
    ActionProcessButton mSendButton;
    DatabaseReference mDatabaseReference;
    int totalPrice;
    Snackbar snackbar;
    Boolean sendOrder;
    public static final String FILTER_SENT_ORDER="FILTER_SENT_ORDER";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        sendOrder=false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mProductsList=(HashMap<String, Integer[]>) getIntent().getSerializableExtra(FirebaseConstants.PRODUCTS);
        if(mProductsList==null || mProductsList.size()<1)
            finish();
        viewInit();
        ArrayList<String> mSummaryArray = new ArrayList<>();
        for(Map.Entry<String,Integer[]> map : mProductsList.entrySet())
        mSummaryArray.add(map.getValue()[1]+" x "+map.getKey()+"   -   "+Utilities.reformatNumberToPrice(map.getValue()[0]*map.getValue()[1])+" ש\"ח");
        mSummaryAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,mSummaryArray);
        mSummaryList.setAdapter(mSummaryAdapter);
        totalPrice=getIntent().getIntExtra(FirebaseConstants.PRICE,0);
        mTotalPriceText.setText(getString(R.string.final_price)+Utilities.reformatNumberToPrice(totalPrice)+" ש\"ח");

        mGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewOrderActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAddress.setText(intent.getStringExtra(MapsActivity.ADDRESS));
                double lat = intent.getDoubleExtra(MapsActivity.LAT,-1);
                double lng = intent.getDoubleExtra(MapsActivity.LNG,-1);
                if(lat!=-1 && lng!=-1)
                  mLatLng=new LatLng(lat,lng);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter(MapsActivity.FILTER_MY_ADDRESS));

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mFullName.getText().toString();
                String phone= mPhoneNumber.getText().toString();
                String address= mAddress.getText().toString();
                if(checkFields(name,phone,address))
                    sendOrder(new Order(name,address,phone,mProductsList,getResources().getString(R.string.status_pending), Calendar.getInstance().getTimeInMillis(),FirebaseConstants.MY_UID,totalPrice));
            }
        });

        Bundle bundle = UtilitiesSharedPreference.getUserOrderDetails(this);
        if(bundle!=null){
            mFullName.setText(bundle.getString(UtilitiesSharedPreference.SHARED_ORDER_NAME));
            mPhoneNumber.setText(bundle.getInt(UtilitiesSharedPreference.SHARED_ORDER_PHONE)+"");
            mAddress.setText(bundle.getString(UtilitiesSharedPreference.SHARED_ORDER_ADDRESS));
        }
    }

    private void viewInit(){
        mTotalPriceText = (TextView)findViewById(R.id.new_order_total_price);
        mSummaryList = (ListView)findViewById(R.id.new_order_summary_list);
        mFullName = (TextInputEditText)findViewById(R.id.new_order_name_text);
        mAddress = (TextInputEditText)findViewById(R.id.new_order_address_text);
        mPhoneNumber= (TextInputEditText)findViewById(R.id.new_order_phone_text);
        mGPSButton = (ImageButton)findViewById(R.id.new_order_gps_button);
        mSendButton = (ActionProcessButton)findViewById(R.id.new_order_send_button);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.ORDERS).child(FirebaseConstants.ACTIVE).push();

    }

    private void sendOrder(final Order order){
        if(order.getAddress()==null||order.getName()==null||order.getPhone()==null||order.getProducts()==null){
            Toast.makeText(this, R.string.error_sending_order,Toast.LENGTH_SHORT).show();
            return;
        }
        if(!AppConstants.getInternetConnection()) {
            Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!AppConstants.getIsStoreOpen()){
            Toast.makeText(this, R.string.store_closed, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!sendOrder) {
            sendOrder=true;
            mDatabaseReference.setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (NewOrderActivity.this != null) {
                        if (task.isSuccessful()) {
                            final String key = mDatabaseReference.getKey();
                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseConstants.MY_UID).child(FirebaseConstants.ACTIVE).child(key);
                            mDatabaseReference.setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (NewOrderActivity.this != null) {
                                        if (task.isSuccessful()) {
                                            UtilitiesSharedPreference.setUserOrderDetails(NewOrderActivity.this, order.getName(), Integer.valueOf(order.getPhone()), order.getAddress());
                                            Toast.makeText(NewOrderActivity.this, R.string.sucess_order_sent, Toast.LENGTH_SHORT).show();
                                            LocalBroadcastManager.getInstance(NewOrderActivity.this).sendBroadcast(new Intent(FILTER_SENT_ORDER));
                                            NewOrderActivity.this.finish();
                                        } else {
                                            Toast.makeText(NewOrderActivity.this, R.string.error_sending_order, Toast.LENGTH_SHORT).show();
                                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.ORDERS).child(FirebaseConstants.ACTIVE).child(key);
                                            mDatabaseReference.removeValue();
                                        }
                                        sendOrder = false;
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(NewOrderActivity.this, R.string.error_sending_order, Toast.LENGTH_SHORT).show();
                            sendOrder = false;
                        }
                    }
                }
            });
        }


    }
        private Boolean checkFields(String name, String phone, String address){
            String nameTrim = name.trim();
            String phoneTrim = phone.trim();
            String addressTrim = address.trim();
            if(TextUtils.isEmpty(nameTrim)){
                mFullName.setError(getString(R.string.error_name_empty));
                mFullName.requestFocus();
                return false;
            }

            if(nameTrim.length()<2){
                mFullName.setError(getString(R.string.error_name_too_short));
                mFullName.requestFocus();
                return false;
            }

            if(TextUtils.isEmpty(phoneTrim)){
                mPhoneNumber.setError(getString(R.string.error_phone_empty));
                mPhoneNumber.requestFocus();
                return false;
            }

            if(phoneTrim.length()<7){
                mPhoneNumber.setError(getString(R.string.error_phone_too_short));
                mPhoneNumber.requestFocus();
                return false;
            }

            if(TextUtils.isEmpty(addressTrim)){
                mAddress.setError(getString(R.string.error_address_empty));
                mAddress.requestFocus();
                return false;
            }

            if(addressTrim.length()<3){
                mAddress.setError(getString(R.string.error_address_too_short));
                mAddress.requestFocus();
                return false;
            }

            return true;
        }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void InternetListener(Boolean active){
        LinearLayout coordinatorLayout = (LinearLayout) findViewById(R.id.new_order_linear_layout);
        if(!active) {
            if(snackbar!=null&& !snackbar.isShown() || snackbar==null)
                snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet_error, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("פתח הגדרות רשת", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    startActivity(intent);
                }
            });
            snackbar.show();
        }else{
            if(snackbar!=null&&snackbar.isShown())
                snackbar.dismiss();
        }
    }
}
