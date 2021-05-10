package com.sharemyfood.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.sharemyfood.commons.Commons;
import com.sharemyfood.R;

public class MapSettingsActivity extends Activity {

    Switch myLocationSwitchButton, mapViewSwitchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_settings);

        myLocationSwitchButton = (Switch)findViewById(R.id.locationSetting);
        mapViewSwitchButton = (Switch) findViewById(R.id.mapviewSetting);

        if(Commons.curMapTypeIndex == 2)mapViewSwitchButton.setChecked(true);
        else mapViewSwitchButton.setChecked(false);

        if(Commons.mapCameraMoveF)myLocationSwitchButton.setChecked(true);
        else myLocationSwitchButton.setChecked(false);


        myLocationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.mapCameraMoveF = true;
                }else {
                    Commons.mapCameraMoveF = false;
                }
            }
        });
        mapViewSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.curMapTypeIndex = 2;
                }else {
                    Commons.curMapTypeIndex = 1;
                }
                Commons.googleMap.setMapType(Commons.curMapTypeIndex);
            }
        });

        ((ImageView)findViewById(R.id.img_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}




























