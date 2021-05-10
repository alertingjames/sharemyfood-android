package com.sharemyfood.main;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
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

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.feedbackBox) EditText feedbackBox;
    //@BindView(R.id.reviews) TextView reivews;
    //@BindView(R.id.ratingBox) TextView ratingBox;
    //@BindView(R.id.ratingbar) RatingBar ratingBar;

    AVLoadingIndicatorView progressBar;
    String lang = "";
    ArrayList<Rating> ratingArrayList = new ArrayList<>();
    DecimalFormat df = new DecimalFormat("0.0");

    int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        ButterKnife.bind(this);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

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

        init();

    }

    void init(){
        AndroidNetworking.upload(ReqConst.SERVER + "checkFeedback")
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

        /*String feedback = editFeedback.getText().toString();*/

        /*if(ratingBar.getRating() == 0){
            showToast(getString(R.string.please_rate_out_of_5_stars));
            return;
        }
        */
        if(feedbackBox.getText().toString().trim().length() == 0){
            showToast(getString(R.string.write_feedback));
            return;
        }

        sendMessage(feedbackBox.getText().toString().trim());
    }

    private void sendMessage(String message){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER + "sendFeedback")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getId()))
                .addMultipartParameter("message", message)
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





    /*private void getRatings() {
        progressBar.setVisibility(View.VISIBLE);
        String url = ReqConst.SERVER_URL + "getRatings";
        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseRatingsResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("debug", error.toString());
                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("store_id", "0");
                return params;
            }
        };
        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SharemyfoodApp.getInstance().addToRequestQueue(post, url);
    }*/

    /*public void parseRatingsResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);
            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);
            if (success.equals("0")) {
                ratingArrayList.clear();
                float ratings = 0.0f;
                JSONArray dataArr = response.getJSONArray("data");
                for(int i=0; i<dataArr.length(); i++) {
                    JSONObject object = (JSONObject) dataArr.get(i);
                    Rating rating = new Rating();
                    rating.setIdx(object.getInt("id"));
                    rating.setStoreId(object.getInt("store_id"));
                    rating.setUserId(object.getInt("member_id"));
                    rating.setUserName(object.getString("member_name"));
                    rating.setUserPictureUrl(object.getString("member_photo"));
                    rating.setRating(Float.parseFloat(object.getString("rating")));
                    rating.setSubject(object.getString("subject"));
                    rating.setDescription(object.getString("description"));
                    rating.setDate(object.getString("date_time"));
                    rating.setLang(object.getString("lang"));

                    ratingArrayList.add(rating);
                    ratings += rating.getRating();
                }

                int reviews = 0;
                float ratingVal = 0.0f;
                if(ratingArrayList.size() > 0){
                    reviews = ratingArrayList.size();
                    ratingVal = ratings/reviews;
                }

                ((TextView)findViewById(R.id.ratings)).setText(df.format(ratingVal));
                ((RatingBar)findViewById(R.id.ratingbar_small)).setRating(ratingVal);
                ((TextView)findViewById(R.id.reviews)).setText(String.valueOf(reviews));

                for(Rating rating:ratingArrayList){
                    if(rating.getStoreId() == 0 && rating.getUserId() == Commons.thisUser.get_idx()){
                        subjectBox.setText(rating.getSubject());
                        ratingBar.setRating(rating.getRating());
                        ratingBox.setText(String.valueOf(rating.getRating()));
                        feedbackBox.setText(rating.getDescription());
                        ((TextView)findViewById(R.id.caption1)).setText(getString(R.string.update_app_feedback));

                        if(rating.getLang().equals("ar")){
                            subjectBox.setGravity(Gravity.END);
                            feedbackBox.setGravity(Gravity.END);
                        }else {
                            subjectBox.setGravity(Gravity.START);
                            feedbackBox.setGravity(Gravity.START);
                        }

                        lang = rating.getLang();

                        return;
                    }

                    if(rating == ratingArrayList.get(ratingArrayList.size() - 1)){
                        ((TextView)findViewById(R.id.caption1)).setText(getString(R.string.feedback_app));
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    /*private void submitRating() {
        progressBar.setVisibility(View.VISIBLE);
        String url = ReqConst.SERVER_URL + "placeAppFeedback";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                showToast(getString(R.string.server_issue));
                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("member_id", String.valueOf(Commons.thisUser.get_idx()));
                params.put("store_id", "0");
                params.put("subject", subjectBox.getText().toString().trim());
                params.put("rating", String.valueOf(ratingBar.getRating()));
                params.put("description", feedbackBox.getText().toString().trim());
                if(lang.length() == 0)params.put("lang", Commons.lang);
                else params.put("lang", lang);

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        QhomeApp.getInstance().addToRequestQueue(post, url);
    }*/

    /*public void parseResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);
            String success = response.getString("result_code");
            if (success.equals("0")) {
                showToast(getString(R.string.feedback_submited));
                getRatings();
            }
        } catch (JSONException e) {
            showToast(getString(R.string.server_issue));
            e.printStackTrace();
        }
    }*/
}








































