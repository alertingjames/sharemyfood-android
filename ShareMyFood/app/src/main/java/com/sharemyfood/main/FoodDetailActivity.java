package com.sharemyfood.main;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leolin.shortcutbadger.ShortcutBadger;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.facebook.common.Common;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.FoodModel;
import com.sharemyfood.models.UserModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FoodDetailActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    public final int REQUEST_CODE = 100;
    public final String REQUEST_MAP = "SET_ORDER_LOCATION";
    private int kg = 1;

    AVLoadingIndicatorView progressBar;
    Toolbar toolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden = false;
    LatLng orderLatLng = Commons.thisUser.getLatLng();
    String orderAddress = Commons.thisUser.getAddress();

    @BindView(R.id.annonce_main_coordinator) CoordinatorLayout container;
    @BindView(R.id.tv_Ddescription) TextView tvDes;
    @BindView(R.id.tv_Dweight) TextView tvWeight;
    @BindView(R.id.tv_Dnumber) TextView tvQuantity;
    @BindView(R.id.tv_Dpickuptime) TextView tvPickUpTime;
    @BindView(R.id.tv_Dexpiry) TextView tvExpiry;
    @BindView(R.id.tv_Dlocation) TextView tvLocation;
    @BindView(R.id.img_food) ImageView imgFood;
    @BindView(R.id.img_user) ImageView imgUser;
    @BindView(R.id.tv_user) TextView tvUser;
    @BindView(R.id.btn_grab) TextView btnGrab;
    @BindView(R.id.btn_reply) TextView btnReply;
    @BindView(R.id.btn_confirm) TextView btnConfirm;
    //@BindView(R.id.tv_phone) TextView tvPhone;

    @BindView(R.id.tv_orderDlocation) TextView tvOLocation;
    //@BindView(R.id.edit_phonenumber) EditText editPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        ButterKnife.bind(this);

        if (Commons.food.getUnit().equals("k")){
            RadioButton rb = findViewById(R.id.dRadioKilogram);
            rb.setChecked(true);
        } else {
            RadioButton rb = findViewById(R.id.dRadioGram);
            rb.setChecked(true);
        }

        //// realtime Blur view
        toolbar = (Toolbar) findViewById(R.id.flexible_example_toolbar);
        toolbar.setTitle(Commons.food.getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.flexible_example_appbar);

        setTitle(Commons.food.getTitle());

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();
        //// realtime Blur view END

        if(Commons.role.equals("customer"))btnConfirm.setVisibility(View.GONE);
        else if(Commons.role.equals("owner")) btnConfirm.setVisibility(View.VISIBLE);
        else btnConfirm.setVisibility(View.GONE);

        /// Dashboard
        if (Commons.bDashHis == true){
            btnGrab.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
            btnReply.setVisibility(View.GONE);
            Commons.bDashHis = false;
            init();
        }

        ///// Home  Food detail
        if (Commons.bNotyOrder == true){
            btnConfirm.setVisibility(View.GONE);
            btnReply.setVisibility(View.GONE);
            Commons.bNotyOrder = false;
            init();
        }

        //// message box
        if (Commons.bNotyMessage == true){
            btnGrab.setVisibility(View.GONE);
            Commons.bNotyMessage = false;
            foodLoad();

        }

    }
    void foodLoad(){

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

                                        if(foodModel.getId() == Commons.product_id) {
                                            Commons.product_id = 0;
                                            Commons.food = foodModel;
                                            init();
                                        }
                                    }
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

    void init(){

        Glide.with(this).load(Commons.food.getPictureUrl()).placeholder(R.drawable.vegteble).into(imgFood);
        imgFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                intent.putExtra("image", Commons.food.getPictureUrl());
                ActivityOptions transitionActivityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(FoodDetailActivity.this, imgFood, getString(R.string.transition));
                }
                Commons.bImageViewState = true;
                startActivity(intent, transitionActivityOptions.toBundle());
            }
        });
        tvDes.setText(Commons.food.getDescription());
        tvWeight.setText(String.valueOf(Commons.food.getWeight()));
        tvQuantity.setText(String.valueOf(Commons.food.getQuantity()));
        tvPickUpTime.setText(Commons.food.getPickuptime());
        tvExpiry.setText(String.valueOf(Commons.food.getLifespan()));
        tvLocation.setText(Commons.food.getAdress());
        Glide.with(this).load(Commons.food.getUserpicture()).placeholder(R.drawable.user).into(imgUser);
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                intent.putExtra("image", Commons.food.getUserpicture());
                ActivityOptions transitionActivityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(FoodDetailActivity.this, imgUser, getString(R.string.transition));
                }
                Commons.bImageViewState = true;
                startActivity(intent, transitionActivityOptions.toBundle());
            }
        });
        tvUser.setText(Commons.food.getUsername());
        //if (!Commons.food.getPhone().equals(""))
            //tvPhone.setText(Commons.food.getPhone());

        tvOLocation.setText(Commons.thisUser.getAddress());
        /*if (!Commons.thisUser.getPhone().equals(""))
            editPhone.setText(Commons.thisUser.getPhone());*/
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100
                / mMaxScrollSize;

        Log.d("Percentage+++", String.valueOf(currentScrollPercentage));

        if (currentScrollPercentage >= 10) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view)).setVisibility(View.VISIBLE);
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .start();
            }
        }else if (currentScrollPercentage <= 20) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .start();
                ((RealtimeBlurView)findViewById(R.id.real_time_blur_view)).setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.img_Dlocation)
    void onLocation(){
        String location = tvLocation.getText().toString();

        Intent intent = new Intent(this, MapsMiniActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @OnClick(R.id.img_orderLocation)
    void onOLocation(){

        Intent intent = new Intent(this, MapsActivity.class);
        //intent.putExtra("requestMap", REQUEST_MAP);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /////////////  Get data from MapsActivity /////////////////
        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                ArrayList<String> temps = data.getStringArrayListExtra(MapsActivity.INTENT_LOCATION_ADDRESSES);
                orderLatLng = new LatLng(Double.parseDouble(temps.get(0)), Double.parseDouble(temps.get(1)));
                orderAddress = temps.get(2);
                tvOLocation.setText(orderAddress);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*@OnClick(R.id.img_call)
    void onCall() {

        String phoneNumber = tvPhone.getText().toString();

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }*/

    @OnClick(R.id.btn_confirm)
    void onConfirm(){

        AndroidNetworking.upload(ReqConst.SERVER + "transact")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .addMultipartParameter("product_id", String.valueOf(Commons.food.getId()))
                .setPriority(Priority.MEDIUM)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        progressBar.setVisibility(View.VISIBLE);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterResponse=====>", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){

                                showToast("Shared!");
                            }/*else if (result.equals("1")){
                                showToast("You had shared already");
                            } */else {
                                showToast("Network issue");
                            }
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showToast("Network issue");
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        Log.d("RegisterError=====>", error.toString());
                        showToast(error.toString());
                    }
                });
    }

    @OnClick(R.id.btn_grab)
    void onGrab(){

        UserModel userModel = new UserModel();
        userModel.setId(Commons.food.getUserId());
        userModel.setUsername(Commons.food.getUsername());
        userModel.setPicture(Commons.food.getUserpicture());
        Commons.user = userModel;
        Intent intent = new Intent(this, ChattingActivity.class);
        intent.putExtra("product_id", String.valueOf(Commons.food.getId()));
        startActivity(intent);
    }

    @OnClick(R.id.btn_reply)
    void onReply(){
        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
        intent.putExtra("product_id", String.valueOf(Commons.food.getId()));
        startActivity(intent);
    }
}