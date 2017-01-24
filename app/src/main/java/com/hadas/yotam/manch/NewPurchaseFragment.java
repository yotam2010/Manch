package com.hadas.yotam.manch;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yotam on 14/12/2016.
 */

public class NewPurchaseFragment extends Fragment implements View.OnClickListener, ProductItemFragment.priceInterface{

    Query mProductsRef;
    static FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    RecyclerView mRecyclerView;
    static int itemFocus=-1;
    static int lastItemFocused=-1;
    int totalPrice;
    static Boolean firstLoad;
    ProgressBar mProgressBar;
    public static HashMap<String,Integer[]> mOrderProducts;
    public static final String QUANTITY_CONS="QUANTITY_CONS";
    public static final String LAST_ITEM="LAST_ITEM";
    public static final String CURRENT_ITEM="CURRENT_ITEM";
    ImageButton mFinishOrder;
    static ImageButton mNextItemButton;
    static ImageButton mPreviousItemButton;
    TextView mTotalPriceText;
    BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        totalPrice=0;
        firstLoad=true;
        if(savedInstanceState!=null && ManagementActivity.mProductsHashMap==null){
            if(savedInstanceState.containsKey(FirebaseConstants.PRODUCTS) && savedInstanceState.containsKey(LAST_ITEM)&&savedInstanceState.containsKey(CURRENT_ITEM)) {
                mOrderProducts = (HashMap<String, Integer[]>) savedInstanceState.getSerializable(FirebaseConstants.PRODUCTS);
                itemFocus = savedInstanceState.getInt(CURRENT_ITEM);
                lastItemFocused = savedInstanceState.getInt(LAST_ITEM);
            }
        }else{
            itemFocus=0;
            lastItemFocused=0;

            if(ManagementActivity.mProductsHashMap==null)
                mOrderProducts = new HashMap<>();
            else
                mOrderProducts=ManagementActivity.mProductsHashMap;
        }
        mRecyclerView = (RecyclerView)getActivity().findViewById(R.id.products_recycler_view);
        mFinishOrder = (ImageButton)getActivity().findViewById(R.id.new_order_finish_button);
        mPreviousItemButton = (ImageButton) getActivity().findViewById(R.id.products_recycler_previous_item);
        mNextItemButton= (ImageButton) getActivity().findViewById(R.id.products_recycler_next_item);
        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.new_purchase_progressBar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mProductsRef = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PRODUCTS).orderByChild("image").startAt("https://firebasestorage");
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product,ProductHolder>(Product.class,R.layout.item_product,ProductHolder.class,mProductsRef) {
            @Override
            protected void populateViewHolder(ProductHolder viewHolder, Product model, int position) {
                    viewHolder.setViews(model, getContext(), getActivity().getSupportFragmentManager(), position, NewPurchaseFragment.this);
                    mProgressBar.setVisibility(View.GONE);
            }
        };
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
        mRecyclerView.smoothScrollToPosition(itemFocus);
        mPreviousItemButton.setOnClickListener(this);
        mNextItemButton.setOnClickListener(this);
        mTotalPriceText = (TextView)getActivity().findViewById(R.id.toolbar_sum_price_text);

        mFinishOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppConstants.getInternetConnection()) {
                    if (AppConstants.getIsStoreOpen()) {
                        if (mOrderProducts != null && mOrderProducts.size() > 0 && totalPrice >= AppConstants.MINIMUM_ORDER) {
                            Intent intent = new Intent(getActivity(), NewOrderActivity.class);
                            intent.putExtra(FirebaseConstants.PRICE, totalPrice);
                            intent.putExtra(FirebaseConstants.PRODUCTS,mOrderProducts);
                            if(Build.VERSION.SDK_INT>=16)
                                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                            else
                                startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.minimum_order_price,AppConstants.MINIMUM_ORDER), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(getContext(), R.string.store_closed, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBroadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshOrder();
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver,new IntentFilter(NewOrderActivity.FILTER_SENT_ORDER));

    }

    public void refreshOrder(){
        itemFocus=0;
        lastItemFocused=0;
        totalPrice=0;
        firstLoad=true;
        ManagementActivity.mProductsHashMap=new HashMap<>();
        mOrderProducts = new HashMap<>();
        mRecyclerView.setAdapter(null);
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(FirebaseConstants.PRODUCTS,mOrderProducts);
        outState.putInt(CURRENT_ITEM,itemFocus);
        outState.putInt(LAST_ITEM,lastItemFocused);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_purchase,container,false);
        return v;
    }
    public static class ProductHolder extends RecyclerView.ViewHolder{
        View v;
        public ProductHolder(View itemView) {
            super(itemView);
            v=itemView;
        }

        private void setViews(final Product model, final Context context, final FragmentManager fragmentManager, final int position, final NewPurchaseFragment newPurchaseFragment){
            TextView itemTitle = (TextView)v.findViewById(R.id.item_product_title);
//            TextView itemDescription = (TextView)v.findViewById(R.id.item_product_description);
            TextView itemPrice = (TextView)v.findViewById(R.id.item_product_price);
            final ImageView itemImage = (ImageView)v.findViewById(R.id.item_product_image);
            final ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.item_product_image_progress);
            final CardView cardView = (CardView)v.findViewById(R.id.item_product_card_layout);
            itemTitle.setText(model.getTitle());
            itemPrice.setText(context.getString(R.string.price)+": "+String.valueOf(model.getPrice()));
//            itemDescription.setText(model.getDescription());
            if(position==itemFocus)
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.transperentBlack));
            else
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cardview_light_background));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemFocus!=-1 &&mFirebaseRecyclerAdapter!=null)
                        lastItemFocused=itemFocus;
                    itemFocus=position;
                    cardView.setCardBackgroundColor(context.getResources().getColor(R.color.transperentBlack));
                    if(itemFocus!=lastItemFocused) {
                        mFirebaseRecyclerAdapter.notifyDataSetChanged();
                        lastItemFocused=itemFocus;
                        ProductItemFragment productItemFragment = new ProductItemFragment();
                        Bundle args = new Bundle();
                        args.putString(FirebaseConstants.TITLE,model.getTitle());
                        args.putString(FirebaseConstants.DESCRIPTION,model.getDescription());
                        args.putInt(FirebaseConstants.PRICE,model.getPrice());
                        args.putString(FirebaseConstants.IMAGE,model.getImage());
                        productItemFragment.setArguments(args);
                        productItemFragment.setPriceListener(newPurchaseFragment);
                        fragmentManager.beginTransaction().replace(R.id.product_card_fragment_container,productItemFragment).commitAllowingStateLoss();
                    }
                    if(itemFocus==0) {
                        mPreviousItemButton.setVisibility(View.GONE);
                        mNextItemButton.setVisibility(View.VISIBLE);
                    }else if(itemFocus == mFirebaseRecyclerAdapter.getItemCount()-1){
                        mPreviousItemButton.setVisibility(View.VISIBLE);
                        mNextItemButton.setVisibility(View.GONE);
                    } else if(itemFocus >0 && itemFocus < mFirebaseRecyclerAdapter.getItemCount()-1){
                        mPreviousItemButton.setVisibility(View.VISIBLE);
                        mNextItemButton.setVisibility(View.VISIBLE);
                    }
                    else
                        if(mFirebaseRecyclerAdapter.getItemCount()==0){
                            mPreviousItemButton.setVisibility(View.GONE);
                            mNextItemButton.setVisibility(View.GONE);
                        }
                }
            });
            if(firstLoad && position==itemFocus){
                firstLoad=false;
                ProductItemFragment productItemFragment = new ProductItemFragment();
                Bundle args = new Bundle();
                args.putString(FirebaseConstants.TITLE,model.getTitle());
                args.putString(FirebaseConstants.DESCRIPTION,model.getDescription());
                args.putInt(FirebaseConstants.PRICE,model.getPrice());
                args.putString(FirebaseConstants.IMAGE,model.getImage());
                productItemFragment.setArguments(args);
                productItemFragment.setPriceListener(newPurchaseFragment);
                fragmentManager.beginTransaction().replace(R.id.product_card_fragment_container,productItemFragment).commitAllowingStateLoss();
                if(itemFocus==0) {
                    mPreviousItemButton.setVisibility(View.GONE);
                    mNextItemButton.setVisibility(View.VISIBLE);
                }else if(itemFocus == mFirebaseRecyclerAdapter.getItemCount()-1){
                    mPreviousItemButton.setVisibility(View.VISIBLE);
                    mNextItemButton.setVisibility(View.GONE);
                } else if(itemFocus >0 && itemFocus < mFirebaseRecyclerAdapter.getItemCount()-1){
                    mPreviousItemButton.setVisibility(View.VISIBLE);
                    mNextItemButton.setVisibility(View.VISIBLE);
                }
                else
                if(mFirebaseRecyclerAdapter.getItemCount()==0){
                    mPreviousItemButton.setVisibility(View.GONE);
                    mNextItemButton.setVisibility(View.GONE);
                }

            }
            Glide.with(context).load(model.getImage()).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).centerCrop().error(R.drawable.placeholder).into(itemImage);


        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        firstLoad=true;
        switch (viewId){
                case R.id.products_recycler_previous_item:
                    if(itemFocus-1>=0){
                        lastItemFocused=itemFocus;
                        itemFocus--;
                        mFirebaseRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(itemFocus);
                    }
                    break;
                case  R.id.products_recycler_next_item:
                    if(itemFocus+1<mFirebaseRecyclerAdapter.getItemCount()){
                        lastItemFocused=itemFocus;
                        itemFocus++;
                        mFirebaseRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(itemFocus);
                    }
                    break;
            }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mOrderProducts!=null&&mOrderProducts.size()>=1)
            ManagementActivity.mProductsHashMap=mOrderProducts;

    }

    @Override
    public void onStart() {
        super.onStart();
        totalPrice=0;
        updateTotalPrice();

    }

    @Override
    public void setPrice(String title, int price,int quantity) {
        mOrderProducts.put(title,new Integer[]{price,quantity});
        totalPrice=0;
        updateTotalPrice();
    }

    private void updateTotalPrice(){
        for(Map.Entry<String,Integer[]> map: mOrderProducts.entrySet())
            totalPrice+=map.getValue()[0]*map.getValue()[1];
        mTotalPriceText.setText(getString(R.string.total_price,totalPrice));

    }

    @Override
    public void onDestroy() {
        if(mBroadcastReceiver!=null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void removePrice(String title) {
        mOrderProducts.remove(title);
        totalPrice=0;
        updateTotalPrice();
    }
}






