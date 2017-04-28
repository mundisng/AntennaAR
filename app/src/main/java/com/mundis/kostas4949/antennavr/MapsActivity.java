package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap=null;
    boolean gps_enabled = false;
    LocationManager locationManager;
    double x,y,z;

    private SensorManager mSensorManager;
    private Sensor mCompass,mCompass1,mCompass2;
    boolean rotation_compatibility=false;
    private boolean ble;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[16];
    private final float[] rotationMatrixFromVector = new float[16];
    private float[] rotation_matrix;
    private GeomagneticField field;
    private float mDeclination;

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
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

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
        //updateCameraBearing(mMap,rotation_matrix);
    }
    ////////////////////////////////////////////////////////////

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    private void updateCamera(float bearing) {
        if(mMap!=null){
        CameraPosition oldPos = mMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));}
        else{
            System.out.println("mMap BEARING NULLERINO");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!rotation_compatibility) {
            ble = mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_FASTEST);

            if (ble == true) {
                System.out.println("Why is is true?");
            } else {
                System.out.println("ha ha,false!");
            }
        }
        else {
            ble=mSensorManager.registerListener(this, mCompass1,
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (ble==true){
                System.out.println("Why is is true?1");
            }
            else {
                System.out.println("ha ha,false!1");
            }
            ble=mSensorManager.registerListener(this, mCompass2,
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (ble==true){
                System.out.println("Why is is true?2");
            }
            else {
                System.out.println("ha ha,false!2");
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sEvent) {
        float[] orientation = new float[3];
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
                SensorManager.getOrientation(mRotationMatrix, orientation);
                float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                //updateCameraBearing(mMap,bearing);
                updateCamera(bearing);
                rotation_matrix=mRotationMatrix;
            }
        }
        else {
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
                SensorManager.getOrientation(rotationMatrixFromVector, orientation);
                float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                updateCamera(bearing);
                rotation_matrix=rotationMatrixFromVector;
            }
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating gps position...");
            if (location != null) {
                field = new GeomagneticField(
                        (float)location.getLatitude(),
                        (float)location.getLongitude(),
                        (float)location.getAltitude(),
                        System.currentTimeMillis()
                );
                x=location.getLatitude();
                y=location.getLongitude();
                // getDeclination returns degrees
                mDeclination = field.getDeclination();
                //Toast.makeText(getApplicationContext(),"location (gps) : " + x + " " + y + "and altitude: " + z,Toast.LENGTH_SHORT).show();
                //System.out.println("(GPS)x is: " + x + "y is: " + y);

            } else {
                //gps_data=0;
                Toast.makeText(getApplicationContext(), "Calculating Position!", Toast.LENGTH_SHORT).show();
            }
            if(mMap!=null){
            mMap.clear();
            LatLng myloc = new LatLng(x, y);
            mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));}
            else{
                System.out.println("mMap NULLERINO");
            }
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
