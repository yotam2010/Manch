package com.hadas.yotam.manch;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yotam on 01/01/2017.
 */

public class Order implements Serializable{
     String name;
     String address;
     String phone;
     String status;
     String uid;
    int totalPrice;
    long timeStamp;
     HashMap<String,Integer> products;

    public Order() {
    }

    public Order(String name, String address, String phone, HashMap<String, Integer[]> productsList, String status,long timeStamp,String uid, int totalPrice) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.status = status;
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.products = new HashMap<>();
        this.totalPrice=totalPrice;
        for(Map.Entry<String, Integer[]>entry : productsList.entrySet()){
            products.put(entry.getKey(),entry.getValue()[1]);
        }
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public HashMap<String, Integer> getProducts() {
        return products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

}
