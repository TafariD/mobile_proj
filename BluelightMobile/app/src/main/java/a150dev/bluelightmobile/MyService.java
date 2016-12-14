package a150dev.bluelightmobile;

/**
 * Created by Tafari on 11/11/2016.
 *
 * Based on code from StackOverflow accessed 11/11/2016
 * http://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
 *
 * Code to send location data to web server provided on 12/8/2016 by Zabir Islam
 *
 */

import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
        import android.location.LocationManager;
        import android.os.Bundle;
import android.os.IBinder;
import android.os.NetworkOnMainThreadException;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;


public class MyService extends Service
{
    public static final String TAG = MyService.class.getSimpleName();
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 70000;
    private static final float LOCATION_DISTANCE = 25f;
    private LatLng latLng;
    private String phone1, phone2, phone3;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        int interval = 8000;

        phone1 = intent.getStringExtra("phone1");
        phone2 = intent.getStringExtra("phone2");
        phone3 = intent.getStringExtra("phone3");

        //For debugging
        //Log.d("Sending to Alarm:", phone1);
        //Log.d("\nSending to Alarm:", phone1);
        //Log.d("\nSending to Alarm:", phone1 + "\n");

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        //Log.d(TAG, "onCreate");
        Fabric.with(this, new Crashlytics());
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            //Log.d(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            //Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            //Log.d(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            //Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


    }

    @Override
    public void onDestroy()
    {
        //Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (SecurityException e) {
                    //Log.i(TAG, "Did they give permission for location?");
                    //Log.i(TAG, e.toString());
                } catch (Exception ex) {
                    //Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        Toast.makeText(MyService.this,
                "Location Transmission Suspended",
                Toast.LENGTH_SHORT)
                .show();
    }

    /*------------------------------------------------------------------------*/
    /*-----------------------LOCATION CODE START------------------------------*/
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            //Log.d(TAG, "LocationListener" + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            //Log.d(TAG, "onLocationChanged: " + location);

            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            latLng = new LatLng(currentLatitude, currentLongitude);
            String street_address = getCompleteAddressString(latLng.latitude, latLng.longitude);
            //Log.d(TAG, "onLocationChanged: " + street_address);
            mLastLocation.set(location);

            SmsManager smsManager = SmsManager.getDefault();
            String text;

            if (street_address == null) {
                text = "Sending Location \n(" + "Lat " + latLng.latitude + "  "
                        + "Long " + latLng.longitude + ")\n to Emergency Services";

                Toast.makeText(MyService.this, text, Toast.LENGTH_LONG).show();

                text = "Current Location \n(" + "http://maps.google.com/?q=" +
                        latLng.latitude + "," + latLng.longitude +
                        ")";

                if (phone1 != null && !phone1.equals("")) {
                    smsManager.sendTextMessage(phone1, null, text, null, null);
                }
                if (phone2 != null && !phone2.equals("")) {
                    smsManager.sendTextMessage(phone2, null, text, null, null);
                }
                if (phone3 != null && !phone3.equals("")) {
                    smsManager.sendTextMessage(phone3, null, text, null, null);
                }

            } else {

                text = "Sending Location (" + street_address + ") to Emergency Services";

                Toast.makeText(MyService.this, text, Toast.LENGTH_LONG).show();

                text = "Current Location (" + street_address + "). \n See map here:"
                        + " \n(" + "http://maps.google.com/?q=" + latLng.latitude
                        + "," + latLng.longitude + ")";

                if (phone1 != null && !phone1.equals("")) {
                    smsManager.sendTextMessage(phone1, null, text, null, null);
                }
                if (phone2 != null && !phone2.equals("")) {
                    smsManager.sendTextMessage(phone2, null, text, null, null);
                }
                if (phone3 != null && !phone3.equals("")) {
                    smsManager.sendTextMessage(phone3, null, text, null, null);
                }
            }


            /* Currently this code is commented out for privacy concerns
               Next update will contain working version of this code and
               an updated privacy policy.

            JSONObject locationn = new JSONObject();
            try {
                locationn.put("lat", latLng.latitude);
                locationn.put("lng", latLng.longitude);
                postData(location.toString());
            } catch (JSONException e) {

               //Log.e(TAG, e.toString());
            } */


        }

        @Override
        public void onProviderDisabled(String provider)
        {
           //Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            //Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                strReturnedAddress.append(returnedAddress.getAddressLine(0)).append(", ");
                strReturnedAddress.append(returnedAddress.getLocality()).append(", ");
                strReturnedAddress.append(returnedAddress.getAdminArea()).append(" ");

                strAdd = strReturnedAddress.toString();
                //Log.w("Current location", "" + strReturnedAddress.toString());
            } else {
               //Log.w("Current location", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("Current location", "Cannot get Address!");
        }
        return strAdd;
    }

    /*------------------------------------------------------------------------*/
    /*------------------------LOCATION CODE END-------------------------------*/
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/



    /*------------------------------------------------------------------------*/
    /*------------------------SERVER CODE START-------------------------------*/
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/

    /*
    public void postData(String location) {
        HttpClient postClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("https://bluelightmobile.herokuapp.com/");

        try{

            List nameValuePairs = new ArrayList();
            nameValuePairs.add(new BasicNameValuePair("location", location));
            postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = postClient.execute(postRequest);

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        } catch (NetworkOnMainThreadException msg) {
            //Log.e(TAG,msg.toString());
        }
    } */

    /*------------------------------------------------------------------------*/
    /*-------------------------SERVER CODE END--------------------------------*/
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/



}