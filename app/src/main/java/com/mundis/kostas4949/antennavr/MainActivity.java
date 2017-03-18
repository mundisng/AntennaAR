package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by kostas4949 on 18/3/2017.
 */

public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView coords;
    private Criteria criteria;
    private String provider;
    LocationManager locationManager;
    private Location location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coords=(TextView)findViewById(R.id.coord);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        provider = locationManager.getBestProvider(criteria, true);
        if (provider!= null) {
            System.out.println("Using provider: "+provider);
            System.out.println("WTF1");
            try {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
                onLocationChanged(location);
            } catch (SecurityException e) {
                coords.setText("Can't get location(security exception). Check your settings!");
            }
        }
        else {
            System.out.println("WTF2");
            coords.setText("Can't get provider. Check your settings!");
        }

    }
    @Override
    public void onProviderEnabled(String eProvider){
        Toast.makeText(getApplicationContext(),"Provider "+eProvider+" enabled, checking...",4000).show();
        provider = locationManager.getBestProvider(criteria, true);
        if (provider!= null) {
            System.out.println("Using provider: "+provider);
            try {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
                onLocationChanged(location);
            } catch (SecurityException e) {
                coords.setText("Can't get location. Check your settings!");
            }
        }
        else {
            coords.setText("Can't get provider. Check your settings!");
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            coords.setText("New Location: " + String.valueOf(lat) + " " + String.valueOf(lng));
        }
        else
            coords.setText("Can't get location(weird). Check your settings!");
    }

    @Override
    public void onProviderDisabled(String dProvider) {
           Toast.makeText(getApplicationContext(),"Provider "+dProvider+" disabled, switching...",4000).show();
        provider = locationManager.getBestProvider(criteria, true);
        if (provider!= null) {
            System.out.println("Using provider: "+provider);
            try {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
                onLocationChanged(location);
            } catch (SecurityException e) {
                coords.setText("Can't get location. Check your settings!");
            }
        }
        else {
            coords.setText("Can't get provider. Check your settings!");
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getApplicationContext(),"Provider "+provider+" status changed to: "+Integer.toString(status),4000).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("WTF3");
        if (provider!= null) {
            try {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
            } catch (SecurityException e) {
                coords.setText("Can't get location. Check your settings!");
            }
        }
        else {
            coords.setText("Can't get provider. Check your settings!");
        }
    }

    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
}
