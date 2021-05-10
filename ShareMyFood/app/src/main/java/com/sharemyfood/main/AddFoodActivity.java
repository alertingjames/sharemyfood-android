package com.sharemyfood.main;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.FoodModel;
import com.sharemyfood.models.UserModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AddFoodActivity extends BaseActivity {

    public final int REQUEST_CODE = 100;
    public final String REQUEST_MAP = "ADD_FOOD";

    private String unit = "k";
    File file = null;
    AVLoadingIndicatorView progressBar;

    @BindView(R.id.linear_container) LinearLayout linear_container;
    @BindView(R.id.numberPicker) NumberPicker numPicker;
    @BindView(R.id.edit_title) EditText editTitle;
    @BindView(R.id.edit_description) EditText editDes;
    @BindView(R.id.edit_weight) EditText editWeight;
    @BindView(R.id.edit_number) EditText editQuantity;
    @BindView(R.id.edit_pickuptime) EditText editPickUpTime;
    //@BindView(R.id.edit_phone) EditText editPhone;
    @BindView(R.id.tv_expiry) TextView tvExpiry;
    @BindView(R.id.tv_location) TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        ButterKnife.bind(this);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
        ///////////////////// click View for hide keyboard //////
       linear_container.setOnTouchListener(new View.OnTouchListener() {

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
        tvExpiry = (TextView) findViewById(R.id.tv_expiry);
        numPicker.setMinValue(1); //from array first value
        numPicker.setMaxValue(values.length); //to array last value
        numPicker.setValue(5);
        numPicker.setDisplayedValues(values);
        numPicker.setWrapSelectorWheel(false);
        numPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                tvExpiry.setText(String.valueOf(numberPicker.getValue()));
                //numberPicker.setVisibility(View.INVISIBLE);
            }
        });

        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioKilogram:
                        unit = "kg";
                        break;
                    case R.id.radioGram:
                        unit = "gms";
                        break;
                }
            }
        });

        //editPhone.setText(Commons.thisUser.getPhone());
        tvLocation.setText(Commons.thisUser.getAddress());
    }

    @OnClick(R.id.tv_camera)
    void onCamera(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                ((ImageView) findViewById(R.id.img_food_image)).setImageURI(resultUri);
                file = new File(resultUri.getPath());
                ((LinearLayout) findViewById(R.id.lin_addImage)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.lin_addImage_control)).setVisibility(View.INVISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        /////////////  Get data from MapsActivity /////////////////
        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                ArrayList<String> temps = data.getStringArrayListExtra(MapsActivity.INTENT_LOCATION_ADDRESSES);
                Commons.food.setLatLng(new LatLng(Double.parseDouble(temps.get(0)), Double.parseDouble(temps.get(1))));
                Commons.food.setAdress(temps.get(2));
                Commons.food.setCountry(temps.get(3));
                Commons.food.setCity(temps.get(4));
                Log.d("--------------", temps.get(0));
                Log.d("--------------", temps.get(1));
                Log.d("--------------", temps.get(2));
                Log.d("--------------", temps.get(3));
                Log.d("--------------", temps.get(4));
                tvLocation.setText(Commons.food.getAdress());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.img_close_picture)
    void onCloseImage(){
        ((LinearLayout) findViewById(R.id.lin_addImage)).setVisibility(View.INVISIBLE);
        ((LinearLayout) findViewById(R.id.lin_addImage_control)).setVisibility(View.VISIBLE);
        file = null;
    }
    /*@OnClick(R.id.tv_pickuptime)
    void onPickUpTime()
    {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        editPickUpTime.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
                // Get Current Time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                editPickUpTime.setText(editPickUpTime.getText() + " " + hourOfDay + ":" + minute);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
        datePickerDialog.show();

    }*/

    @OnClick(R.id.tv_expiry)
    void onExpiry(){
        numPicker.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.img_location)
    void onLocation(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("requestMap", REQUEST_MAP);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @OnClick(R.id.tv_addFood)
    void addFood(){

        FoodModel foodModel = new FoodModel();
        String title = editTitle.getText().toString().trim();
        String des = editDes.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();
        String pickup = editPickUpTime.getText().toString().trim();
        //String phone = editPhone.getText().toString().trim();
        String expiry = tvExpiry.getText().toString().trim();
        String location = tvLocation.getText().toString().trim();

        //////  creation a custom file when file is non exist
        if(file == null){
            showToast(getString(R.string.takePhoto));
            return;
        }
        if(title.length() == 0){
            editTitle.setError("Enter food name.");
            return;
        }
        if(des.length() == 0){
            editDes.setError("Enter food description.");
            return;
        }
        if(weight.length() == 0){
            editWeight.setError("Enter food weight.");
            return;
        }
//        Double input = Double.valueOf(weight);
//        if (input % 0.5 != 0){ //Check if you number divides by 0.5 with no fraction
//            editWeight.setError("Enter food weigtht.");
//            return;
//        }
        boolean isDigits = TextUtils.isDigitsOnly(quantity);
        if(quantity.length() == 0){
            editQuantity.setError("Enter food quantity.");
            return;
        }
        /*if(phone.length() == 0){
            editPhone.setError("Enter your phone number.");
            return;
        }
        if(!validatePhone(phone)){
            editPhone.setError("Enter your valid phone number.");
            return;
        }*/
        if(pickup.length() == 0){
            editPickUpTime.setError("Enter food Pickup Time.");
            return;
        }
        if(location.length() == 0){
            showToast("Select a food location");
            return;
        }


        foodModel.setTitle(title);
        foodModel.setDescription(des);
        foodModel.setWeight(Double.parseDouble(weight));
        foodModel.setQuantity(Integer.parseInt(quantity));
        foodModel.setPickuptime(pickup);
        foodModel.setLifespan(Integer.parseInt(expiry));
        foodModel.setAdress(location);
        foodModel.setUserId(Commons.thisUser.getId());
        foodModel.setUsername(Commons.thisUser.getUsername());
        foodModel.setUserpicture(Commons.thisUser.getPicture());
        //foodModel.setPhone(phone);
        if (Commons.food.getLatLng() == null){
            foodModel.setLatLng(Commons.thisUser.getLatLng());
        } else {
            foodModel.setLatLng(Commons.food.getLatLng());
        }
        if (Commons.food.getCountry() == null || Commons.food.getCountry().equals("")){
            foodModel.setCountry(Commons.thisUser.getCountry());
        } else {
            foodModel.setCountry(Commons.food.getCountry());
        }
        if (Commons.food.getCity() == null || Commons.food.getCity().equals("")){
            foodModel.setCity(Commons.thisUser.getCity());
        } else {
            foodModel.setCity(Commons.food.getCity());
        }
        foodModel.setUnit(unit);

        uploadFood(foodModel);
    }

    private void uploadFood(FoodModel food){
        /// API call
        AndroidNetworking.upload(ReqConst.SERVER + "uploadProduct")
                .addMultipartFile("file",file)
                .addMultipartParameter("member_id", String.valueOf(food.getUserId()))
                .addMultipartParameter("name", food.getTitle())
                .addMultipartParameter("description", food.getDescription())
                //.addMultipartParameter("phone_number", food.getPhone())
                .addMultipartParameter("address", food.getAdress())
                .addMultipartParameter("weight", String.valueOf(food.getWeight()))
                .addMultipartParameter("quantity", String.valueOf(food.getQuantity()))
                .addMultipartParameter("unit", unit)
                .addMultipartParameter("country", food.getCountry())
                .addMultipartParameter("city", food.getCity())
                .addMultipartParameter("pickup_time", food.getPickuptime())
                .addMultipartParameter("latitude", String.valueOf(food.getLatLng().latitude))
                .addMultipartParameter("longitude", String.valueOf(food.getLatLng().longitude))
                .addMultipartParameter("lifespan", String.valueOf(food.getLifespan()))
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
                                /*JSONObject foodObj = response.getJSONObject("data");
                                food.setId(foodObj.getInt("id"));
                                food.setPictureUrl(foodObj.getString("file"));
                                food.setLikes(Integer.parseInt(foodObj.getString("likes")));
                                food.setRequests(Integer.parseInt(foodObj.getString("name")));
                                food.setbLike(Boolean.parseBoolean(foodObj.getString("email")));*/
                                editTitle.setText("");
                                editDes.setText("");
                                editWeight.setText("");
                                editQuantity.setText("");
                                editPickUpTime.setText("");
                                tvExpiry.setText("5");
                                onCloseImage();
                                showToast("Submitted!");
                                finish();
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

    }


    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }

}
