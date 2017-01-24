package com.hadas.yotam.manch;

import android.animation.Animator;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import static com.hadas.yotam.manch.ManagementActivity.SectionsPagerAdapter.purchaseFragment;
import static com.hadas.yotam.manch.ManagementActivity.SectionsPagerAdapter.trackFragment;

public class ManagementActivity extends AppCompatActivity implements ChangeTab {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthStateListener;
    ImageButton mFinishOrder;
    TextView mOrderTotalPrice;
    public static HashMap<String,Integer[]> mProductsHashMap;
    BroadcastReceiver mBroadcastReceiver;
    InternetBrodacestReciver mInternetBrodacestReciver;
    Intent mServiceIntent;
    Boolean backPressed;
    Snackbar snackbar;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        setFirebase();
        backPressed=false;
            final FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getFragments()!=null)
            for (Fragment fragment : fragmentManager.getFragments()){
                if(fragment!=null && fragment.getClass().equals(ProductItemFragment.class))
                getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss();
            }

        AppConstants.resetDatabaseConnection();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFinishOrder = (ImageButton)findViewById(R.id.new_order_finish_button);
        mOrderTotalPrice = (TextView)findViewById(R.id.toolbar_sum_price_text);
        mProductsHashMap=null;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(position==purchaseFragment) {
                    mFinishOrder.setVisibility(View.VISIBLE);
                    mOrderTotalPrice.setVisibility(View.VISIBLE);
                }
                    else {
                    mFinishOrder.setVisibility(View.GONE);
                    mOrderTotalPrice.setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            mViewPager.setCurrentItem(trackFragment);
                mProductsHashMap=null;
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter(NewOrderActivity.FILTER_SENT_ORDER));
            mInternetBrodacestReciver = new InternetBrodacestReciver();
            this.registerReceiver(mInternetBrodacestReciver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

         mServiceIntent = new Intent(this,FirebaseServiceListener.class);
        startService(mServiceIntent);

    }

    private void setFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null) {
                }
                else {
                    Intent intent = new Intent(ManagementActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    ManagementActivity.this.finish();
                }

                }

        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_sign_out) {
            Utilities.signOut(this,mAuth);
            return true;
        }
        if(!getResources().getBoolean(R.bool.tablet))
             if(id==R.id.menu_call_us){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0508329286"));
            if(intent.resolveActivity(getPackageManager())!=null)
                startActivity(intent);
            else
                Toast.makeText(this,"למכשיר אין אפשרות להתקשר",Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth!=null && mAuthStateListener!=null)
            mAuth.removeAuthStateListener(mAuthStateListener);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void InternetListener(Boolean active){
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public static final int purchaseFragment=2;
        public static final int trackFragment=1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case purchaseFragment:
                    return  new NewPurchaseFragment();
                case trackFragment:
                    return new OrderTrackFragment();
                default:
                    return new HistoryFragment();
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.purchase_history);
                case 1:
                    return getString(R.string.purchase_track);
                case 2:
                    return getString(R.string.purchase_new_order);
            }
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(mBroadcastReceiver!=null)
             LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if(mServiceIntent!=null)
            stopService(mServiceIntent);
        if(mInternetBrodacestReciver!=null)
           unregisterReceiver(mInternetBrodacestReciver);

    }

    @Override
    public void onBackPressed() {
        if(!backPressed) {
            Toast.makeText(this, R.string.press_back_to_leave, Toast.LENGTH_SHORT).show();
            backPressed=true;
            new CountDownTimer(3000,3000){
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                backPressed=false;
                }
            }.start();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void setNewOrderTab() {
        if(mViewPager!=null){
            mViewPager.setCurrentItem(SectionsPagerAdapter.purchaseFragment);
        }
    }
}
