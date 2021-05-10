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

public class MapsMiniActivity extends BaseActivity implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener {

    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;


    private GoogleMap mMap;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private com.google.android.gms.location.LocationListener listener;

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private boolean isPermission;

    LatLng myLatLng;
    Marker myMarker = null;
    Marker newMarker = null;
    Circle circle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_mini);

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


        }
        ((RoundedImageView) findViewById(R.id.img_mylocation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movoToMyLocation();
            }
        });
    }

    public ArrayList<String> getPermissionList() {
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(INTERNET);
        permissions.add(ACCESS_WIFI_STATE);
        permissions.add(ACCESS_NETWORK_STATE);
        return permissions;
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsMiniActivity.this)
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
                }

                break;
        }
    }

    private void movoToMyLocation() {

        if (myLatLng != null) {
            initCamera(myLatLng);
            //mLocationManager.removeUpdates((LocationListener) this);
        }

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


    @Override
    public void onClick(View view) {

    }


    public void addMarkers(LatLng pLatLng) {

        if (pLatLng != null) {
            newMarker = mMap.addMarker(new MarkerOptions().position(pLatLng).title(Commons.food.getAdress()).icon(BitmapDescriptorFactory.fromResource(R.drawable.targetmarker)));
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
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                    setMyMarker(latLng);
                    initCamera(latLng);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setMyMarker(LatLng latLng) {
        drawCircle(latLng);
        if (myMarker == null)
            myMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        Commons.thisUser.setLatLng(latLng);
        myLatLng = latLng;
        Log.d("MyLatLng+++", String.valueOf(myLatLng));
        if (Commons.mapCameraMoveF) { ////move position with me
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
        }
        Location location = new Location("Refreshed Location");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        myMarker.setPosition(latLng);
    }

    private void drawCircle(LatLng latLng) {
        if (circle == null) {
            try {
                LatLng loc = getCenterCoordinate(latLng);
                double radius = Constants.RADIUS;
                CircleOptions options = new CircleOptions();
                if (loc != null) {
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
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else circle.setCenter(latLng);
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
        LatLng temp = new LatLng(Commons.food.getLatLng().latitude, Commons.food.getLatLng().longitude);
        addMarkers(temp);
        initCamera(temp);
        if (Commons.mapCameraMoveF) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, mMap.getCameraPosition().zoom));
        }

    }

    @OnClick(R.id.img_setting)
    void onSettings() {
        Commons.googleMap = mMap;
        Intent intent = new Intent(getApplicationContext(), MapSettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.img_back)
    void onBack() {
        finish();
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
}






