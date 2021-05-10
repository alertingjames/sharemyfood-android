package com.sharemyfood.models;

import com.google.android.gms.maps.model.LatLng;
import com.sharemyfood.R;

import java.sql.Timestamp;

public class FoodModel{

    private int id = 0;//
    private int userId = 0;//
    private String username = "";// api
    private String userpicture = "";// api
    private String phone = "";// api
    private String title = "";//
    private String description = ""; //
    private String pictureUrl = ""; //
    private String pickuptime = ""; //
    private String adress = ""; //
    private double weight = 0.0; //
    private int quantity = 0; //
    private LatLng latLng = null;
    private int lifespan = 0;//lisi style
    private int likes = 0;///// like number to this food about all users
    private int requests = 0; /////// order number to this food about all users
    private boolean bLike = false;  //// to like for this food about this user
    private String unit = "k";
    private long registered_tiime = 0;
    private long current_tiime = 0;
    private String state = "";

    private String country = "";
    private String city = "";


    public FoodModel(){

    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getCurrent_tiime() {
        return current_tiime;
    }

    public void setCurrent_tiime(long current_tiime) {
        this.current_tiime = current_tiime;
    }

    public long getRegistered_tiime() {
        return registered_tiime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRegistered_tiime(long registered_tiime) {
        this.registered_tiime = registered_tiime;
    }

    public boolean isbLike() {
        return bLike;
    }

    public void setbLike(boolean bLike) {
        this.bLike = bLike;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpicture() {
        return userpicture;
    }

    public void setUserpicture(String userpicture) {
        this.userpicture = userpicture;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getRequests() {
        return requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getPickuptime() {
        return pickuptime;
    }

    public void setPickuptime(String pickuptime) {
        this.pickuptime = pickuptime;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}