package com.hadas.yotam.manch;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements SignInFragment.SignIn, SignUpFragment.SignUp, SwitchLoginFragment {


    FragmentManager fragmentManager;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    SignInFragment mSignInFragment;
    SignUpFragment mSignUpFragment;
    public static final String SIGN_IN_FRAGMENT="SIGN_IN_FRAGMENT";
    public static final String SIGN_UP_FRAGMENT="SIGN_UP_FRAGMENT";
    public static final String CURRENT_FRAGMENT="CURRENT_FRAGMENT";
    public static final String EMAIL="EMAIL";
    public static final String PASSWORD="PASSWORD";
    private boolean login_fragment;
    Boolean displayedFragment;
    Boolean allowStateChange;
    int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowStateChange=true;
        orientation=getRequestedOrientation();
        if(savedInstanceState!=null && savedInstanceState.containsKey(CURRENT_FRAGMENT))
            login_fragment=savedInstanceState.getBoolean(CURRENT_FRAGMENT);
        else
            login_fragment=true;

         fragmentManager = getSupportFragmentManager();

        mSignInFragment=(SignInFragment) fragmentManager.findFragmentByTag(SIGN_IN_FRAGMENT);
        Bundle bundle = new Bundle();
        if(mSignInFragment==null){
            mSignInFragment = new SignInFragment();
            mSignInFragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.login_container,mSignInFragment,SIGN_IN_FRAGMENT).commit();
        }

        mSignUpFragment=(SignUpFragment) fragmentManager.findFragmentByTag(SIGN_UP_FRAGMENT);
        if(mSignUpFragment==null){
            mSignUpFragment = new SignUpFragment();
            mSignUpFragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.login_container,mSignUpFragment,SIGN_UP_FRAGMENT).commit();
        }

        displayedFragment=false;
        setFirebase();


    }
    private void setFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if(firebaseAuth.getCurrentUser()!=null) {
                FirebaseConstants.MY_UID=firebaseAuth.getCurrentUser().getUid();
                Intent intent = new Intent(MainActivity.this,ManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                MainActivity.this.finish();
            }
            else
            if(!displayedFragment) {
                if(login_fragment)
                   replaceFragment(SIGN_IN_FRAGMENT);
                else
                    replaceFragment(SIGN_UP_FRAGMENT);
            }
            }
        };
    }

    @Override
    public Boolean signIn(String email, String password) {
        if(mAuth==null)
            return false;
        allowStateChange=false;
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mSignInFragment.signInResult(true,null);
                }else{
                    mSignInFragment.signInResult(false,task.getException());
                }
                allowStateChange=true;
            }
        });
        return true;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(allowStateChange)
            newConfig.orientation=orientation;
         super.onConfigurationChanged(newConfig);
    }

    @Override
    public Boolean signUp(String email, String password) {
        if(mAuth==null)
            return false;
        allowStateChange=false;

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mSignUpFragment.signUpResult(true,null);
                }else{
                    mSignUpFragment.signUpResult(false,task.getException());
                }
                allowStateChange=true;

            }
        });
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CURRENT_FRAGMENT,login_fragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void replaceFragment(String fragment) {
        if(fragment.equals(SIGN_IN_FRAGMENT)){
            login_fragment=true;
            fragmentManager.beginTransaction().replace(R.id.login_container,mSignInFragment).commitAllowingStateLoss();

        }else if (fragment.equals(SIGN_UP_FRAGMENT)){
            login_fragment=false;
            fragmentManager.beginTransaction().replace(R.id.login_container,mSignUpFragment).commitAllowingStateLoss();
        }
    }
}
