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
import android.view.ViewGroup.LayoutParams;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by kostas4949 on 18/3/2017.
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private Toolbar my_toolbar;
    double x,y,z;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientationAngles = new float[3];
    private boolean ble;
    boolean gps_enabled = false;
    boolean rotation_compatibility=false;
    LocationManager locationManager;
    private AROverlay arOverlay;
    private SensorManager mSensorManager;
    private Sensor mCompass,mCompass1,mCompass2;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private ARCamera arCamera;
    private Camera camera;
    TextView coords,compa;

    //private AROverlayView arOverlayView; tha xreiastei sto mellon gia tis koukides sthn kamera
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    Intent in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //arOverlayView = new AROverlayView(this); tha xreiastei sto mellon gia tis koukides sthn kamera
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        int i=1;
        for (Sensor sensor : sensors) {
            System.out.println(i+" "+sensor.getName());
            i++;
        }
        mCompass=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mCompass==null || mCompass.getMinDelay()==0){
            System.out.println("Going into compatibility mode");
            rotation_compatibility=true;
            mCompass1=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mCompass2=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            System.out.println("Min delay:"+mCompass.getMinDelay());
        }
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        //surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        //surfaceView.setZOrderOnTop(false);
        coords = (TextView) findViewById(R.id.tv_current_location);
        compa= (TextView) findViewById(R.id.textView2);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        arCamera=new ARCamera(this, (SurfaceView) findViewById(R.id.surface_view));
        arCamera.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        cameraContainerLayout.addView(arCamera);
       // coords = (TextView) findViewById(R.id.coord);
        coords.setText("Calculating position....");
        //compa.setText("Calculating phone rotation..");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled) {
            coords.setText("Can't get location.GPS is disabled!");
        }

            try { //System.out.println("Start: if (gps_enabled) is true");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListenerGps);
            } catch (SecurityException e) {
                coords.setText("Can't get gps location(security exception). Check your settings!");
            }


        arOverlay = new AROverlay(this);
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
                //Toast.makeText(MainActivity.this, "Settings Pressed MAIN", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                //finish();
                return true;
            case R.id.action_maps:
                Toast.makeText(getApplicationContext(), "Clicked Maps Icon", Toast.LENGTH_SHORT).show();
                Intent j = new Intent(MainActivity.this, MapsActivity.class);
                j.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(j);
                finish();
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
        initAROverlay();
        requestCameraPermission();
        if (!rotation_compatibility) {
            mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_FASTEST);

        }
        else {
            mSensorManager.registerListener(this, mCompass1,
                    SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mCompass2,
                    SensorManager.SENSOR_DELAY_FASTEST);


        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera(); //prepei na kanei release thn kamera otan einai se pause, to sygkekrimeno einai akoma buggy

            mSensorManager.unregisterListener(this);

    }
    public void initAROverlay() {
        if (arOverlay.getParent() != null) {
            ((ViewGroup) arOverlay.getParent()).removeView(arOverlay);
        }
        cameraContainerLayout.addView(arOverlay);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sEvent) {
        if (rotation_compatibility) {
            if (sEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(sEvent.values, 0, mAccelerometerReading,
                        0, mAccelerometerReading.length);


            } else if (sEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(sEvent.values, 0, mMagnetometerReading,
                        0, mMagnetometerReading.length);


            }
            if (mSensorManager.getRotationMatrix(mRotationMatrix, null,
                    mAccelerometerReading, mMagnetometerReading)) {
                String bla = "";
                for (int i = 0; i < mRotationMatrix.length; i++) {
                    bla = bla + mRotationMatrix[i] + " ";

                }
                compa.setText(bla);
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];

                //mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
                if (arCamera != null) {
                    projectionMatrix = arCamera.getProjectionMatrix();   //Get dimensions of camera
                }
                Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, mRotationMatrix, 0); //Combine rotation with dimensions of camera
                this.arOverlay.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

            }
        }
        //System.out.println("PEW PEW");
        else {
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                float[] rotationMatrixFromVector = new float[16];
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
                //System.out.println("CELL ROTATION: \n");
                //System.out.println("Not compatiblity!");
                String bla = "";
                for (int i = 0; i < rotationMatrixFromVector.length; i++) {
                    bla = bla + rotationMatrixFromVector[i] + " ";
                    //System.out.print(+rotationMatrixFromVector[i]+ " ");
                }
                compa.setText(bla);
                if (arCamera != null) {
                    projectionMatrix = arCamera.getProjectionMatrix();   //Get dimensions of camera
                }
                Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0); //Combine rotation with dimensions of camera
                this.arOverlay.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
            }
        }
    }


    private void releaseCamera() {
        if(camera != null) {
            //camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }


    public void requestCameraPermission() {
        System.out.println("We requested camera permission!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Camera option 1");
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            System.out.println("Camera option 2");
            initCamera();
        }
    }

    /*public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
           // arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }*/

  /*  private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }*/
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
                arOverlay.updateCurrentLocation(location);
                x = location.getLatitude();
                y = location.getLongitude();
                z=location.getAltitude();
                //gps_data=1;
                coords.setText("location (gps) : " + x + " " + y+"and altitude: "+z);
                System.out.println("(GPS)x is: "+x+"y is: "+y);

            }
            else {
                arOverlay.updateCurrentLocation(location);
                //gps_data=0;
                coords.setText("Calculating position...");
            }
        }
        public void onProviderDisabled(String provider) {
            System.out.println("We know "+provider+" is disabled in gps listener!");
            coords.setText("Gps disabled, please enable it!");
            arOverlay.updateCurrentLocation(null);
            //if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
           //     System.out.println("Option 1");
           //     gps_data=0;
           //     coords.setText("Can't get location from either gps or network. Check settings!");
          //  }
           // else{
            //    gps_data=0;
           //     System.out.println("Option 2");
           //     coords.setText("Calculating position...");
                /*try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                            locationListenerNetwork);
                } catch (SecurityException e) {
                    coords.setText("Can't get network location(security exception). Check your settings!");
                }*/
           // }


        }

        public void onProviderEnabled(String provider) {
            System.out.println("Gps provider knows we enabled: "+provider);
            coords.setText("Calculating position...");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
      if (status==0){
          arOverlay.updateCurrentLocation(null);
      }
      if (status==1){
          arOverlay.updateCurrentLocation(null);
          coords.setText("Calculating position...");
      }
        }
    };

   /* LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating network position for: "+location+"...");
            if (location != null && gps_data == 0) {
                x = location.getLatitude();
                y = location.getLongitude();
                System.out.println("(Network)x is: "+x+"y is: "+y);
                coords.setText("location (network) : " + x + " " + y);
                /*extras.putDouble("x",x);
                extras.putDouble("y",y);
                in.putExtras(extras);
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
                }
            }
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Network provider knows we enabled: "+provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };*/

}
