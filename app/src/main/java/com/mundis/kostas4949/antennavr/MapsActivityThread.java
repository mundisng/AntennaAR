package com.mundis.kostas4949.antennavr;

import android.location.Location;

import java.util.ArrayList;

public class MapsActivityThread extends Thread{
    volatile boolean is_Running=true;
    double my_radius;

    MapsActivityThread(double my_radius) {
        this.my_radius=my_radius;
    }

    public void run() {
        try {
            while (is_Running) {
                Location current_location;
                synchronized (App.current_location_flag) {
                    current_location=App.current_location;
                }
                if (current_location != null) {
                    ArrayList<ARCoord> arPoints;
                    arPoints= App.databaseAccess.getAntennasWithinRadius(current_location.getLatitude(),current_location.getLongitude(),my_radius);
                    synchronized (App.my_antennas_flag) {
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
