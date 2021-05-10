package com.sharemyfood.base;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.sharemyfood.R;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.models.FoodModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class BaseActivity extends AppCompatActivity {

    KProgressHUD kProgress;

    public static String removeWord(String string, String word)
    {
        if (string.contains(word)) {

            string = string.replaceAll(word, "");
        }
        return string;
    }

    public static long getCurrentTimeStamp(){
        // long millis = new Date().getTime();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        try {
            long currentTime = timestamp.getTime();
            return currentTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static String getCurrentTimeString(long timeStamp){
        // long millis = new Date().getTime();
        Date df = new java.util.Date(timeStamp);
        String timeStampString = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);
        return timeStampString;
    }

    public static long getDifferenceTwoTimestamp(long millis1, long millis2){

        long diff = millis2 - millis1;

        // Calculate difference in seconds
        long diffSeconds = diff / 1000;
        // Calculate difference in minutes
        long diffMinutes = diff / (60 * 1000);
        // Calculate difference in hours
        long diffHours = diff / (60 * 60 * 1000);
        // Calculate difference in days
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    public static double distance(LatLng myLatLng, LatLng yourLanLng){

        Location myLocation = new Location("MyLocation");
        myLocation.setLatitude(myLatLng.latitude);
        myLocation.setLongitude(myLatLng.longitude);

        Location yourLocation = new Location("YourLocation");
        yourLocation.setLatitude(yourLanLng.latitude);
        yourLocation.setLongitude(yourLanLng.longitude);
        double distance = myLocation.distanceTo(yourLocation);
        distance = (double)((int)(distance/1000 * 100)) /100;
        return distance;
    }

    public static boolean validateEmail(String email){


        String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        if (email.matches(emailPattern)) {
            return true;
        }
        else {
            return false;
        }

    }

    public static boolean validatePhone(String phone){

        String regexStr = "^[+]?[0-9]{8,15}$";
        if (phone.matches(regexStr))
            return true;
        else
            return false;
    }

    private File saveImageToExternalStorage(Bitmap image) {
        File file = null;
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            file = new File(fullPath, System.currentTimeMillis() + ".png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

// 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return file;

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
            return file;
        }
    }

    public File createBitmapFromLayoutWithText(String name) {
        File file = null;
        LayoutInflater mInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Inflate the layout into a view and configure it the way you like
        RelativeLayout view = new RelativeLayout(getApplicationContext());
        mInflater.inflate(R.layout.icon, view, true);
        FrameLayout icon = (FrameLayout)view.findViewById(R.id.icon);
        icon.setBackgroundColor(getRandomColor());
        TextView tv = (TextView) view.findViewById(R.id.icon_name);
        tv.setText(name.substring(0,1));

        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        view.setLayoutParams(new WindowManager.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        file = saveImageToExternalStorage(bitmap);
        if(file != null){
            return file;
        }

        return file;
    }

    public int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }


    ///////////// permission check  ////////////////////////////////////////////////////
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
            //android.Manifest.permission.READ_CONTACTS,
//            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            //android.Manifest.permission.CALL_PHONE,
            //android.Manifest.permission.CALL_PRIVILEGED,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.LOCATION_HARDWARE
    };


    public void checkAllPermission() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (hasPermissions(this, PERMISSIONS)){

        }else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void showToast(String content){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(content);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void setupUI(View view, Activity activity) {
// Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    try{
                        hideSoftKeyboard(activity);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }
//If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void showSoftKeyboard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

}













































