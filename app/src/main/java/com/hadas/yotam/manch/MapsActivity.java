package com.hadas.yotam.manch;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    public static final int REQUEST_FINE_LOCATION = 1001;
    Boolean byClick;
    Boolean updatedOnStart;
    LatLng mLatLng;
    String mAddressString;
    GoogleApiClient mGoogleApiClient;
    public static final String FILTER_MY_ADDRESS = "MY_ADDRESS";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String ADDRESS = "adress";
    private static final int REQUEST_INTERVAL = 5;
    private static final int REQUEST_INTERVAL_FASTEST = 1;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.map_tool_bar);
        setSupportActionBar(toolbar);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this).addConnectionCallbacks(this)
                    .addApi(LocationServices.API).build();
        }

        updatedOnStart = false;
        mLatLng = null;
        mAddressString = null;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        byClick = false;


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.map_reset_location);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byClick = false;
                getLocation();
            }
        });

    }

    private void updateLocation(LatLng currentPosition) {
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String addressString = null;
        if (addressList != null) {
            for (Address address : addressList) {
                addressString = address.getLocality() + ", " + address.getThoroughfare() + ", " + address.getSubThoroughfare();
            }
            if (addressString != null) {
                addressString = addressString.replace("null, ", "");
                addressString = addressString.replace(", null", "");
                addressString = addressString.replace("null", "");
            }
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentPosition).title(addressString != null ? addressString : currentPosition.toString()).visible(true));
        if (!byClick)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, mMap.getMaxZoomLevel() - 5));
        else
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPosition));
        if (!TextUtils.isEmpty(addressString)) {
            mAddressString = addressString;
            setTitle(addressString);
        }
        else {
            mAddressString = currentPosition.toString();
            setTitle(getString(R.string.place_with_no_name));
        }
        mLatLng = currentPosition;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        mMap.setOnMapClickListener(this);
        if (updatedOnStart) {
            getLocation();
        } else
            updatedOnStart = true;

    }


    private void getLocation() {
        Boolean permited = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    Toast.makeText(this, R.string.app_gps_permmision_required, Toast.LENGTH_SHORT).show();
                else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_FINE_LOCATION);
                }
            } else {
                permited = true;
            }
        } else {
            permited = true;
        }
        if (permited && mGoogleApiClient != null && mGoogleApiClient.isConnected() && mMap != null) {
            setTitle(getString(R.string.locate_gps_signal));
            final LocationRequest locationRequest = new LocationRequest().setInterval(REQUEST_INTERVAL).setFastestInterval(REQUEST_INTERVAL_FASTEST)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setSmallestDisplacement(5).setNumUpdates(1);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequest);
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    Status status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MapsActivity.this);
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(
                                        MapsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    return;
                                }
                                setTitle(getString(R.string.gps_offline));


                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            setTitle(getString(R.string.gps_settings_not_avilable));
                            break;
                    }
                }
            });

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_FINE_LOCATION){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getLocation();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void InternetListener(Boolean active){
         RelativeLayout coordinatorLayout = (RelativeLayout) findViewById(R.id.maps_activity_relative_layout);
        if(!active) {
            if(snackbar!=null&& !snackbar.isShown() || snackbar==null)
                snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet_error, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.open_network_settings, new View.OnClickListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_get_LangLat){
            if(!TextUtils.isEmpty(mAddressString)) {
                Intent intent = new Intent(FILTER_MY_ADDRESS);
                intent.putExtra(ADDRESS,mAddressString);
                intent.putExtra(LAT,mLatLng.latitude);
                intent.putExtra(LNG,mLatLng.longitude);
                LocalBroadcastManager.getInstance(MapsActivity.this).sendBroadcast(intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        byClick=true;
        updateLocation(latLng);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(updatedOnStart) {
            getLocation();
        }
        else
            updatedOnStart=true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy()>=150){
            Toast.makeText(this, R.string.bad_gps_signal,Toast.LENGTH_SHORT).show();
            setTitle(R.string.bad_gps_signal);
            return;
        }
        Geocoder geocoder = new Geocoder(this);
        String address=null;
        List<Address> addressList=null;
        try {
             addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }catch (IOException e){

        }
        if(addressList!=null && addressList.size()>0) {
             address = addressList.get(0).getLocality() + ", " + addressList.get(0).getThoroughfare() + ", " + addressList.get(0).getSubThoroughfare();
            if (address != null) {
                address = address.replace("null, ", "");
                address = address.replace(", null", "");
                address = address.replace("null", "");
            }

        }

        mLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        if(!TextUtils.isEmpty(address)){
            mAddressString=address;
                setTitle(address);
        } else {
            mAddressString = mLatLng.toString();
            setTitle(R.string.place_with_no_name);
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().visible(true).position(mLatLng).title(mAddressString));
        if(byClick)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mLatLng));
        else
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng,17));
        Toast.makeText(MapsActivity.this,"מדויק ב: "+location.getAccuracy()+" מטרים",Toast.LENGTH_SHORT).show();
    }
}
