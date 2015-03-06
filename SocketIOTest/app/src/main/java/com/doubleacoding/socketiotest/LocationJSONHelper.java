package com.doubleacoding.socketiotest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aaron on 3/3/2015.
 */
public class LocationJSONHelper {
    public static final String LAT_TAG = "LATITUDE";
    public static final String LON_TAG = "LONGITUDE";

    static JSONObject createJSONLocation(double lat, double lon) {
        JSONObject result = new JSONObject();
        try {
            result.put(LAT_TAG, lat);
            result.put(LON_TAG, lon);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
