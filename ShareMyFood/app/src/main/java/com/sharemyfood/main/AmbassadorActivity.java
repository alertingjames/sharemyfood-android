package com.sharemyfood.main;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.request.Request;
import com.google.android.gms.common.api.Response;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.R;
import com.sharemyfood.SharemyfoodApp;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.Constants;
import com.sharemyfood.commons.ReqConst;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AmbassadorActivity extends BaseActivity {

    @BindView(R.id.edit_name) EditText editName;
    @BindView(R.id.edit_email) EditText editEmail;
    @BindView(R.id.edit_phonenumber) EditText editPhone;

    AVLoadingIndicatorView progressBar;
    String lang = "";
    int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambassador);

        ButterKnife.bind(this);

        ///// layout inflater
        ScrollView src = (ScrollView) findViewById(R.id.src_container);
        ///////////////////// click View for hide keyboard //////
        src.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // TODO Auto-generated method stub
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }

        });

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        editName.setText(Commons.thisUser.getUsername());
        editEmail.setText(Commons.thisUser.getEmail());
        editPhone.setText(Commons.thisUser.getPhone());

        setupUI(findViewById(R.id.activity), this);

        init();
    }


    void init(){
        AndroidNetworking.upload(ReqConst.SERVER + "checkAmbassador")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .setPriority(Priority.MEDIUM)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("1")){
                                status = 1;
                                ((ImageView)findViewById(R.id.img_check)).setVisibility(View.VISIBLE);

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
                        //showToast(error.toString());
                    }
                });
    }

    @OnClick(R.id.submit)
    void onSubmit(){

        if (status == 1)
            return;

        String name = editName.getText().toString().trim();
        String mail = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();


        if(name.length() == 0){
            editName.setError("Enter your name.");
            return;
        }

        if(mail.length() == 0){
            editEmail.setError("Enter your email.");
            return;
        }

        if (!validateEmail(mail)){
            editEmail.setError("Invalid email");
            return;
        }

        if(phone.length() == 0){
            editPhone.setError("Enter your phone number.");
            return;
        }
        if(!validatePhone(phone)){
            editPhone.setError("Invalid phone number.");
            return;
        }

        sendMessage(name, mail, phone);
    }


    private void sendMessage(String name, String email, String phone){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER + "sendAmbassador")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .addMultipartParameter("email", email)
                .addMultipartParameter("phone_number", phone)
                .addMultipartParameter("name", name)
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
                                showToast("Submitted!");

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





































