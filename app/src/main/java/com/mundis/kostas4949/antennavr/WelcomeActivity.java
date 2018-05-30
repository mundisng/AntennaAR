package com.mundis.kostas4949.antennavr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;


public class WelcomeActivity extends AppCompatActivity {
    private static int TIME_OUT = 4000;
    private static Toolbar my_toolbar;
    boolean cameramode_flag=true;
    boolean hascamerapermission=false;
    boolean haslocationpermission=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        requestPermissions();
        if (hascamerapermission && haslocationpermission) {
            System.out.println("We got in here with all the permissions!");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("WE GOT INTO FIRST RUN!");
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                    cameramode_flag = sharedPref.getBoolean(getString(R.string.cameramode), true);
                    if (cameramode_flag) {
                        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(WelcomeActivity.this, MapsActivity.class);
                        startActivity(i);
                    }
                    finish();
                }
            }, TIME_OUT);
        }
        else {
            System.out.println("We are in the not accepted permissions");
        }
    }


    public void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){  //8eloume kai ta 2
                    this.requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION}, 11);
                }
                else{
                    this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 11); //mono kamera
                    haslocationpermission=true;
                }
            }
            else {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11); //mono location
                    hascamerapermission=true;
                }
                else {
                    hascamerapermission=true;    //ok kai ta 2
                    haslocationpermission=true;
                }
            }
        }
        else{
            hascamerapermission=true;
            haslocationpermission=true;
        }

    }
    public void requestCameraPermission() {
        System.out.println("We requested camera permission!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Camera option 1");
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 11);
        } else {
            System.out.println("Camera option 2");
            hascamerapermission=true;
            //initCamera();
        }
    }

    public void requestLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location option 1");
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 15);
         } else {
            System.out.println("Location option 2");
            haslocationpermission=true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){

        switch (requestCode){
            case 11:{
                if (grantResults.length > 0) {
                    for (int i=0; i<permissions.length; i++){
                        if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)){
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                                haslocationpermission=true;
                            }
                        }
                        if (permissions[i].equals(Manifest.permission.CAMERA)){
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                                hascamerapermission=true;
                            }
                        }
                    }
                    if (hascamerapermission && haslocationpermission){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("WE GOT INTO SECOND RUN!");
                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                                cameramode_flag = sharedPref.getBoolean(getString(R.string.cameramode), true);
                                if (cameramode_flag) {
                                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(WelcomeActivity.this, MapsActivity.class);
                                    startActivity(i);
                                }
                                finish();
                            }
                        }, TIME_OUT);
                    }
                    else {
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Permissions");
                        alertDialog.setMessage("This app doesn't work without the appropriate permissions!");
                        alertDialog.show();
                    }
                }
                        /*System.out.println("Have both permissions!");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("WE GOT INTO SECOND RUN!");
                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                                cameramode_flag = sharedPref.getBoolean(getString(R.string.cameramode), true);
                                if (cameramode_flag) {
                                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(WelcomeActivity.this, MapsActivity.class);
                                    startActivity(i);
                                }
                                finish();
                            }
                        }, TIME_OUT);
                        }
*/


                }

        }

    }

    /*@Override
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
                Toast.makeText(WelcomeActivity.this, "Settings Pressed WELCOME", Toast.LENGTH_SHORT).show();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }*/
}

