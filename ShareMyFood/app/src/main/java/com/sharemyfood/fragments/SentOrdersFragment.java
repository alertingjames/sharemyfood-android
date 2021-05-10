package com.sharemyfood.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sharemyfood.R;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.main.OrderMessageActivity;
import com.sharemyfood.models.OrderHistory;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.sharemyfood.base.BaseActivity.getCurrentTimeStamp;
import static com.sharemyfood.base.BaseActivity.getCurrentTimeString;

public class SentOrdersFragment extends Fragment {

    ArrayList<OrderHistory> orderHistories = new ArrayList<>();

    public SentOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = (View)inflater.inflate(R.layout.fragment_sent, container, false);
        init(view);
        return view;
    }

    void init(View view) {

        ///////////////////// UI VERSION /////////////////
        LinearLayout lin_order_container = (LinearLayout) view.findViewById(R.id.linear_Sorder_container);
        lin_order_container.removeAllViews();

        //// Original download call Api //////
        String[] descriptoins = {"Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you.", "Hello how are you? I appreciate you."};
        String[] usernames = new String[]{"UserName1", "UserName2", "UserName3","UserName4","UserName5","UserName6"};
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
        /// Add messageCells to messageContainer
        for (int i = 0; i < descriptoins.length; i++) {
            OrderHistory orderHistory = new OrderHistory();
            ////////// Add Model
            orderHistory.setWeight(i * 2.4);
            orderHistory.setQuanitty(i + 1);
            orderHistory.setFoodNmae("Food" + i);
            orderHistory.setLocation("Food Address" + i);
            orderHistory.setContact("+119123423422");
            orderHistory.setDescription(descriptoins[i]);
            orderHistory.setFoodImage(foodimages[i]);
            orderHistory.setUsername(usernames[i]);
            orderHistory.setUserpicture(userphotos[i]);
            orderHistory.setTimestamp(getCurrentTimeStamp());
            orderHistories.add(orderHistory);

            /// Add List Linear Models
            LinearLayout myMessageCellLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_orderhistory_message, null);
            Glide.with(this).load(userphotos[i]).placeholder(R.drawable.user).into((ImageView) myMessageCellLayout.findViewById(R.id.order_userimage));
            Glide.with(this).load(foodimages[i]).placeholder(R.drawable.vegteble).into((ImageView) myMessageCellLayout.findViewById(R.id.order_foodimage));
            ((TextView) myMessageCellLayout.findViewById(R.id.order_username)).setText(orderHistory.getUsername());
            ((TextView) myMessageCellLayout.findViewById(R.id.order_title)).setText(orderHistory.getFoodNmae());
            ((TextView) myMessageCellLayout.findViewById(R.id.order_contact)).setText(orderHistory.getContact());
            ((TextView) myMessageCellLayout.findViewById(R.id.order_weight)).setText(String.valueOf(Math.round(orderHistory.getWeight()*100)/100.0));
            ((TextView) myMessageCellLayout.findViewById(R.id.order_quantity)).setText(String.valueOf(orderHistory.getQuanitty()));
            ((TextView) myMessageCellLayout.findViewById(R.id.order_datetime)).setText(getCurrentTimeString(orderHistories.get(i).getTimestamp()));
            //// inflate
            lin_order_container.addView(myMessageCellLayout);

            ////////// close setting
            ImageView imgClose = (ImageView) myMessageCellLayout.findViewById(R.id.order_close);
            final int ii = i;
            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index = orderHistories.indexOf(orderHistory);
                    onDelete(index, lin_order_container, myMessageCellLayout);
                }
            });

            myMessageCellLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), OrderMessageActivity.class);
                    Commons.orderHistory = orderHistory;
                    Commons.bNotyOrder = true;
                    startActivity(intent);
                }
            });

        }

        if(orderHistories.size() == 0){
            ((TextView) view.findViewById(R.id.tv_Sorderresult)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) view.findViewById(R.id.tv_Sorderresult)).setVisibility(View.GONE);
        }
    }

    void onDelete(int index, LinearLayout lin, LinearLayout cell){

        View pinDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_confirm_delete, null, false);
        androidx.appcompat.app.AlertDialog pinDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
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
                orderHistories.remove(index);
                lin.removeView(cell);
                (new BaseActivity()).showToast("Sent share history delect success");
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();

    }
}
