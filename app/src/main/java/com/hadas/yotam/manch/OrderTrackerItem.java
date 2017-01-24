package com.hadas.yotam.manch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kofigyan.stateprogressbar.StateProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Yotam on 14/01/2017.
 */

public class OrderTrackerItem extends Fragment implements OnMapReadyCallback {
    StateProgressBar stateProgressBar;
    TextView mDateTextView, mFinalPriceText, mWaitForDeliverText;
    Order mOrder;
    ListView mProductsList;
    GoogleMap mMap;
    DatabaseReference mDatabaseReference;
    ChildEventListener mChildEventListener;
    String[] statusArray;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrder = (Order) getArguments().getSerializable("order");
        initializeFirebase();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         statusArray = getActivity().getResources().getStringArray(R.array.status_array);
        stateProgressBar.setStateDescriptionData(statusArray);
        setStatus();
        setDate();
        setProducts();
        mFinalPriceText.setText("מחיר כולל: "+mOrder.getTotalPrice());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.item_purchase_tracker,container,false);
        stateProgressBar = (StateProgressBar)v.findViewById(R.id.state_progress_bar);
        mDateTextView = (TextView) v.findViewById(R.id.item_purchase_tracker_date);
        mFinalPriceText = (TextView) v.findViewById(R.id.item_purchase_tracker_finalPrice);
        mWaitForDeliverText = (TextView) v.findViewById(R.id.item_purchase_tracker_wait_for_deliver);
        mProductsList = (ListView) v.findViewById(R.id.item_purchase_tracker_list);
        SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager().findFragmentById(R.id.item_purchase_tracker_map);
        mapFragment.getMapAsync(this);
        return v;
    }

    private void setStatus(){
        for(int i=0;i<statusArray.length;i++){
            if(statusArray[i].equals(mOrder.getStatus())) {
                if(i==0)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                else if(i==1)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                else if(i==2)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                else if(i==3)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
            }
        }
        if(!mOrder.getStatus().equals(getContext().getString(R.string.status_sent)))
            mWaitForDeliverText.setVisibility(View.VISIBLE);
        else
            mWaitForDeliverText.setVisibility(View.GONE);

    }

    private void setDate(){
        long date = mOrder.getTimeStamp();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("DD/MM - HH:mm");
        String dateString = simpleDateFormat.format(date);
        mDateTextView.setText(dateString);
    }
    private void setProducts(){
        HashMap<String, Integer> mProducts = mOrder.getProducts();
        ArrayList<String> arrayList = new ArrayList<>();
        for(HashMap.Entry<String,Integer> entry : mProducts.entrySet())
            arrayList.add(entry.getValue()+" X "+entry.getKey());
        ArrayAdapter adaper = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,arrayList);
        mProductsList.setAdapter(adaper);
    }

    private void initializeFirebase(){
        if(FirebaseConstants.MY_UID==null)
            FirebaseConstants.MY_UID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseConstants.MY_UID).child(FirebaseConstants.ACTIVE)
                .child(getArguments().getString(FirebaseConstants.KEY));
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals(FirebaseConstants.STATUS)) {
                    mOrder.setStatus(dataSnapshot.getValue(String.class));
                    setStatus();
                }else
                    if(dataSnapshot.getKey().equals(FirebaseConstants.ORDER_LOCATION)){
                        setDeliverPosition(dataSnapshot.child(FirebaseConstants.LAT).getValue(Double.class),dataSnapshot.child(FirebaseConstants.LNG).getValue(Double.class));
                    }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals(FirebaseConstants.STATUS)) {
                    mOrder.setStatus(dataSnapshot.getValue(String.class));
                    setStatus();
                }else
                if(dataSnapshot.getKey().equals(FirebaseConstants.ORDER_LOCATION)){
                    setDeliverPosition(dataSnapshot.child(FirebaseConstants.LAT).getValue(Double.class),dataSnapshot.child(FirebaseConstants.LNG).getValue(Double.class));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }
    private void setDeliverPosition(double lat, double lng){
        if(mMap!=null){
            mMap.clear();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),15));
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("השליח כבר בדרך!"));
            mMap.addCircle(new CircleOptions().center(new LatLng(lat,lng)).radius(20).fillColor(Color.BLUE).strokeWidth(1));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(mDatabaseReference!=null&&mChildEventListener!=null)
            mDatabaseReference.addChildEventListener(mChildEventListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mDatabaseReference!=null&&mChildEventListener!=null)
            mDatabaseReference.removeEventListener(mChildEventListener);

    }

}
