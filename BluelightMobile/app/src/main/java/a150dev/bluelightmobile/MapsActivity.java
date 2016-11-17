package a150dev.bluelightmobile;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; //For errors

    @Override //Initalizing everyting.
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //This gets the Google API ready and running to start collecting the location.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds

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

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
            }
            else {
                handleNewLocation(location);
            };

        } catch (SecurityException e) {
            Log.i(TAG,"There is a problem with the GPS!");
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
            if(grantResults.length == 1
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
        }
    }

    // For now, this simple logs the location to the screen.
    public void doActivity(View view) {

        Context context = getApplicationContext();
        CharSequence text = "Signal Recieved!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


        if (latLng != null) {
            Toast.makeText(
                    context,
                    "Lat " + latLng.latitude + " "
                            + "Long " + latLng.longitude,
                    Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(context, "Something is wrong.", Toast.LENGTH_LONG).show();
        }
    }
}
