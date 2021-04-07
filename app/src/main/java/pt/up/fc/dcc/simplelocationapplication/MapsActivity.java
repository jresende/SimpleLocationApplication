package pt.up.fc.dcc.simplelocationapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import inconnu.anonymization.core.databases.dynamic.GeoIndistinguishabilityLaplaceCluster;
import inconnu.anonymization.core.databases.dynamic.utils.GeoLocationPoint;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MarkerOptions mMarkerOptions;
    private MarkerOptions mMarkerOptionsAnon;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean permissionCoarseLocation;

    Marker mMarker;
    Marker mMarkerAnon;

    RelativeLayout rl_layout;

    TextView tv_lat;
    TextView tv_lng;

    EditText et_interval;
    EditText et_fast_interval;

    int interval;
    int fast_interval;

    private boolean zoomed = false;



    GeoIndistinguishabilityLaplaceCluster geoIndistinguishabilityLaplaceCluster;


    private final String TAG = "SimpleLocationApp";

    private final int PERMISSION_REQUEST_LOCATION = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
         geoIndistinguishabilityLaplaceCluster = new GeoIndistinguishabilityLaplaceCluster(300);
        rl_layout = findViewById(R.id.rl_layout);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lng = findViewById(R.id.tv_lng);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        permissionCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(!permissionCoarseLocation){
            Log.d(TAG,"Missing ACCESS_COARSE_LOCATION permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
        et_interval = findViewById(R.id.et_interval);
        et_interval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @SuppressLint("MissingPermission")
            @Override
            public void afterTextChanged(Editable s) {
                if(!et_interval.getText().toString().isEmpty()) interval = Integer.parseInt(et_interval.getText().toString());
                else interval=10000;
                    locationRequest.setInterval(interval);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    et_interval.setCursorVisible(false);
                    Log.d(TAG, "Interval: " + locationRequest.getInterval());
            }
        });
        interval = Integer.parseInt(et_interval.getText().toString());
        et_fast_interval = findViewById(R.id.et_fast_interval);
        et_fast_interval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @SuppressLint("MissingPermission")
            @Override
            public void afterTextChanged(Editable s) {
                if(!et_fast_interval.getText().toString().isEmpty()) fast_interval = Integer.parseInt(et_fast_interval.getText().toString());
                else fast_interval=5000;
                locationRequest.setFastestInterval(fast_interval);
                fusedLocationClient.removeLocationUpdates(locationCallback);
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                Log.d(TAG, "Fast interval: " + locationRequest.getFastestInterval());
            }
        });
        fast_interval = Integer.parseInt(et_fast_interval.getText().toString());


        ((EditText) findViewById(R.id.anon_radius)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty() || Integer.parseInt(editable.toString())==0){
                    geoIndistinguishabilityLaplaceCluster = new GeoIndistinguishabilityLaplaceCluster(400);
                }else {
                    geoIndistinguishabilityLaplaceCluster = new GeoIndistinguishabilityLaplaceCluster(Integer.parseInt(editable.toString()));
                    Log.d(TAG,"Radius: "+Integer.parseInt(editable.toString()));
                }
            }

        });





        locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval)
                .setFastestInterval(fast_interval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult result) {
                LatLng lastLatLng = null;
                for(Location location: result.getLocations()){
                    lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                }
                if(lastLatLng != null){
                    tv_lat.setText("Lat: " + lastLatLng.latitude);
                    tv_lng.setText("Lng: " + lastLatLng.longitude);
                    GeoLocationPoint geoLocationPoint = geoIndistinguishabilityLaplaceCluster.sanitizeNewPoint(new GeoLocationPoint(lastLatLng.latitude, lastLatLng.longitude));
                    ((TextView) findViewById(R.id.anon_lat)).setText("Lat: "+geoLocationPoint.latitude);
                    ((TextView) findViewById(R.id.anon_lng)).setText("Lng: "+geoLocationPoint.longitude);

                    if(!zoomed){
                        Log.d(TAG,"First location");
                        mMarkerOptions = new MarkerOptions().position(lastLatLng).title("You are here!");
                        mMarker = mMap.addMarker(mMarkerOptions);
                        mMarker.showInfoWindow();

//
//                        mMarkerOptionsAnon = new MarkerOptions().position(new LatLng(geoLocationPoint.latitude,geoLocationPoint.longitude)).title("Anonymized Position");
//                        mMarkerAnon = mMap.addMarker(mMarkerOptionsAnon);
//                        mMarkerAnon.showInfoWindow();
//


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,17));
                        zoomed = true;
                    } else {
                        Location lastLoc = new Location("LastLoc");
                        lastLoc.setLatitude(mMarker.getPosition().latitude);
                        lastLoc.setLongitude(mMarker.getPosition().longitude);
                        Log.d(TAG,"Updating location");
                        mMarker.remove();
                        mMarkerOptions = new MarkerOptions().position(lastLatLng).title("You are here!");
                        mMarker = mMap.addMarker(mMarkerOptions);
                        mMarker.showInfoWindow();

//
//                        mMarkerAnon.remove();
//                        mMarkerOptionsAnon = new MarkerOptions().position(new LatLng(geoLocationPoint.latitude,geoLocationPoint.longitude)).title("Anonymized Position");
//                        mMarkerAnon = mMap.addMarker(mMarkerOptionsAnon);
//                        mMarkerAnon.showInfoWindow();




                        Location newLoc = new Location("NewLoc");
                        newLoc.setLatitude(lastLatLng.latitude);
                        newLoc.setLongitude(lastLatLng.longitude);
                        if(lastLoc.distanceTo(newLoc) >= 1000) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,17)); //Move camera if distance greater than 1000 meeter
                    }
                }

            }
        };
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null);
                } else {
                    Toast.makeText(this,"No permission for location", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
