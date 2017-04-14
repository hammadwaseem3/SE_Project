package com.example.dell_pc.se_project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private double longitudeUsers;
    private double latitudeUsers;
    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    ProgressDialog mapProgressDialog;
    TextView nameOfDriver;
    TextView numOfDriver;
    Marker markerForUser;
    Marker markerForAmbulance;
    private Driver ourDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        nameOfDriver=(TextView) findViewById(R.id.nameofdriver);
        nameOfDriver.setVisibility(View.GONE);
        numOfDriver=(TextView) findViewById(R.id.numofdriver);
        numOfDriver.setVisibility(View.GONE);

        mapProgressDialog = new ProgressDialog(MapsActivity.this,0);
        mapProgressDialog.setMessage("Loading");
        mapProgressDialog.setCanceledOnTouchOutside(false);
        mapProgressDialog.show();

        mapFragment.getMapAsync(this);
        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        ImageButton navBarButton = (ImageButton) findViewById(R.id.navBarButton);
        navBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, NavigationMenu.class));
            }
        });

        database = FirebaseDatabase.getInstance();

        ourDriver = new Driver();
        Button requestButton = (Button) findViewById(R.id.requestbutton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ourDriver.selectBestDriver(latitudeUsers,longitudeUsers);

                final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
                dialog.setTitle("Searching For Ambulance");
                dialog.setMessage("Please wait...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();

                long delayInMillis = 6000;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        final String temp =ourDriver.getIdAfterCall();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //Toast.makeText(MapsActivity.this,temp,Toast.LENGTH_SHORT).show();
                                ourDriver.attachListner(mMap);
                                nameOfDriver.setVisibility(View.VISIBLE);
                                nameOfDriver.setText(ourDriver.getName());
                                numOfDriver.setVisibility(View.VISIBLE);
                                numOfDriver.setText(ourDriver.getNum());

                            }
                        });
                        //Toast.makeText(MapsActivity.this,temp,Toast.LENGTH_SHORT).show();
                    }
                }, delayInMillis);



            }
        });

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

        if (displayGpsStatus()) {

            Log.v(TAG, "onClick");


            locationListener = new MyLocationListener();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                locationMangaer.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            }
            else{
                Toast.makeText(this,"Kindly Provide Location Acces",Toast.LENGTH_SHORT).show();
            }

            Log.v(TAG,latitudeUsers +" , "+longitudeUsers);

        } else {
            Toast.makeText(this,"GPS not available!!",Toast.LENGTH_SHORT).show();
        }

    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {

            String longitude = "" +loc.getLongitude();
            //Log.v(TAG, longitude);
            String latitude = ""+loc.getLatitude();
            //Log.v(TAG, latitude);

    /*----------to get City-Name from coordinates ------------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String s = longitude + "\n" + latitude +
                    "\n\nMy Currrent City is: " + cityName;
            longitudeUsers = loc.getLongitude();
            latitudeUsers = loc.getLatitude();

            LatLng userLocation = new LatLng(latitudeUsers, longitudeUsers);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.homeicon);

            if (markerForUser == null){
                markerForUser = mMap.addMarker(new MarkerOptions().position(userLocation).icon(icon).title("Home"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
            else{
                markerForUser.remove();
                markerForUser = mMap.addMarker(new MarkerOptions().position(userLocation).icon(icon).title("Home"));

            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String uid = user.getUid();
                myRef = database.getReference("Users/"+uid+"/Lat");
                myRef.setValue(""+latitude+"");
                myRef = database.getReference("Users/"+uid+"/Long");
                myRef.setValue(""+longitude+"");
            }
            mapProgressDialog.cancel();
        }

        public Bitmap resizeMapIcons(String iconName,int width, int height){
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
            return resizedBitmap;
        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
}
