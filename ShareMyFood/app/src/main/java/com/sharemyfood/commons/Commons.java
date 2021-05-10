package com.sharemyfood.commons;

import com.google.android.gms.maps.GoogleMap;
import com.sharemyfood.models.FoodModel;
import com.sharemyfood.models.Message;
import com.sharemyfood.models.OrderHistory;
import com.sharemyfood.models.OrderModel;
import com.sharemyfood.models.UserModel;

public class Commons {

    public static UserModel thisUser = new UserModel();//use
    public static UserModel user = new UserModel();

    public static int curMapTypeIndex = 1;//use
    public static boolean mapCameraMoveF = false;//use
    public static GoogleMap googleMap = null;//use

    public static FoodModel food = new FoodModel();//use
    public static OrderHistory orderHistory = new OrderHistory();
    public static OrderModel order = new OrderModel();
    public static boolean bImageViewState = false;

    public static boolean bNotyAdmin = false;   /// NotificationActivity
    public static boolean bNotyOrder = false;  //// NotificationActivity
    public static boolean bNotyMessage = false;  //// NotificationActivity
    public static boolean bOrderHis = false;  //// OrderHistoryActivity
    public static boolean bDashHis = false;  //// OrderHistoryActivity

    public static Double latitude = 0.0;
    public static Double longitude = 0.0;

    public static int chattingID = 0;
    public static int product_id = 0;
    public static String role = "";

    public static Message message = new Message();
}
