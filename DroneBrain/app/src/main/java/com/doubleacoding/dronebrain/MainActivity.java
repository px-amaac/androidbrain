package com.doubleacoding.dronebrain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.melnykov.fab.FloatingActionButton;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity implements DroneBrainService.NavDataUpdate {
    @InjectView(R.id.flyingstatedata) TextView mFlyingState;
    @InjectView(R.id.batterypercentagedata) TextView mBatteryPercent;
    @InjectView(R.id.rotationdata) TextView mRotation;
    @InjectView(R.id.velocitydata) TextView mVelocity;
    @InjectView(R.id.magnetodata) TextView mMagneto;
    @InjectView(R.id.gpsdata) TextView mGPS;
    @InjectView(R.id.xpos) TextView mXPos;
    @InjectView(R.id.ypos) TextView mYPos;

    @InjectView(R.id.takeofflandlayout) LinearLayout takeoffLand;
    @InjectView(R.id.calibratelayout) LinearLayout calibrate;
    @InjectView(R.id.resetlayout) LinearLayout reset;
    @InjectView(R.id.stoplayout) LinearLayout stopall;
    @InjectView(R.id.buttonpanel) LinearLayout buttonPanel;
    @InjectView(R.id.buttonpanel2) LinearLayout buttonPanel2;
    @InjectView(R.id.navdatapanel) LinearLayout navDataPanel;

    //m-variables for service management.
    private DroneBrainService mDroneService;
    private Intent mDroneIntent;
    private boolean mDroneBound;
    private boolean mIsUsingKalman;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DroneBrainService.DroneBinder binder = (DroneBrainService.DroneBinder) service;
            mDroneService = binder.getService();
            mDroneService.setNavDataInterface(MainActivity.this);
            mIsUsingKalman = mDroneService.isUsingKalmanFilter();
            mDroneBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDroneBound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mDroneIntent == null) {
            mDroneIntent = new Intent(this, DroneBrainService.class);
        }
        bindService(mDroneIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mDroneIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    public void onToggleClicked(View v) {
        boolean on = ((ToggleButton) v).isChecked();
        if(mDroneService != null) {
            mDroneService.toggleUsingKalmanFilter();
            mIsUsingKalman = !mIsUsingKalman;
        }
    }

    @OnClick(R.id.mainfab)
    public void mainFab(){
        if (buttonPanel2.getVisibility() == View.GONE) {
            buttonPanel2.setVisibility(View.VISIBLE);
            buttonPanel.setVisibility(View.VISIBLE);
            navDataPanel.setVisibility(View.GONE);
        } else {
            buttonPanel2.setVisibility(View.GONE);
            buttonPanel.setVisibility(View.GONE);
            navDataPanel.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.stop)
    public void stop(){
        if (mDroneService != null) {
            mDroneService.stop();
        }
    }

    @OnClick(R.id.calibrate)
    public void calibrate(){
        if (mDroneService != null) {
            mDroneService.calibrate();
        }
    }
    @OnClick(R.id.reset)
    public void reset(){
        if (mDroneService != null) {
            mDroneService.reset();
        }
    }
    @OnClick(R.id.takeofflandfab)
    public void takeoffland(){
        if (mDroneService != null) {
            mDroneService.takeoff();
        }
    }
    @OnClick(R.id.forwardfab)
    public void forward(){
        if (mDroneService != null) {
            mDroneService.forward();
        }
    }
    @OnClick(R.id.backwardfab)
    public void backward(){
        if (mDroneService != null) {
            mDroneService.backward();
        }
    }
    @OnClick(R.id.leftfab)
    public void left(){
        if (mDroneService != null) {
            mDroneService.left();
        }
    }
    @OnClick(R.id.rightfab)
    public void right(){
        if (mDroneService != null) {
            mDroneService.right();
        }
    }
    @OnClick(R.id.upfab)
    public void up(){
        if (mDroneService != null) {
            mDroneService.up();
        }
    }
    @OnClick(R.id.downfab)
    public void down(){
        if (mDroneService != null) {
            mDroneService.down();
        }
    }
    @OnClick(R.id.clockwisefab)
    public void clockwise(){
        if (mDroneService != null) {
            mDroneService.clockwise();
        }
    }
    @OnClick(R.id.zerofab)
    public void zero(){
        if (mDroneService != null) {
            mDroneService.zero();
        }
    }
    @OnClick(R.id.followme)
    public void followMe(){
        if (mDroneService != null) {
            mDroneService.followMe();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: this method could use some cleanup. The final navdata that will be presented is still unknown so until I have narrowed down the data to what I want this method will stay the same.
    @Override
    public void updateNavData(final JSONObject navData) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (navData.has("demo")) {
                    JSONObject demo = navData.optJSONObject("demo");
                    mFlyingState.setText(demo.optString("flyState"));
                    mBatteryPercent.setText(demo.optString("batteryPercentage"));
                    JSONObject rotation = demo.optJSONObject("rotation");
                    mRotation.setText(
                            " y= " + rotation.optString("y") +
                                    "\nroll= " + rotation.optString("roll") +
                                    "\nx= " + rotation.optString("x") +
                                    "\nclockwise= " + rotation.optString("clockwise") +
                                    "\nyaw= " + rotation.optString("yaw")
                    );
                    JSONObject velocity = demo.optJSONObject("velocity");
                    mVelocity.setText(
                            " x= " + velocity.optString("x") +
                                    "\ny= " + velocity.optString("y") +
                                    "\nz= " + velocity.optString("z")
                    );
                }
                if (navData.has("magneto")) {
                    JSONObject magneto = navData.optJSONObject("magneto");
                    JSONObject heading = magneto.optJSONObject("heading");
                    JSONObject offset = magneto.optJSONObject("offset");
                    JSONObject rectified = magneto.optJSONObject("rectified");

                    mMagneto.setText(
                            " mx= " + magneto.optString("mx") +
                                    "\nmy= " + magneto.optString("my") +
                                    "\nmz= " + magneto.optString("mz") +
                                    "\nradius= " + magneto.optString("radius") +
                                    "\nheading= " + heading.optString("fusionUnwrapped") +
                                    "\nrectifiedx= " + rectified.optString("x") +
                                    "\nrectifiedy= " + rectified.optString("y") +
                                    "\nrectifiedz= " + rectified.optString("z") +
                                    "\noffestx= " + offset.optString("x") +
                                    "\noffesty= " + offset.optString("y") +
                                    "\noffestz= " + offset.optString("z")

                    );
                }
                if (navData.has("gps")) {
                    JSONObject gps = navData.optJSONObject("gps");
                    mGPS.setText(
                            " latitude= " + gps.optString("latitude") +
                                    "\nlongitude= " + gps.optString("longitude") +
                                    "\nelevation= " + gps.optString("elevation") +
                                    "\ndataAvailable= " + gps.optString("dataAvailable") +
                                    "\ndegree= " + gps.optString("degree")
                    );
                }
            }
        });
    }

    //This method updates the local position. It tells me where the drone Thinks I am in relation to it.
    @Override
    public void updateLocalPos(final JSONObject localprose) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mXPos.setText(localprose.optString("x"));
                mYPos.setText(localprose.optString("y"));
            }
        });
    }
}
