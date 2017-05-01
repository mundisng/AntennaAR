package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    private static int TIME_OUT = 4000;
    private static Toolbar my_toolbar;
    boolean cameramode_flag=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        my_toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref =getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                cameramode_flag=sharedPref.getBoolean(getString(R.string.cameramode),true);
                if(cameramode_flag){
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(i);
                }
                else{
                    Intent i= new Intent(WelcomeActivity.this,MapsActivity.class);
                    startActivity(i);
                }
                finish();
            }
        }, TIME_OUT);
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
