package com.mundis.kostas4949.antennavr;
import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DatabaseOpenHelper extends SQLiteAssetHelper{ //Handling the database opening
    private static final String DATABASE_NAME = "cell_towers_greece.db";
    private static final int DATABASE_VERSION = 1;
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
