package com.house.security;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class ProfileDataHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "profileManager";

    // Contacts table name
    private static final String TABLE_PROFILE = "profile";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_STRING = "key_string";
    private static final String KEY_VALUE = "pass_value";
//    private static final String KEY_LOC_DETAILS = "location_details"; // TODO:

	static final int INDEX_LOGIN_ID = 0;
	static final int INDEX_KEY_STRING = 1;
	static final int INDEX_VALUE = 2;

    public ProfileDataHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_PROFILE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_STRING + " TEXT,"
                + KEY_VALUE + " TEXT" +")";

        Log.d("QR_SCAN_PROFILE_DB", "onCreate Login In ");

        db.execSQL(CREATE_LOGIN_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);

        Log.d("QR_SCAN_PROFILE_DB", "onUpgrade In ");

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new entry
    void addEntry(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();

		Log.d("QR_SCAN_PROFILE_DB", "addLogin In "+ key +" "+ value);

        ContentValues values = new ContentValues();
        values.put(KEY_STRING, key);
        values.put(KEY_VALUE, value);

        // Inserting Row
        Log.d("QR_SCAN_PROFILE_DB", "addEntry In "+db.insert(TABLE_PROFILE, null, values));

        db.close(); // Closing database connection
    }

   
     public String getValue(String key) {        
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PROFILE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_PROFILE_DB", "getValue In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do 
			{
				if(key.equals(cursor.getString(INDEX_KEY_STRING)))
				{
	                return cursor.getString(INDEX_VALUE);
	            }
            } while (cursor.moveToNext());
        }

        // return login list
        return null;
    }

    // Deleting single login
    public void deleteLogin(String key) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROFILE, KEY_STRING + " = ?",
                new String[] { key });
        db.close();
    }

	public void deleteAllProfile()
	{
        SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from "+ TABLE_PROFILE);
        db.close();
	}
	

}
