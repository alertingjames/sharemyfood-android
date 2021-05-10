package com.sharemyfood.models;

import com.google.android.gms.maps.model.LatLng;

public class OrderModel {

    private int id = 0;//
    private int order_id = 0;//
    private int receiver_id = 0;//
    private int sender_id = 0;//
    private String sender_name = "";//
    private String sender_photo = "";//
    private String sender_phone = "";//
    private String message = "";//
    private long date_time = 0; //
    private String option = ""; //

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_photo() {
        return sender_photo;
    }

    public void setSender_photo(String sender_photo) {
        this.sender_photo = sender_photo;
    }

    public String getSender_phone() {
        return sender_phone;
    }

    public void setSender_phone(String sender_phone) {
        this.sender_phone = sender_phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate_time() {
        return date_time;
    }

    public void setDate_time(long date_time) {
        this.date_time = date_time;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
