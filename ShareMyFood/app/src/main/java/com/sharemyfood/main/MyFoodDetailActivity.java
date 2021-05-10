package com.sharemyfood.main;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.UserModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MyFoodDetailActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    public final int REQUEST_CODE = 100;

    AVLoadingIndicatorView progressBar;
    Toolbar toolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden = false;
    private String unit = "k";

    @BindView(R.id.linear_container) LinearLayout container;
    @BindView(R.id.dNumberPicker) NumberPicker numPicker;
    @BindView(R.id.edit_Ddescription) EditText editDes;
    @BindView(R.id.edit_Dweight) EditText editWeight;
    @BindView(R.id.edit_Dnumber) EditText editQuantity;
    @BindView(R.id.edit_Dpickuptime) EditText editPickUpTime;
    @BindView(R.id.tv_Dexpiry) TextView tvExpiry;
    @BindView(R.id.tv_Dlocation) TextView tvLocation;
    @BindView(R.id.img_food) ImageView imgFood;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_food_detail);

        ButterKnife.bind(this);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        ///////////////////// click View for hide keyboard //////
        container.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // TODO Auto-generated method stub
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }

        });

        /// Expiry date setting ///
        final String[] values= {"1","2", "3", "4", "5", "6", "7", "8", "9", "10"};
        numPicker.setMinValue(1); //from array first value
        numPicker.setMaxValue(values.length); //to array last value
        numPicker.setValue(Commons.food.getLifespan());
        numPicker.setDisplayedValues(values);
        numPicker.setWrapSelectorWheel(false);
        numPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                tvExpiry.setText(String.valueOf(numberPicker.getValue()));
                //numberPicker.setVisibility(View.INVISIBLE);
            }
        });

        if (Commons.food.getUnit().equals("k")){
            RadioButton rb = findViewById(R.id.dRadioKilogram);
            rb.setChecked(true);
        } else {
            RadioButton rb = findViewById(R.id.dRadioGram);
            rb.setChecked(true);
        }
        RadioGroup rg = (RadioGroup) findViewById(R.id.dRadioGroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.dRadioKilogram:
                        unit = "k";
                        break;
                    case R.id.dRadioGram:
                        unit = "g";
                        break;
                }
            }
        });

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

        ((FrameLayout)findViewById(R.id.btn_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodDelete();
            }
        });

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();
        //// realtime Blur view END

        init();
    }

    void init(){

        Glide.with(this).load(Commons.food.getPictureUrl()).placeholder(R.drawable.vegteble).into(imgFood);
        editDes.setText(Commons.food.getDescription());
        editWeight.setText(String.valueOf(Commons.food.getWeight()));
        editQuantity.setText(String.valueOf(Commons.food.getQuantity()));
        editPickUpTime.setText(Commons.food.getPickuptime());
        tvExpiry.setText(String.valueOf(Commons.food.getLifespan()));
        tvLocation.setText(Commons.food.getAdress());
    }

    void foodDelete(){

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
                AndroidNetworking.upload(ReqConst.SERVER + "deleteProduct")
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

                                        showToast("Deleted!");
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
                                showToast(error.toString());
                            }
                        });
                finish();
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();
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

    @OnClick(R.id.tv_Dexpiry)
    void onExpiry(){
        numPicker.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.img_Dlocation)
    void onLocation(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /////////////  Get data from MapsActivity /////////////////
        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                ArrayList<String> temps = data.getStringArrayListExtra(MapsActivity.INTENT_LOCATION_ADDRESSES);
                Commons.food.setLatLng(new LatLng(Double.parseDouble(temps.get(0)), Double.parseDouble(temps.get(1))));
                Commons.food.setAdress(temps.get(2));
                tvLocation.setText(Commons.food.getAdress());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @OnClick(R.id.tv_save)
    void onSave(){

        String des = editDes.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();
        String pickup = editPickUpTime.getText().toString().trim();
        String expiry = tvExpiry.getText().toString().trim();
        String location = tvLocation.getText().toString().trim();

        if(des.length() == 0){
            editDes.setError("Enter food description.");
            return;
        }
        if(weight.length() == 0){
            editWeight.setError("Enter food weight.");
            return;
        }
        /*Double input = Double.valueOf(weight);
        if (input % 0.5 != 0){ //Check if you number divides by 0.5 with no fraction
            editWeight.setError("Enter food weigth.");
            return;
        }*/
        boolean isDigits = TextUtils.isDigitsOnly(quantity);
        if(quantity.length() == 0){
            editQuantity.setError("Enter food quantity.");
            return;
        }
        if(pickup.length() == 0){
            editPickUpTime.setError("Enter food Pickup Time.");
            return;
        }
        if(location.length() == 0){
            showToast("Select a food location");
            return;
        }
        //goAPI();
        AndroidNetworking.upload(ReqConst.SERVER + "updateProduct")
                .addMultipartParameter("product_id", String.valueOf(Commons.food.getId()))
                .addMultipartParameter("name", Commons.food.getTitle())
                .addMultipartParameter("description", des)
                //.addMultipartParameter("phone_number", Commons.food.getPhone())
                .addMultipartParameter("address", location)
                .addMultipartParameter("weight", weight)
                .addMultipartParameter("quantity", quantity)
                .addMultipartParameter("unit", unit)
                .addMultipartParameter("pickup_time", pickup)
                .addMultipartParameter("latitude", String.valueOf(Commons.food.getLatLng().latitude))
                .addMultipartParameter("longitude", String.valueOf(Commons.food.getLatLng().longitude))
                .addMultipartParameter("lifespan", expiry)
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

                                showToast("Updated!");
                            }else if (result.equals("1")) {
                                showToast("Email not exist!");
                            } else {
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
                        showToast(error.toString());
                    }
                });
    }

}