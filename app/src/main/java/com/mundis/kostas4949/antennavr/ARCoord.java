package com.mundis.kostas4949.antennavr;

import android.location.Location;

/**
 * Created by kostas4949 on 24/4/2017.
 */

public class ARCoord {
    Location location;
    String name;

    public ARCoord(String name, double lat, double lon, double altitude) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }


}
