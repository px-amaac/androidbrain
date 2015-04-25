package com.doubleacoding.dronebrain;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LinearLayout takeoffLand = (LinearLayout) findViewById(R.id.takeofflandlayout);
        final LinearLayout calibrate = (LinearLayout) findViewById(R.id.calibratelayout);
        final LinearLayout reset = (LinearLayout) findViewById(R.id.resetlayout);
        final FloatingActionButton mainFab = (FloatingActionButton) findViewById(R.id.mainfab);
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(takeoffLand.getVisibility() == View.GONE){
                    takeoffLand.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.VISIBLE);
                    calibrate.setVisibility(View.VISIBLE);
                } else {
                    takeoffLand.setVisibility(View.GONE);
                    reset.setVisibility(View.GONE);
                    calibrate.setVisibility(View.GONE);
                }
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
