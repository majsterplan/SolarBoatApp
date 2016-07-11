package com.example.marcin.solarboatapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.Locale;

/**
 * GpsService Class created by Marcin on 2016-04-01.
 */
public class GpsService extends Service {
    public final static int GPS_ERROR = -1;
    public final static int GPS_ON = 1;
    public final static int GPS_OFF = 0;

    private Context appContext;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public GpsService(Context context) {
        appContext = context;
    }

    @Override
    public StartUpStatus start() {
        StartUpStatus startUpStatus;
        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            status = false;
            startUpStatus = StartUpStatus.NO_SUPPORT;
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        Intent intent = new Intent();
                        intent.setAction("INCOMING_FRAME");
                        intent.putExtra("frame", "[1.1|" + String.format(Locale.US, "%.1f", location.getSpeed()) + "]");
                        appContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    if (provider.equals(LocationManager.GPS_PROVIDER)) {
                        Intent intent = new Intent();
                        intent.setAction("GPS_STATE_CHANGED");
                        intent.putExtra("state", GPS_ON);
                        appContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    if (provider.equals(LocationManager.GPS_PROVIDER)) {
                        Intent intent = new Intent();
                        intent.setAction("GPS_STATE_CHANGED");
                        intent.putExtra("state", GPS_OFF);
                        appContext.sendBroadcast(intent);
                    }
                }
            };
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                status = false;
                startUpStatus = StartUpStatus.NEED_USER_INTERACTION;
            } else {
                status = true;
                startUpStatus = StartUpStatus.ON;
            }
        }
        return startUpStatus;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}
