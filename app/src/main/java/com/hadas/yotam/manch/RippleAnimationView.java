package com.hadas.yotam.manch;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by Yotam on 22/01/2017.
 */

@TargetApi(21)
public class RippleAnimationView extends View {
    private View animationView;
    Boolean animationFlag;
    public RippleAnimationView(Context context) {
        super(context);
        setVisibility(INVISIBLE);
        setBackground(getResources().getDrawable(R.drawable.circle_button));
        setElevation(1);
    }


    public void setViewForAnimation(View v){
        animationView=v;
        this.setLayoutParams(new LinearLayout.LayoutParams(150,150));
        setX(animationView.getX());
        setY(animationView.getY());

    }

    public void startAnimation(){
        if(animationView==null)
            return;
        final int height =150;
        final int width = 150;
        if(this.isAttachedToWindow()){
            setVisibility(VISIBLE);
            animationFlag=true;
            showAnimation(height,width);
        }
        this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                animationFlag=true;
                setVisibility(VISIBLE);
                showAnimation(height,width);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                animationFlag=false;
            }
        });
    }

    Animator showAnimator;

    private void showAnimation(final int height, final int width){

        int cy = height/2;
        int cx = width/2;
        float radius = (float) Math.hypot(cx,cy)/2;
         showAnimator = ViewAnimationUtils.createCircularReveal(this,cx,cy,0,radius);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
            if(animationFlag)
                stopShowAnimation(height,width);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showAnimator.setDuration(1000);
        showAnimator.setInterpolator(new AccelerateInterpolator());
        showAnimator.start();

    }

    Animator hideAnimator;
    private void stopShowAnimation(final int height,final int width){
        int cy = height/2;
        int cx = width/2;
        float radius = (float) Math.hypot(cx,cy)/2;
        hideAnimator = ViewAnimationUtils.createCircularReveal(this,cx,cy,radius,0);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(animationFlag)
                    showAnimation(height,width);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnimator.setDuration(1000);
        hideAnimator.setInterpolator(new AccelerateInterpolator());
        hideAnimator.start();
    }



}
