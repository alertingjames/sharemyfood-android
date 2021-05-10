package com.sharemyfood.main;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;
import com.sharemyfood.commons.ReqConst;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

public class MyAccountActivity extends BaseActivity {

    @BindView(R.id.img_avatar) ImageView imgAvatar;
    @BindView(R.id.edit_name) EditText editName;
    @BindView(R.id.edit_phonenumber) EditText editPhonenumber;
    @BindView(R.id.tv_email) TextView tvEmail;
    @BindView(R.id.tv_Gverified) TextView tvGverified;
    @BindView(R.id.tv_Gunverified) TextView tvGunverified;
    @BindView(R.id.tv_Fverified) TextView tvFverified;
    @BindView(R.id.tv_Funverified) TextView tvFunverified;
    @BindView(R.id.tv_saveAccount) TextView tvSaveAccount;

    File file = null;
    AVLoadingIndicatorView progressBar;

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INSTALL_PACKAGES,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.SET_TIME,
            android.Manifest.permission.READ_PHONE_STATE,
//            android.Manifest.permission.RECORD_AUDIO,
//            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            android.Manifest.permission.WAKE_LOCK,
//            android.Manifest.permission.READ_CALENDAR,
//            android.Manifest.permission.WRITE_CALENDAR,
//            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_CONTACTS,
//            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.CALL_PRIVILEGED,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.LOCATION_HARDWARE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        //// check permission
//        checkAllPermission();

        ButterKnife.bind(this);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        /////////// permission related //////////
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ///////////////////// click View for hide keyboard //////
        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linear_container);
        linLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // TODO Auto-generated method stub
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        init();
    }

    void init(){

        /*Commons.user.setId("abc");
        Commons.user.setUsername("Jhon James");
        Commons.user.setEmail("handsomeguy@gamil.com");
        Commons.user.setPhone("+119108542345");
        Commons.user.setgAccountState(true);
        Commons.user.setfAccountState(false);
        Commons.user.setPicture("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sO248RG0DlymYJZrdB0nR2GqcGkNhdI_P1BO7DYmWjoNxa-1");*/

        /////////// setting userInfo
        Glide.with(this).load(Commons.thisUser.getPicture()).placeholder(R.drawable.user).into((imgAvatar));
        file = new File(Commons.thisUser.getPicture());

        editName.setText(Commons.thisUser.getUsername());
        editPhonenumber.setText(Commons.thisUser.getPhone());
        tvEmail.setText(Commons.thisUser.getEmail());

        if (Commons.thisUser.getOption().equals("google"))
        {
            tvGunverified.setVisibility(View.INVISIBLE);
            tvGverified.setVisibility(View.VISIBLE);
        } else if (Commons.thisUser.getOption().equals("facebook")){
            tvFverified.setVisibility(View.VISIBLE);
            tvFunverified.setVisibility(View.INVISIBLE);
        } else if (Commons.thisUser.getOption().equals("both")){
            tvGunverified.setVisibility(View.INVISIBLE);
            tvGverified.setVisibility(View.VISIBLE);

            tvFverified.setVisibility(View.VISIBLE);
            tvFunverified.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.frm_avatar)
    void onAvatar(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imgUri = result.getUri();
                file = new File(imgUri.getPath());
                imgAvatar.setImageURI(imgUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.tv_saveAccount)
    void onSave()
    {
        String name = editName.getText().toString().trim();
        String phone = editPhonenumber.getText().toString().trim();

        //// non change state
        if (Commons.thisUser.getUsername().equals(name) && Commons.thisUser.getPhone().equals(phone)/* && !photoChangeState*/)
        {
            return;
        }

        //////  creation a custom file when file is non exist
        if(file == null){
            file = createBitmapFromLayoutWithText(editName.getText().toString().trim());
            if(file == null){
                return;
            }
            imgAvatar.setImageURI(Uri.fromFile(file));
        }
        if(name.length() == 0){
            editName.setError("Enter your name.");
            return;
        }
        if(phone.length() == 0){
            editPhonenumber.setError("Enter your phone number.");
            return;
        }
        if(!validatePhone(phone)){
            editPhonenumber.setError("Enter your valid phone number.");
            return;
        }
        if (file.getPath().contains("http")){

            AndroidNetworking.upload(ReqConst.SERVER + "updateAccount")
                    .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                    .addMultipartParameter("name", name)
                    .addMultipartParameter("phone_number", phone)
                    .setTag(this)
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
                                    Commons.thisUser.setPhone(phone);
                                    showToast("Updated!");
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
        else {

            AndroidNetworking.upload(ReqConst.SERVER + "updateAccount")
                    .addMultipartFile("file",file)
                    .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                    .addMultipartParameter("name", name)
                    .addMultipartParameter("phone_number", phone)
                    .setTag(this)
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
                                    Commons.thisUser.setPhone(phone);
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

    }

    @OnClick(R.id.tv_deleteAccount)
    void onDeleteAccount(){

        View pinDialogView = LayoutInflater.from(this).inflate(R.layout.alert_confirm, null, false);
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
                goDeleteAccount();
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();
    }
    void goDeleteAccount(){
        AndroidNetworking.upload(ReqConst.SERVER + "delAccount")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
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

                                EasyPreference.with(MyAccountActivity.this).addString("email", "").save();
                                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
