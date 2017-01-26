package com.hadas.yotam.manch;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static com.hadas.yotam.manch.AppConstants.VIEW_REVEAL_ANIMATION_TIME;

/**
 * Created by Yotam on 14/12/2016.
 */

public class OrderTrackFragment extends Fragment {

    ViewPager mViewPager;
    DatabaseReference mDatabaseReference;
    TrackerFragment mViewFlipperAdapter;
    ArrayList<String> mViewPagerArrayList;
    ChildEventListener mChildEventListener;
    TextView mPagerCountText;
    TextView mNewOrderTextView;
    ImageView mNewOrderImageView;
//    Animation mNewOrderAnimation;
    ChangeTab mChangeTab;
    ArrayList<Order> mOrderArrayList;
    Boolean loadedOldOrders;
    CountDownTimer mCountDownTimer;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mChangeTab = (ChangeTab)context;
        }catch (ClassCastException e){
            throw new ClassCastException("OrderTrackFragment  נפתח ממקום שגוי");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPagerArrayList = new ArrayList<>();
        mOrderArrayList = new ArrayList<>();
        loadedOldOrders=false;
        mPagerCountText = (TextView)getActivity().findViewById(R.id.purchase_tracker_pageCount);
        mNewOrderImageView = (ImageView) getActivity().findViewById(R.id.purchase_tracker_empty_imageView);
        mNewOrderTextView = (TextView)getActivity().findViewById(R.id.purchase_tracker_empty_textView);
        mNewOrderTextView.setVisibility(View.VISIBLE);
        mNewOrderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChangeTab!=null)
                    mChangeTab.setNewOrderTab();
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseConstants.MY_UID).child(FirebaseConstants.ACTIVE);
        mViewFlipperAdapter = new TrackerFragment(getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager)getActivity().findViewById(R.id.purchase_tracker_ViewPager);
        mViewPager.setPageTransformer(true,new CustomPagerViewAnimation());
        mViewPager.setAdapter(mViewFlipperAdapter);
        if(savedInstanceState!=null&& savedInstanceState.containsKey(AppConstants.TRACK_FRAGMENT_DESTROYED_ORDERS))
        {
            ArrayList<Order> orderArrayList =  savedInstanceState.getParcelableArrayList(AppConstants.TRACK_FRAGMENT_DESTROYED_ORDERS);
            if(orderArrayList!=null && orderArrayList.size()>0) {
                for (Order order : orderArrayList) {
                    mViewFlipperAdapter.addView(order, AppConstants.TRACK_FRAGMENT_EMPTY_KEY);
                }
                loadedOldOrders=true;
            }
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setPageText();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(loadedOldOrders){
                    loadedOldOrders=false;
                    while (mViewPagerArrayList.contains(AppConstants.TRACK_FRAGMENT_EMPTY_KEY)){
                        mViewFlipperAdapter.removeView(mViewPagerArrayList.indexOf(AppConstants.TRACK_FRAGMENT_EMPTY_KEY));
                    }
                }
                if(!mViewPagerArrayList.contains(dataSnapshot.getKey()))
                    mViewFlipperAdapter.addView(dataSnapshot.getValue(Order.class),dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                if(mViewPagerArrayList.contains(dataSnapshot.getKey())){
                    mViewFlipperAdapter.removeView(mViewPagerArrayList.indexOf(dataSnapshot.getKey()));
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

            mDatabaseReference.addChildEventListener(mChildEventListener);
        setPageText();
//        setRevealAnimation();
    }

//    @TargetApi(21)
//    private void setRevealAnimation(){
//
//        mCountDownTimer = new CountDownTimer(VIEW_REVEAL_ANIMATION_TIME*1000,VIEW_REVEAL_ANIMATION_TIME*1000){
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                Utilities.clickMeAnimation(mNewOrderTextView,getContext());
//                this.start();
//            }
//        };
//
//        if(mNewOrderTextView.isAttachedToWindow()) {
//            mCountDownTimer.start();
//        }
//
//        mNewOrderTextView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(final View v) {
//                mCountDownTimer.start();
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//                mCountDownTimer.cancel();
//            }
//        });
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_purchase_tracker,container,false);
        return v;
    }

    @Override
    public void onDestroy() {
        if(mDatabaseReference!=null&&mChildEventListener!=null)
            mDatabaseReference.removeEventListener(mChildEventListener);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mOrderArrayList!=null&&mOrderArrayList.size()>0)
            outState.putParcelableArrayList(AppConstants.TRACK_FRAGMENT_DESTROYED_ORDERS,mOrderArrayList);
        super.onSaveInstanceState(outState);
    }

    public void setPageText(){
        int count =mViewFlipperAdapter.count;
        int position = mViewPager.getCurrentItem()+1;
        mPagerCountText.setText(count==0?"0/0":position+"/"+count);
    }
    private class TrackerFragment extends FragmentStatePagerAdapter {
        int count=0;
        ArrayList<Fragment> mFragmentArrayList;
        public TrackerFragment(FragmentManager fm) {
            super(fm);
            count=0;
            mFragmentArrayList = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentArrayList.size()>0 ? mFragmentArrayList.get(position): null;
        }

        @Override
        public int getCount() {
            return count;
        }

        public void addView(Order order,String key){
            count++;
            OrderTrackerItem orderTrackerItem = new OrderTrackerItem();
            Bundle bundle = new Bundle();
            bundle.putParcelable("order",order);
            bundle.putString(FirebaseConstants.KEY,key);
            orderTrackerItem.setArguments(bundle);
            mFragmentArrayList.add(orderTrackerItem);
            mOrderArrayList.add(order);
            mViewPagerArrayList.add(key);
            notifyDataSetChanged();
            if(count==1){
                mNewOrderTextView.setVisibility(View.GONE);
                mNewOrderImageView.setVisibility(View.GONE);
                mViewPager.setCurrentItem(0);
            }
            setPageText();
        }

        public void removeView(int position){
            if(mFragmentArrayList.size()>0) {
                mFragmentArrayList.remove(position);
                mOrderArrayList.remove(position);
                mViewPagerArrayList.remove(position);
                count--;
                notifyDataSetChanged();
                if(count==0){
                    mNewOrderTextView.setVisibility(View.VISIBLE);
                    mNewOrderImageView.setVisibility(View.VISIBLE);
                }
                setPageText();

            }
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
