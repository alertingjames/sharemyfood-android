package com.sharemyfood.main;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.iamhabib.easy_preference.EasyPreference;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.ReqConst;
import com.sharemyfood.models.UserModel;
import com.sharemyfood.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;

import com.facebook.FacebookSdk;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SigninActivity extends BaseActivity {

    @BindView(R.id.lin_signin) LinearLayout linSignin;
    @BindView(R.id.day) TextView tvDay;

    public static CallbackManager callbackManager;  /// for facebook login
    GoogleSignInClient mGoogleSignInClient; /// for google login
    GoogleApiClient mGoogleApiClient;   // for google login
    UserModel user = null;
    LatLng myLatLang = null;

    String address = "";
    String country = "";
    String city = "";

    private LocationManager mLocationManager;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    AVLoadingIndicatorView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        ButterKnife.bind(this);

        onLoadText();

        checkAllPermission();

        ///// current user position
        checkForLocationPermission();

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        ///////////// facebook login //////////////
        FacebookSdk.sdkInitialize(getApplicationContext());
        //        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        try {

            PackageInfo info = getPackageManager().getPackageInfo("com.sharemyfood", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());
                Log.i("KeyHash::", Base64.encodeToString(md.digest(), Base64.DEFAULT));//will give developer key hash
        // Toast.makeText(getApplicationContext(), Base64.encodeToString(md.digest(), Base64.DEFAULT), Toast.LENGTH_LONG).show(); //will give app key hash or release key hash
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        //////////// googel login //////////
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        ////////////  Network communication  //////////////
        AndroidNetworking.initialize(getApplicationContext());
        /*OkHttpClient okHttpClient = new OkHttpClient() .newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);*/
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

    }

    private void onLoadText(){

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today); // don't forget this if date is arbitrary
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 being Sunday
        switch (dayOfWeek)
        {
            case 1:
                tvDay.setText(getText(R.string.sun));
                break;
            case 2:
                tvDay.setText(getText(R.string.mon));
                break;
            case 3:
                tvDay.setText(getText(R.string.tue));
                break;
            case 4:
                tvDay.setText(getText(R.string.wed));
                break;
            case 5:
                tvDay.setText(getText(R.string.thu));
                break;
            case 6:
                tvDay.setText(getText(R.string.fri));
                break;
            case 7:
                tvDay.setText(getText(R.string.sat));
                break;

        }
    }


    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null)
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location == null){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                myLatLang = latLng;
                                Log.d("2222222222222",myLatLang.latitude + ":" + myLatLang.longitude) ;
                                address = getAddress(myLatLang.latitude, myLatLang.longitude);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "GPS Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "GPS Provider disabled");
                    }
                });
            }
            if (location != null) {
                try {
                    myLatLang = new LatLng(location.getLatitude(), location.getLongitude());
                    address = getAddress(myLatLang.latitude, myLatLang.longitude);
                    Log.d("11111111111111111",myLatLang.latitude + ":" + myLatLang.longitude) ;
                    ///////////////////////////////////////////////////////////////////////////////////////////////
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getAddress(double ltt, double lgt)
    {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(ltt, lgt, 1);
            if (addresses.isEmpty()) {

            }
            else {
                if (addresses.size() > 0) {

                    String temp = addresses.get(0).getAddressLine(0);

                    country = addresses.get(0).getCountryName();
                    if (addresses.get(0).getLocality() == null)
                    {
                        city = addresses.get(0).getAdminArea();
                    } else {
                        city = addresses.get(0).getLocality();
                    }

                    return temp;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "Undefined";
    }
    @OnClick(R.id.tv_facebook_signin)
    void onFacebook() {

        loginWithFB();

    }
    @OnClick(R.id.tv_email_signin)
    void onEmail() {

        signIn();

    }

    private String FEmail, Name, Firstname, Lastname, Id, Gender="", Image_url, Birthday="";

    private void loginWithFB() {
        // set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile = Profile.getCurrentProfile();

                // Facebook Email address
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted( JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity Response ", response.toString());

                                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                                Log.d("IsLoggedIn???", String.valueOf(isLoggedIn));

                                Log.d("Login Token!!!", loginResult.getAccessToken().getToken());

                                try {

                                    user = new UserModel();

                                    Name = object.getString("name");
                                    Name.trim();
                                    Id = object.getString("id");
                                    Firstname = object.getString("first_name");
                                    Lastname = object.getString("last_name");
                                    FEmail = object.getString("email");
                                    Image_url = "http://graph.facebook.com/(Id)/picture?type=large";
                                    Image_url = URLEncoder.encode(Image_url);
                                    user.setPicture(Image_url);

                                    Log.d("Email = ", " " + FEmail);
                                    Log.d("Name======", Name);
                                    Log.d("Image====",Image_url.toString());
                                    Log.d("firstName======", Firstname);
                                    Log.d("lastName======", Lastname);
                                    Log.d("id======", Id);
                                    Log.d("Object=====>", object.toString());
                                    Log.d("photourl======", Image_url);

                                    if (object.has("picture")) {
                                        JSONObject jsonPicture = object.getJSONObject("picture");
                                        if (jsonPicture.has("data")) {
                                            JSONObject jsonData = jsonPicture.getJSONObject("data");
                                            if (jsonData.has("url"))
                                                user.setPicture(jsonData.getString("url"));
                                        }
                                    }

                                    user.setUsername(Firstname + " " + Lastname);
                                    user.setEmail(FEmail);
                                    user.setLatLng(myLatLang);
                                    user.setAddress(address);
                                    user.setCountry(country);
                                    user.setCity(city);
                                    user.setOption("facebook");

                                    LoginManager.getInstance().logOut();

                                    login(user);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, first_name, last_name, email, gender, birthday, picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();

            }

            @Override
            public void onError(FacebookException error) {

            }

        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
        // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account){
        if(account != null){
            String id = account.getId();
            String name = account.getDisplayName();
            String personEmail = account.getEmail();
            String photo = "";
            try{
                photo = account.getPhotoUrl().toString();
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            user = new UserModel();
            user.setUsername(name);
            user.setEmail(personEmail);
            user.setPicture(photo);
            user.setAddress(address);
            user.setCountry(country);
            user.setCity(city);
            user.setLatLng(myLatLang);
            user.setOption("google");

            signOut();

            login(user);
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });

        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private static final int RC_SIGN_IN = 9001;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
        // The Task returned from this call is always completed, no need to attach
        // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void login(UserModel user){
        if (user.getEmail().equals("") || user.getEmail() == null)
        {
            showToast("Authorization failed");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        /// API call
        AndroidNetworking.post(ReqConst.SERVER + "register")
                .addBodyParameter("name", user.getUsername())
                .addBodyParameter("email", user.getEmail())
                .addBodyParameter("picture_url", user.getPicture())
                .addBodyParameter("address", user.getAddress())
                .addBodyParameter("country", country)
                .addBodyParameter("city", city)
                .addBodyParameter("latitude", user.getLatLng() == null? "": String.valueOf(user.getLatLng().latitude))
                .addBodyParameter("longitude", user.getLatLng() == null? "": String.valueOf(user.getLatLng().longitude))
                .addBodyParameter("option", user.getOption())
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        // do anything with response
                        Log.d("RegisterResponse=====>", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0") || result.equals("1")){
                                JSONObject userObj = response.getJSONObject("data");
                                UserModel userModel = new UserModel();
                                userModel.setId(userObj.getInt("id"));
                                userModel.setUsername(userObj.getString("name"));
                                userModel.setPhone(userObj.getString("phone_number"));
                                userModel.setEmail(userObj.getString("email"));
                                userModel.setPicture(userObj.getString("picture_url"));
                                userModel.setAddress(userObj.getString("address"));
                                userModel.setCountry(userObj.getString("country"));
                                userModel.setCity(userObj.getString("city"));
                                //userModel.setLatLng(new LatLng(userObj.getDouble("latitude"), userObj.getDouble("longitude")));
                                userModel.setLatLng(new LatLng(Double.parseDouble(userObj.getString("latitude")), Double.parseDouble(userObj.getString("longitude"))));
                                userModel.setOption(userObj.getString("status"));

                                EasyPreference.with(SigninActivity.this).addString("email", user.getEmail()).save();
                                Commons.thisUser = userModel;
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
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
                        progressBar.setVisibility(View.GONE);
                        // handle error
                        Log.d("RegisterError=====>", error.toString());
                        //showToast("Network issue");
                    }
                });

    }

}
















































