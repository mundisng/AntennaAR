package com.mundis.kostas4949.antennavr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by kostas4949 on 18/3/2017.
 */

public class MainActivity extends AppCompatActivity{
    private static Toolbar myToolbar;
    double x,y,z;
    int gps_data=0;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    LocationManager locationManager;
    private SensorManager sensorManager;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private ARCamera arCamera;
    TextView coords;
    private Camera camera;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    Intent in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        coords = (TextView) findViewById(R.id.tv_current_location);
       // coords = (TextView) findViewById(R.id.coord);
        coords.setText("Calculating position....");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && !network_enabled) {
            coords.setText("Can't get location.Both gps and network is disabled!");
        }

        //if (gps_enabled) {
            try { System.out.println("Start: if (gps_enabled) is true");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListenerGps);
            } catch (SecurityException e) {
                coords.setText("Can't get gps location(security exception). Check your settings!");
            }
       // }
        //if (network_enabled) {
            try { System.out.println("Start: if (network_enabled) is true");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListenerNetwork);
            } catch (SecurityException e) {
                coords.setText("Can't get network location(security exception). Check your settings!");
            }
        //}


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(MainActivity.this, "Settings Pressed MAIN", Toast.LENGTH_SHORT).show();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requestCameraPermission();
    }
    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }
    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating gps position...");
            if (location!=null) {
                x = location.getLatitude();
                y = location.getLongitude();
                z=location.getAltitude();
                gps_data=1;
                coords.setText("location (gps) : " + x + " " + y+"and altitude: "+z);
                System.out.println("(GPS)x is: "+x+"y is: "+y);

            }
            else {
                gps_data=0;
            }
        }
        public void onProviderDisabled(String provider) {
            System.out.println("We know "+provider+" is disabled in gps listener!");
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                System.out.println("Option 1");
                gps_data=0;
                coords.setText("Can't get location from either gps or network. Check settings!");
            }
            else{
                gps_data=0;
                System.out.println("Option 2");
                coords.setText("Calculating position...");
                /*try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                            locationListenerNetwork);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }*/
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
            System.out.println("Calculating network position for: "+location+"...");
            if (location != null && gps_data == 0) {
                x = location.getLatitude();
                y = location.getLongitude();
                System.out.println("(Network)x is: "+x+"y is: "+y);
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
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gps_data=0;
                System.out.println("Option 1/network");
                coords.setText("Can't get location from either gps or network. Check settings!");
            }
            else{
                System.out.println("Option 2/network");
                coords.setText("Calculating position...");
                /*try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            locationListenerNetwork);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }*/
            }
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Network provider knows we enabled: "+provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

}
