package com.mualab.org.user.activity.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.utils.LocationDetector;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_splash);
        // Session session = Mualab.getInstance().getSessionManager();
        // startActivity(new Intent(SplashActivity.this, LoginActivity.class));

        if (Mualab.getInstance().getSessionManager() != null) {

            if (Mualab.getInstance().getSessionManager().isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                //updateLocation();
                Log.d("checkEnterstatus","MainActivity");
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                Log.d("checkEnterstatus","LoginActivity111");
            }

            finish();

        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            Log.d("checkEnterstatus","LoginActivity222");
            finish();
        }

    }

    private void updateLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationDetector locationDetector = new LocationDetector();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(SplashActivity.this);
        if (locationDetector.isLocationEnabled(SplashActivity.this) &&
                locationDetector.checkLocationPermission(SplashActivity.this)) {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(SplashActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Mualab.currentLocation.lat = latitude;
                        Mualab.currentLocation.lng = longitude;

                        Mualab.currentLocationForBooking.lat = latitude;
                        Mualab.currentLocationForBooking.lng = longitude;




                    }
                }
            });

        }


    }

}
