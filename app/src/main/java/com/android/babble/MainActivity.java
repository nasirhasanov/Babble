package com.android.babble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.babble.activity.AddPostActivity;
import com.android.babble.activity.AllowLocationActivity;
import com.android.babble.activity.SignInActivity;
import com.android.babble.fragment.HomeFragment;
import com.android.babble.fragment.LoadingFragment;
import com.android.babble.fragment.CreateProfileFragment;
import com.android.babble.fragment.ProfileFragment;
import com.android.babble.helper.Constants;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    private BottomNavigationView bottomNavigation;

    private SharedPreferences sharedpreferences;

    private boolean isLocationAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        if (!checkPermissions()){
            startActivity(new Intent(MainActivity.this, AllowLocationActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                    startActivity(new Intent(MainActivity.this, AddPostActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }
            }
        });

        sharedpreferences = getSharedPreferences(Constants.SHARED_PREFS_LOCATION_SETTINGS, Context.MODE_PRIVATE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        bottomNavigation.setSelectedItemId(R.id.bottom_navigation_home);


        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                List<Address> address = null;

                try{
                    address = geocoder.getFromLocation(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(), 1);
                }catch (Exception exception){
                    if (sharedpreferences.getString("country_code",null)!=null){
                        return;
                    }else{
                        finish();
                    }
                }
                if (address != null) {
                    String countryCode = address.get(0).getCountryCode();
                    String countryName = address.get(0).getCountryName();
                    String subAdminArea = address.get(0).getSubAdminArea();
                    String local = address.get(0).getLocality();
                    String fullAddress = subAdminArea+","+local;
                    double latitude = currentLocation.getLatitude();
                    double longitude = currentLocation.getLongitude();

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("country_code", countryCode);
                    editor.putString("country_name", countryName);
                    editor.putString("full_address",fullAddress);
                    editor.putString("latitude", String.valueOf(latitude));
                    editor.putString("longitude", String.valueOf(longitude));
                    editor.apply();

                    stopLocationUpdates();
                    if (!isLocationAvailable) {
                        replaceFragment(new HomeFragment());
                        bottomNavigation.setVisibility(View.VISIBLE);
                    }
                }

                }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                finish();
                            }
                        }
                    }
                });
    }

    public void stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_navigation_home:
                            if (sharedpreferences.getString("country_code",null)!=null){
                                isLocationAvailable = true;
                                replaceFragment(new HomeFragment());
                            }else{
                                replaceFragment(new LoadingFragment());
                                bottomNavigation.setVisibility(View.GONE);
                            }
                            return true;

                        case R.id.bottom_navigation_profile:
                            if (FirebaseAuth.getInstance().getCurrentUser()==null){
                            replaceFragment(new CreateProfileFragment());}
                            else{replaceFragment(new ProfileFragment());}
                            return true;
                    }
                    return false;
                }
            };

    private void replaceFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newFragment)
                .commit(); }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}
