package com.hadas.yotam.manch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Yotam on 20/01/2017.
 */

public class HistoryOrderHolder extends RecyclerView.ViewHolder {
    interface NewOrderFromHistory{
        public void newOrder(HashMap<String,Integer> productsMap);
    }

    View v;
    Boolean productsDisplay;
    ArrayList<TextView> mProductsList;
    public HistoryOrderHolder(View itemView) {
        super(itemView);
        v=itemView;
    }
    public void setLayout(final ManagementActivity context, final Order order,final NewOrderFromHistory mNewOrder){
        productsDisplay=false;
        ImageButton duplicateButton = (ImageButton)v.findViewById(R.id.history_item_duplicate_button);
        TextView nameText = (TextView)v.findViewById(R.id.history_item_name_text);
        TextView addressText = (TextView)v.findViewById(R.id.history_item_address_text);
        TextView phoneText = (TextView)v.findViewById(R.id.history_item_phone_text);
        TextView totalText = (TextView)v.findViewById(R.id.history_item_price_text);
        TextView dateText = (TextView)v.findViewById(R.id.history_item_date_text);
        final Button productsButton = (Button)v.findViewById(R.id.history_item_products_button);
        final LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.history_item_linear_layout);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("DD/MM - HH:mm");
        dateText.setText(simpleDateFormat.format(order.getTimeStamp()));

        nameText.setText(order.getName());
        addressText.setText(order.getAddress());
        phoneText.setText(order.getPhone());
        totalText.setText(String.valueOf(order.getTotalPrice()));


        duplicateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mNewOrder.newOrder(order.getProducts());
            }
        });
        if(mProductsList==null) {
            mProductsList = new ArrayList<>();
            for (HashMap.Entry<String, Integer> entry : order.getProducts().entrySet()){
                TextView textView = new TextView(context);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText(entry.getValue()+" X "+entry.getKey());
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.bodyTextSize));
                mProductsList.add(textView);
            }
        }
        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mProductsList!=null) {
                    if (productsDisplay)
                        for (TextView textView : mProductsList)
                            linearLayout.removeView(textView);
                     else
                        for (TextView textView : mProductsList)
                            linearLayout.addView(textView);
                    productsDisplay=!productsDisplay;
                }
            }
        });


    }
}
