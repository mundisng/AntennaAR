package com.mundis.kostas4949.antennavr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Intent intent=getIntent();
        //Bundle extras = intent.getExtras();
        //new MyNewThread().execute(extras);
        /*Intent intent=getIntent();
        Bundle extras = intent.getExtras();
        System.out.println("x is: "+extras.getDouble("x")+" and y is: "+extras.getDouble("y"));*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        /*Handler handler = new Handler();
        for (int i=0; i>-1; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent=getIntent();
                    Bundle extras = intent.getExtras();
                    System.out.println("x is: "+extras.getDouble("x")+" and y is: "+extras.getDouble("y"));
                }
            }, 4000);
        }*/
    }
    /*private class MyNewThread extends AsyncTask<Bundle, Void, String> {
        protected String doInBackground(Bundle... params ){
            Bundle extra;
            extra=params[0];
            for (int i=0; i>-1; i++) {

                        System.out.println("x is: "+extra.getDouble("x")+" and y is: "+extra.getDouble("y"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return "blaaa";
        }
    }*/
}
