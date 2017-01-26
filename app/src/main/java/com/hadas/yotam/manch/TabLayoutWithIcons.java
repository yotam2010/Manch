package com.hadas.yotam.manch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Yotam on 26/01/2017.
 */

public class TabLayoutWithIcons extends TabLayout {

    int color;
    public TabLayoutWithIcons(Context context) {
        super(context);
        color = context.getResources().getColor(R.color.tab_icon_selected);
    }

    public TabLayoutWithIcons(Context context, AttributeSet attrs) {
        super(context, attrs);
        color = context.getResources().getColor(R.color.tab_icon_selected);
    }

    public TabLayoutWithIcons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        color = context.getResources().getColor(R.color.tab_icon_selected);
    }



    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        switch (position){
            case 0:
                tab.setIcon(R.drawable.ic_history_black_24dp);
                break;
            case 1:
                tab.setIcon(R.drawable.ic_weekend_black_24dp);
                break;
            case 2:
                tab.setIcon(R.drawable.ic_note_add_black_24dp);
                break;

        }
        super.addTab(tab, position, setSelected);
    }



    @Override
    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {

        super.addOnTabSelectedListener(listener);
        super.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                if(tab.getIcon()!=null)
                    tab.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onTabUnselected(Tab tab) {
                if(tab.getIcon()!=null)
                    tab.getIcon().clearColorFilter();
            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });
    }






}
