package com.mundis.kostas4949.antennavr;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        System.out.println("TEEEEEST: "+coordlist.get(1).getName()+" "+ coordlist.get(1).getLocation().getLatitude()+ " "+ coordlist.get(1).getLocation().getLongitude());
        cursor.close();
        System.out.println("LISTA ME POSA STOIXEIA "+coordlist.size());
        return coordlist;
    }

    public ArrayList<ARCoord> getAntennasWithinRadius(String x,String y,String radius){
        ArrayList<ARCoord> coordlist=new ArrayList<>();
        String stuff[]={x,x,y,radius};
        Cursor cursor=database.rawQuery("SELECT cell,lat,lon FROM cell_towers_greece a WHERE (acos(sin(a.lat * 0.0175)" +
                        "* sin(? * 0.0175)+ cos(a.lat * 0.0175) * cos(? * 0.0175) *"
                +"cos((? * 0.0175) - (a.lon * 0.0175))) * 6371 <= ?) ",stuff);  //6371 for km, 3959 for miles
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            coordlist.add(new ARCoord(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2)),30.0));
            cursor.moveToNext();
        }
        System.out.println("RADIUS: "+coordlist.get(1).getName()+" "+ coordlist.get(1).getLocation().getLatitude()+ " "+ coordlist.get(1).getLocation().getLongitude());
        cursor.close();
        return coordlist;
    }

}
