package com.example.android.decorsinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.decorsinventory.data.DecorContract.DecorEntry;

/**
 * Database helper for Decors Inventory app. Manages database creation and version management.
 */

public class DecorDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    public static final String DATABASE_NAME = "Decors.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    public static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DecorEntry.TABLE_NAME + " (" +
                    DecorEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_NAME + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_MATERIAL + INTEGER_TYPE + " NOT NULL" + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_HEIGHT + INTEGER_TYPE + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_PRICE + REAL_TYPE + " NOT NULL DEFAULT 0" + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_QUANTITY + INTEGER_TYPE + " NOT NULL DEFAULT 0" + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_SUPPLIER_NAME + TEXT_TYPE + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_SUPPLIER_EMAIL + TEXT_TYPE + COMMA_SEP +
                    DecorEntry.COLUMN_DECOR_IMAGE + BLOB_TYPE +
                    ");";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXIST " + DecorEntry.TABLE_NAME;

    /**
     * Constructs a new instance of {@link DecorDbHelper}.
     *
     * @param context of the app
     */
    public DecorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.e(DATABASE_NAME, "on create with DbHelper:" + SQL_CREATE_ENTRIES);
    }

    /**
     * This is called when the database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
