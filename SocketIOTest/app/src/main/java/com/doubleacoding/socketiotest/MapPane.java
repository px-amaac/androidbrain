package com.doubleacoding.socketiotest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Aaron on 3/1/2015.
 */
public class MapPane extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        googleMap.setMyLocationEnabled(true);
        JSONArray locations = getLocationsFromFile();
        LatLng lastPosition = null;
        for(int i = 0; i < locations.length(); i++) {
            JSONObject location = null;
            try {
                location = locations.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            lastPosition = new LatLng(location.optDouble(LocationJSONHelper.LAT_TAG), location.optDouble(LocationJSONHelper.LON_TAG));
            googleMap.addMarker(makeMarker(lastPosition));
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 18));
    }

    private MarkerOptions makeMarker(LatLng position) {
        return new MarkerOptions()
                .anchor(0.0f, 1.0f)
                .position(position);

    }

    private JSONArray getLocationsFromFile() {
        String s = "";
        try {
            FileInputStream fileIn = openFileInput(LocationService.DEFAULT_FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[100];
            int charRead;
            while((charRead = inputReader.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s += readString;
            }
            inputReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray result = null;
        try {
            result = new JSONArray(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
