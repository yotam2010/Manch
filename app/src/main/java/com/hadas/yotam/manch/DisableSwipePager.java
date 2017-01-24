package com.hadas.yotam.manch;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Yotam on 14/01/2017.
 */

public class DisableSwipePager extends ViewPager {
    public DisableSwipePager(Context context) {
        super(context);
    }

    public DisableSwipePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}

