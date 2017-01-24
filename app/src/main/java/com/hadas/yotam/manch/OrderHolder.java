package com.hadas.yotam.manch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kofigyan.stateprogressbar.StateProgressBar;

/**
 * Created by Yotam on 13/01/2017.
 */

public class OrderHolder extends RecyclerView.ViewHolder {
    View v;
    Context mContext;
    public OrderHolder(View itemView) {
        super(itemView);
        v=itemView;
        mContext = v.getContext();
    }

    public void setOrder(Order order){
        StateProgressBar stateProgressBar = (StateProgressBar)v.findViewById(R.id.state_progress_bar);
        String[] statusArray = mContext.getResources().getStringArray(R.array.status_array);
        stateProgressBar.setStateDescriptionData(statusArray);
        for(int i=0;i<statusArray.length;i++){
            if(statusArray[i].equals(order.getStatus())) {
                if(i==0)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                else if(i==1)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                else if(i==2)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                else if(i==3)
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
            }
        }
    }

}
