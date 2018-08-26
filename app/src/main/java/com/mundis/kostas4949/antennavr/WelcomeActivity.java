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
        //set up the GUI
        setContentView(R.layout.activity_welcome);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        requestPermissions(); //check if we have all permissions
        if (hascamerapermission && haslocationpermission) { //if we have all permissions
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() { //run the app
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                    cameramode_flag = sharedPref.getBoolean(getString(R.string.cameramode), true);
                    //start the app in given mode based on sharedpreferences saved value
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
        }
    }


    public void requestPermissions(){   //check and request the permissions needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //check if we are using a mobile phone with android marshmallow or newer
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){  //if we don't have access for camera or location, request both
                    this.requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION}, 11);
                }
                else{
                    this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 11); //if we only need camera permission
                    haslocationpermission=true;
                }
            }
            else {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11); //if we only need location permission
                    hascamerapermission=true;
                }
                else {
                    hascamerapermission=true;    //if we have both
                    haslocationpermission=true;
                }
            }
        }
        else{ //if we are using phone before android M, we have both permissions
            hascamerapermission=true;
            haslocationpermission=true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){  //check results of the permissions requested

        switch (requestCode){
            case 11:{
                if (grantResults.length > 0) { //if we got at least 1 permission granted
                    for (int i=0; i<permissions.length; i++){ //check if we got both permissions needed granted
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
                    if (hascamerapermission && haslocationpermission){ //if we did get all permissions granted, run app
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                                cameramode_flag = sharedPref.getBoolean(getString(R.string.cameramode), true);
                                //start the app in given mode based on sharedpreferences saved value
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
                    else {  //else show warning message and don't run app
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Permissions");
                        alertDialog.setMessage("This app doesn't work without the appropriate permissions!");
                        alertDialog.show();
                    }
                }
            }

        }

    }

}

