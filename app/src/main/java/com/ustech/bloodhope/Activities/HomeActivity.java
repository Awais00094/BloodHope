package com.ustech.bloodhope.Activities;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.print.PrintAttributes;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.jaredrummler.android.widget.AnimatedSvgView;
import com.ustech.bloodhope.R;
import com.ustech.bloodhope.Receivers.NetworkReceiver;
import com.ustech.bloodhope.Services.GPSService;
import com.ustech.bloodhope.Utils.Constants;
import com.ustech.bloodhope.Utils.MyApplication;

import org.json.JSONObject;

import static android.telephony.CellLocation.requestLocationUpdate;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    GoogleApiClient mGoogleApiClient;
    private boolean isCurrentLocationShown = false;
    private double myLatitude;
    private double myLongitude;
    ImageButton btnMyLocation;
    Button home_btnBook;
    private double myLatitude_Drag;
    private double myLongitude_Drag;

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private float DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    AnimatedSvgView current_location_indicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //registring the reciever
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        HomeActivity.this.registerReceiver(new NetworkReceiver(),filter);
        //end receicer

        current_location_indicator= (AnimatedSvgView) findViewById(R.id.user_current_logo);
        //settings the maps
         if( !((MyApplication)getApplication()).isGpsOn())
         {
             new MaterialDialog.Builder(this)
                     .title("Gps is turnd off")
                     .content("can't reach to your location please turn on location service")
                     .positiveText("GPS").onPositive(new MaterialDialog.SingleButtonCallback() {
                             @Override
                             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                 startActivity(intent);
                             }
                         })
                     .negativeText("cancel").cancelable(false)
                     .show();
         }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            if (mGoogleApiClient == null) {
                // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
//                        .addApi(AppIndex.API).build();
                        .build();
            }

            locationRequest = new LocationRequest();
            locationRequest.setInterval(25 * 1000);
            locationRequest.setFastestInterval(15 * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            Log.d(Constants.TAG,"on create");
            if( !((MyApplication)getApplication()).isOnline())
            {
                //Toast.makeText(this, "no network found", Toast.LENGTH_SHORT).show();
                new MaterialDialog.Builder(this)
                        .title("No network found")
                        .content("can't reach to the hope drop network please check you connectivity")
                        .positiveText("WiFi").onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                }
                            })
                        .negativeText("cancel").cancelable(false)
                        .show();

            }
            else
            {
                //Toast.makeText(this, "network found", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
           //e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
         //   stopService(new Intent(HomeActivity.this, GPSService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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
        if (id == R.id.option_get_place) {
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        getLocationPermission();

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        updateLocationUI();
        //mMap.setTrafficEnabled(true);
        //mMap.setIndoorEnabled(true);
        //mMap.setBuildingsEnabled(true);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                final double myLatitude_Drag_s = mMap.getCameraPosition().target.latitude;
                final double myLongitude_Drag_s = mMap.getCameraPosition().target.longitude;

                myLatitude_Drag = myLatitude_Drag_s;
                myLongitude_Drag = myLongitude_Drag_s;
                current_location_indicator.setVisibility(View.VISIBLE);

                current_location_indicator.start();
            }
        });
        final LatLng PERTH = new LatLng(33.667425, 73.150671);
        final LatLng SYDNEY = new LatLng(33.669845, 73.153879);
        final LatLng BRISBANE = new LatLng(33.670863, 73.147731);

        Marker mPerth;
        Marker mSydney;
        Marker mBrisbane;
        BitmapDescriptor icon;
        if(android.os.Build.VERSION.SDK_INT >= 21){
            icon = BitmapDescriptorFactory.fromBitmap(Constants.drawableToBitmap(getResources().getDrawable(R.drawable.ic_doner_location_indicator,getTheme())) );
        }
        else
        {
            icon=  BitmapDescriptorFactory.fromResource(R.drawable.ic_doner_location_indicator);
        }
        mPerth = mMap.addMarker(new MarkerOptions()
                .position(PERTH)
                .icon(icon)
                .snippet("Last donation 23-10-2017")
                .title("Ali"));
        mPerth.setTag(1);
        mSydney = mMap.addMarker(new MarkerOptions()
                .position(SYDNEY)
                .icon(icon)
                .snippet("Last donation 23-10-2017")
                .title("Osama"));
        mSydney.setTag(2);
        mBrisbane = mMap.addMarker(new MarkerOptions()
                .position(BRISBANE)
                .icon(icon)
                .snippet("Last donation 23-10-2017")
                .title("Haider"));
        mBrisbane.setTag(3);
        Log.d(Constants.TAG,"map is ready "+ ((MyApplication)getApplication()).getLat());


        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(HomeActivity.this, "name is "+marker.getTitle(), Toast.LENGTH_SHORT).show();
                //move marker code
                LatLng newQuard = new LatLng(33.667136, 73.151392);
                animateMarker(1,marker.getPosition(),newQuard,false,marker);
                current_location_indicator.setVisibility(View.INVISIBLE);
                return false;
            }
        });
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{   Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_WIFI_STATE
                                    },
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //        mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            requestLocationUpdate();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to Get Current Location. " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.TAG, "GoogleApiClient location changer");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.TAG, "onConnectionFailed");

        stopLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        try {

            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();
            Log.d(Constants.TAG,"on location Changed = "+myLatitude);
            //Store Current Location In MyApplication
            ((MyApplication)getApplication()).setLat(String.valueOf(myLatitude));
            ((MyApplication)getApplication()).setLon(String.valueOf(myLongitude));
            if (!isCurrentLocationShown) {
                CameraUpdate camup = CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude, myLongitude), 16);
                mMap.animateCamera(camup);
                isCurrentLocationShown = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
    @Override
    protected void onStart() {
        try {
            super.onStart();
            mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (mGoogleApiClient.isConnected()) {
                requestLocationUpdate();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        try {
            super.onPause();
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }catch (Exception e){

            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        try {
            super.onStop();
            mGoogleApiClient.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void requestLocationUpdate() {
        try {
            Log.d(Constants.TAG,"location update");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void animateMarker(final int position, final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker,final  Marker marker) {


//        final Marker marker = mMap.addMarker(new MarkerOptions()
//                .position(startPosition)
//                .icon(BitmapDescriptorFactory.fromBitmap(Constants.drawableToBitmap(getResources().getDrawable(R.drawable.ic_doner_location_indicator,null)) ) )
//                .snippet("Last donation 23-10-2017")
//                .title("Haider"));

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 15*1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


}
