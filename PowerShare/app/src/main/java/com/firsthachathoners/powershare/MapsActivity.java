package com.firsthachathoners.powershare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String EXTRA_MESSAGE = "com.firsthachathoners.powershare.MESSAGE";
    private  UiSettings uiSettings;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 4000; /* 4 sec */
    static int REQUEST_FINE_LOCATION = 0, zoomMX = 1;
    Example userInfo;
    private String uName;
    public LatLng myLoc, destPost;
    private Marker marker;
    private Marker destMarker;
    private HTTPInterface httpInterface;
    private boolean isStationFound = false, isPathSet = false, isDestSet = false/*, isSesSt = false*/;
    private TextView rangeTxt;
    Polyline paths;
    Marker []sMarker;

    public List<Result> stations;

    private DirectionsApiRequest dAR;

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey(getString(R.string.directionsApiKey)).
                setConnectTimeout(1, TimeUnit.SECONDS).
                setReadTimeout(1, TimeUnit.SECONDS).setWriteTimeout(1, TimeUnit.SECONDS);
    }

    public DirectionsResult prepareRequest( LatLng dest ) throws InterruptedException, ApiException, IOException {
        if ( dest == null )
            return null;
        DateTime now = new DateTime();
        System.out.println("f;FASD;JF;KLfjdas;lkfjdas;lkjfasd;lkjAT--------------------------------------------");
        dAR = DirectionsApi.newRequest(getGeoContext()).
                mode(TravelMode.WALKING).origin( Double.toString(myLoc.latitude) + "," +
                Double.toString(myLoc.longitude)  ).destination( Double.toString(dest.latitude)  +
                "," + Double.toString(dest.longitude) ).departureTime(now);

        return dAR.await();
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[0].legs[0].duration.humanReadable +
                " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        paths = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        rangeTxt = (EditText) view.findViewById(R.id.rng);
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        userInfo = (Example) intent.getSerializableExtra(EXTRA_MESSAGE);

        userInfo.printInfo();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationUpdates();
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps

        myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title("Current Location").position(myLoc)
                .snippet("Thinking of finding POWERBANK <3 ..."));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16.0f));

        httpInterface = APIClient.getClient().create(HTTPInterface.class);

        System.out.println(zoomMX);
        setR(rangeTxt);
        Call<JSONData> newCall = httpInterface.getAllRecords(myLoc.longitude, myLoc.latitude, zoomMX * 1000);
        //Call<JSONData> sesCall = httpInterface.getPSs((float) myLoc.longitude, (float)myLoc.latitude, zoomMX * 1000);

        newCall.enqueue(new Callback<JSONData>() {
                @Override
                public void onResponse(Call<JSONData> call, Response<JSONData> response) {
                    DirectionsResult res;
                    stations = response.body().getResult();
                    if (!isStationFound && stations.size() > 0) {
                        fillMapWithStations(stations);
                    }
                    if (!isPathSet) {
                        try {
                            if (isDestSet) {
                                res = prepareRequest(destPost);

                                addMarkersToMap(res, mMap);

                                addPolyline(res, mMap);
                                isPathSet = true;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (isReachedDest()) {
                            popAlertDialogIsReached();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JSONData> call, Throwable t) {

                }
            });

    }

    public void setR( View view ){
        try {
            zoomMX = Integer.parseInt(rangeTxt.getText().toString());
        }catch (NullPointerException e){
            System.out.println("EXCEPTION");
            zoomMX = 1;
        }
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        destMarker = mMap.addMarker(new MarkerOptions().position( destPost ).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void fillMapWithStations(List<Result> stations) {
        sMarker = new Marker[stations.size()];
        Result temp;
        for ( int i = 0; i < stations.size(); i++ ){
            temp = stations.get(i);
            sMarker[i] = mMap.addMarker( new MarkerOptions().
                    position(new LatLng(temp.getLocation().get(1), temp.getLocation().get(0))).title(temp.getName()).
                    icon( BitmapDescriptorFactory.fromResource(R.drawable.ic_power_settings_new_black_24dp)) );
        }
        isStationFound = true;
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        // Add a marker in Sydney and move the camera
        if(checkPermissions()) {
            googleMap.setMyLocationEnabled(true);
        }
        uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(true);
        myLoc = new LatLng(-34, 151);
        marker = mMap.addMarker( new MarkerOptions().position(myLoc).title("Initial position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

    }

    public boolean isReachedDest(){
        float[] gap = new float[1];
        Location.distanceBetween( myLoc.latitude, myLoc.longitude, destPost.latitude, destPost.longitude, gap);
        if ( gap[0] < 150 ){
            return true;
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        popAlertDialog(marker);
        return true;
    }

    public void popAlertDialogIsReached( ){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Have you reached your destination? " )
                .setMessage("Will you leave powerbank?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        destMarker.remove();
                        isPathSet = false;
                        isDestSet = false;
                        //isSesSt = false;
                        paths.remove();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void popAlertDialog(final Marker tapMarker){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        if (  !(tapMarker.getPosition().equals(this.marker.getPosition()) || ( this.destMarker != null &&
                tapMarker.getPosition().equals(this.destMarker.getPosition())) ) ) {
            builder.setTitle("Path to: " + tapMarker.getTitle())
                    .setMessage("Do you want to go this station?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (paths != null)
                                paths.remove();
                            if (destMarker != null)
                                destMarker.remove();
                            destPost = new LatLng(tapMarker.getPosition().latitude, tapMarker.getPosition().longitude);
                            isPathSet = false;
                            isDestSet = true;
                            //isSesSt = true;
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}
