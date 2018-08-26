package com.mundis.kostas4949.antennavr;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;


    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) { //singleton access
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }
    public void open() { //open the database

        this.database = openHelper.getReadableDatabase();
    }
    public void close() { //close the database
        if (database != null) {
            this.database.close();
        }
    }
/* NOT USED IN CURRENT IMPLEMENTATION
    public ArrayList<ARCoord> getAllCellCoords(){
        ArrayList<ARCoord> coordlist = new ArrayList<>();
        Cursor cursor = database.rawQuery("select cell,latitude,longitude,altitude,range from cell_towers_greece limit 7", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            coordlist.add(new ARCoord(cursor.getString(0),cursor.getDouble(1),cursor.getDouble(2),cursor.getDouble(3),cursor.getDouble(4)));
            cursor.moveToNext();
        }
        cursor.close();
        return coordlist;
    }*/

    public static double get_DistanceBetweenTwoPoints(double p1_lat,double p1_lon,double p2_lat,double p2_lon) { //get distance between 2 points using GPS locations
        double R = 6371000; // m
        double dLat = Math.toRadians(p2_lat - p1_lat);
        double dLon = Math.toRadians(p2_lon - p1_lon);
        double lat1 = Math.toRadians(p1_lat);
        double lat2 = Math.toRadians(p2_lat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }

    public LatLng calculateDerivedPosition(double x, double y, double range, double bearing) { //get angular distance in 1 direction (bearing) that is at a bit more than max (*1.1) that range
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(x);
        double lonA = Math.toRadians(y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);
        LatLng my_point=new LatLng(lat,lon);
        return my_point;

    }
    /* NOT USED IN CURRENT IMPLEMENTATION
    //maximum antennas just limit, not ordered
    public ArrayList<ARCoord> getMaximumAntennas(double x,double y,int antenum,int range){
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        String stuff[]={String.valueOf(range),String.valueOf(antenum)};
        Cursor cursor = database.rawQuery("select cell,latitude,longitude,altitude,range from cell_towers_greece where range<=? limit ?", stuff);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            coordlist.add(new ARCoord(cursor.getString(0),cursor.getDouble(1),cursor.getDouble(2),cursor.getDouble(3),cursor.getDouble(4)));
            cursor.moveToNext();
        }
        cursor.close();
        return coordlist;
    }
*/
    public ArrayList<ARCoord> getAntennasWithinRadius(double x,double y,double radius,int antenum,int range){ //Make an optimized database search based on range and number of antennas
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        final double mult = 1.1;
        LatLng p1 = calculateDerivedPosition(x,y, mult * radius, 0);   //get 4 locations (east,west,north,south) at 1.1*max range
        LatLng p2 = calculateDerivedPosition(x,y, mult * radius, 90);
        LatLng p3 = calculateDerivedPosition(x,y, mult * radius, 180);
        LatLng p4 = calculateDerivedPosition(x,y, mult * radius, 270);
        double fudge = Math.pow(Math.cos(Math.toRadians(x)),2); //get those locations based on current position, default only works at the equator
        String stuff[]={String.valueOf(range),String.valueOf(p1.latitude),String.valueOf(p3.latitude),String.valueOf(p2.longitude),String.valueOf(p4.longitude),
                String.valueOf(x),String.valueOf(x),String.valueOf(y),String.valueOf(y),String.valueOf(fudge)}; //use those 4 points to limit the sql request so as to make the database search faster
        Cursor cursor=database.rawQuery("SELECT cell,latitude,longitude,altitude,range FROM cell_towers_greece WHERE range<=? AND latitude<? AND latitude>? AND longitude<? AND longitude>? ORDER BY ((? - latitude) * (? - latitude) +" +
                "(? - longitude) * (? - longitude) * ?)",stuff);
        cursor.moveToFirst();
        int ante_counter=0;
        while ((!cursor.isAfterLast()) && (ante_counter<antenum)){   //Check those points from the sql search if they are really within the radius
            double lat0=cursor.getDouble(1);
            double lon0=cursor.getDouble(2);
            if (get_DistanceBetweenTwoPoints(x,y,lat0,lon0)<=radius) {
                ante_counter++;
                coordlist.add(new ARCoord(cursor.getString(0), lat0, lon0, cursor.getDouble(3),cursor.getDouble(4)));
            }
            cursor.moveToNext();
        }

        cursor.close();
        return coordlist;
    }

    public ArrayList<ARCoord> getAntennasWithinRadius(double x,double y,double radius,int range){ //Do the same optimized database search without the use of a number of antennas
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        final double mult = 1.1;
        LatLng p1 = calculateDerivedPosition(x,y, mult * radius, 0);
        LatLng p2 = calculateDerivedPosition(x,y, mult * radius, 90);
        LatLng p3 = calculateDerivedPosition(x,y, mult * radius, 180);
        LatLng p4 = calculateDerivedPosition(x,y, mult * radius, 270);
        String stuff[]={String.valueOf(range),String.valueOf(p1.latitude),String.valueOf(p3.latitude),String.valueOf(p2.longitude),String.valueOf(p4.longitude)};
        Cursor cursor=database.rawQuery("SELECT cell,latitude,longitude,altitude,range FROM cell_towers_greece WHERE range<=? AND latitude<? AND latitude>? AND longitude<? AND longitude>?",stuff);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            double lat0=cursor.getDouble(1);
            double lon0=cursor.getDouble(2);
            if (get_DistanceBetweenTwoPoints(x,y,lat0,lon0)<=radius) {
                coordlist.add(new ARCoord(cursor.getString(0), lat0, lon0, cursor.getDouble(3),cursor.getDouble(4)));
            }
                cursor.moveToNext();
        }

        cursor.close();
        return coordlist;
    }

}
