package com.hadas.yotam.manch;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yotam on 01/01/2017.
 */

public class Order implements Serializable, Parcelable{
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

    public Order(Parcel p){

        this.name = p.readString();
        this.address = p.readString();
        this.phone = p.readString();
        this.status = p.readString();
        this.timeStamp = p.readLong();
        this.uid = p.readString();
        this.totalPrice=p.readInt();
        products = new HashMap<>();
        final int N = p.readInt();
        if(N>0)
        for(int i=0;i<N;i++){
            String key = p.readString();
            Integer value = p.readInt();
            products.put(key,value);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(status);
        dest.writeLong(timeStamp);
        dest.writeString(uid);
        dest.writeInt(totalPrice);
        if(products.size()>0)
            for(Map.Entry<String,Integer> entry : products.entrySet()){
                dest.writeString(entry.getKey());
                dest.writeInt(entry.getValue());
            }

    }

    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>(){
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
}
