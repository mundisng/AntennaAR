package com.mundis.kostas4949.antennavr;

import android.app.Application;


public class App extends Application {
    public static DatabaseAccess databaseAccess;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("APP CREATED");
        databaseAccess=DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();

    }
}
