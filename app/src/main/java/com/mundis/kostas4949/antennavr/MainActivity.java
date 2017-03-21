package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by kostas4949 on 18/3/2017.
 */

public class MainActivity extends AppCompatActivity{
    double x,y;
    int gps_data=0;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    LocationManager locationManager;
    TextView coords;
    Intent in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coords = (TextView) findViewById(R.id.coord);
        coords.setText("Calculating position....");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && !network_enabled) {
            coords.setText("Can't get location.Both gps and network is disabled!");
        }

        if (gps_enabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListenerGps);
            } catch (SecurityException e) {
                coords.setText("Can't get gps location(security exception). Check your settings!");
            }
        }
        if (network_enabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListenerNetwork);
            } catch (SecurityException e) {
                coords.setText("Can't get network location(security exception). Check your settings!");
            }
        }
        /*in = new Intent(this, MapsActivity.class);
        Bundle extras = new Bundle();
        extras.putDouble("x",x);
        extras.putDouble("y",y);
        in.putExtras(extras);
        startActivity(in);*/
    }
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating gps position...");
            if (location!=null) {
                x = location.getLatitude();
                y = location.getLongitude();
                gps_data=1;
                coords.setText("location (gps) : " + x + " " + y);
                /*extras.putDouble("x",x);
                extras.putDouble("y",y);
                in.putExtras(extras);*/
            }
            else {
                gps_data=0;
            }
        }
        public void onProviderDisabled(String provider) {
            System.out.println("We know "+provider+" is disabled in gps listener!");
            //System.out.println("Provider= "+provider);
            //System.out.println("Provider equals GPS? "+provider.equals("GPS_PROVIDER"));
             //   System.out.println("isgpsproviderenabled:"+locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            //System.out.println("isnetworkproviderenabled:"+locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                System.out.println("Option 1");
                gps_data=0;
                coords.setText("Can't get location from either gps or network. Check settings!");
            }
            else{
                gps_data=0;
                System.out.println("Option 2");
                coords.setText("Calculating position...");
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                            locationListenerNetwork);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }
            }


        }

        public void onProviderEnabled(String provider) {
            System.out.println("Gps provider knows we enabled: "+provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating network position...");
            if (location != null && gps_data == 0) {
                x = location.getLatitude();
                y = location.getLongitude();
                coords.setText("location (network) : " + x + " " + y);
                /*extras.putDouble("x",x);
                extras.putDouble("y",y);
                in.putExtras(extras);*/
            }
            if (location==null && gps_data == 0){
                coords.setText("Can't get location from gps or network. Check settings!");
            }

        }
        public void onProviderDisabled(String provider) {
            System.out.println("We know "+provider+" is disabled in network listener!");
            //System.out.println("Provider= "+provider);
            //System.out.println("Provider equals GPS? "+provider.equals("GPS_PROVIDER"));
            //   System.out.println("isgpsproviderenabled:"+locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            //System.out.println("isnetworkproviderenabled:"+locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gps_data=0;
                System.out.println("Option 1/network");
                coords.setText("Can't get location from either gps or network. Check settings!");
            }
            else{
                System.out.println("Option 2/network");
                coords.setText("Calculating position...");
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            locationListenerNetwork);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }
            }
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Network provider knows we enabled: "+provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
   /* class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);
            Location net_loc = null, gps_loc = null;
            if (gps_enabled) {
                try {
                    gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } catch (SecurityException e) {
                    coords.setText("Can't get gps location(security exception). Check your settings!");
                }
            }
            if (network_enabled) {
                try {
                    net_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }
            }
            if(gps_loc!=null && net_loc!=null) {
                if (gps_loc.getTime() > net_loc.getTime()) {
                    x = gps_loc.getLatitude();
                    y = gps_loc.getLongitude();
                    coords.setText("location (gps) : " + x + " " + y);
                }
                else
                {x = net_loc.getLatitude();
                    y = net_loc.getLongitude();
                    coords.setText("location (network) : " + x + " " + y);

                }
            }
            if(gps_loc!=null){
                {x = gps_loc.getLatitude();
                    y = gps_loc.getLongitude();
                    coords.setText("location (gps2) : " + x + " " + y);
                }

            }
            if(net_loc!=null){
                {x = net_loc.getLatitude();
                    y = net_loc.getLongitude();
                    coords.setText("location (network2) : " + x + " " + y);

                }
            }
            coords.setText("No last known location.");
        }
    }
        /*criteria = new Criteria();
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
        }*/

    /*
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
    }*/
}
