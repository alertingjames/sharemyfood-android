package com.sharemyfood.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.OrderHistory;
import com.sharemyfood.R;
import com.sharemyfood.models.OrderModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotificationActivity extends BaseActivity {


    ArrayList<OrderModel> messages = new ArrayList<>();
    AVLoadingIndicatorView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ButterKnife.bind(this);
        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        //init();  UI Version
        setDataMessage();

    }

    void init() {

        ///////////////////// UI VERSION /////////////////
        LinearLayout lin_noty_container = (LinearLayout) findViewById(R.id.linear_notification_container);
        lin_noty_container.removeAllViews();

        //// Original download call Api //////
        String[] descriptoins = {"Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you."};
        /// Add messageCells to messageContainer
        for (int i = 0; i < descriptoins.length; i++) {
            OrderHistory orderHistory = new OrderHistory();
            ////////// Add Model
            orderHistory.setId("123");
            orderHistory.setTitle("NotificationActivity" + i);
            orderHistory.setDate_time("09-09-2019 1:35 PM");
            orderHistory.setTimestamp(getCurrentTimeStamp());
            orderHistory.setDescription(descriptoins[i]);
            //messages.add(orderHistory);

            /// Add List Linear Models
            LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_admin_message, null);
            ((TextView) myMessageCellLayout.findViewById(R.id.admin_title)).setText(orderHistory.getTitle());
            ((TextView) myMessageCellLayout.findViewById(R.id.admin_description)).setText(orderHistory.getDescription());
            //((TextView) myMessageCellLayout.findViewById(R.id.admin_datetime)).setText(getCurrentTimeString(messages.get(i).getTimestamp()));
            //// inflate
            lin_noty_container.addView(myMessageCellLayout);

            ////////// close setting
            ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.admin_close);
            final int ii = i;
            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index = messages.indexOf(orderHistory);
                    onDelete(index, lin_noty_container, myMessageCellLayout);
                    //lin_noty_container.removeView(myMessageCellLayout);
                }
            });

            myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AdminMessageActivity.class);
                    Commons.orderHistory = orderHistory;
                    Commons.bNotyAdmin = true;
                    startActivity(intent);
                }
            });

        }
        if(messages.size() == 0){
            ((TextView) findViewById(R.id.tv_notyresult)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.tv_notyresult)).setVisibility(View.GONE);
        }

    }

    private void setDataMessage() {

        messages.clear();
        progressBar.setVisibility(View.VISIBLE);

        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "getNotifications")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getId()))
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

                                JSONArray messageArr = response.getJSONArray("data");
                                if (messageArr.length() != 0) {

                                    for (int i=0 ; i < messageArr.length(); i++){

                                        JSONObject json =  messageArr.getJSONObject(i);
                                        OrderModel messageModel = new OrderModel();

                                        messageModel.setId(json.getInt("id"));
                                        messageModel.setOrder_id(json.getInt("order_id"));
                                        messageModel.setReceiver_id(json.getInt("receiver_id"));
                                        messageModel.setSender_id(json.getInt("sender_id"));
                                        messageModel.setMessage(json.getString("orderHistory"));
                                        messageModel.setSender_name(json.getString("sender_name"));
                                        messageModel.setSender_phone(json.getString("sender_phone"));
                                        messageModel.setDate_time(json.getLong("date_time"));
                                        messageModel.setSender_photo(json.getString("sender_photo"));
                                        messageModel.setOption(json.getString("option"));

                                        if(messageModel.getReceiver_id() == Commons.thisUser.getId()) {
                                            messages.add(messageModel);
                                        }
                                    }
                                    onMessageLoad(messages);
                                }
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
                        showToast("Network issue");
                    }
                });
    }

    void onMessageLoad(ArrayList<OrderModel> messageModels) {

        ///////////////////// UI VERSION /////////////////
        LinearLayout lin_noty_container = (LinearLayout) findViewById(R.id.linear_notification_container);
        lin_noty_container.removeAllViews();

        int i = 0;
        for (OrderModel message: messageModels) {

            i = i + 1;
            if (message.getOption().equals("admin")) {
                /// Add List Linear Models for admin
                LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_admin_message, null);
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_title)).setText(message.getOption());
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_description)).setText(message.getMessage());
                ((TextView) myMessageCellLayout.findViewById(R.id.admin_datetime)).setText(getCurrentTimeString(message.getDate_time()));
                //// inflate
                lin_noty_container.addView(myMessageCellLayout);

                ////////// close setting
                ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.admin_close);
                final int ii = i;
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int index = messages.indexOf(message);
                        onDelete(index, lin_noty_container, myMessageCellLayout);
                    }
                });

                myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), AdminMessageActivity.class);
                        Commons.order = message;
                        startActivity(intent);
                    }
                });
            } else { //// "order"

                /// Add List Linear Models for order
                LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_order_message, null);
                Glide.with(this).load(message.getSender_photo()).placeholder(R.drawable.user).into((ImageView) myMessageCellLayout.findViewById(R.id.order_userimage));
                ((TextView) myMessageCellLayout.findViewById(R.id.order_username)).setText(message.getSender_name());
                ((TextView) myMessageCellLayout.findViewById(R.id.order_message)).setText(message.getMessage());
                ((TextView) myMessageCellLayout.findViewById(R.id.order_datetime)).setText(getCurrentTimeString(message.getDate_time()));
                //// inflate
                lin_noty_container.addView(myMessageCellLayout);

                ////////// close setting
                ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.order_close);
                final int ii = i;
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int index = messages.indexOf(message);
                        onDelete(index, lin_noty_container, myMessageCellLayout);
                    }
                });

                myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), OrderMessageActivity.class);
                        Commons.order = message;
                        startActivity(intent);
                    }
                });
            }
        }

        if(messageModels.size() == 0){
            ((TextView) findViewById(R.id.tv_messageresult)).setText(getString(R.string.message_noresult));
        } else {
            ((TextView) findViewById(R.id.tv_messageresult)).setText(getString(R.string.message_yesresult));
        }
    }


    void onDelete(int index, LinearLayout lin, LinearLayout cell){

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
                goApi(index);
                showToast("Notification History delect success");
                messages.remove(index);
                lin.removeView(cell);
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();

    }

    private void goApi(int index){

        showToast(index + " : Notification History Delete in Api");
    }
    @OnClick(R.id.img_back)
    void onBack(){
        finish();
    }
}
