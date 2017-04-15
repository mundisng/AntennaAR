package com.mundis.kostas4949.antennavr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by kostas4949 on 18/3/2017.
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static Toolbar myToolbar;
    double x,y,z;
    int gps_data=0;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    LocationManager locationManager;
    private SensorManager mSensorManager;
    private Sensor mCompass;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private ARCamera arCamera;
    TextView coords,compa;
    private Camera camera;
    //private AROverlayView arOverlayView; tha xreiastei sto mellon gia tis koukides sthn kamera
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    Intent in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        //arOverlayView = new AROverlayView(this); tha xreiastei sto mellon gia tis koukides sthn kamera
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        int i=1;
        for (Sensor sensor : sensors) {
            System.out.println(i+" "+sensor.getName());
            i++;
        }
        mCompass=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        coords = (TextView) findViewById(R.id.tv_current_location);
        compa= (TextView) findViewById(R.id.textView2);
       // coords = (TextView) findViewById(R.id.coord);
        coords.setText("Calculating position....");
        compa.setText("Calculating phone rotation..");
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
        mSensorManager.registerListener(this,mCompass,SensorManager.SENSOR_DELAY_FASTEST);
        //initAROverlayView(); tha xreiastei sto mellon gia tis koukides sthn kamera
    }

    @Override
    public void onPause() {
        super.onPause();
        //releaseCamera();
        mSensorManager.unregisterListener(this);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sEvent) {
        //System.out.println("PEW PEW");
        if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
            System.out.println("CELL ROTATION: \n");
            String bla="";
            for (int i = 0; i < rotationMatrixFromVector.length; i++) {
                //bla=bla+rotationMatrixFromVector[i]+ " ";
                    System.out.print(+rotationMatrixFromVector[i]+ " ");
            }
            compa.setText(bla);
            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();   //Get dimensions of camera
            }
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0); //Combine rotation with dimensions of camera
        }
    }


    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }
    /*public void initAROverlayView() {  tha xreiastei sto mellon gia tis koukides sthn kamera
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }*/

    public void requestCameraPermission() {
        System.out.println("We requested camera permission!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Camera option 1");
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            System.out.println("Camera option 2");
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
