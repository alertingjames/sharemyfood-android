package com.sharemyfood.main;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;

public class AdminMessageActivity extends BaseActivity {

    public final int REQUEST_CODE = 100;
    private int kg = 1;

    @BindView(R.id.admin_Dtitle) TextView adminTitle;
    @BindView(R.id.admin_Ddatetime) TextView adminTime;
    @BindView(R.id.admin_Ddescription) TextView adminDes;
    @BindView(R.id.admin_confirm) TextView adminConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_message);

        ButterKnife.bind(this);

        if (Commons.bNotyAdmin){
            adminConfirm.setVisibility(View.GONE);
            Commons.bNotyAdmin = false;
        }

        init();
    }

    void init(){

        adminTitle.setText(Commons.message.getTitle());
        adminTime.setText(Commons.message.getDate_time());
        adminDes.setText(Commons.message.getBody());
    }

    @OnClick(R.id.admin_confirm)
    void onConfirm(){
        //showToast("New Noty state Confirm");
        finish();
    }

    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }

}































