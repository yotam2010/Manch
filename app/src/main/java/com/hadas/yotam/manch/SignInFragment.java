package com.hadas.yotam.manch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * Created by Yotam on 12/12/2016.
 */

public class SignInFragment extends Fragment {

    ActionProcessButton mLogInButton;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    TextView mNewUserText;
    private String email;
    private String password;
    SwitchLoginFragment mSwitchFragment;
    SignIn mSignIn;

    interface SignIn{
        public Boolean signIn(String email,String password);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mSwitchFragment = (SwitchLoginFragment) context;
            mSignIn = (SignIn) context;
        }catch (ClassCastException e){
            throw new ClassCastException("מקושר לחלון שגוי");
        }
    }

    private void displayLoginProcess(){
        mLogInButton.setMode(ActionProcessButton.Mode.ENDLESS);
        mLogInButton.setProgress(1);
        mLogInButton.setEnabled(false);
        mNewUserText.setEnabled(false);
        if(checkAcceptedText()){
            if(email!=null&&password!=null)
                mSignIn.signIn(email,password);
        }else{
            mLogInButton.setProgress(-1);
            mLogInButton.setEnabled(true);
            mNewUserText.setEnabled(true);
        }
    }

    private Boolean checkAcceptedText(){
         email = mEmailEditText.getText().toString().trim();
         password= mPasswordEditText.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmailEditText.setError(getString(R.string.email_format_error));
            mEmailEditText.requestFocus();
            return false;
        }else
            mEmailEditText.setError(null);

        if(password.length()<6){
            mPasswordEditText.setError(getString(R.string.password_short_error));
            mPasswordEditText.requestFocus();
            return false;
        }else
            mPasswordEditText.setError(null);

            return true;
    }

    protected void signInResult(Boolean signedIn,@Nullable Exception e){
    if(signedIn){
        mLogInButton.setProgress(100);
    }else{
        mLogInButton.setProgress(-1);

        if(e.getClass().equals(FirebaseAuthInvalidUserException.class))
            Toast.makeText(getContext(),R.string.firebase_wrong_email_error, Toast.LENGTH_SHORT).show();
        else if(e.getClass().equals(FirebaseAuthInvalidCredentialsException.class))
            Toast.makeText(getContext(),R.string.firebase_wrong_password_error, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(),R.string.not_known_error, Toast.LENGTH_SHORT).show();
    }
        mNewUserText.setEnabled(true);
        mLogInButton.setEnabled(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in,container,false);
        mEmailEditText = (TextInputEditText)v.findViewById(R.id.sign_in_email);
        mNewUserText = (TextView) v.findViewById(R.id.sign_in_text_newUser);
        mPasswordEditText= (TextInputEditText)v.findViewById(R.id.sign_in_password);
        mLogInButton = (ActionProcessButton)v.findViewById(R.id.login_sign_in_button);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utilities.internetConnection(getActivity()))
                    displayLoginProcess();
                else
                    Toast.makeText(getContext(), R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        });
        mNewUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSwitchFragment!=null)
                    mSwitchFragment.replaceFragment(MainActivity.SIGN_UP_FRAGMENT);
            }
        });

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(MainActivity.EMAIL))
                mEmailEditText.setText(savedInstanceState.getString(MainActivity.EMAIL));

            if(savedInstanceState.containsKey(MainActivity.PASSWORD))
                mPasswordEditText.setText(savedInstanceState.getString(MainActivity.PASSWORD));

        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MainActivity.EMAIL,mEmailEditText.getText().toString());
        outState.putString(MainActivity.PASSWORD,mPasswordEditText.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
