package com.mundis.kostas4949.antennavr;


import android.location.Location;

import java.util.ArrayList;

public class MainActivityThread extends Thread{
    volatile boolean is_Running=true;
    double my_radius;
    int antenum;
    int range;

    MainActivityThread(double my_radius,int antenum) {
        this.my_radius=my_radius;
        this.antenum=antenum;
    }

    public void run() {
        try {
            while (is_Running) {
                Location current_location;
                synchronized (App.current_location_flag) { //singleton access to current location
                    current_location=App.current_location;
                }
                if (current_location != null) {
                    ArrayList<ARCoord> arPoints;
                    if (my_radius > 0 && antenum > 0) { //get antennas from database based on given parameters
                        arPoints = App.databaseAccess.getAntennasWithinRadius(current_location.getLatitude(), current_location.getLongitude(), my_radius, antenum);
                    } else if (my_radius > 0 && antenum == 0) {
                        arPoints = App.databaseAccess.getAntennasWithinRadius(current_location.getLatitude(), current_location.getLongitude(), my_radius);
                    } else {
                        arPoints = null;
                    }
                    synchronized (App.my_antennas_flag) { //synchronized renew of the antenna list
                        App.my_antennas = arPoints;
                    }
                }
                sleep(2000);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop_running(){
        this.is_Running=false;
    }
}
