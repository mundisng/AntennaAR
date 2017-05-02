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
        Cursor cursor = database.rawQuery("select cell,lat,lon from cell_towers_greece", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            coordlist.add(new ARCoord(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2)),30.0));
            cursor.moveToNext();
        }
        System.out.println("TEEEEEST: "+coordlist.get(1).getName()+" "+ coordlist.get(1).getLocation().getLatitude()+ " "+ coordlist.get(1).getLocation().getLongitude());
        cursor.close();
        return coordlist;
    }

}
