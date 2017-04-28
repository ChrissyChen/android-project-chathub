package edu.sfsu.csc780.chathub;

import android.content.Context;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import edu.sfsu.csc780.chathub.ui.MainActivity;

/**
 * Created by Xinlu Chen on 4/2/17.
 */

public class LocationUtils {

    private static final String LOG_TAG = LocationUtils.class.getSimpleName();
    private static String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static int GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int REQUEST_CODE = 100;
    private static final int MIN_TIME = 6000;
    private static final int MIN_DISTANCE = 10;
    private static final String[] LOCATION_PERMISSIONS =
            {FINE_LOCATION, COARSE_LOCATION};
    private static Location sLocation;
    private static LocationListener sLocationListener;

    public static double getLat() {
        return (sLocation != null) ? sLocation.getLatitude() : 0.0;
    }

    public static double getLon() {
        return (sLocation != null) ? sLocation.getLongitude() : 0.0;
    }

    public static void startLocationUpdates(Activity activity) {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager =
                (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (sLocationListener == null) {
            // Define a listener that responds to location updates
            sLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    Log.d(LOG_TAG, "Location changed! lat: " + location.getLatitude()
                            + " lon: " + location.getLongitude());
                    sLocation = location;
                }

                public void onStatusChanged(String provider, int status, Bundle
                        extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
        }

        if (!checkLocationPermission(activity)) {
            return;
        }


        Log.d(LOG_TAG, "CLASS locationUtils  startLocationUpdate()");
        Log.d(LOG_TAG, "requesting updates");
        Location location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            Log.d(LOG_TAG, "last known lat: " + location.getLatitude()
                    + " lon: " + location.getLongitude());
            sLocation = location;
        }
        Log.d(LOG_TAG, "requesting updates");
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME,
                MIN_DISTANCE, sLocationListener);

//        MainActivity ma = new MainActivity();
//        ma.loadMap();
    }

    public static boolean checkLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, FINE_LOCATION) !=
                GRANTED && ActivityCompat.checkSelfPermission(activity,
                COARSE_LOCATION) != GRANTED) {
            Log.d(LOG_TAG, "requesting permissions for starting");
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_CODE);
            return false;
        }
        return true;
    }
}
