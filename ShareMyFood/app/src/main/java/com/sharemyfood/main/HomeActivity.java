package com.sharemyfood.main;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leolin.shortcutbadger.ShortcutBadger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.daasuu.bl.BubbleLayout;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.db.TinyDB;
import com.sharemyfood.models.FoodModel;
import com.sharemyfood.R;
import com.sharemyfood.models.Message;
import com.sharemyfood.models.OrderModel;
import com.sharemyfood.models.UserModel;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    boolean bottomTabState = true; // true: food, false: orderHistory

    public final int REQUEST_CODE = 100;
    public final String REQUEST_MAP = "SET_MY_LOCATION";
    private LocationManager mLocationManager;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001; LatLng myLatLang = null;

    String address = "";
    String country = "";
    String city = "";


    ArrayList<FoodModel> foods = new ArrayList<>();
    ArrayList<FoodModel> foodIndia = new ArrayList<>();
    ArrayList<OrderModel> messages = new ArrayList<>();

    @BindView(R.id.img_search) ImageView imgSearch;
    @BindView(R.id.home_title) TextView tvTitle;
    @BindView(R.id.edit_search) EditText editSearch;
    @BindView(R.id.foodButton) ImageView imgFood;
    @BindView(R.id.tv_food) TextView tvFood;
    @BindView(R.id.messageButton) ImageView imgMessage;
    @BindView(R.id.tv_message) TextView tvMessage;
    @BindView(R.id.tv_noty) TextView tvNoty;

    double radius = 0.0d;
    AVLoadingIndicatorView progressBar;
    LinearLayout lin_message_container;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        tinyDB = new TinyDB(this);

        ///Title ActionBar Transperent Custom
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ButterKnife.bind(this);

        ///// drawer Menu
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
       // navigationView.inflateHeaderView(R.layout.draw_menu_header); ///programmatically

        navigationView.setNavigationItemSelectedListener(this);
        ImageView imvavartar = navigationView.getHeaderView(0).findViewById(R.id.avatar);
        TextView tvuser = navigationView.getHeaderView(0).findViewById(R.id.username);

        //onFoodLoad(setDataFood());
        //onMessageLoad(setDataMessage());

        Glide.with(this).load(Commons.thisUser.getPicture()).placeholder(R.drawable.user).into((imvavartar));
        tvuser.setText(Commons.thisUser.getUsername());

        //getApi();

        setupUI(findViewById(R.id.activity), this);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String keyword = editable.toString();
                if(keyword.trim().length() > 0){
                    if (bottomTabState){
                        filterFood(keyword);
                    } else
                    {
                        filterMessage(keyword);
                    }
                }else {
                    if (bottomTabState){
                        onFoodLoad(foods);
                    } else
                    {
                        onMessageLoad(messages);
                    }
                }
            }
        });

        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                radius = (double) progress / 10;
                /*if(radius == 1000){
                    ((TextView)findViewById(R.id.radius)).setText("1k");
                }else {*/
                    ((TextView)findViewById(R.id.radius)).setText(String.valueOf((double) progress / 10));
                //}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lin_message_container = (LinearLayout) findViewById(R.id.linear_message_container);
        lin_message_container.removeAllViews();


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("HomeActivity:", "getInstanceId failed", task.getException());
                            return;
                        }

// Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("Token!!!", token);
                        uploadNewToken(token);
                    }
                });

        new Thread(new Runnable() {
            @Override
            public void run() {
                getCustomerNotification();
                //getAdminNotification();
            }
        }).start();
        ///// current user position
        checkForLocationPermission();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goApi(Commons.thisUser.getId());
            }
        }, 6000);

        String Bubble = EasyPreference.with(HomeActivity.this).getString("EasyBubble", "");
        if (Bubble.equals("")){
            bubbleFunc();
        }

    }

    void bubbleFunc(){

        EasyPreference.with(HomeActivity.this).addString("EasyBubble", "true").save();

        BubbleLayout blAdd = (BubbleLayout) findViewById(R.id.bl_add);
        BubbleLayout blFilter = (BubbleLayout) findViewById(R.id.bl_filter);
        BubbleLayout blFood = (BubbleLayout) findViewById(R.id.bl_food);
        blAdd.setVisibility(View.VISIBLE);
        blFilter.setVisibility(View.VISIBLE);
        blFood.setVisibility(View.VISIBLE);

        blAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blAdd.setVisibility(View.GONE);
            }
        });
        blFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blFilter.setVisibility(View.GONE);

            }
        });
        blFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blFood.setVisibility(View.GONE);

            }
        });
    }

    private void goApi(int userId){

        Commons.thisUser.setLatLng(myLatLang);
        Commons.thisUser.setAddress(address);
        Commons.thisUser.setCountry(country);
        Commons.thisUser.setCity(city);

        AndroidNetworking.post(ReqConst.SERVER + "setMyLocation")
                .addBodyParameter("member_id", String.valueOf(userId))
                .addBodyParameter("address", address)
                .addBodyParameter("country", country)
                .addBodyParameter("city", city)
                .addBodyParameter("latitude", myLatLang == null? "": String.valueOf(myLatLang.latitude))
                .addBodyParameter("longitude", myLatLang == null? "": String.valueOf(myLatLang.longitude))
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                    @Override
                    public void onError(ANError error) {
                    }
                });
    }

    private void uploadNewToken(String token){

        AndroidNetworking.post(ReqConst.SERVER + "fcm_insert")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .addBodyParameter("token", token)
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });

    }

    private void filterFood(String keyword){
        if(keyword.length() == 0){
            onFoodLoad(foods);
        }else {
            ArrayList<FoodModel> filteredFoods = new ArrayList<>();
            for(FoodModel food: foods){
                if(food.getTitle().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }else if(food.getDescription().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }else if(food.getAdress().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                /*}else if(food.getPhone().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);*/
                }else if(food.getPickuptime().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }else if(String.valueOf(food.getQuantity()).toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }else if(String.valueOf(food.getQuantity()).toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }else if(food.getUsername().toLowerCase().contains(keyword.toLowerCase())){
                    filteredFoods.add(food);
                }
            }

            onFoodLoad(filteredFoods);
        }
    }

    private void filterMessage(String keyword){
        if(keyword.length() == 0){
            onMessageLoad(messages);
        }else {
            ArrayList<OrderModel> filteredMessages = new ArrayList<>();
            for(OrderModel message: messages){
                if(message.getMessage().toLowerCase().contains(keyword.toLowerCase())){
                    filteredMessages.add(message);
                }else if(message.getSender_name().toLowerCase().contains(keyword.toLowerCase())){
                    filteredMessages.add(message);
                }else if(message.getSender_phone().toLowerCase().contains(keyword.toLowerCase())){
                    filteredMessages.add(message);
                }else if(getCurrentTimeString(message.getDate_time()).contains(keyword.toLowerCase())){
                    filteredMessages.add(message);
                }else if(message.getOption().toLowerCase().contains(keyword.toLowerCase())){
                    filteredMessages.add(message);
                }
            }

            onMessageLoad(filteredMessages);
        }
    }

    //private ArrayList<FoodModel> setDataFood() {
    private void setDataFood() {

        foods.clear();
        progressBar.setVisibility(View.VISIBLE);

        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "getAllProducts")
                .addBodyParameter("me_id", String.valueOf(Commons.thisUser.getId()))
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response

                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterResponse=====>", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){

                                JSONArray foodArr = response.getJSONArray("data");
                                if (foodArr.length() != 0) {
                                    ArrayList<FoodModel> foodstemp = new ArrayList<>();

                                    for (int i=0 ; i < foodArr.length(); i++){

                                        JSONObject json =  foodArr.getJSONObject(i);
                                        FoodModel foodModel = new FoodModel();

                                        foodModel.setId(json.getInt("id"));
                                        foodModel.setUserId(json.getInt("member_id"));
                                        foodModel.setUsername(json.getString("member_name"));
                                        foodModel.setUserpicture(json.getString("member_photo"));
                                        foodModel.setTitle(json.getString("name"));
                                        foodModel.setPictureUrl(json.getString("picture_url"));
                                        foodModel.setDescription(json.getString("description"));
                                        foodModel.setPhone(json.getString("phone_number"));
                                        foodModel.setWeight(json.getDouble("weight"));
                                        foodModel.setUnit(json.getString("unit"));
                                        foodModel.setQuantity(json.getInt("quantity"));
                                        foodModel.setPickuptime(json.getString("pickup_time"));
                                        foodModel.setLifespan(json.getInt("lifespan"));
                                        foodModel.setLikes(json.getInt("likes"));
                                        foodModel.setRequests(json.getInt("orders"));
                                        foodModel.setAdress(json.getString("address"));
                                        foodModel.setCountry(json.getString("country"));
                                        foodModel.setCity(json.getString("city"));
                                        foodModel.setLatLng(new LatLng(json.getDouble("latitude"), json.getDouble("longitude")));
                                        foodModel.setRegistered_tiime(json.getLong("registered_time"));
                                        if (json.getString("is_like").equals("yes")) {
                                            foodModel.setbLike(true);
                                        }else{
                                            foodModel.setbLike(false);
                                        }
                                        foodModel.setState(json.getString("status"));

                                        Log.d("DISTANCE" + i, String.valueOf(distance(Commons.thisUser.getLatLng(), foodModel.getLatLng()) / 1000));

                                        if(foodModel.getUserId() != Commons.thisUser.getId()) {
                                            if (distance(Commons.thisUser.getLatLng(), foodModel.getLatLng()) / 1000 <= 5)
                                                foodstemp.add(foodModel);
                                            foods.add(foodModel);
                                        }
                                        if (foodModel.getCountry().equalsIgnoreCase("india")){
                                            foodIndia.add(foodModel);
                                        }

                                    }
                                    onFoodLoad(foodstemp);
                                }
                            }else {
                                //showToast("Network issue");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //showToast("Network issue");
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterError=====>", error.toString());
                        //showToast("Network issue");
                    }
                });
        //return foods;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Commons.role = "";
        setDataFood();
        //setDataMessage();
    }

    void onFoodLoad(ArrayList<FoodModel> foodModels){

        ///////////////////// UI VERSION /////////////////
        LinearLayout lin_food_container = (LinearLayout) findViewById(R.id.linear_food_container);
        lin_food_container.removeAllViews();

        for (int i = 0; i < foodModels.size(); i++) {

            /// Add List Linear Models
            LinearLayout myCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_home_cell, null);
            ImageView imgLike = (ImageView) myCellLayout.findViewById(R.id.cell_like_image);

            ((TextView) myCellLayout.findViewById(R.id.cell_title)).setText(foodModels.get(i).getTitle());
            TextView txv_like =(TextView) myCellLayout.findViewById(R.id.cell_like_text);
            txv_like.setText(String.valueOf(foodModels.get(i).getLikes()));
            Glide.with(this).load(foodModels.get(i).getPictureUrl()).placeholder(R.drawable.cabage).into((ImageView) myCellLayout.findViewById(R.id.cell_image));
            ((TextView) myCellLayout.findViewById(R.id.cell_star_number)).setText("5");
            Glide.with(this).load(foodModels.get(i).getUserpicture()).placeholder(R.drawable.user).into((ImageView) myCellLayout.findViewById(R.id.cell_user_image));
            ((TextView) myCellLayout.findViewById(R.id.cell_like_text)).setText(String.valueOf(foodModels.get(i).getLikes()));
            ((TextView) myCellLayout.findViewById(R.id.cell_weight)).setText(String.valueOf(foodModels.get(i).getWeight()) + " " + foodModels.get(i).getUnit());
            ((TextView) myCellLayout.findViewById(R.id.cell_distance)).setText(String.valueOf((distance(Commons.thisUser.getLatLng(), foodModels.get(i).getLatLng()))) + "km");
            ((TextView) myCellLayout.findViewById(R.id.cell_user_text)).setText(foodModels.get(i).getUsername());

            ////////// Like setting
            if(foodModels.get(i).isbLike()){
                imgLike.setImageResource(R.drawable.like1);
            }else{
                imgLike.setImageResource(R.drawable.likewhite);
            }
            int finalI = i;
            imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                     if(foodModels.get(finalI).isbLike()){
                         // call LikeAPI (foodModels.get(finalI).getUserId(), foodModels.get(finalI).getId)
                         AndroidNetworking.post(ReqConst.SERVER + "unlikeProduct")
                                 .addBodyParameter("product_id", String.valueOf(foodModels.get(finalI).getId()))
                                 .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                                 .setTag(this)
                                 .setPriority(Priority.MEDIUM)
                                 .build()
                                 .getAsJSONObject(new JSONObjectRequestListener() {
                                     @Override
                                     public void onResponse(JSONObject response) {
                                         // do anything with response
                                         Log.d("RegisterResponse=====>", response.toString());
                                         txv_like.setText(String.valueOf(foodModels.get(finalI).getLikes() - 1));
                                         imgLike.setImageResource(R.drawable.likewhite);
                                         foods.get(finalI).setLikes(foodModels.get(finalI).getLikes() - 1);
                                         foods.get(finalI).setbLike(false);
                                     }
                                     @Override
                                     public void onError(ANError error) {
                                         // handle error
                                         Log.d("RegisterError=====>", error.toString());
                                     }
                                 });
                     }else{
                         // call LikeAPI (foodModels.get(finalI).getUserId(), foodModels.get(finalI).getId)
                         AndroidNetworking.post(ReqConst.SERVER + "likeProduct")
                                 .addBodyParameter("product_id", String.valueOf(foodModels.get(finalI).getId()))
                                 .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                                 .setTag(this)
                                 .setPriority(Priority.MEDIUM)
                                 .build()
                                 .getAsJSONObject(new JSONObjectRequestListener() {
                                     @Override
                                     public void onResponse(JSONObject response) {
                                         // do anything with response
                                         Log.d("RegisterResponse=====>", response.toString());
                                         txv_like.setText(String.valueOf(foodModels.get(finalI).getLikes() + 1));
                                         imgLike.setImageResource(R.drawable.like1);
                                         foods.get(finalI).setLikes(foodModels.get(finalI).getLikes() + 1);
                                         foods.get(finalI).setbLike(true);
                                     }
                                     @Override
                                     public void onError(ANError error) {
                                         // handle error
                                     }
                                 });
                     }
                }
            });
            myCellLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                    Commons.role = "customer";
                    Commons.food = foodModels.get(finalI);
                    Commons.bNotyOrder = true;
                    startActivity(intent);
                }
            });
            //// inflate
            lin_food_container.addView(myCellLayout);
        }

        if(foodModels.size() == 0){
            ((TextView) findViewById(R.id.tv_foodresult)).setText(R.string.food_noresult);
        } else {
            ((TextView) findViewById(R.id.tv_foodresult)).setText(R.string.food_yesresult);
        }
    }


    private void setDataMessage() {

        messages.clear();
        progressBar.setVisibility(View.VISIBLE);

        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "getNotifications")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response

                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterResponse=====>", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){

                                JSONArray messageArr = response.getJSONArray("data");
                                if (messageArr.length() != 0) {

                                    for (int i=0 ; i < messageArr.length(); i++){

                                        JSONObject json =  messageArr.getJSONObject(i);
                                        OrderModel messageModel = new OrderModel();

                                        messageModel.setId(json.getInt("id"));
                                        messageModel.setOrder_id(json.getInt("order_id"));
                                        messageModel.setReceiver_id(json.getInt("receiver_id"));
                                        messageModel.setSender_id(json.getInt("sender_id"));
                                        messageModel.setSender_name(json.getString("sender_name"));
                                        messageModel.setSender_phone(json.getString("sender_phone"));
                                        messageModel.setDate_time(json.getLong("date_time"));
                                        messageModel.setSender_photo(json.getString("sender_photo"));
                                        messageModel.setOption(json.getString("option"));
                                        messageModel.setMessage(json.getString("message"));

                                        if(messageModel.getReceiver_id() == Commons.thisUser.getId()) {
                                            messages.add(messageModel);
                                        }
                                    }
                                    onMessageLoad(messages);
                                    notyManager();
                                }
                            }else {
                                //showToast("Network issue");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //showToast("Network issue");
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterError=====>", error.toString());
                        //showToast("Network issue");
                    }
                });
    }

    void onMessageLoad(ArrayList<OrderModel> messageModels) {

        ///////////////////// UI VERSION /////////////////
        LinearLayout lin_message_container = (LinearLayout) findViewById(R.id.linear_message_container);
        lin_message_container.removeAllViews();

        int i = 0;
        for (OrderModel message: messageModels) {

            i = i + 1;
            if (message.getOption().equals("admin")) {
                /// Add List Linear Models for admin
                LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_admin_message, null);
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_title)).setText(message.getOption());
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_description)).setText(message.getMessage());
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_datetime)).setText(getCurrentTimeString(message.getDate_time()));
                //// inflate
                lin_message_container.addView(myMessageCellLayout);

                ////////// close setting
                ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.admin_close);
                final int ii = i;
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int index = messages.indexOf(message);
                        messages.remove(index);
                        onDelete(index, lin_message_container, myMessageCellLayout);
                        //lin_message_container.removeView(myMessageCellLayout);
                    }
                });

                myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), AdminMessageActivity.class);
                        Commons.order = message;
                        startActivity(intent);
                    }
                });
            } else { //// "order"

                /// Add List Linear Models for order
                LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_order_message, null);
                Glide.with(this).load(message.getSender_photo()).placeholder(R.drawable.user).into((ImageView) myMessageCellLayout.findViewById(R.id.order_userimage));
                ((TextView) myMessageCellLayout.findViewById(R.id.order_username)).setText(message.getSender_name());
                ((TextView) myMessageCellLayout.findViewById(R.id.order_message)).setText(message.getMessage());
                ((TextView) myMessageCellLayout.findViewById(R.id.order_datetime)).setText(getCurrentTimeString(message.getDate_time()));
                //// inflate
                lin_message_container.addView(myMessageCellLayout);

                ////////// close setting
                ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.order_close);
                final int ii = i;
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int index = messages.indexOf(message);
                        onDelete(index, lin_message_container, myMessageCellLayout);
                        //lin_message_container.removeView(myMessageCellLayout);
                    }
                });

                myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), OrderMessageActivity.class);
                        Commons.order = message;
                        //startActivity(intent);
                    }
                });

                ////////// close setting
                Button btnReply = (Button) myMessageCellLayout.findViewById(R.id.btn_reply);
                btnReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserModel userModel = new UserModel();
                        userModel.setId(message.getSender_id());
                        userModel.setUsername(message.getSender_name());
                        userModel.setPicture(message.getSender_photo());
                        Commons.user = userModel;
                        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                        startActivity(intent);
                    }
                });

            }
        }

        if(messageModels.size() == 0){
            ((TextView) findViewById(R.id.tv_messageresult)).setText(getString(R.string.message_noresult));
        } else {
            ((TextView) findViewById(R.id.tv_messageresult)).setText(getString(R.string.message_yesresult));
        }
    }

    void notyManager()
    {
        if (messages.size() > 0){
            ((TextView) findViewById(R.id.tv_messageresult)).setText(R.string.message_yesresult);
            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
            tvNoty.setText(String.valueOf(messages.size()));
        } else {
            ((TextView) findViewById(R.id.tv_messageresult)).setText(R.string.message_noresult);
            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
        }
    }

    void onDelete(int index, LinearLayout lin, LinearLayout cell){

        View pinDialogView = LayoutInflater.from(this).inflate(R.layout.alert_confirm_delete, null, false);
        androidx.appcompat.app.AlertDialog pinDialog = new androidx.appcompat.app.AlertDialog.Builder(this).create();
        pinDialog.setView(pinDialogView);
        TextView txvCancel = (TextView) pinDialogView.findViewById(R.id.tv_cancel);
        TextView txvLogout = (TextView) pinDialogView.findViewById(R.id.tv_confirm);
        txvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinDialog.hide();
            }
        });
        txvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinDialog.hide();
                showToast("New Notification delect success");

                //// If Api success
                lin.removeView(cell);
                messages.remove(index);
                notyManager();
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();

    }
    public void onSearch(View view)
    {
        if (editSearch.getVisibility() == View.VISIBLE){
            imgSearch.setImageResource(R.drawable.search);
            tvTitle.setVisibility(View.VISIBLE);
//            editSearch.setText("");
            editSearch.setVisibility(View.GONE);
        } else {
            imgSearch.setImageResource(R.drawable.close);
            tvTitle.setVisibility(View.GONE);
            editSearch.setVisibility(View.VISIBLE);
            showSoftKeyboard((View)editSearch, this);
        }
    }

    @OnClick(R.id.messageButton)
    void onMessage(){
        bottomTabState = false;
        imgFood.setImageResource(R.drawable.food);
        tvFood.setTextColor(getResources().getColor(R.color.colorLightBlack));
        imgMessage.setImageResource(R.drawable.message_red);
        tvMessage.setTextColor(getResources().getColor(R.color.colorPunc));
        ((ScrollView) findViewById(R.id.food_fragement)).setVisibility(View.INVISIBLE);
        ((ScrollView) findViewById(R.id.message_fragement)).setVisibility(View.VISIBLE);
//        ((ScrollView)findViewById(R.id.scrollView)).scrollTo(0,0);
    }

    @OnClick(R.id.foodButton)
    void onSearchButton(){
        bottomTabState = true;
        imgFood.setImageResource(R.drawable.food_red);
        tvFood.setTextColor(getResources().getColor(R.color.colorPunc));
        imgMessage.setImageResource(R.drawable.message);
        tvMessage.setTextColor(getResources().getColor(R.color.colorLightBlack));
        ((ScrollView) findViewById(R.id.food_fragement)).setVisibility(View.VISIBLE);
        ((ScrollView) findViewById(R.id.message_fragement)).setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.menu_icon)
    void onNavigation(View view){
        drawer.openDrawer(GravityCompat.START, true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        displaySelectedScreen(menuItem.getItemId());
        return false;
    }

    private void displaySelectedScreen(int itemId) {
        Intent intent;
        switch (itemId) {
            case R.id.dashboard:
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);
                break;
            case R.id.myaccount:
                intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(intent);
                break;
            case R.id.location:
                intent = new Intent(this, MapsActivity.class);
                intent.putExtra("requestMap", REQUEST_MAP);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            /*case R.id.noty:
                intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
                break;*/
            /*case R.id.oder_history:
                intent = new Intent(getApplicationContext(), OrdersHistoryActivity.class);
                startActivity(intent);
                break;*/
          /*  case R.id.settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;*/
            case R.id.ambassador:
                intent = new Intent(getApplicationContext(), AmbassadorActivity.class);
                startActivity(intent);
                break;
            case R.id.help:
                intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
                break;
            case R.id.feedback:
                intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                logout();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /////////////  Get data from MapsActivity /////////////////
        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                ArrayList<String> temps = data.getStringArrayListExtra(MapsActivity.INTENT_LOCATION_ADDRESSES);
                Commons.thisUser.setLatLng(new LatLng(Double.parseDouble(temps.get(0)), Double.parseDouble(temps.get(1))));
                Commons.thisUser.setAddress(temps.get(2));
                Commons.thisUser.setCountry(temps.get(3));
                Commons.thisUser.setCity(temps.get(4));

                //showToast("Your current location\nCoordinate : (" + temps.get(0) + "," + temps.get(1) + ")\n" + "Address : " + temps.get(2));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.newAddButton)
    void onAddNew()
    {
        Intent intent = new Intent(getApplicationContext(), AddFoodActivity.class);
        startActivity(intent);
    }

    public void onMyAccount(View view){
        Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
        startActivity(intent);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void logout(){
        EasyPreference.with(getApplicationContext()).addString("email", "").save();
        EasyPreference.with(HomeActivity.this).addString("EasyBubble", "").save();
        Commons.thisUser = null;
        Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void getApi(){

        /*webView.loadUrl("http://www.pixinx.com/");*/
        //EasyPreference.with(HomeActivity.this).addString("easyHelpLink", json.get("help_link").getAsString()).save();
        EasyPreference.with(HomeActivity.this).addString("easyHelpLink", "http://www.pixinx.com/").save();
    }

    public void goToFilter(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_in);
        ((LinearLayout)findViewById(R.id.radiusLayout)).setAnimation(animation);
        ((LinearLayout)findViewById(R.id.radiusLayout)).setVisibility(View.VISIBLE);
        ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.VISIBLE);
    }

    public void dismissRadiusLayout(View view){
        closeRadiusLayout();
    }

    private void closeRadiusLayout(){
        if(((LinearLayout)findViewById(R.id.radiusLayout)).getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_out);
            ((LinearLayout)findViewById(R.id.radiusLayout)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.radiusLayout)).setVisibility(View.GONE);
            ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.GONE);
        }
    }

    public void getAllFood(View view){
        onFoodLoad(foodIndia);
        //onFoodLoad(foods);
        closeRadiusLayout();
    }

    public void done(View view){
        filterByRadius(radius);
        closeRadiusLayout();
    }

    private void filterByRadius(double radius){
        if(radius == 0){
            return;
        }

        Location myLocation = new Location("MyLocation");
        myLocation.setLatitude(Commons.thisUser.getLatLng().latitude);
        myLocation.setLongitude(Commons.thisUser.getLatLng().longitude);
        //ArrayList<FoodModel> foodModels = new ArrayList<>();
        Log.d("RADIUS+++", String.valueOf(radius));
        ArrayList<FoodModel> filteredFood = new ArrayList<>();
        for(int i=0; i<foods.size(); i++){
            Location foodLocation = new Location("FoodLocation");
            foodLocation.setLatitude(foods.get(i).getLatLng().latitude);
            foodLocation.setLongitude(foods.get(i).getLatLng().longitude);
            double distance = myLocation.distanceTo(foodLocation);
            Log.d("my++++", String.valueOf(myLocation));
            Log.d("food++++", String.valueOf(foods.get(i).getLatLng()));
            Log.d("distance++++", String.valueOf(distance));
            if(distance/1000 < radius){
                filteredFood.add(foods.get(i));
            }
            if(i == foods.size() - 1){
                onFoodLoad(filteredFood);
                break;
            }
        }
    }

    ArrayList<Long> timeList = new ArrayList<>();
    ArrayList<Integer> senderIDList = new ArrayList<>();
    int cnt = 0;

    private void getCustomerNotification(){

        ArrayList<String> keylist = new ArrayList<>();
        keylist = tinyDB.getListString("keys");

        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + String.valueOf(Commons.thisUser.getId()));
        ArrayList<String> finalKeylist = keylist;

        for(String key:finalKeylist){
            Log.d("KEY!!!", key);
        }


        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Firebase childRef = ref.child(dataSnapshot.getKey());
                Log.d("ffffffffffffffff", childRef.toString());
                childRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Map map = dataSnapshot.getValue(Map.class);
                        Log.d("mmmmmmmmmmmmmmmmm", map.toString());
                        try{
                            LayoutInflater inflater = getLayoutInflater();
                            View myMessageCellLayout = inflater.inflate(R.layout.layout_order_message, null);
                            String noti = map.get("message").toString();
                            String time = map.get("time").toString();
                            String sender_id = map.get("sender_id").toString();
                            String sender_name = map.get("sender_name").toString();
                            String sender_photo = map.get("sender_photo").toString();
                            String sender_product_id = map.get("product_id").toString();  Log.d("PRODUCT_ID!!!", sender_product_id);
                            String role = map.get("role").toString();

                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);

                            OrderModel messageModel = new OrderModel();

                            //// inflate

                            if(!timeList.contains(Long.parseLong(time)) && !senderIDList.contains(Integer.parseInt(sender_id))){

                                Log.d("CUSTOMER NOTI!!!", noti);

                                Glide.with(getApplicationContext()).load(sender_photo).placeholder(R.drawable.user).into((ImageView) myMessageCellLayout.findViewById(R.id.order_userimage));
                                ((TextView) myMessageCellLayout.findViewById(R.id.order_username)).setText(sender_name);
                                if(role.equals("customer"))((TextView) myMessageCellLayout.findViewById(R.id.role)).setText("Customer");
                                else if(role.equals("owner"))((TextView) myMessageCellLayout.findViewById(R.id.role)).setText("Food Owner");
                                else ((TextView) myMessageCellLayout.findViewById(R.id.role)).setText("Message");
                                ((TextView) myMessageCellLayout.findViewById(R.id.order_message)).setText(noti);
                                ((TextView) myMessageCellLayout.findViewById(R.id.order_datetime)).setText(getCurrentTimeString(Long.parseLong(time)));

                                timeList.add(Long.parseLong(time));
                                senderIDList.add(Integer.parseInt(sender_id));
                                lin_message_container.addView(myMessageCellLayout);

                                Log.d("KEY2!!!", dataSnapshot.getKey());

                                if (!finalKeylist.contains(dataSnapshot.getKey())){
                                    cnt++;
                                }

                                messageModel.setSender_id(Integer.parseInt(sender_id));
                                messageModel.setSender_name(sender_name);
                                messageModel.setSender_photo(sender_photo);
                                messageModel.setMessage(noti);

                                messages.add(messageModel);
                            }

                            ////////// close setting
                            ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.order_close);
                            imgClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    dataSnapshot.getRef().removeValue();
                                    if(lin_message_container.getChildCount() > 0) {
                                        lin_message_container.removeView(myMessageCellLayout);
                                        timeList.remove(Long.parseLong(time));
                                        senderIDList.remove(senderIDList.indexOf(Integer.parseInt(sender_id)));
                                        int count = lin_message_container.getChildCount();
                                        messages.remove(messageModel);
                                        tvNoty.setText(String.valueOf(count));
                                        Log.d("NOTICOUNT!!!", String.valueOf(count));
                                        if(count == 0){
                                            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                                            ShortcutBadger.removeCount(getApplicationContext());
                                        }
                                        else {
                                            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                                            ShortcutBadger.applyCount(getApplicationContext(), count);
                                        }
                                    }
                                }
                            });

                            ////////// Reply setting
                            Button btnReply = (Button) myMessageCellLayout.findViewById(R.id.btn_reply);
                            btnReply.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //dataSnapshot.getRef().removeValue();
                                    if(lin_message_container.getChildCount() > 0) {
                                        lin_message_container.removeView(myMessageCellLayout);
                                        timeList.remove(Long.parseLong(time));
                                        senderIDList.remove(senderIDList.indexOf(Integer.parseInt(sender_id)));
                                        int count = lin_message_container.getChildCount();
                                        messages.remove(messageModel);
                                        tvNoty.setText(String.valueOf(count));
                                        Log.d("NOTICOUNT!!!", String.valueOf(count));
                                        if(count == 0){
                                            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                                            ShortcutBadger.removeCount(getApplicationContext());
                                        }
                                        else {
                                            ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                                            ShortcutBadger.applyCount(getApplicationContext(), count);
                                        }
                                    }

                                    UserModel userModel = new UserModel();
                                    userModel.setId(Integer.parseInt(sender_id));
                                    userModel.setUsername(sender_name);
                                    userModel.setPicture(sender_photo);
                                    Commons.user = userModel;
                                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                                    startActivity(intent);
                                }
                            });

                            ////////// Detail setting
                            Button btnDetail = (Button) myMessageCellLayout.findViewById(R.id.btn_detail);
                            btnDetail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    UserModel userModel = new UserModel();
                                    userModel.setId(Integer.parseInt(sender_id));
                                    userModel.setUsername(sender_name);
                                    userModel.setPicture(sender_photo);
                                    Commons.user = userModel;
                                    Commons.product_id = Integer.parseInt(sender_product_id);
                                    Commons.bNotyMessage = true;

                                    finalKeylist.add(dataSnapshot.getKey());
                                    cnt--;
                                    if(cnt > 0)((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                                    else ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                                    tvNoty.setText(String.valueOf(cnt));
                                    ShortcutBadger.applyCount(getApplicationContext(), cnt);
                                    tinyDB.putListString("keys", finalKeylist);

                                    Log.d("ssssssssss", String.valueOf(senderIDList.indexOf(Integer.parseInt(sender_id))) );
                                    //if ( senderIDList.indexOf(Integer.parseInt(sender_id)) )
                                    timeList.remove(Long.parseLong(time));
                                    senderIDList.remove(senderIDList.indexOf(Integer.parseInt(sender_id)));

                                    if (role.equals("customer"))Commons.role = "owner";
                                    else if (role.equals("owner"))Commons.role = "";

                                    Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                                    startActivity(intent);
                                }
                            });

                            if(cnt > 0)((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                            else ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                            tvNoty.setText(String.valueOf(cnt));
                            ShortcutBadger.applyCount(getApplicationContext(), cnt);
                            tinyDB.putListString("keys", finalKeylist);

                        }catch (NullPointerException e){}
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    /*private void getAdminNotification(){

        Firebase ref;
        ref = new Firebase(ReqConst.FIREBASE_URL + "admin/" + String.valueOf(Commons.thisUser.getId()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myMessageCellLayout = inflater.inflate(R.layout.layout_admin_message, null);
                    String noti = map.get("message").toString();
                    String time = map.get("time").toString();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING,200);

                    OrderModel messageModel = new OrderModel();

                    if(!timeList.contains(Long.parseLong(time))){

                        Log.d("ADMIN NOTI!!!", noti);

//                        Glide.with(getApplicationContext()).load(R.mipmap.logo).placeholder(R.drawable.user).into((ImageView) myMessageCellLayout.findViewById(R.id.order_userimage));
                        ((TextView) myMessageCellLayout.findViewById(R.id.admin_title)).setText("ShareMyFood Admin");
                        ((TextView) myMessageCellLayout.findViewById(R.id.admin_description)).setText(noti);
                        ((TextView) myMessageCellLayout.findViewById(R.id.admin_datetime)).setText(getCurrentTimeString(Long.parseLong(time)));

                        timeList.add(Long.parseLong(time));
                        lin_message_container.addView(myMessageCellLayout);
                        cnt++;

                        messageModel.setMessage(noti);

                        messages.add(messageModel);
                    }

                    ////////// close setting
                    ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.admin_close);
                    imgClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dataSnapshot.getRef().removeValue();
                            if(lin_message_container.getChildCount() > 0) {
                                lin_message_container.removeView(myMessageCellLayout);
                                timeList.remove(Long.parseLong(time));
                                int count = lin_message_container.getChildCount();
                                messages.remove(messageModel);
                                cnt--;
                                tvNoty.setText(String.valueOf(cnt));
                                if(cnt == 0){
                                    ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                                    ShortcutBadger.removeCount(getApplicationContext());
                                }
                                else {
                                    ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                                    ShortcutBadger.applyCount(getApplicationContext(), cnt);
                                }
                            }
                        }
                    });

                    myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dataSnapshot.getRef().removeValue();
                            if(lin_message_container.getChildCount() > 0) {
                                lin_message_container.removeView(myMessageCellLayout);
                                timeList.remove(Long.parseLong(time));
                                int count = lin_message_container.getChildCount();
                                messages.remove(messageModel);
                                cnt--;
                                tvNoty.setText(String.valueOf(cnt));
                                if(cnt == 0){
                                    ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.GONE);
                                    ShortcutBadger.removeCount(getApplicationContext());
                                }
                                else {
                                    ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                                    ShortcutBadger.applyCount(getApplicationContext(), cnt);
                                }
                            }

                            Message message = new Message();
                            message.setTitle("ShareMyFood Admin");
                            message.setBody(noti);
                            message.setDate_time(getCurrentTimeString(Long.parseLong(time)));

                            Commons.message = message;
                            Intent intent = new Intent(getApplicationContext(), AdminMessageActivity.class);
                            startActivity(intent);
                        }
                    });

                    ((FrameLayout)findViewById(R.id.notimark)).setVisibility(View.VISIBLE);
                    tvNoty.setText(String.valueOf(cnt));
                    ShortcutBadger.applyCount(getApplicationContext(), cnt);

                }catch (NullPointerException e){}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }*/


    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null)
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location == null){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                myLatLang = latLng;
                                address = getAddress(myLatLang.latitude, myLatLang.longitude);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "GPS Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "GPS Provider disabled");
                    }
                });
            }
            if (location != null) {
                try {
                    myLatLang = new LatLng(location.getLatitude(), location.getLongitude());
                    address = getAddress(myLatLang.latitude, myLatLang.longitude);
                    Log.d("333333333333",myLatLang.latitude + ":" + myLatLang.longitude) ;
                    //showToast(latLng.latitude + ":" + latLng.longitude) ;
                    ///////////////////////////////////////////////////////////////////////////////////////////////
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getAddress(double ltt, double lgt)
    {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(ltt, lgt, 1);
            if (addresses.isEmpty()) {

            }
            else {
                if (addresses.size() > 0) {

                    String temp = addresses.get(0).getAddressLine(0);

                    country = addresses.get(0).getCountryName();
                    if (addresses.get(0).getLocality() == null)
                    {
                        city = addresses.get(0).getAdminArea();
                    } else {
                        city = addresses.get(0).getLocality();
                    }

                    return temp;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "Undefined";
    }


}
































