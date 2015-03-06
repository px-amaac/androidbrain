package com.doubleacoding.socketiotest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Aaron on 3/1/2015.
 */
public class LocationService extends Service implements android.location.LocationListener {
    public static final String TAG = LocationService.class.getSimpleName();

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 66;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = TAG + "requesting-location-updates-key";
    protected final static String LOCATION_KEY = TAG + "location-key";
    public static final String FILE_KEY = TAG + "filename";
    public static final String DEFAULT_FILE_NAME = "dronebrainlocations.txt";
    private String mFileName = DEFAULT_FILE_NAME;

    private FileOutputStream fileOut;
    private JSONArray locations;
    private int currentUpdates = 0;
    private static final int MAX_UPDATES = 50;
    private LocationManager locationManager;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startLocationUpdates();
                }
            }).start();
        }
    }

    public LocationService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent.hasExtra(FILE_KEY)) {
            mFileName = intent.getStringExtra(FILE_KEY);
        }

        startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestingLocationUpdates = false;
        Log.i(TAG, "Service Created");
    }

    private void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        Log.i(TAG, "Starting Location Updates");
        mRequestingLocationUpdates = true;
        locations = new JSONArray();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        locationManager.removeUpdates(this);
        saveLocations();
        stopSelf();
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(":::::::::LOCATIONUPDATE:::::::", location.toString());
        JSONObject currentLocation = LocationJSONHelper.createJSONLocation(location.getLatitude(), location.getLongitude());
        mCurrentLocation = location;
        Log.d(TAG, "Current Updates: " + currentUpdates);
        if(currentLocation != null) {
            Log.d(TAG, "Put location into Array");
            locations.put(currentLocation);
        }
        if(currentUpdates++ > MAX_UPDATES)
        {
            Log.d(TAG, "StopLocationUpdates because we have what we needed.");
            stopLocationUpdates();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void saveLocations() {
        try {
            fileOut = openFileOutput(mFileName, Context.MODE_PRIVATE);
            OutputStreamWriter out = new OutputStreamWriter((fileOut));
            out.write(locations.toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }
}
