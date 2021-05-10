package com.sharemyfood.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iamhabib.easy_preference.EasyPreference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.commons.Constants;
import com.sharemyfood.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.INTERNET;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener{

    private static final String TAG = "MapsActivity Log ====>";
    public static final String INTENT_LOCATION_ADDRESSES = "INTENT_LOCATION_ADDRESSES";
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;


    private GoogleMap mMap;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private LocationListener mLocationListner;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 100000;  /* 10 secs */
    private long FASTEST_INTERVAL = 200000; /* 20 sec */

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private boolean isPermission;

    LatLng myLatLng;
    String myLatLngString;
    String myAddress = "";

    LatLng newLatLng;
    String newLatLngString;
    String newAddress = "";
    String country = "";
    String countryNew = "";
    String city = "";
    String cityNew = "";
    //private ArrayList<LatLng> latLngs = new ArrayList<>();/// for multi pickup

    SearchView searchView;

    ////////////////////////////////////////
    Marker myMarker = null;
    Marker newMarker = null;
    Circle circle = null;

    /////////////////////////////////
    String requestActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        if (requestSinglePermission()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            isLocationEnabled();

            searchView = (SearchView)findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    //Log.e("onQueryTextChange", "called");
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Do your task here
                    Log.d("Tapped on Search!!!", "Yes");
                    searchLocationFromAddress(query);
                    return false;
                }

            });

            ((RoundedImageView)findViewById(R.id.img_mylocation)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movoToMyLocation();
                }
            });

            //Intent intent = getIntent();
            //requestActivity = intent.getStringExtra("requestMap");


        }
    }

    private void searchLocationFromAddress(String q) {

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(q, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();

            newLatLng = new LatLng(latitude, longitude);
            newLatLngString = latitude + "," + longitude;
            newAddress = getAddressNew(latitude, longitude);

            countryNew = addresses.get(0).getCountryName();
            if (addresses.get(0).getLocality() == null)
            {
                cityNew = addresses.get(0).getAdminArea();
            } else {
                cityNew = addresses.get(0).getLocality();
            }

            addMarkers(newLatLng);
        }

    }


    public ArrayList<String> getPermissionList() {
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(INTERNET);
        permissions.add(ACCESS_WIFI_STATE);
        permissions.add(ACCESS_NETWORK_STATE);
        return permissions;
    }
    private boolean checkForPendingPermission() {
        permissionsToRequest = findUnAskedPermissions(getPermissionList());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                Log.d("checkPendingPermission", "All permissions not available.");
                return true;
            }
        }
        Log.d("checkPendingPermission", "All permissions available.");
        return false;
    }
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    //fetchLocation();
                }

                break;
        }
    }

    private void isLocationEnabled() {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && !mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            Log.d("Info+++", "Location enabled");
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationListner = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListner);
            }
            else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
//                                initCamera(latLng);

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
                        Log.d("INFO+++", "NETWORK Provider enabled");
                    }
                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "NETWORK Provider disabled");
                    }
                });
            }
            else if(mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                Log.d("Info+++", "Passive Location Provider enabled");
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
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
                        Log.d("INFO+++", "PASSIVE Provider enabled");
                    }
                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "PASSIVE Provider disabled");
                    }
                });
            }
        }
    }

    private void  movoToMyLocation(){

        if(newMarker != null){
            newMarker.remove();
            newLatLng = null;
        }

        if(myLatLng != null) {
            initCamera(myLatLng);
            //mLocationManager.removeUpdates((LocationListener) this);
        }

    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean requestSinglePermission() {

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermission = true;
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            isPermission = false;
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        return isPermission;
    }


    @OnClick(R.id.tv_submit)
    void onSubmit(){

        String tempAddress = "";
        ArrayList<String> intent_datas = new ArrayList<>();
        if (newLatLng == null)
        {
            intent_datas.add(String.valueOf(myLatLng.latitude));
            intent_datas.add(String.valueOf(myLatLng.longitude));
            intent_datas.add(myAddress);
            tempAddress = myAddress;
            intent_datas.add(country);
            intent_datas.add(city);
        } else {
            intent_datas.add(String.valueOf(newLatLng.latitude));
            intent_datas.add(String.valueOf(newLatLng.longitude));
            intent_datas.add(newAddress);
            tempAddress = newAddress;
            intent_datas.add(countryNew);
            intent_datas.add(cityNew);
        }
        if (tempAddress.equals("Undefined"))
        {
            showToast("Undefined Address\nTry another location.");
            return;
        }

        Log.d("8888888888888", country);
        Log.d("8888888888888", city);
        Log.d("8888888888888", countryNew);
        Log.d("8888888888888", cityNew);
        Intent intent = new Intent();
        intent.putStringArrayListExtra(INTENT_LOCATION_ADDRESSES, intent_datas);
        setResult(RESULT_OK, intent);

        View pinDialogView = LayoutInflater.from(this).inflate(R.layout.alert_setpickup_confirm, null, false);
        androidx.appcompat.app.AlertDialog pinDialog = new androidx.appcompat.app.AlertDialog.Builder(this).create();
        pinDialog.setView(pinDialogView);

        TextView txvMessage = (TextView) pinDialogView.findViewById(R.id.tv_alert_message);
        txvMessage.setText(tempAddress);
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
                finish();
            }
        });
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pinDialog.show();
    }

    @Override
    public void onClick(View view) {

    }


    public void addMarkers(LatLng pLatLng) {
        if(newMarker != null){
            newMarker.remove();
        }
        if(myLatLng!=null && myMarker == null) {
             myMarker = mMap.addMarker(new MarkerOptions().position(myLatLng).title(getAddress(myLatLng.latitude, myLatLng.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
             myMarker.setPosition(myLatLng);
        }

        if (pLatLng != null) {
            newMarker = mMap.addMarker(new MarkerOptions().position(pLatLng).title(getAddress(pLatLng.latitude, pLatLng.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.targetmarker)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pLatLng, mMap.getCameraPosition().zoom));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationListner = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationListner = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location == null) location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null) location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                    refreshMyMarker(latLng);
                    initCamera(latLng);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void refreshMyMarker(LatLng latLng) {
        drawCircle(latLng);
        if(myMarker == null)
            myMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        myLatLng = latLng;
        myAddress = getAddress(latLng.latitude, latLng.longitude);


        myMarker.setPosition(latLng);

        Commons.thisUser.setLatLng(myLatLng); Commons.thisUser.setAddress(myAddress);
        Log.d("MyLatLng+++", String.valueOf(myLatLng));

        if(Commons.mapCameraMoveF){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
        }
        Location location = new Location("Refreshed Location");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
    }

    private void drawCircle(LatLng latLng) {
        if(circle == null){
            try{
                LatLng loc = getCenterCoordinate(latLng);
                double radius = Constants.RADIUS;
                CircleOptions options = new CircleOptions();
                if(loc != null) {
                    options.center(loc);
                    //Radius in meters
                    options.radius(radius);
                    options.fillColor(getResources()
                            .getColor(R.color.circle_fill_color));
                    options.strokeColor(getResources()
                            .getColor(R.color.circle_stroke_color));
                    options.strokeWidth(2);
                    circle = mMap.addCircle(options);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }else circle.setCenter(latLng);
    }

    public LatLng getCenterCoordinate(LatLng latLng) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng);
        LatLngBounds bounds = builder.build();
        return bounds.getCenter();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

        newLatLng = new LatLng(latLng.latitude, latLng.longitude);
        newLatLngString = latLng.latitude + "," + latLng.longitude;
        newAddress = getAddressNew(latLng.latitude, latLng.longitude);
        //showSnackBar(getAddress(latLng.latitude, latLng.longitude), false);
        //latLngs.add(latLng); /// for multi pickup
        addMarkers(latLng);
        //onSearch();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);

        mMap.setMyLocationEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMapType(Commons.curMapTypeIndex);

        checkForLocationPermission();

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
                        Log.d("country", country);
                        if (addresses.get(0).getLocality() == null)
                        {
                            city = addresses.get(0).getAdminArea();
                        } else {
                            city = addresses.get(0).getLocality();
                        }
                    Log.d("city", city);


                    return temp;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "Undefined";
    }


    public String getAddressNew(double ltt, double lgt)
    {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(ltt, lgt, 1);
            if (addresses.isEmpty()) {

            }
            else {
                if (addresses.size() > 0) {

                    String temp = addresses.get(0).getAddressLine(0);


                    countryNew = addresses.get(0).getCountryName();
                    Log.d("countryNew", countryNew);
                    if (addresses.get(0).getLocality() == null)
                    {
                        cityNew = addresses.get(0).getAdminArea();
                    } else {
                        cityNew = addresses.get(0).getLocality();
                    }
                    Log.d("cityNew", cityNew);


                    return temp;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "Undefined";
    }

    @OnClick(R.id.img_setting)
    void onSettings(){
        Commons.googleMap = mMap;
        Intent intent = new Intent(getApplicationContext(), MapSettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.img_back)
    void onBack(){
        mLocationListner = null;
        finish();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /// device density getting
    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    ////////// camera Init
    private void initCamera(LatLng location) {
        CameraPosition position = CameraPosition.builder()
                .target(location)
                .zoom(20f)
                .bearing(30)                // Sets the orientation of the camera to east
                .tilt(30)
                .build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);
    }
    ////////FireBase use
    /*private void getAdminNotification(){
        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "verify" + "/" + String.valueOf(Commons.thisUser.get_idx()));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Map map = dataSnapshot.getValue(Map.class);
                try{
                    LayoutInflater inflater = getLayoutInflater();
                    View myLayout = inflater.inflate(R.layout.noti_alert_layout, null);
                    String noti = map.get("msg").toString();
                    String time = map.get("date").toString();
                    ((TextView)myLayout.findViewById(R.id.notiText)).setText(noti);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = dateFormat.format(new Date(Long.parseLong(time)));
                    ((TextView)myLayout.findViewById(R.id.date)).setText(date);
                    count++;
                    ((TextView)myLayout.findViewById(R.id.count)).setText(String.valueOf(count));
                    ((TextView)myLayout.findViewById(R.id.okButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            homeLayout.removeView(myLayout);
                            count--;
                            getMyBusinesses();
                            getMyProfile();
                        }
                    });
                    ((ImageView)myLayout.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSnapshot.getRef().removeValue();
                            homeLayout.removeView(myLayout);
                            count--;
                            getMyBusinesses();
                            getMyProfile();
                        }
                    });

                    homeLayout.addView(myLayout);
                    ShortcutBadger.applyCount(getApplicationContext(), 1);
                }catch (NullPointerException e){}
            }*/
}




























