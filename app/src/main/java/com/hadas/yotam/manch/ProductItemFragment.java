package com.hadas.yotam.manch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.HashMap;

/**
 * Created by Yotam on 23/12/2016.
 */

public class ProductItemFragment extends Fragment implements View.OnClickListener {
    interface priceInterface{
        public void setPrice(String title,int price,int quantity);
        public void removePrice(String title);
    }
    Product mProduct;
    String title, image, description;
    int price, quantity;
    ImageView mImageView;
    TextView mTitleView, mDescriptionView, mPriceView;
    CardView mCardView;
    ImageButton mPlusButton, mMinusButton;
    ProgressBar mProgressBar;
    TextInputEditText mQuantityView;
    priceInterface mPriceInterface;
    Button mAddToCart;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        quantity=0;
        title = args.getString(FirebaseConstants.TITLE);
        description = args.getString(FirebaseConstants.DESCRIPTION);
        price= args.getInt(FirebaseConstants.PRICE);
        image = args.getString(FirebaseConstants.IMAGE);
        if(title==null||description==null||image==null )
            return;
        mProduct = new Product(title,description,price,image);
        mImageView = (ImageView)getActivity().findViewById(R.id.product_card_image);
        mTitleView = (TextView) getActivity().findViewById(R.id.product_card_title);
        mDescriptionView = (TextView) getActivity().findViewById(R.id.product_card_description);
        mPriceView= (TextView) getActivity().findViewById(R.id.product_card_price);
        mAddToCart= (Button)getActivity().findViewById(R.id.product_card_add_to_cart);
        mPlusButton= (ImageButton) getActivity().findViewById(R.id.product_card_quantity_plus);
        mMinusButton= (ImageButton) getActivity().findViewById(R.id.product_card_quantity_minus);
        mPlusButton.setOnClickListener(this);
        mMinusButton.setOnClickListener(this);
        mQuantityView= (TextInputEditText) getActivity().findViewById(R.id.product_card_quantity_editText);
        mQuantityView.addTextChangedListener(new TextWatcher() {
            int newNum;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    int zero = Integer.valueOf(s.toString().substring(0,1));
                    Boolean update=false;
                    while(s.length()>1&&zero==0) {
                        s = s.subSequence(1, s.length());
                        zero = Integer.valueOf(s.toString().substring(0,1));
                        update=true;
                    }
                    int newValue = Integer.valueOf(s.toString());
                    if (newValue > 999) {
                        newValue = 999;
                        update=true;

                    } else if (newValue < 0) {
                        newValue = 0;
                        update = true;
                    }
                    else if(newValue==0) {
                        mQuantityView.setText(null);
//                        if(mPriceInterface!=null)
//                            mPriceInterface.removePrice(title);
                    }
                    quantity = newValue;

//                    if(mPriceInterface!=null){
//                        mPriceInterface.setPrice(title,price,quantity);
//                    }

                    if(update)
                        mQuantityView.setText(String.valueOf(newValue));
                }else{
                    quantity=0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCardView= (CardView) getActivity().findViewById(R.id.product_card_layout);
        mProgressBar= (ProgressBar) getActivity().findViewById(R.id.product_card_image_progress);
        mTitleView.setText(title);
        mPriceView.setText(getActivity().getString(R.string.price)+": "+String.valueOf(mProduct.getPrice()));
        mDescriptionView.setText(description);
        Log.d("tag",mDescriptionView.getText().toString());
        Glide.with(getActivity()).load(image).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                mProgressBar.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mProgressBar.setVisibility(View.INVISIBLE);
                return false;
            }
        }).error(R.drawable.placeholder).into(mImageView);
        if(NewPurchaseFragment.mOrderProducts.containsKey(title)){
            mQuantityView.setText(String.valueOf(NewPurchaseFragment.mOrderProducts.get(title)[1]));
        }

        mAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPriceInterface!=null) {
                    if(quantity==0)
                      mPriceInterface.removePrice(title);
                    else
                        mPriceInterface.setPrice(title,price,quantity);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id.product_card_quantity_plus){
            if(quantity<999)
                quantity++;
        }else  if(id==R.id.product_card_quantity_minus){
            if(quantity>0)
                quantity--;
        }
        mQuantityView.setText(String.valueOf(quantity));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.product_item_purchase,container,false);
        return v;
    }
    public void setPriceListener(priceInterface mInterface){
        mPriceInterface=mInterface;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}
