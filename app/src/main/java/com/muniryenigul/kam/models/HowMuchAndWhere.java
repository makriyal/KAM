package com.muniryenigul.kam.models;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
public class HowMuchAndWhere implements Serializable, Parcelable {
    private String howMuch, site, price, URL, cargo, totalPrice, payAtTheDoorCash, payAtTheDoorCard, otherMethods, info;
    private boolean isFree, hasCard, hasTransfer, hasPayPal;
    public HowMuchAndWhere(String site, String price, String URL) {
        this.site = site;
        this.price = price;
        this.URL = URL;
    }
    public HowMuchAndWhere(String howMuch, String site, String price, String cargo, String totalPrice, boolean isFree,  boolean hasCard, boolean hasTransfer, boolean hasPayPal,String otherMethods, String payAtTheDoorCash, String payAtTheDoorCard, String info, String URL) {
        this.howMuch = howMuch;
        this.site = site;
        this.price = price;
        this.cargo = cargo;
        this.totalPrice = totalPrice;
        this.payAtTheDoorCash = payAtTheDoorCash;
        this.payAtTheDoorCard = payAtTheDoorCard;
        this.otherMethods = otherMethods;
        this.isFree = isFree;
        this.hasCard = hasCard;
        this.hasTransfer = hasTransfer;
        this.hasPayPal = hasPayPal;
        this.info = info;
        this.URL = URL;
    }
    public HowMuchAndWhere(Parcel in) {
        site = in.readString();
        price = in.readString();
        URL = in.readString();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(site);
        dest.writeString(price);
        dest.writeString(URL);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    public static final Creator<HowMuchAndWhere> CREATOR = new Creator<HowMuchAndWhere>() {
        @Override
        public HowMuchAndWhere createFromParcel(Parcel in) {
            return new HowMuchAndWhere(in);
        }
        @Override
        public HowMuchAndWhere[] newArray(int size) {
            return new HowMuchAndWhere[size];
        }
    };
    public String getPrice() {
        return price;
    }
    public String getURL() {
        return URL;
    }
    public String getSite() {
        return site;
    }
    public String getHowMuch() {
        return howMuch;
    }
    public String getCargo() { return cargo; }
    public String getTotalPrice() { return totalPrice; }
    public String getPayAtTheDoorCash() { return payAtTheDoorCash; }
    public String getPayAtTheDoorCard() { return payAtTheDoorCard; }
    public String getInfo() { return info; }
    public boolean isFree() { return isFree; }
    public String getOtherMethods() { return otherMethods; }
    public boolean isHasPayPal() { return hasPayPal; }
    public boolean isHasTransfer() { return hasTransfer; }
    public boolean isHasCard() { return hasCard; }
    public void setPrice(String price) { this.price = price; }
}