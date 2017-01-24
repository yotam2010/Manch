package com.hadas.yotam.manch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.TransitionRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Yotam on 14/12/2016.
 */

public class HistoryFragment extends Fragment implements HistoryOrderHolder.NewOrderFromHistory {

    RecyclerView mRecyclerView;
    FirebaseRecyclerAdapter<Order,HistoryOrderHolder> mFirebaseRecyclerAdapter;
    DatabaseReference mDatabaseReference;
    Boolean loadNewOrder=false;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.USERS).child(FirebaseConstants.MY_UID).child(FirebaseConstants.COMPLETE);
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, HistoryOrderHolder>(Order.class,R.layout.item_history_order,HistoryOrderHolder.class,mDatabaseReference) {
            @Override
            protected void populateViewHolder(HistoryOrderHolder viewHolder, Order model, int position) {
            viewHolder.setLayout((ManagementActivity)getActivity(),model,HistoryFragment.this);
            }
        };

        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_orders,container,false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.history_recycler_view);
        return v;
    }

    @Override
    public void newOrder(final HashMap<String, Integer> productsMap) {
        if(!loadNewOrder) {
            if (!AppConstants.getInternetConnection()) {
                Toast.makeText(getContext(), R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!AppConstants.getIsStoreOpen()) {
                Toast.makeText(getContext(), R.string.store_closed, Toast.LENGTH_SHORT).show();
                return;
            }
            loadNewOrder = true;
            Toast.makeText(getContext(),"טוען הזמנה, לתשומת לבך, עלולים להיות שינויים בתפריט.",Toast.LENGTH_LONG).show();
            final HashMap<String, Integer[]> newProductsList = new HashMap<>();
            DatabaseReference mProductsDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.PRODUCTS);
            mProductsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int newTotalPrice = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.getKey().equals("titleList")) {
                            Product product = ds.getValue(Product.class);
                            if (productsMap.containsKey(product.getTitle())) {
                                newProductsList.put(product.getTitle(), new Integer[]{product.getPrice(), productsMap.get(product.getTitle())});
                                newTotalPrice += productsMap.get(product.getTitle()) * product.getPrice();
                            }
                        }
                    }
                    if (newTotalPrice >= AppConstants.MINIMUM_ORDER) {
                        Intent intent = new Intent(getContext(), NewOrderActivity.class);
                        intent.putExtra(FirebaseConstants.PRODUCTS, newProductsList);
                        intent.putExtra(FirebaseConstants.PRICE, newTotalPrice);

                        if (Build.VERSION.SDK_INT >= 16)
                            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                        else
                            startActivity(intent);
                    }else
                        Toast.makeText(getContext(),getString(R.string.minimum_order_price,AppConstants.MINIMUM_ORDER),Toast.LENGTH_LONG).show();

                    loadNewOrder = false;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loadNewOrder = false;
                }
            });
        }

    }
}
