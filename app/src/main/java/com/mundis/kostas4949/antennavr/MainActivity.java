package com.mundis.kostas4949.antennavr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private Toolbar my_toolbar;
    double x,y,z;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[16];
    boolean gps_enabled = false;
    boolean rotation_compatibility=false;
    LocationManager locationManager;
    private AROverlay arOverlay;
    private SensorManager mSensorManager;
    private Sensor mCompass,mCompass1,mCompass2;
    private FrameLayout cameraContainerLayout;
    private SurfaceView surface_viewLayout;
    private ARCamera arCamera;
    private Camera camera;
    TextView coords;
    private long my_minTime;
    private double my_radius;
    private int my_antenum;
    private int my_range;
    private MainActivityThread my_thread;
    int defaultcameraid;
    private int cellid;
    String strength;
    Intent in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences my_sharedPref = PreferenceManager.getDefaultSharedPreferences(this); //get settings values
        String radiusstr = my_sharedPref.getString("pref_radius", "100");
        String antenumstr=my_sharedPref.getString("pref_antenum","5");
        String minTimestr = my_sharedPref.getString("pref_minTime", "50");
        String rangestr= my_sharedPref.getString("pref_range","200");
        my_minTime=Long.parseLong(minTimestr);
        my_radius=Double.parseDouble(radiusstr);
        my_antenum=Integer.parseInt(antenumstr);
        my_range=Integer.parseInt(rangestr);
        if(my_range==0){
            my_range=1;
        }
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            List<CellInfo> cellInfos = telephony.getAllCellInfo(); //get connected cell info
            if (!cellInfos.isEmpty()) {
                for (int i=0; i<cellInfos.size(); i++ ){
                    if (cellInfos.get(i).isRegistered()){
                        if(cellInfos.get(i) instanceof CellInfoWcdma){  //if wcdma cell
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                            cellid=cellInfoWcdma.getCellIdentity().getCid();
                        }else if(cellInfos.get(i) instanceof CellInfoGsm){ //if gsm cell
                            CellInfoGsm cellInfogsm = (CellInfoGsm) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                            cellid=cellInfogsm.getCellIdentity().getCid();
                        }else if(cellInfos.get(i) instanceof CellInfoLte){ //if lte cell
                            CellInfoLte cellInfoLte = (CellInfoLte) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthLte.getDbm());
                            cellid=cellInfoLte.getCellIdentity().getCi();
                        }
                        i=cellInfos.size();
                    }
                    }
                }

        } catch (SecurityException e){
            System.out.println("Couldn't get COARSE LOCATION");
        }
        arOverlay = new AROverlay(this);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mCompass==null || mCompass.getMinDelay()==0){    //if rotation_vector sensor doesn't exist on this phone
            rotation_compatibility=true;    //set compatibility mode for sensors on this phone
            mCompass1=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mCompass2=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        //set up GUI
        surface_viewLayout=(SurfaceView) findViewById(R.id.surface_view);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        coords = (TextView) findViewById(R.id.tv_current_location);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        arCamera=new ARCamera(this,surface_viewLayout);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numCams = Camera.getNumberOfCameras();
        for (int xx = 0; xx < numCams; xx++) {  //Set default back facing camera to be used for AR
            Camera.getCameraInfo(xx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultcameraid = xx;
            }
        }
        coords.setText("Calculating position.... ");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); //check if gps is enabled
        if (!gps_enabled) {  //if not, display message
            coords.setText("Can't get location.GPS is disabled!");
        }
        try { //in any case, try to get location to check for app permissions
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, my_minTime, 0,
                    locationListenerGps);
        } catch (SecurityException e) {
            coords.setText("Can't get gps location. Check app permissions!");
        }
    }
    @Override
    public void onStart(){
                super.onStart();
                my_thread=new MainActivityThread(my_radius,my_antenum,my_range); //set up thread to make GUI more responsive
                if(my_thread!=null){
                        my_thread.start();
                    }
            }

            @Override
    public void onStop(){
                super.onStop();
                if (my_thread != null) { //stop the running thread
                        my_thread.stop_running();
                        my_thread.interrupt();
                   }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_menu, menu);
        MenuItem item = menu.findItem(R.id.action_mode);
        if (item != null) {
            int id=getResources().getIdentifier("ic_maps","mipmap",getPackageName());
            item.setIcon(id);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtra("EXTRA_PARENT_COMPONENT_NAME", new ComponentName(this, MainActivity.class));
                startActivity(i);
                finish();
                return true;
            case R.id.action_mode:
                // change to map mode and save mode value in sharedpreferences
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.cameramode), false);
                editor.commit();
                Intent j = new Intent(MainActivity.this, MapsActivity.class);
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
        cameraContainerLayout.addView(arCamera);
        super.onResume();
        initCamera();
        initAROverlay();
        if (!rotation_compatibility) { //if we don't use compatibility mode for sensors, use rotation_vector info
            mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_FASTEST);

        }
        else {   //else use accelerometer and magnetometer as mCompass1 and mCompass2 respectively
            mSensorManager.registerListener(this, mCompass1,
                    SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mCompass2,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); //release sensors on pause to save battery
        releaseCamera();  //release camera on pause so it can be used by another app
        cameraContainerLayout.removeView(arCamera);

    }

    public void initAROverlay() {  //set up view to draw on camera preview
        if (arOverlay.getParent() != null) {
            ((ViewGroup) arOverlay.getParent()).removeView(arOverlay);
        }
        cameraContainerLayout.addView(arOverlay);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sEvent) {  //if sensors change values(this also runs the first time sensors are set up)
        if (rotation_compatibility) {   //if we are using sensors in compatibility mode
            if (sEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {  //get value of accelerometer
                System.arraycopy(sEvent.values, 0, mAccelerometerReading,
                        0, mAccelerometerReading.length);


            } else if (sEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) { //get value of magnetometer
                System.arraycopy(sEvent.values, 0, mMagnetometerReading,
                        0, mMagnetometerReading.length);
            }
            if (mSensorManager.getRotationMatrix(mRotationMatrix, null,
                    mAccelerometerReading, mMagnetometerReading)) {   //combine them to get roation
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                if (arCamera != null) {
                    projectionMatrix = arCamera.getProjectionMatrix();   //Get dimensions of camera
                }
                Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, mRotationMatrix, 0); //Combine rotation with dimensions of camera
                this.arOverlay.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

            }
        }
        else {  //if we are not using sensors in compatibility mode
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) { //get the rotation_vector sensor
                float[] rotationMatrixFromVector = new float[16];
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
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
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void initCamera() {  //Opening default camera
        camera=Camera.open();
        arCamera.setCamera(camera);

    }
    LocationListener locationListenerGps = new LocationListener() {  //Listener for GPS location
        public void onLocationChanged(Location location) {
            if (location!=null) {
                synchronized(App.current_location_flag) { //singleton access to current location
                    App.current_location = location;
                }
                arOverlay.invalidate();
                x = location.getLatitude();
                y = location.getLongitude();
                z=location.getAltitude();
                coords.setText("location (gps) : " + x + " " + y);

            }
            else {
                coords.setText("Calculating position...");
            }
        }
        public void onProviderDisabled(String provider) {  //If we disable GPS
            coords.setText("Gps disabled, please enable it!");
        }

        public void onProviderEnabled(String provider) { //If we enable GPS
            coords.setText("Calculating position...");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

}