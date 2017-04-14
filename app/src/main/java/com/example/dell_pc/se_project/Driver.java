package com.example.dell_pc.se_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dell-Pc on 4/13/2017.
 */

public class Driver {
    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    private String name;
    private String id;
    private String Lat;
    private String Long;
    private String num;
    private String tempName;
    private String tempId;
    private String tempLat;
    private String tempLong;
    private String tempNum;
    private Marker marker;


    public Driver(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Ambulance");
        marker=null;

    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getLat() {
        return Lat;
    }

    public String getLong() {
        return Long;
    }

    public String getNum() {
        return num;
    }

    public String getIdAfterCall(){
        id=tempId;
        name=tempName;
        Lat=tempLat;
        Long=tempLong;
        num=tempNum;
        return id;
    }
    public String selectBestDriver(final double latUser,final double longUser){


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Driver Class: ", "yhn par agya :P");
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                List<HashMap<String,String>> value = (ArrayList<HashMap<String,String>>)dataSnapshot.getValue();

                double[] distance = new double[value.size()];

                    int count=0;
                    double min=Double.MAX_VALUE;
                    Log.d("Driver Class: ",""+ min);
                    for(HashMap<String,String> v: value){

                        distance[count]=Math.sqrt(
                                Math.pow(latUser - Double.parseDouble(v.get("Lat")),2)
                                                    +
                                Math.pow(longUser - Double.parseDouble(v.get("Long")),2)


                        );
                        Log.d("Driver Class: ", "Distance: "+distance[count]);
                        if(distance[count] < min){
                            min=distance[count];
                            tempId=v.get("id");
                            tempName=v.get("name");
                            tempLat=v.get("Lat");
                            tempLong=v.get("Long");
                            tempNum=v.get("num");
                        }

                        count++;
                    }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Driver Class: ", "Failed to read value.", error.toException());

            }
        });
        id=tempId;
        name=tempName;
        Lat=tempLat;
        Long=tempLong;
        num=tempNum;
        return id;
    }

    /*private Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }*/

    public void attachListner(final GoogleMap mMap){
        Log.d("Driver Class: ", "Id: " +id);
        database.getReference("Ambulance/"+id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap<String,String> value = (HashMap<String,String>)dataSnapshot.getValue();
                Double runtimeLat= Double.parseDouble(value.get("Lat"));
                Double runtimeLong= Double.parseDouble(value.get("Long"));
                LatLng driverLocation = new LatLng(runtimeLat, runtimeLong);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ambulanceicon);
                if (marker == null){

                    marker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(icon).title("Ambulance"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));
                }
                else{
                    marker.remove();
                    marker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(icon).title("Ambulance"));

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Driver Class: ", "Failed to read value.", error.toException());

            }
        });
    }

}
