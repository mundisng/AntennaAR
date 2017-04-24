package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean gps_enabled = false;
    LocationManager locationManager;
    double x,y,z;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //////////////////////////////////////////////////
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enabled) {
            Toast.makeText(getApplicationContext(), "Can't get location.GPS is disabled!", Toast.LENGTH_SHORT).show();
        }
        try { //System.out.println("Start: if (gps_enabled) is true");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(),"Can't get gps location(security exception). Check your settings!",Toast.LENGTH_SHORT).show();
        }
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
        LatLng myloc = new LatLng(x, y);
        mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
    }
    ////////////////////////////////////////////////////////////


    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating gps position...");
            if (location != null) {
                x = location.getLatitude();
                y = location.getLongitude();
                z = location.getAltitude();
                //gps_data=1;
                Toast.makeText(getApplicationContext(),"location (gps) : " + x + " " + y + "and altitude: " + z,Toast.LENGTH_SHORT).show();
                System.out.println("(GPS)x is: " + x + "y is: " + y);

            } else {
                //gps_data=0;
                Toast.makeText(getApplicationContext(), "Calculating Position!", Toast.LENGTH_SHORT).show();
            }
            mMap.clear();
            LatLng myloc = new LatLng(x, y);
            mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
        }


        public void onProviderDisabled(String provider) {
            System.out.println("We know " + provider + " is disabled in gps listener!");
            Toast.makeText(getApplicationContext(), "Gps disabled, please enable it!", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Gps provider knows we enabled: " + provider);
            Toast.makeText(getApplicationContext(), "Calculating Position!", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };


}
