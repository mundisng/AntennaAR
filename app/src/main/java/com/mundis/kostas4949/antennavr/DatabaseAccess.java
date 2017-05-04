package com.mundis.kostas4949.antennavr;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;

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

    public static ArrayList<Double> calculateDerivedPosition(double x,double y,
                                                  double range, double bearing)
    {
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

        ArrayList<Double> newPoint=new ArrayList<>();
        //PointF newPoint = new PointF((float) lat, (float) lon);
        newPoint.add(lat);
        newPoint.add(lon);
        return newPoint;

    }

    public ArrayList<ARCoord> getAntennasWithinRadius(double x,double y,double radius){
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        //System.out.println("DOUBLE: x: "+x+" y: "+y+"     FLOAT: x: "+(float)x+" y: "+(float)y);
        //PointF center = new PointF((float)x,(float) y);
        final double mult = 1.1;
        ArrayList<Double> p1 = calculateDerivedPosition(x,y, mult * radius, 0);
        ArrayList<Double> p2 = calculateDerivedPosition(x,y, mult * radius, 90);
        ArrayList<Double> p3 = calculateDerivedPosition(x,y, mult * radius, 180);
        ArrayList<Double> p4 = calculateDerivedPosition(x,y, mult * radius, 270);
        String stuff[]={String.valueOf(p1.get(0)),String.valueOf(p3.get(0)),String.valueOf(p2.get(1)),String.valueOf(p4.get(1))};
        System.out.println("Current coordinates: "+x+" "+y);
   System.out.println("SELECT cell,lat,lon FROM cell_towers_greece WHERE lat<"+String.valueOf(p1.get(0))+" AND lat>"+String.valueOf(p3.get(0))+" AND lon<"+String.valueOf(p2.get(1))+" AND lon>"+String.valueOf(p4.get(1)));
        Cursor cursor=database.rawQuery("SELECT cell,lat,lon FROM cell_towers_greece WHERE lat<? AND lat>? AND lon<? AND lon>?",stuff);
      //  Cursor cursor=database.rawQuery("SELECT cell,lat,lon FROM cell_towers_greece a WHERE (acos(sin(a.lat * 0.0175)" +
        //                "* sin(? * 0.0175)+ cos(a.lat * 0.0175) * cos(? * 0.0175) *"
        //        +"cos((? * 0.0175) - (a.lon * 0.0175))) * 6371 <= ?) ",stuff);  //6371 for km, 3959 for miles
        cursor.moveToFirst();
        double R=6371000;
        while (!cursor.isAfterLast()){
            double dLat=Math.toRadians(x-Double.parseDouble(cursor.getString(1)));
            double dLon=Math.toRadians(y-Double.parseDouble(cursor.getString(2)));
            double lat1=Math.toRadians(Double.parseDouble(cursor.getString(1)));
            double lat2=Math.toRadians(x);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                    * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;
            if (d<=radius) {
                coordlist.add(new ARCoord(cursor.getString(0), Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(2)), 30.0));
                System.out.println("RADIUS: "+coordlist.get(0).getName()+" "+ coordlist.get(0).getLocation().getLatitude()+ " "+ coordlist.get(0).getLocation().getLongitude());
            }
                cursor.moveToNext();
        }

        cursor.close();
        return coordlist;
    }

}
