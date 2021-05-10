package com.sharemyfood.main;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.FoodModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderMessageActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    public final int REQUEST_CODE = 100;
    private int kg = 1;

    AVLoadingIndicatorView progressBar;
    Toolbar toolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden = false;

    FoodModel foodModel = new FoodModel();

    @BindView(R.id.annonce_main_coordinator) CoordinatorLayout container;
    @BindView(R.id.order_Dfoodimage) ImageView orderFoodImage;
    @BindView(R.id.order_Ddescription) TextView orderDes;
    @BindView(R.id.order_Dweight) TextView orderWeight;
    @BindView(R.id.order_Dquantity) TextView orderQuantity;
    //@BindView(R.id.order_Dcontact) TextView orderContact;
    @BindView(R.id.order_Dlocation) TextView orderLocation;
    @BindView(R.id.order_Duserimage) ImageView orderUserImage;
    @BindView(R.id.order_Dusername) TextView orderUserName;
    @BindView(R.id.order_Dconfirm) TextView orderConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_message);

        ButterKnife.bind(this);

        progressBar = (AVLoadingIndicatorView) findViewById(R.id.loading_bar);

        if (Commons.bNotyOrder) {
            //orderConfirm.setText("Request");
            Commons.bNotyOrder = false;
        } else if (Commons.bOrderHis){
            //orderConfirm.setVisibility(View.GONE);
        }
        setDataFood();
    }

    private void setDataFood() {

        progressBar.setVisibility(View.VISIBLE);

        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "getFood")
                .addBodyParameter("order_id", String.valueOf(Commons.thisUser.getId()))
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

                                    for (int i=0 ; i < foodArr.length(); i++){

                                        JSONObject json =  foodArr.getJSONObject(i);

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
                                        foodModel.setLatLng(new LatLng(json.getDouble("latitude"), json.getDouble("longitude")));
                                        foodModel.setRegistered_tiime(json.getLong("registered_time"));
                                        if (json.getString("is_like").equals("yes")) {
                                            foodModel.setbLike(true);
                                        }else{
                                            foodModel.setbLike(false);
                                        }
                                        foodModel.setState(json.getString("status"));
                                    }
                                    init(foodModel);
                                }
                            }else {
                                showToast("Network issue");
                            }
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
                        showToast("Network issue");
                    }
                });
    }

    void init(FoodModel food) {

        //// realtime Blur view
        toolbar = (Toolbar) findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(food.getTitle());
        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.flexible_example_appbar);
        setTitle(food.getTitle());
        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();
        //// realtime Blur view END

        if (food.getUnit().equals("k")){
            RadioButton rb = findViewById(R.id.dRadioKilogram);
            rb.setChecked(true);
        } else {
            RadioButton rb = findViewById(R.id.dRadioGram);
            rb.setChecked(true);
        }

        Glide.with(this).load(food.getPictureUrl()).placeholder(R.drawable.vegteble).into(orderFoodImage);
        orderDes.setText(food.getDescription());
        orderWeight.setText(String.valueOf(food.getWeight()));
        orderQuantity.setText(String.valueOf(food.getQuantity()));
        //orderContact.setText(String.valueOf(food.getPhone()));
        orderLocation.setText(food.getAdress());
        Glide.with(this).load(food.getUserpicture()).placeholder(R.drawable.user).into(orderUserImage);
        orderUserName.setText(food.getUsername());
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
                ((RealtimeBlurView) findViewById(R.id.real_time_blur_view)).setVisibility(View.VISIBLE);
                ((RealtimeBlurView) findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .start();
            }
        } else if (currentScrollPercentage <= 20) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ((RealtimeBlurView) findViewById(R.id.real_time_blur_view))
                        .animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .start();
                ((RealtimeBlurView) findViewById(R.id.real_time_blur_view)).setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.img_Dlocation)
    void onLocation(){

        Commons.latitude = foodModel.getLatLng().latitude;
        Commons.longitude = foodModel.getLatLng().longitude;
        Intent intent = new Intent(this, MapsMiniActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /*@OnClick(R.id.img_call)
    void onCall() {

        String phoneNumber = orderContact.getText().toString();

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }*/

    @OnClick(R.id.order_Dconfirm)
    void onConfirm(){
        showToast("New Noty state Confirm"); ///
        finish();
    }

}