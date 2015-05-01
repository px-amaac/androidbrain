package com.doubleacoding.dronebrain;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

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
    private static final int MINACCURACY = 8;

    private KalmanLocationManager mKalmanLocationManager;
    private boolean mIsRequestingUpdates = false;

    private String mServerIpAddress = "192.168.0.1";
    private int mServerPort = 4242;
    private boolean mSocketConnected;
    private NavDataUpdate navDataUpdate;
    boolean mUsingGPSKalmanFilter;
    private LocationManager mLocationManager;

    public interface NavDataUpdate {
        public void updateNavData(JSONObject navData);
        public void updateLocalPos(JSONObject localprose);
    }

    //this is used to set the nave data update interface
    public void setNavDataInterface(NavDataUpdate updateInterface){
        navDataUpdate = updateInterface;
    }

    public void toggleUsingKalmanFilter() {
        if(mIsRequestingUpdates){
            if(mKalmanLocationManager != null) {
                Log.i(TAG, "Using Location Manager");
                mKalmanLocationManager.removeUpdates(mLocationListener);
                mKalmanLocationManager = null;
                mUsingGPSKalmanFilter = false;
                getLocationManager();
            } else if(mLocationManager != null) {
                Log.i(TAG, "Using Kalman Location Manager");
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager = null;
                mUsingGPSKalmanFilter = true;
                getKalmanLocationManager();
            }
        }
    }

    public boolean isUsingKalmanFilter() {
        return mUsingGPSKalmanFilter;
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mSocket.connected() && location.getAccuracy() < MINACCURACY) {
                JSONObject message = LocationJSONHelper.createJSONLocation(location.getLatitude(), location.getLongitude());
                Log.i(TAG, "Accuracy " + location.getAccuracy());
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
        stop();
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
        mIsRequestingUpdates = false;
        try {
            mSocket = IO.socket("http://" + mServerIpAddress + ":" + mServerPort);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        setupSocketConnection();
        return Service.START_NOT_STICKY;
    }

    private void startLocationUpdates() {
        if(mUsingGPSKalmanFilter) {
            getKalmanLocationManager();
        } else {
            getLocationManager();
        }
        mIsRequestingUpdates = true;
    }

    private void getLocationManager() {
        if(mLocationManager == null) {
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    private void getKalmanLocationManager() {
        if(mKalmanLocationManager == null){
            mKalmanLocationManager = new KalmanLocationManager(this);
        }
        mKalmanLocationManager.requestLocationUpdates(KalmanLocationManager.UseProvider.GPS_AND_NET,
                FILTER_TIME,
                GPS_TIME,
                NET_TIME,
                mLocationListener,
                true);
    }

    private void setupSocketConnection() {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocketConnected = true;
            }

        }).on("dronedata", new Emitter.Listener(){

            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if(navDataUpdate != null)
                    navDataUpdate.updateNavData(data);
                Log.i(TAG, data.toString());

            }
        }).on("localxy", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                if(navDataUpdate != null) {
                    navDataUpdate.updateLocalPos(data);
                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocketConnected = false;
            }

        });
        mSocket.connect();
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
            mSocket.emit("reset", "RESET!!!");
        }
    }
    public void forward() {
        if (mSocket.connected()) {
            mSocket.emit("forward", "FORWARD");
        }
    }
    public void backward() {
        if (mSocket.connected()) {
            mSocket.emit("backward", "BACKWARD");
        }
    }
    public void left() {
        if (mSocket.connected()) {
            mSocket.emit("left", "LEFT");
        }
    }
    public void right() {
        if (mSocket.connected()) {
            mSocket.emit("right", "RIGHT!!!");
        }
    }
    public void clockwise() {
        if (mSocket.connected()) {
            mSocket.emit("clockwise", "CLOCKWISE!!!");
        }
    }
    public void up() {
        if (mSocket.connected()) {
            mSocket.emit("up", "UP!!!");
        }
    }
    public void down() {
        if (mSocket.connected()) {
            mSocket.emit("down", "DOWN!!!");
        }
    }
    public void zero() {
        if (mSocket.connected()) {
            mSocket.emit("zero", "ZERO!!");
        }
    }

    public void followMe() {
        if (mSocket.connected()) {
            mSocket.emit("follow", "FOLLOW!!");
        }
    }

    //stop location updates and end the service.
    public void stop() {
        if(mIsRequestingUpdates) {
            if(mKalmanLocationManager != null){
                mKalmanLocationManager.removeUpdates(mLocationListener);
            } else if(mLocationManager != null) {
                mLocationManager.removeUpdates(mLocationListener);
            }
            mIsRequestingUpdates = false;
        }
        if(mSocket.connected()) {
            mSocket.emit("stop", "STOP!!!!");
        }
        stopSelf();
    }


    public class DroneBinder extends Binder {
        DroneBrainService getService() {
            return DroneBrainService.this;
        }
    }
}
