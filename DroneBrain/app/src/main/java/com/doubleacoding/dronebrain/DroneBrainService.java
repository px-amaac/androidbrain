package com.doubleacoding.dronebrain;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.doubleacoding.dronebrain.lib.KalmanLocationManager;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by Aaron on 4/25/2015.
 */
public class DroneBrainService extends Service {
    private static final String TAG = DroneBrainService.class.getSimpleName();

    private Socket mSocket;
    private final IBinder droneBind = new DroneBinder();
    private static final long GPS_TIME = 1000;
    private static final long NET_TIME = 5000;
    private static final long FILTER_TIME = 200;

    private KalmanLocationManager mKalmanLocationManager;
    private boolean mIsRequestingUpdates = false;

    private String mServerIpAddress = "192.168.0.1";
    private int mServerPort = 4242;
    private boolean mSocketConnected;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mSocket.connected()) {
                JSONObject message = LocationJSONHelper.createJSONLocation(location.getLatitude(), location.getLongitude());

                //this line sends the location as a JSONObject as the event 'location'
                mSocket.emit("location", message);
            }
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
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

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
        try {
            mSocket = IO.socket("http://" + mServerIpAddress + ":" + mServerPort);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mKalmanLocationManager.requestLocationUpdates(KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME, NET_TIME, mLocationListener, true);
        mIsRequestingUpdates = true;
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocketConnected = true;
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocketConnected = false;
            }

        });
        mSocket.connect();
        return Service.START_NOT_STICKY;
    }
    //emit the event takeoff on the socket to the server.
    public void takeoff() {
        if (mSocket.connected()) {
            mSocket.emit("takeoff", "TAKEOFFDRONE!!!");
        }
    }
    //emit the event calibrate on the socket to the server
    public void calibrate() {
        if (mSocket.connected()) {
            mSocket.emit("calibrate", "CALIBRATEDRONE!!!");
        }
    }

    //emit the event reset on the socket connection to the server
    public void reset() {
        if (mSocket.connected()) {
            mSocket.emit("testdata", "testdata!!!");
        }
    }
    //stop location updates and end the service.
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
