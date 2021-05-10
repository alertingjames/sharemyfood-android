package com.sharemyfood.models;

import com.google.android.gms.maps.model.LatLng;

public class OrderHistory {

    String id = "";
    String title ="";

    String useId = "";
    String username = "";
    String userpicture = "";
    String userPhone = "";

    String foodId = "";
    String foodImage = "";
    String foodNmae = "";

    String orderId = "";
    String ordername = "";
    String orderpicture = "";
    String orderPhone = "";

    int quanitty = 0;
    double weight = 0.0;
    String contact = "";
    String location = "";
    String date_time = "";
    long timestamp = 0;
    String description = "";

    int orderState = 0;  // 0: request, 1: ordering, 2: deliveried


    /*private int id = 0;//
    private int order_id = 0;//
    private int receiver_id = 0;//
    private int sender_id = 0;//
    private String sender_name = "";//
    private String sender_photo = "";//
    private String sender_phone = "";//
    private String message = "";//
    private long date_time = 0; //
    private String option = ""; //


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
    private String state = "";*/

    public OrderHistory(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodNmae() {
        return foodNmae;
    }

    public void setFoodNmae(String foodNmae) {
        this.foodNmae = foodNmae;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUseId() {
        return useId;
    }

    public void setUseId(String useId) {
        this.useId = useId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrdername() {
        return ordername;
    }

    public void setOrdername(String ordername) {
        this.ordername = ordername;
    }

    public String getOrderpicture() {
        return orderpicture;
    }

    public void setOrderpicture(String orderpicture) {
        this.orderpicture = orderpicture;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getOrderPhone() {
        return orderPhone;
    }

    public void setOrderPhone(String orderPhone) {
        this.orderPhone = orderPhone;
    }

    public int getQuanitty() {
        return quanitty;
    }

    public void setQuanitty(int quanitty) {
        this.quanitty = quanitty;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getOrderState() {
        return orderState;
    }

    public void setOrderState(int orderState) {
        this.orderState = orderState;
    }

    public String getUserpicture() {
        return userpicture;
    }

    public void setUserpicture(String userpicture) {
        this.userpicture = userpicture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
