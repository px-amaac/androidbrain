package com.doubleacoding.dronebrain;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.doubleacoding.dronebrain.lib.KalmanLocationManager;

import java.net.Socket;

/**
 * Created by Aaron on 4/25/2015.
 */
public class DroneBrainService extends Service{
    private static final String TAG = DroneBrainService.class.getSimpleName();

    private Socket mSocket;
    private final IBinder droneBind = new DroneBinder();
    private static final long GPS_TIME = 1000;
    private static final long NET_TIME = 5000;
    private static final long FILTER_TIME = 200;

    private KalmanLocationManager mKalmanLocationManager;
    private boolean mIsRequestingUpdates = false;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusString = "Unknown";

            switch (status) {

                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "Out of service";
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "Temporary unavailable";
                    break;

                case LocationProvider.AVAILABLE:
                    statusString = "Available";
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return droneBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mKalmanLocationManager = new KalmanLocationManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mKalmanLocationManager.requestLocationUpdates(KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME, NET_TIME, mLocationListener, true);
        mIsRequestingUpdates = true;
        return Service.START_NOT_STICKY;
    }

    public void takeoff() {

    }

    public void calibrate() {

    }

    public void reset() {

    }
    public void stop() {
        mKalmanLocationManager.removeUpdates(mLocationListener);
        stopSelf();
    }

    public class DroneBinder extends Binder {
        DroneBrainService getService() {
            return DroneBrainService.this;
        }
    }
}
