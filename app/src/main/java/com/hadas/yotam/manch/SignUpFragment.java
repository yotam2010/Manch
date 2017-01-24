package com.hadas.yotam.manch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

/**
 * Created by Yotam on 13/12/2016.
 */

public class SignUpFragment extends Fragment {
    ActionProcessButton mSignUpButton;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    TextInputEditText mRePasswordEditText;
    TextView mExistingUserText;
    private String email;
    private String password;
    SwitchLoginFragment mSwitchLoginFragment;
    SignUp mSignUp;

    interface SignUp{
        public Boolean signUp(String email,String password);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSwitchLoginFragment = (SwitchLoginFragment)context;
        mSignUp = (SignUp) context;
    }

    private void displaySignUpProcess(){
        mSignUpButton.setMode(ActionProcessButton.Mode.ENDLESS);
        mSignUpButton.setProgress(1);
        mSignUpButton.setEnabled(false);
        mExistingUserText.setEnabled(false);
        if(checkAcceptedText()){
            if(email!=null&&password!=null)
                mSignUp.signUp(email,password);
        }else{
            mSignUpButton.setProgress(-1);
            mSignUpButton.setEnabled(true);
            mExistingUserText.setEnabled(true);
        }
    }

    private Boolean checkAcceptedText(){
        email = mEmailEditText.getText().toString().trim();
        password= mPasswordEditText.getText().toString().trim();
        String rePassword= mRePasswordEditText.getText().toString().trim();

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

        if(!password.equals(rePassword)){
            mRePasswordEditText.setError(getString(R.string.password_not_match_error));
            mRePasswordEditText.requestFocus();
            return false;
        }else
            mRePasswordEditText.setError(null);

        return true;
    }

    protected void signUpResult(Boolean signedIn,@Nullable Exception e){
        if(signedIn){
            mSignUpButton.setProgress(100);
        }else{
            mSignUpButton.setProgress(-1);

            if(e.getClass().equals(FirebaseAuthWeakPasswordException.class))
                Toast.makeText(getContext(),R.string.firebase_weak_password, Toast.LENGTH_SHORT).show();
            else if(e.getClass().equals(FirebaseAuthInvalidCredentialsException.class))
                Toast.makeText(getContext(),R.string.firebase_bad_email, Toast.LENGTH_SHORT).show();
            else if(e.getClass().equals(FirebaseAuthUserCollisionException.class))
                Toast.makeText(getContext(),R.string.firebase_user_already_exist, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(),R.string.not_known_error, Toast.LENGTH_SHORT).show();
        }
        mExistingUserText.setEnabled(true);
        mSignUpButton.setEnabled(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up,container,false);

        mExistingUserText = (TextView) v.findViewById(R.id.sign_up_text_existingUser);
        mEmailEditText = (TextInputEditText)v.findViewById(R.id.sign_up_email);
        mPasswordEditText= (TextInputEditText)v.findViewById(R.id.sign_up_password);
        mRePasswordEditText= (TextInputEditText)v.findViewById(R.id.sign_up_repassword);
        mSignUpButton = (ActionProcessButton)v.findViewById(R.id.login_sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utilities.internetConnection(getActivity()))
                    displaySignUpProcess();
                else
                    Toast.makeText(getContext(), R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        });
        mExistingUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchLoginFragment.replaceFragment(MainActivity.SIGN_IN_FRAGMENT);
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
