package com.mundis.kostas4949.antennavr;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by kostas4949 on 2/5/2017.
 */

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;


    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    public ArrayList<ARCoord> getAllCellCoords(){
        ArrayList<ARCoord> coordlist = new ArrayList<>();
        Cursor cursor = database.rawQuery("select cell,lat,lon from cell_towers_greece limit 6", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            coordlist.add(new ARCoord(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2)),30.0));
            cursor.moveToNext();
        }
        System.out.println("TEEEEEST: "+coordlist.get(0).getName()+" "+ coordlist.get(0).getLocation().getLatitude()+ " "+ coordlist.get(0).getLocation().getLongitude());
        cursor.close();
        System.out.println("LISTA ME POSA STOIXEIA "+coordlist.size());
        return coordlist;
    }

    public LatLng calculateDerivedPosition(double x, double y, double range, double bearing) {
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
    public ArrayList<ARCoord> getAntennasWithinRadius(double x,double y,double radius,int antenum){
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        final double mult = 1.1;
        LatLng p1 = calculateDerivedPosition(x,y, mult * radius, 0);
        LatLng p2 = calculateDerivedPosition(x,y, mult * radius, 90);
        LatLng p3 = calculateDerivedPosition(x,y, mult * radius, 180);
        LatLng p4 = calculateDerivedPosition(x,y, mult * radius, 270);
        System.out.println("Current coordinates: "+x+" "+y);
        double fudge = Math.pow(Math.cos(Math.toRadians(x)),2);
        String stuff[]={String.valueOf(p1.latitude),String.valueOf(p3.latitude),String.valueOf(p2.longitude),String.valueOf(p4.longitude),
                String.valueOf(x),String.valueOf(x),String.valueOf(y),String.valueOf(y),String.valueOf(fudge)};
        System.out.println("SELECT cell,latitude,longitude,altitude FROM cell_towers_greece WHERE latitude<"+String.valueOf(p1.latitude)+" AND latitude>"+String.valueOf(p3.latitude)+" AND longitude<"+String.valueOf(p2.longitude)+" AND longitude>"+String.valueOf(p4.longitude));
        Cursor cursor=database.rawQuery("SELECT cell,latitude,longitude,altitude FROM cell_towers_greece WHERE latitude<? AND latitude>? AND longitude<? AND longitude>? ORDER BY ((? - latitude) * (? - latitude) +" +
                "(? - longitude) * (? - longitude) * ?)",stuff);
        cursor.moveToFirst();
        double R=6371000;
        int ante_counter=0;
        while ((!cursor.isAfterLast()) && (ante_counter<antenum)){
            System.out.println("Got cell: "+cursor.getString(0));
            double lat0=cursor.getDouble(1);
            double lon0=cursor.getDouble(2);
            double dLat=Math.toRadians(x-lat0);
            double dLon=Math.toRadians(y-lon0);
            double lat1=Math.toRadians(lat0);
            double lat2=Math.toRadians(x);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                    * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;
            if (d<=radius) {
                ante_counter++;
                coordlist.add(new ARCoord(cursor.getString(0), lat0, lon0, cursor.getDouble(3)));
                System.out.println("RADIUS: "+coordlist.get(ante_counter-1).getName()+" "+ coordlist.get(ante_counter-1).getLocation().getLatitude()+ " "+ coordlist.get(ante_counter-1).getLocation().getLongitude()+" "+coordlist.get(ante_counter-1).getLocation().getAltitude());
            }
            cursor.moveToNext();
        }

        cursor.close();
        return coordlist;
    }

    public ArrayList<ARCoord> getAntennasWithinRadius(double x,double y,double radius){
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        //System.out.println("DOUBLE: x: "+x+" y: "+y+"     FLOAT: x: "+(float)x+" y: "+(float)y);
        //PointF center = new PointF((float)x,(float) y);
        final double mult = 1.1;
        LatLng p1 = calculateDerivedPosition(x,y, mult * radius, 0);
        LatLng p2 = calculateDerivedPosition(x,y, mult * radius, 90);
        LatLng p3 = calculateDerivedPosition(x,y, mult * radius, 180);
        LatLng p4 = calculateDerivedPosition(x,y, mult * radius, 270);
        String stuff[]={String.valueOf(p1.latitude),String.valueOf(p3.latitude),String.valueOf(p2.longitude),String.valueOf(p4.longitude)};
        System.out.println("Current coordinates: "+x+" "+y);
   System.out.println("SELECT cell,latitude,longitude,altitude FROM cell_towers_greece WHERE latitude<"+String.valueOf(p1.latitude)+" AND latitude>"+String.valueOf(p3.latitude)+" AND longitude<"+String.valueOf(p2.longitude)+" AND longitude>"+String.valueOf(p4.longitude));
        Cursor cursor=database.rawQuery("SELECT cell,latitude,longitude,altitude FROM cell_towers_greece WHERE latitude<? AND latitude>? AND longitude<? AND longitude>?",stuff);
      //  Cursor cursor=database.rawQuery("SELECT cell,lat,lon FROM cell_towers_greece a WHERE (acos(sin(a.lat * 0.0175)" +
        //                "* sin(? * 0.0175)+ cos(a.lat * 0.0175) * cos(? * 0.0175) *"
        //        +"cos((? * 0.0175) - (a.lon * 0.0175))) * 6371 <= ?) ",stuff);  //6371 for km, 3959 for miles
        cursor.moveToFirst();
        double R=6371000;
        while (!cursor.isAfterLast()){
            double lat0=cursor.getDouble(1);
            double lon0=cursor.getDouble(2);
            double dLat=Math.toRadians(x-lat0);
            double dLon=Math.toRadians(y-lon0);
            double lat1=Math.toRadians(lat0);
            double lat2=Math.toRadians(x);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                    * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;
            if (d<=radius) {
                coordlist.add(new ARCoord(cursor.getString(0), lat0, lon0, cursor.getDouble(3)));
                System.out.println("RADIUS: "+coordlist.get(0).getName()+" "+ coordlist.get(0).getLocation().getLatitude()+ " "+ coordlist.get(0).getLocation().getLongitude()+" "+coordlist.get(0).getLocation().getAltitude());
            }
                cursor.moveToNext();
        }

        cursor.close();
        return coordlist;
    }

}
