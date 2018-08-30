package com.mundis.kostas4949.antennavr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    final int MARKER_UPDATE_INTERVAL = 2000; /* milliseconds */
    Handler my_handler = new Handler();
    private Marker my_last_known_loc;

    private Toolbar my_toolbar;
    private GoogleMap mMap=null;
    boolean gps_enabled = false;
    LocationManager locationManager;
    double x,y;

    private SensorManager mSensorManager;
    private Sensor mCompass,mCompass1,mCompass2;
    boolean rotation_compatibility=false;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[16];
    private final float[] rotationMatrixFromVector = new float[16];
    private float[] rotation_matrix;
    private GeomagneticField field;
    private float mDeclination,bearing;
    ArrayList<ARCoord> my_antennas;
    private double my_radius;
    private long my_minTime;
    private Circle my_last_circle;
    private MapsActivityThread my_thread;
    private LatLng highlighted_latlng;
    private String highlighted_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //get saved values from sharedpreferences
        SharedPreferences my_sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String radiusstr = my_sharedPref.getString("pref_radius", "100");
        String minTimestr = my_sharedPref.getString("pref_minTime", "50");
        my_minTime=Long.parseLong(minTimestr);
        my_radius=Double.parseDouble(radiusstr);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enabled) {
            Toast.makeText(getApplicationContext(), "Can't get location.GPS is disabled!", Toast.LENGTH_SHORT).show();
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, my_minTime, 0, locationListenerGps);
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
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        my_thread=new MapsActivityThread(my_radius); //set up thread to make GUI more responsive
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
            int id=getResources().getIdentifier("ic_camera","mipmap",getPackageName());
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
                i.putExtra("EXTRA_PARENT_COMPONENT_NAME", new ComponentName(this, MapsActivity.class));
                startActivity(i);
                finish();
                return true;
            case R.id.action_mode:
                //change to camera mode and save mode value in sharedpreferences
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.cameramode), true);
                editor.commit();
                Intent j = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(j);
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    Runnable updateMarker = new Runnable() {
        @Override
        public void run() { //handler for the update of the map
            synchronized(App.my_antennas_flag){ //synchronized retrieval of antenna list
                my_antennas=App.my_antennas;
            }
            if(my_last_known_loc!=null){
                LatLng last=my_last_known_loc.getPosition();
                mMap.clear(); //clear the map
                my_last_known_loc=mMap.addMarker(new MarkerOptions().position(last).title("You are here!"));
                my_last_circle=mMap.addCircle(new CircleOptions().center(last).radius(my_radius).strokeWidth(0f).fillColor(0x550000FF));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(last));
                updateCamera(bearing);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_roundantenna);
                if(highlighted_latlng!=null){
                    Marker marker=mMap.addMarker(new MarkerOptions().position(highlighted_latlng).icon(icon).title(highlighted_title));
                    marker.showInfoWindow();
                }
                if(my_antennas!=null && !my_antennas.isEmpty()) {
                    int i = 0;
                    while (i < my_antennas.size()) { //add antennas on map
                        try {
                            ARCoord antenna_coord = my_antennas.get(i);
                            Location antenna_loc = antenna_coord.getLocation();
                            LatLng my_latlng = new LatLng(antenna_loc.getLatitude(), antenna_loc.getLongitude());
                            if((highlighted_latlng==null) || !(my_latlng.equals(highlighted_latlng))) {
                                mMap.addMarker(new MarkerOptions().position(my_latlng).icon(icon).title(antenna_coord.getName()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            break;
                        }
                        i++;
                    }
                }
            }
            my_handler.postDelayed(this, MARKER_UPDATE_INTERVAL);
        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                highlighted_latlng=marker.getPosition();
                highlighted_title = marker.getTitle();
                return false;

            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                highlighted_latlng=null;

            }
        });
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        my_handler.postDelayed(updateMarker,0);
    }

    @Override
    protected void onDestroy() {
        my_handler.removeCallbacks(updateMarker);
        super.onDestroy();
    }



    private void updateCamera(float bearing) { //update camera bearing
        if(mMap!=null){
        CameraPosition oldPos = mMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));}
        else{
            System.out.println("mMap BEARING NULL");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!rotation_compatibility) { //if we don't use compatibility mode for sensors, use rotation_vector info
            mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_FASTEST);

        }
        else { //else use accelerometer and magnetometer as mCompass1 and mCompass2 respectively
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
                bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                rotation_matrix=mRotationMatrix;
            }
        }
        else {
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
                SensorManager.getOrientation(rotationMatrixFromVector, orientation);
                bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                rotation_matrix=rotationMatrixFromVector;
            }
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            System.out.println("Calculating gps position...");
            if (location != null) {
                synchronized(App.current_location_flag){ //synchronized retrieval of current location
                    App.current_location=location;
                }
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

            } else {
                Toast.makeText(getApplicationContext(), "Calculating Position!", Toast.LENGTH_SHORT).show();
            }
            if(mMap!=null){
                if(my_last_known_loc!=null){
                    my_last_known_loc.remove();
                    if(my_last_circle!=null){
                        my_last_circle.remove();
                    }

                }
                LatLng myloc = new LatLng(x, y);
                my_last_known_loc=mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
                my_last_circle=mMap.addCircle(new CircleOptions().center(new LatLng(x,y)).radius(my_radius).strokeWidth(0f).fillColor(0x550000FF));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                updateCamera(bearing);
            }
            else{
                System.out.println("mMap NULL");
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
