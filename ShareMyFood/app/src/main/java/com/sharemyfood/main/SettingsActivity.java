package com.sharemyfood.main;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.os.Bundle;

import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.R;
import com.suke.widget.SwitchButton;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.switch_button) SwitchButton settingsSwithchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        boolean notyState = EasyPreference.with(this).getBoolean("easyNotyState", true);
        if (notyState)
        {
            settingsSwithchButton.setChecked(true);
        }
        else {
            settingsSwithchButton.setChecked(false);
        }

        /////////// SwitchButton Click : Toggle State /////////
        settingsSwithchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {

                EasyPreference.with(getApplicationContext()).addBoolean("easyNotyState", isChecked).save();

            }
        });
    }


    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }
}

