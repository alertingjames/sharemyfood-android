package com.sharemyfood.main;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.iamhabib.easy_preference.EasyPreference;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.FoodModel;
import com.sharemyfood.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class DashboardActivity extends BaseActivity {

    RemoteAdapter remoteAdapter;
    @BindView(R.id.recycler_remote_picture)
    RecyclerView recyclerRemotePicture;

    LatLng mylatLang;
    ArrayList<FoodModel> foods = new ArrayList<>();
    ArrayList<LatLng> latLngs = new ArrayList<>();
    LinearLayout linear_scroll_Hview, linear_scroll_Vview;
    AVLoadingIndicatorView progressBar;
    GridView gridView;
    @BindView(R.id.tv_htitle) TextView hTitle;
    @BindView(R.id.tv_total_count) TextView tvTotalCount;
    @BindView(R.id.tv_total_weight) TextView tvTotalWeight;
    @BindView(R.id.tv_total_consumed) TextView tvTotalConsumed;

    int totalCount = 0;
    double totalWeight = 0.0;
    int totalConsumed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        ButterKnife.bind(this);

        //init();
        loadLayout();
        setDataFood();
    }

    private void setDataFood() {

        foods.clear();
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
                                long now = response.getLong("now");
                                JSONArray foodArr = response.getJSONArray("data");
                                if (foodArr.length() != 0) {

                                    for (int i=0 ; i < foodArr.length(); i++){

                                        JSONObject json =  foodArr.getJSONObject(i);
                                        FoodModel foodModel = new FoodModel();

                                        foodModel.setId(json.getInt("id"));
                                        foodModel.setUserId(json.getInt("member_id"));
                                        foodModel.setUserpicture(json.getString("member_photo"));
                                        foodModel.setUsername(json.getString("member_name"));
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
                                        if (json.getString("is_like").equals("yes"))
                                        {
                                            foodModel.setbLike(true);
                                        } else if(json.getString("is_like").equals("no")){
                                            foodModel.setbLike(false);
                                        }
                                        foodModel.setState(json.getString("status"));

                                        if(foodModel.getUserId() == Commons.thisUser.getId()) {
                                            totalCount = totalCount + 1;
                                            foods.add(foodModel);
                                        }
                                    }

                                    tvTotalCount.setText(String.valueOf(totalCount));
                                    //onFoodLoad(foods, now);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            remoteAdapter.setRemotePictures(foods, now);
                                        }
                                    });

                                    getConsumedProductInfo();
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

    private void getConsumedProductInfo(){

        AndroidNetworking.post(ReqConst.SERVER + "getConsumedProductInfo")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
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

                                int consumedCount = response.getInt("consumed_count");
                                double unconsumedWeight = response.getDouble("consumed_weight");

                                tvTotalWeight.setText(String.valueOf(unconsumedWeight) + "kg");
                                tvTotalConsumed.setText(String.valueOf(consumedCount));


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
                    }
                });

    }






    void onFoodLoad(ArrayList<FoodModel> foodModels, long now){

        ///// layout inflater
        linear_scroll_Hview = (LinearLayout) findViewById(R.id.Linear_scroll_hview);
        linear_scroll_Vview = (LinearLayout) findViewById(R.id.Linear_scroll_vview);
        linear_scroll_Hview.removeAllViews();
        linear_scroll_Vview.removeAllViews();

        int countTemp = 0;
        int countVtemp = 0;
        for (int i = 0; i < foodModels.size(); i++) {

            /// Add Horizental List Linear Models
            long currentTimestamp = now, diff;
            diff = getDifferenceTwoTimestamp(foodModels.get(i).getRegistered_tiime(), currentTimestamp);

            if (foodModels.get(i).getUserId() != Commons.thisUser.getId())
                return;
            if (diff <= 30){
                 countTemp = countTemp + 1;
                /// Add Horizental List Linear Models
                LinearLayout myCellLayoutH = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_dashboard_h_cell, null);

                Glide.with(this).load(foodModels.get(i).getPictureUrl()).placeholder(R.drawable.cabage).into((ImageView) myCellLayoutH.findViewById(R.id.cell_image));

                TextView txv_time =(TextView) myCellLayoutH.findViewById(R.id.cell_time_text);
                TextView label =(TextView) myCellLayoutH.findViewById(R.id.label);
                txv_time.setText(String.valueOf(diff));

                if(diff < 1){
                    txv_time.setVisibility(View.GONE);
                    label.setText("Today");
                }else if(diff == 1){
                    label.setText(" day ago");
                }else {
                    label.setText(" days ago");
                }
                int finalI = i;
                myCellLayoutH.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                        Commons.food = foodModels.get(finalI);
                        Commons.bDashHis = true;
                        startActivity(intent);
                    }
                });
                linear_scroll_Hview.addView(myCellLayoutH);
            }

            /// Add Vertical List Linear Models
            LinearLayout myCellLayoutV = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_dashboard_v_cell, null);
            ((TextView) myCellLayoutV.findViewById(R.id.cell_title)).setText(foodModels.get(i).getTitle());
            Glide.with(this).load(foodModels.get(i).getPictureUrl()).placeholder(R.drawable.cabage).into((ImageView) myCellLayoutV.findViewById(R.id.cell_image));
            ((TextView) myCellLayoutV.findViewById(R.id.cell_like_text)).setText(String.valueOf(foodModels.get(i).getLikes()));
            ((TextView) myCellLayoutV.findViewById(R.id.cell_request_text)).setText(String.valueOf(foodModels.get(i).getRequests()));

            int finalI = i;
            myCellLayoutV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                    Commons.food = foodModels.get(finalI);
                    startActivity(intent);
                }
            });
            //// inflate
            linear_scroll_Vview.addView(myCellLayoutV);
        }
        if (countTemp == 0){
            ((TextView) findViewById(R.id.tv_htitle)).setText(getString(R.string.noresult));
        }
        if (countVtemp == 0){
            ((TextView) findViewById(R.id.tv_vtitle)).setVisibility(View.GONE);
        }


    }
    /*void init(){

        ///////////////////// UI VERSION /////////////////
        ///// layout inflater
        linear_scroll_Hview = (LinearLayout) findViewById(R.id.Linear_scroll_hview);
        linear_scroll_Vview = (LinearLayout) findViewById(R.id.Linear_scroll_vview);

        //// Original download call Api //////
        int[] myFoodImage = {R.drawable.cabage, R.drawable.vegteble, R.drawable.vegteble, R.drawable.cabage, R.drawable.vegteble, R.drawable.cabage};
        String[] title = {getString(R.string.food), getString(R.string.food), getString(R.string.food), getString(R.string.food), getString(R.string.food), getString(R.string.food)};
        String[] usernames = new String[]{"user1", "user2", "user3","user4","user5","user6"};
        String[] foodimages = new String[]{
                "https://media.gettyimages.com/photos/authentic-indian-food-picture-id639389404?s=612x612",
                "https://upload.wikimedia.org/wikipedia/commons/6/6d/Good_Food_Display_-_NCI_Visuals_Online.jpg",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcThU0ZGeEOg3AHcqMiPQBppTG0hgCKr2aIJo9ifU7YZiUFfQ0zL",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbp40HELwGGM7ylFHjprPVlXIRKaY40xbojxQ3Uviy231U6lrT9g",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbSWjHBojui3f9nzJwe369pWejXMEi8DT4nxpq7YzH10SF4uVnew",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTV_p0qejm8O4Oeexa0mBkTA65Yjgmhsv_3eZkFwIKj-QGkz5N4Bw"
        };

        String[] userphotos = new String[]{
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sO248RG0DlymYJZrdB0nR2GqcGkNhdI_P1BO7DYmWjoNxa-1",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRvHVRnqli6L02dAUqnOiFNctD3_y8bxm9HtV_XObwXL809BD8wTQ",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR0ueHsBNklxKbfCJQ_PGoSoLBg1cxu2XiRKwY9ftibkF45jzh58g",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbp40HELwGGM7ylFHjprPVlXIRKaY40xbojxQ3Uviy231U6lrT9g",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlWTyruSkqCfLfp05mLh09J6kKS95F4Hi04_MmrLRGSVjUPwJs",
                "https://www.naaree.com/wp-content/uploads/2016/11/Best-Jobs-For-Women-In-India.jpg"
        };


        for (int i = 0; i < myFoodImage.length; i++) {
            FoodModel foodModel = new FoodModel();
            ////////// Add Model
            for (int j = 0; j< i+1; j++){
                LatLng latLng = new LatLng(i*5, i*(-5));
                latLngs.add(latLng);
            }
            foodModel.setUserId(Commons.thisUser.getId());
            foodModel.setTitle(title[i]);
            foodModel.setDescription("Description" + i);
            foodModel.setPickuptime("4-6 PM");
            foodModel.setPictureUrl(foodimages[i]);
            foodModel.setQuantity(25);
            String myAddress = EasyPreference.with(this).getString("MyAddress", "");
            foodModel.setAdress(myAddress + ":" + i);
            foodModel.setId(i); foodModel.setLifespan(i+10); foodModel.setWeight(i*2);
            foodModel.setLatLng(new LatLng(0.00000, 0.00000));
            foods.add(foodModel);

            if (Commons.thisUser.getId() == foods.get(i).getUserId()){

                /// Add Horizental List Linear Models
                long currentTimestamp = getCurrentTimeStamp(), diff;
                //diff = getDifferenceTwoTimestamp(String.valueOf(currentTimestamp), String.valueOf(foods.get(i).getPickuptime()));
                diff = getDifferenceTwoTimestamp(currentTimestamp, currentTimestamp);

                if (diff <= 30){
                    /// Add Horizental List Linear Models
                    LinearLayout myCellLayoutH = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_dashboard_h_cell, null);

                    Glide.with(this).load(foodimages[i]).placeholder(R.drawable.cabage).into((ImageView) myCellLayoutH.findViewById(R.id.cell_image));

                    TextView txv_time =(TextView) myCellLayoutH.findViewById(R.id.cell_time_text);
                    txv_time.setText(String.valueOf(diff));

                    myCellLayoutH.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), MyFoodDetailActivity.class);
                            Commons.food = foodModel;
                            startActivity(intent);
                        }
                    });
                    linear_scroll_Hview.addView(myCellLayoutH);
                }

                /// Add Vertical List Linear Models
                LinearLayout myCellLayoutV = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_dashboard_v_cell, null);

                Glide.with(this).load(foodimages[i]).placeholder(R.drawable.cabage).into((ImageView) myCellLayoutV.findViewById(R.id.cell_image));

                ////////// Like setting
                ImageView imgLike = (ImageView) myCellLayoutV.findViewById(R.id.cell_like_image);
                if(foods.get(i).isbLike()==true){
                    imgLike.setImageResource(R.drawable.like1);
                }else{
                    imgLike.setImageResource(R.drawable.like);
                }
                ((TextView) myCellLayoutV.findViewById(R.id.cell_title)).setText(title[i]);
                TextView txv_like =(TextView) myCellLayoutV.findViewById(R.id.cell_like_text);
                txv_like.setText(String.valueOf(i));
                TextView txv_request =(TextView) myCellLayoutV.findViewById(R.id.cell_request_text);
                txv_like.setText(String.valueOf(i*3));

                myCellLayoutV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), MyFoodDetailActivity.class);
                        Commons.food = foodModel;
                        startActivity(intent);
                    }
                });
                linear_scroll_Vview.addView(myCellLayoutV);
            }

        }
    }*/

    @SuppressLint("NewApi")
    private void loadLayout() {

        remoteAdapter = new RemoteAdapter(this);
        recyclerRemotePicture.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerRemotePicture.setAdapter(remoteAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //setDataFood();
    }

    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }
}


////////////************ RemotePicture Adapter ****************//////////////////
class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.ViewHolder> {

    DashboardActivity context;
    long now;
    /*ArrayList<String> arrayRemotePictures = new ArrayList<>();
    ArrayList<Boolean> arrayChecked = new ArrayList<>();*/


    private LayoutInflater mInflater;

    public RemoteAdapter(DashboardActivity context) {

        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }


    public void setRemotePictures(ArrayList<FoodModel> list, long now) {

        this.now = now;
        notifyDataSetChanged();
    }


    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.layout_dashboard_h_cell, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each cell
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FoodModel foodModel = context.foods.get(position);

        long currentTimestamp = now, diff;
        diff = context.getDifferenceTwoTimestamp(foodModel.getRegistered_tiime(), currentTimestamp);


        Glide.with(context).load(foodModel.getPictureUrl()).placeholder(R.drawable.cabage).into(holder.imgRemotePicture);
        holder.timeText.setText(String.valueOf(diff));

        if(diff < 1){
            holder.timeText.setVisibility(View.GONE);
            holder.lbText.setText("Today");
        }else if(diff == 1){
            holder.lbText.setText(" day ago");
        }else {
            holder.lbText.setText(" days ago");
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                Commons.food = foodModel;
                Commons.bDashHis = true;

                context.startActivity(intent);

                notifyItemChanged(position);
            }
        });

    }
    // total number of cells
    @Override
    public int getItemCount() {
        return context.foods.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder  {

        @BindView(R.id.card_view)
        CardView cardView;
        @BindView(R.id.cell_image) ImageView imgRemotePicture;
        @BindView(R.id.cell_time_text) TextView timeText;
        @BindView(R.id.label) TextView lbText;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}