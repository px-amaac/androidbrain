package com.doubleacoding.dronebrain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;


public class MainActivity extends ActionBarActivity {

    //m-variables for service management.
    private DroneBrainService mDroneService;
    private Intent mDroneIntent;
    private boolean mDroneBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DroneBrainService.DroneBinder binder = (DroneBrainService.DroneBinder)service;
            mDroneService = binder.getService();
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
        if(mDroneIntent == null) {
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
        final LinearLayout takeoffLand = (LinearLayout) findViewById(R.id.takeofflandlayout);
        final LinearLayout calibrate = (LinearLayout) findViewById(R.id.calibratelayout);
        final LinearLayout reset = (LinearLayout) findViewById(R.id.resetlayout);
        final LinearLayout stopall = (LinearLayout) findViewById(R.id.stoplayout);
        final FloatingActionButton mainFab = (FloatingActionButton) findViewById(R.id.mainfab);
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(takeoffLand.getVisibility() == View.GONE){
                    takeoffLand.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.VISIBLE);
                    calibrate.setVisibility(View.VISIBLE);
                    stopall.setVisibility(View.VISIBLE);

                } else {
                    takeoffLand.setVisibility(View.GONE);
                    reset.setVisibility(View.GONE);
                    calibrate.setVisibility(View.GONE);
                    stopall.setVisibility(View.GONE);
                }
            }
        });
        FloatingActionButton stopButton = (FloatingActionButton) findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDroneService != null) {
                    mDroneService.stop();
                }
            }
        });
        FloatingActionButton calibrateButton = (FloatingActionButton) findViewById(R.id.calibrate);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDroneService.calibrate();
            }
        });
        FloatingActionButton resetButton = (FloatingActionButton) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDroneService.reset();
            }
        });
        FloatingActionButton takeoffButton = (FloatingActionButton) findViewById(R.id.takeofflandfab);
        takeoffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDroneService.takeoff();
            }
        });
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


}
