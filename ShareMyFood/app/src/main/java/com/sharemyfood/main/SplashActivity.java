package com.sharemyfood.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.R;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    static final int SPLASH_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String email = EasyPreference.with(SplashActivity.this).getString("email", "");
                Log.d("+++++++++++++", email);
                login(email);
            }
        }, SPLASH_DELAY);
    }

    private void login(String email){
        if(email.length() == 0){
            Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "login")
                .addBodyParameter("email", email)
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RegisterResponse=====>", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                JSONObject userObj = response.getJSONObject("data");
                                UserModel userModel = new UserModel();
                                userModel.setId(userObj.getInt("id"));
                                userModel.setUsername(userObj.getString("name"));
                                userModel.setEmail(userObj.getString("email"));
                                userModel.setPhone(userObj.getString("phone_number"));
                                userModel.setPicture(userObj.getString("picture_url"));
                                userModel.setAddress(userObj.getString("address"));
                                userModel.setCountry(userObj.getString("country"));
                                userModel.setCity(userObj.getString("city"));
                                userModel.setLatLng(new LatLng(Double.parseDouble(userObj.getString("latitude")), Double.parseDouble(userObj.getString("longitude"))));
                                //userModel.setLatLng(new LatLng(userObj.getDouble("latitude"), userObj.getDouble("longitude")));
                                userModel.setOption(userObj.getString("status"));

                                Commons.thisUser = userModel;
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }else if ( result.equals("1")){
                                EasyPreference.with(SplashActivity.this).addString("email", "").save();
                                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //showToast("Network issue");
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("RegisterError=====>", error.toString());
                        //showToast("Network issue");
                    }
                });

    }
}




































