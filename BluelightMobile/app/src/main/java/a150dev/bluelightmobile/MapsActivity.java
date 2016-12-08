package a150dev.bluelightmobile;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//Many thanks to this tutorial http://blog.teamtreehouse.com/beginners-guide-location-android
//Which got us started on implementing the location part.


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Creating global variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng latLng;
    private int REQUEST_LOCATION = 0;
    private int REQUEST_CALL_PHONE = 0;
    private int REQUEST_SEND_SMS = 0;
    private boolean active = false;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; //For errors
    private Button Signal_Button;
    private Handler mUiHandler = new Handler();
    private String phone1, phone2, phone3;
    private MyWorkerThread mWorkerThread;

    @Override //Initalizing everyting.
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Signal_Button = (Button) findViewById(R.id.doButton);
        Signal_Button.setBackgroundColor(Color.GREEN);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            phone1 = extras.getString("phone1");
            phone2 = extras.getString("phone2");
            phone3 = extras.getString("phone3");

            Log.w("Phone 1 is ", phone1);
            Log.w("\nPhone 2 is ", phone2);
            Log.w("\nPhone 3 is ", phone3 + "\n");
            //The key argument here must match that used in the other activity
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        //This gets the Google API ready and running to start collecting the location.
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS);
        }

    }

    @Override
    protected void onDestroy() {
        if (mWorkerThread != null) {
            mWorkerThread.quit();
        }
        super.onDestroy();
    }


    @Override  //Reconnect location services on app open.
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }  //When the app is running properly in the background, this will be redundant or at least
    //Limited so that it only runs when the signal to TUPD is not triggered.


    @Override //Disconnect everything when the app is paused.
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mGoogleApiClient.connect();
    }

    @Override //Actions taken when location connection is established.
    public void onConnected(Bundle bundle) {

        Location location = null;
        //Wrapped in a try/catch in case location throws an exeception for some reason.
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            } else {
                // permission has been granted, continue as usual
                location =
                        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }

            Log.i(TAG, "Location services connected.");

            //Just in case the phone didn't already have a past location stored, we prepare to call
            //get an updated one.
            if (location == null) {
                Log.i(TAG, "Requesting location...");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } else {
                handleNewLocation(location);
            }
            ;

        } catch (SecurityException e) {
            Log.i(TAG, "There is a problem with the GPS!");
            Log.i(TAG, e.getMessage());

        }
    }

    @Override // This is if they suspend the service while the app is running.
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override //If we can't connect to GPS at all for some reason (but no crazy errors were
    // detected), we handle it here.
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override //Make sure we update the map when the location changes.
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    // This is where we update the map once a new location is received.
    private void handleNewLocation(Location location) {

        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                try {
                    Location myLocation =
                            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                } catch (SecurityException e) {
                    Log.i(TAG, e.getMessage());
                }
            } else {
                // Permission was denied or request was cancelled
                Log.i(TAG, "User said no!");
            }
        } else if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                Context context = getApplicationContext();
                CharSequence text = "You can now contact TUPD via the signal button.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            } else {
                // Permission was denied or request was cancelled
                Log.i(TAG, "User said no phone access!");
            }
        } else if (requestCode == REQUEST_SEND_SMS){
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                Context context = getApplicationContext();
                CharSequence text = "We will now alert your emergency contacts via the signal button.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            } else {
                // Permission was denied or request was cancelled
                Log.i(TAG, "User said no phone sms access!");
            }
        }
    }

    // For now, this simple logs the location to the screen.
    public void doActivity(View view) {

        Log.i(TAG, "IN DO ACTIVITY!");

        boolean isThreadRunning;

        Context context = getApplicationContext();
        CharSequence text;
        if (!active) {
            Signal_Button.setText("Stop Signaling");
            Signal_Button.setBackgroundColor(Color.RED);
            text = "Signal Recieved! Starting transmission";
            active = !active;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PHONE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_SEND_SMS);
            }

            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:17155720662")));

            String msg = "I'M IN DANGER! I've called TUPD.";

            SmsManager smsManager = SmsManager.getDefault();

            Log.i(TAG, "IN SENDING SMS IN ACTIVITY!");

            if (!phone1.equals("")) {
                smsManager.sendTextMessage(phone1, null, msg, null, null);
            }
            if (!phone2.equals("")) {
                smsManager.sendTextMessage(phone2, null, msg, null, null);
            }
            if (!phone3.equals("")) {
                smsManager.sendTextMessage(phone3, null, msg, null, null);
            }

            Log.i(TAG, "WE OK!");
        } else {
            text = "Ending transmission!";
            Signal_Button.setText("Signal");
            Signal_Button.setBackgroundColor(Color.GREEN);
            active = !active;

            String msg = "The danger has passed, I'm ok.";

            SmsManager smsManager = SmsManager.getDefault();

            if (!phone1.equals("")) {
                smsManager.sendTextMessage(phone1, null, msg, null, null);
            }
            if (!phone2.equals("")) {
                smsManager.sendTextMessage(phone2, null, msg, null, null);
            }
            if (!phone3.equals("")) {
                smsManager.sendTextMessage(phone3, null, msg, null, null);
            }
        }

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        mWorkerThread = new MyWorkerThread("myWorkerThread");
        Runnable task = new Runnable() {
            @Override
            public void run() {

                while (active) {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String street_address = getCompleteAddressString(latLng.latitude, latLng.longitude);
                            SmsManager smsManager = SmsManager.getDefault();
                            String text;

                            if (street_address.equals("")) {
                                text = "Sending Location \n(" + "Lat " + latLng.latitude + "  "
                                        + "Long " + latLng.longitude + ")\n to Emergency Services";

                                Toast.makeText(MapsActivity.this,text,Toast.LENGTH_LONG).show();

                                text = "Current Location \n(" + "Lat " + latLng.latitude + "  "
                                        + "Long " + latLng.longitude + ")\n to Emergency Services";

                                if (!phone1.equals("")) {
                                    smsManager.sendTextMessage(phone1, null, text, null, null);
                                }
                                if (!phone2.equals("")) {
                                    smsManager.sendTextMessage(phone2, null, text, null, null);
                                }
                                if (!phone3.equals("")) {
                                    smsManager.sendTextMessage(phone3, null, text, null, null);
                                }

                            } else {

                                text = "Sending Location (" + street_address + ") to Emergency Services";

                                Toast.makeText(MapsActivity.this,text,Toast.LENGTH_LONG).show();

                                text = "Current Location (" + street_address + ") to Emergency Services";

                                if (!phone1.equals("")) {
                                    smsManager.sendTextMessage(phone1, null, text, null, null);
                                }
                                if (!phone2.equals("")) {
                                    smsManager.sendTextMessage(phone2, null, text, null, null);
                                }
                                if (!phone3.equals("")) {
                                    smsManager.sendTextMessage(phone3, null, text, null, null);
                                }
                            }
                        }
                    });

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapsActivity.this,
                                "Location Transmission Suspended",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

                if (latLng != null) {

                    // Send location to server here
                    PostTask postDataTask = new PostTask();
                    JSONObject location = new JSONObject();
                    location.put("lat", latLng.latitude);
                    location.put("lng", latLng.longitude);
                    postDataTask.execute(location.toString());

                    Toast.makeText(
                            context,
                            "Lat " + latLng.latitude + " "
                                    + "Long " + latLng.longitude,
                            Toast.LENGTH_LONG).show();
                }
        };


        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        mWorkerThread.postTask(task);

    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        mGoogleApiClient.disconnect();
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
                Log.w("Current location", "" + strReturnedAddress.toString());
            } else {
                Log.w("Current location", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current location", "Cannot get Address!");
        }
        return strAdd;
    }

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

        }
    }

    private class PostTask extends AsyncTask<String, Integer, Long>{
        @Override
        protected Long doInBackground(String... locString) {
            postData(locStrings[0]);
            return null;
        }

        protected void onPostExecute(Long result) {
            //say that it's done here instead of in main function
        }
    }
}
}
