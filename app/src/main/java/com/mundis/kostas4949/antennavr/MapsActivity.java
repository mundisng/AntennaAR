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
import android.opengl.Matrix;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    final int MARKER_UPDATE_INTERVAL = 2000; /* milliseconds */
    Handler my_handler = new Handler();
    private Marker my_last_known_loc,last_loc_antennas_updated;
    private Context my_context;

    private Toolbar my_toolbar;
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
    private float mDeclination,bearing;
    ArrayList<ARCoord> my_antennas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        my_context=this;
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
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //////////////////////////FROM DATABASE///////////////////////////
        //DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        //databaseAccess.open();
        //my_antennas= databaseAccess.getAllCellCoords();
        //databaseAccess.close();
        ////////////////////////MANUAL////////////////////////////////////
        //my_antennas=new ArrayList<>();
        //my_antennas.add(new ARCoord("coconut",24.25,42.54,0));
        //my_antennas.add(new ARCoord("my",21.5,42.54,0));
        //my_antennas.add(new ARCoord("name",14.25,42.54,0));
        //my_antennas.add(new ARCoord("is",44.25,42.54,0));
        //my_antennas.add(new ARCoord("what",32.25,42.54,0));
        /////////////////////END///////////////////////////////////////
        mapFragment.getMapAsync(this);
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
                //Toast.makeText(MainActivity.this, "Settings Pressed MAIN", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, SettingsActivity.class);
                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("EXTRA_PARENT_COMPONENT_NAME", new ComponentName(this, MapsActivity.class));
                startActivity(i);
                finish();
                return true;
            case R.id.action_mode:
                //Toast.makeText(getApplicationContext(), "Clicked Camera Icon", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.cameramode), true);
                editor.commit();
                Intent j = new Intent(MapsActivity.this, MainActivity.class);
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


    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {
            if(my_antennas!=null && !my_antennas.isEmpty()) {
                if(my_last_known_loc!=null){
                    if(last_loc_antennas_updated==null){
                        last_loc_antennas_updated=my_last_known_loc;
                        LatLng last=my_last_known_loc.getPosition();
                        Double my_lat=last.latitude;
                        Double my_long=last.longitude;
                        String my_latstr=my_lat.toString();
                        String my_longstr=my_long.toString();
                        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(my_context);
                        databaseAccess.open();
                        //my_antennas= databaseAccess.getAntennasWithinRadius(my_latstr,my_longstr,"20");
                        my_antennas= databaseAccess.getAllCellCoords();
                        databaseAccess.close();
                        int i=0;
                        while(i<my_antennas.size()){
                            try {
                                ARCoord antenna_coord=my_antennas.get(i);
                                Location antenna_loc=antenna_coord.getLocation();
                                LatLng my_latlng=new LatLng(antenna_loc.getLatitude(),antenna_loc.getLongitude());
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_roundantenna);
                                mMap.addMarker(new MarkerOptions().position(my_latlng).icon(icon));
                            }catch(IndexOutOfBoundsException e){
                                break;
                            }
                            i++;
                        }
                    }
                    else{
                        LatLng last=my_last_known_loc.getPosition();
                        LatLng updated=last_loc_antennas_updated.getPosition();
                        if(((last.latitude > updated.latitude+0.01) || (last.latitude<updated.latitude-0.01))&&((last.longitude>
                        updated.longitude+0.01)||(last.longitude<updated.longitude-0.01))){
                            mMap.clear();
                            my_last_known_loc=mMap.addMarker(new MarkerOptions().position(last).title("You are here!"));
                            last_loc_antennas_updated=my_last_known_loc;
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(last));
                            updateCamera(bearing);
                            Double my_lat=last.latitude;
                            Double my_long=last.longitude;
                            String my_latstr=my_lat.toString();
                            String my_longstr=my_long.toString();
                            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(my_context);
                            databaseAccess.open();
                            //my_antennas= databaseAccess.getAntennasWithinRadius(my_latstr,my_longstr,"20");
                            my_antennas= databaseAccess.getAllCellCoords();
                            databaseAccess.close();
                            int i=0;
                            while(i<my_antennas.size()){
                                try {
                                    ARCoord antenna_coord=my_antennas.get(i);
                                    Location antenna_loc=antenna_coord.getLocation();
                                    LatLng my_latlng=new LatLng(antenna_loc.getLatitude(),antenna_loc.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(my_latlng));
                                }catch(IndexOutOfBoundsException e){
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                }
            }
            my_handler.postDelayed(this, MARKER_UPDATE_INTERVAL);
        }
    };

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
        my_handler.postDelayed(updateMarker,0);
        // Add a marker in Sydney and move the camera
        //LatLng myloc = new LatLng(x, y);
        //mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
        //updateCameraBearing(mMap,rotation_matrix);
    }

    @Override
    protected void onDestroy() {
        my_handler.removeCallbacks(updateMarker);

        super.onDestroy();
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
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));}
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
                bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                //updateCameraBearing(mMap,bearing);
                //updateCamera(bearing);
                rotation_matrix=mRotationMatrix;
            }
        }
        else {
            if (sEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sEvent.values); //Get rotation of cell phone
                SensorManager.getOrientation(rotationMatrixFromVector, orientation);
                bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                //updateCamera(bearing);
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
                if(my_last_known_loc!=null){
                    my_last_known_loc.remove();
                }
                LatLng myloc = new LatLng(x, y);
                my_last_known_loc=mMap.addMarker(new MarkerOptions().position(myloc).title("You are here!"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                updateCamera(bearing);
            }
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
