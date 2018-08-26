package com.mundis.kostas4949.antennavr;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity { //enables back button for the settings view
    Toolbar my_toolbar;
    private ComponentName parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = getIntent().getExtras().getParcelable("EXTRA_PARENT_COMPONENT_NAME");
        setContentView(R.layout.activity_settings);

        my_toolbar=(Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(my_toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //enable back button


        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() { //when device's back button is pressed
        final Intent parentIntent = new Intent();
        parentIntent.setComponent(parent);
        startActivity(parentIntent); //start parent and finish current activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //when app's back button is pressed
                if (parent != null) {
                    final Intent parentIntent = new Intent();
                    parentIntent.setComponent(parent);
                    startActivity(parentIntent); //start parent and finish current activity
                    finish();
                    return true;
                }
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
