package com.mundis.kostas4949.antennavr;

import android.location.Location;


public class ARCoord { //class used by cell tower points and what data every point holds
    Location location;
    String name;
    double range;

    public ARCoord(String name, double lat, double lon, double altitude, double range) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        this.range=range;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public double getRange() { return range; }


}
