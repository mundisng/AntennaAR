package com.mundis.kostas4949.antennavr;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
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
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

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
    TextView coords;//compa;
    private long my_minTime;
    private double my_radius;
    private int my_antenum;
    private int my_range;
    //DatabaseAccess databaseAccess;
    private MainActivityThread my_thread;
    int defaultcameraid;
    private int cellid;
    String strength;


    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    private final static int REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS_CODE=15;
    Intent in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences my_sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
        System.out.println("radius="+my_radius+" ,antenum="+my_antenum+", minTime="+my_minTime+", range="+my_range);
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            List<CellInfo> cellInfos = telephony.getAllCellInfo();
            if (!cellInfos.isEmpty()) {
                for (int i=0; i<cellInfos.size(); i++ ){
                    if (cellInfos.get(i).isRegistered()){
                        if(cellInfos.get(i) instanceof CellInfoWcdma){
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                            cellid=cellInfoWcdma.getCellIdentity().getCid();
                        }else if(cellInfos.get(i) instanceof CellInfoGsm){
                            CellInfoGsm cellInfogsm = (CellInfoGsm) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                            cellid=cellInfogsm.getCellIdentity().getCid();
                        }else if(cellInfos.get(i) instanceof CellInfoLte){
                            CellInfoLte cellInfoLte = (CellInfoLte) telephony.getAllCellInfo().get(0);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthLte.getDbm());
                            cellid=cellInfoLte.getCellIdentity().getCi();
                        }
                        i=cellInfos.size();
                    }
                    }
                    //System.out.println("Cell info "+aa+" has data: "+all.get(aa));
                }

            else {
                System.out.println("Cell info is empty!");
            }
        } catch (SecurityException e){
            System.out.println("Couldn't get COARSE LOCATION");
        }
        /*if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            try {
                final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
                if (location != null) {
                    System.out.println("LAC: " + location.getLac() + " CID: " + location.getCid());
                }
            }catch (SecurityException e) {
                System.out.println("Can't get CELL ID");
            }
        }*/
        arOverlay = new AROverlay(this);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
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
        surface_viewLayout=(SurfaceView) findViewById(R.id.surface_view);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        coords = (TextView) findViewById(R.id.tv_current_location);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        arCamera=new ARCamera(this,surface_viewLayout);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numCams = Camera.getNumberOfCameras();
        for (int xx = 0; xx < numCams; xx++) {
            Camera.getCameraInfo(xx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultcameraid = xx;
            }
        }
        coords.setText("Calculating position.... "+"\n CELL ID: "+cellid);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enabled) {
            coords.setText("Can't get location.GPS is disabled!"+"\n CELL ID: "+cellid);
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, my_minTime, 0,
                    locationListenerGps);
        } catch (SecurityException e) {
            coords.setText("Can't get gps location. Check app permissions!"+"\n CELL ID: "+cellid);
        }
    }
    @Override
    public void onStart(){
                super.onStart();
                my_thread=new MainActivityThread(my_radius,my_antenum,my_range);
                if(my_thread!=null){
                        my_thread.start();
                    }
            }

            @Override
    public void onStop(){
                super.onStop();
                if (my_thread != null) {
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
                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("EXTRA_PARENT_COMPONENT_NAME", new ComponentName(this, MainActivity.class));
                startActivity(i);
                finish();
                return true;
            case R.id.action_mode:
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.cameramode), false);
                editor.commit();
                Intent j = new Intent(MainActivity.this, MapsActivity.class);
                //j.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        System.out.println("WE RESUMED!");
        super.onResume();
        initCamera();
        initAROverlay();
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
        mSensorManager.unregisterListener(this);
        releaseCamera(); //prepei na kanei release thn kamera otan einai se pause, to sygkekrimeno einai akoma buggy
        cameraContainerLayout.removeView(arCamera);

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
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                if (arCamera != null) {
                    projectionMatrix = arCamera.getProjectionMatrix();   //Get dimensions of camera
                }
                Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, mRotationMatrix, 0); //Combine rotation with dimensions of camera
                this.arOverlay.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

            }
        }
        else {
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                float[] rotationMatrixFromVector = new float[16];
                float[] projectionMatrix = new float[16];
                float[] rotatedProjectionMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
                String bla = "";
                for (int i = 0; i < rotationMatrixFromVector.length; i++) {
                    bla = bla + rotationMatrixFromVector[i] + " ";
                }
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
            //camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void initCamera() {
        System.out.println("Opening camera with id: "+defaultcameraid);
        camera=Camera.open();
        arCamera.setCamera(camera);
        System.out.println("Opened and set camera in main thread");

    }
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                List<CellInfo> cellInfos = telephony.getAllCellInfo();
                if (!cellInfos.isEmpty()) {
                    for (int i=0; i<cellInfos.size(); i++ ){
                        if (cellInfos.get(i).isRegistered()){
                            if(cellInfos.get(i) instanceof CellInfoWcdma){
                                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephony.getAllCellInfo().get(0);
                                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                                cellid=cellInfoWcdma.getCellIdentity().getCid();
                            }else if(cellInfos.get(i) instanceof CellInfoGsm){
                                CellInfoGsm cellInfogsm = (CellInfoGsm) telephony.getAllCellInfo().get(0);
                                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                                strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                                cellid=cellInfogsm.getCellIdentity().getCid();
                            }else if(cellInfos.get(i) instanceof CellInfoLte){
                                CellInfoLte cellInfoLte = (CellInfoLte) telephony.getAllCellInfo().get(0);
                                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                strength = String.valueOf(cellSignalStrengthLte.getDbm());
                                cellid=cellInfoLte.getCellIdentity().getCi();
                            }
                            i=cellInfos.size();
                        }
                    }
                    //System.out.println("Cell info "+aa+" has data: "+all.get(aa));
                }

                else {
                    System.out.println("Cell info is empty!");
                }
            } catch (SecurityException e){
                System.out.println("Couldn't get COARSE LOCATION");
            }

            System.out.println("Calculating gps position..."+"\n CELL ID: "+cellid);
            if (location!=null) {
                synchronized(App.current_location_flag){
                                        App.current_location=location;
                                    }
                                arOverlay.invalidate();
                //arOverlay.updateCurrentLocation(location);
                x = location.getLatitude();
                y = location.getLongitude();
                z=location.getAltitude();
                coords.setText("location (gps) : " + x + " " + y+"\n CELL ID: "+cellid);

            }
            else {
                coords.setText("Calculating position..."+"\n CELL ID: "+cellid);
            }
        }
        public void onProviderDisabled(String provider) {
            System.out.println("We know "+provider+" is disabled in gps listener!");
            coords.setText("Gps disabled, please enable it!"+"\n CELL ID: "+cellid);
        }

        public void onProviderEnabled(String provider) {
            System.out.println("Gps provider knows we enabled: "+provider);
            coords.setText("Calculating position..."+"\n CELL ID: "+cellid);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

}