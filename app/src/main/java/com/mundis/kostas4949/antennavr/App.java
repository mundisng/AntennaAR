package com.mundis.kostas4949.antennavr;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;


public class App extends Application { //Aplication class for singleton access
    public static DatabaseAccess databaseAccess;
    public static volatile ArrayList<ARCoord> my_antennas;
    public static volatile Location current_location;
    public static volatile Object my_antennas_flag=new Object();
    public static volatile Object current_location_flag=new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        databaseAccess=DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open(); //open the antenna database

    }
}
