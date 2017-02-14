package com.house.security;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class VisitorDBHandler  extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "VisitorManager";

    // Visitor Columns names
    private static final String KEY_VIS_ID = "vis_id";
    private static final String KEY_VIS_NAME = "vis_name";
    private static final String KEY_VIS_PH = "vis_phone";
    private static final String KEY_VIS_TIME = "vis_time";
    private static final String KEY_VIS_DATE = "vis_date";
    private static final String KEY_VIS_VEHICLE = "vis_vehicle_no";
    private static final String KEY_VIS_STATUS = "vis_status";
    private static final String KEY_VIS_NOTE = "vis_note";
    private static final String KEY_VIS_SQL_ID = "vis_sql_id";
    private static final String KEY_VIS_TIME_IN = "vis_time_in";
    private static final String KEY_VIS_TIME_OUT = "vis_time_out";

    // Visitor management table name
    private static final String TABLE_VISITORS = "visitors";
    private String fullTime;

    public VisitorDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("QR_SCAN_DB", "VisitorDBHandler constructor ");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VISITORS_TABLE = "CREATE TABLE " + TABLE_VISITORS + "("
                + KEY_VIS_SQL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VIS_NAME + " TEXT,"
                + KEY_VIS_DATE + " TEXT," + KEY_VIS_TIME + " TEXT," + KEY_VIS_PH + " TEXT,"
                + KEY_VIS_VEHICLE + " TEXT," + KEY_VIS_STATUS + " TEXT," + KEY_VIS_NOTE + " TEXT,"
                + KEY_VIS_ID + " STRING,"+ KEY_VIS_TIME_IN + " STRING,"+ KEY_VIS_TIME_OUT + " STRING"
                +")";
        db.execSQL(CREATE_VISITORS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // Adding new visitor
    long addVisitor(Visitor visitor) {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("QR_SCAN_DB", "addVisitor In " + visitor.getName() + " " + visitor.getDate() + " " + visitor.getTime() + " " + visitor.getStatus());

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_NAME, visitor.getName());
        if(visitor.getDate() != null)
            values.put(KEY_VIS_DATE, visitor.getDate());
        else
            values.put(KEY_VIS_DATE, "NULL");

        values.put(KEY_VIS_TIME, visitor.getTime());
        values.put(KEY_VIS_PH, visitor.getPhone());
        values.put(KEY_VIS_STATUS, visitor.getStatus());
        values.put(KEY_VIS_VEHICLE, visitor.getVehicleNumber());
        values.put(KEY_VIS_NOTE, visitor.getNote());
        values.put(KEY_VIS_ID, 0);
        values.put(KEY_VIS_TIME_IN, visitor.getInTime());
        values.put(KEY_VIS_TIME_OUT, visitor.getOutTime());

        // Inserting Row
        long val = db.insert(TABLE_VISITORS, null, values);
        Log.d("QR_SCAN_DB", "addVisitor In " + val);

        db.close(); // Closing database connection
        return val;
    }

    // Getting single contact
    Visitor getVisitor(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VISITORS, new String[]{KEY_VIS_SQL_ID,
                        KEY_VIS_NAME, KEY_VIS_PH, KEY_VIS_TIME, KEY_VIS_DATE, KEY_VIS_VEHICLE, KEY_VIS_STATUS, KEY_VIS_NOTE, KEY_VIS_ID, KEY_VIS_TIME_IN,
                        KEY_VIS_TIME_OUT
                }, KEY_VIS_SQL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Visitor vis = new Visitor(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6) ,cursor.getString(7)
        );
        vis.setDbId(cursor.getString(8));
        vis.setInTime(cursor.getString(9));
        vis.setOutTime(cursor.getString(10));

        Log.d("QR_SCAN_DB", "getVisitor :" + vis);

        // return visitor
        return vis;
    }

    // Getting visitor Count
    public synchronized int getVisitorsCount()
    {
        String countQuery = "SELECT  * FROM " + TABLE_VISITORS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        // return count
        return cursor.getCount();
    }

    // Getting All visitor
    public synchronized List<Visitor> getAllVisitors() {
        List<Visitor> visList = new ArrayList<Visitor>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_VISITORS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                Visitor visitor = new Visitor();
                visitor.setId(Integer.parseInt(cursor.getString(0)));

                visitor.setName(cursor.getString(1));
                visitor.setDate(cursor.getString(2));
                visitor.setTime(cursor.getString(3));
                visitor.setPhone(cursor.getString(4));
                visitor.setVehicleNumber(cursor.getString(5));
                visitor.setStatus(cursor.getString(6));
                visitor.setNote(cursor.getString(7));
                visitor.setDbId(cursor.getString(8));
                visitor.setInTime(cursor.getString(9));
                visitor.setOutTime(cursor.getString(10));
                Log.d("QR_SCAN_DB",visitor.toString());

                Log.d("QR_SCAN_DB", "getAllVisitors size "+visList.size()+" "+ cursor.getString(1));

                // Adding contact to list
                visList.add(visitor);
            } while (cursor.moveToNext());
        }
        Log.d("QR_SCAN_DB", visList.toString());
        // return contact list
        return visList;
    }



    public List<String> getallVisDates() {

        Log.d("QR_SCAN_DB","getallVisDates");
        List<String> visDateList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  DISTINCT "+KEY_VIS_DATE+" FROM " + TABLE_VISITORS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                visDateList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        Log.d("QR_SCAN_DB","getallVisDates :"+visDateList);
        // return visitor date list
        return visDateList;
    }

    public List<Visitor> getallVisitorsFromDate(String date) {
        List<Visitor> visList = new ArrayList<Visitor>();
        // Select All Query
        Log.d("db : ","getallVisitorsFromDate date ="+date);
        String selectQuery = "SELECT  * FROM " + TABLE_VISITORS + " WHERE "+KEY_VIS_DATE+" = \'"+date+"\'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("db : ","getallVisitorsFromDate cursor ="+cursor.getCount());
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                Visitor visitor = new Visitor();
                visitor.setId(Integer.parseInt(cursor.getString(0)));
                visitor.setName(cursor.getString(1));
                visitor.setDate(cursor.getString(2));
                visitor.setTime(cursor.getString(3));
                visitor.setPhone(cursor.getString(4));
                visitor.setVehicleNumber(cursor.getString(5));
                visitor.setStatus(cursor.getString(6));
                visitor.setNote(cursor.getString(7));
                visitor.setDbId(cursor.getString(8));
                visitor.setInTime(cursor.getString(9));
                visitor.setOutTime(cursor.getString(10));

                Log.d("QR_SCAN_DB", "getAllVisitors size "+visList.size()+" "+ cursor.getString(1));

                // Adding contact to list
                visList.add(visitor);
            } while (cursor.moveToNext());
        }

        // return visitor list
        return visList;
    }

    public void deleteAllVisitors_onlogout() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_VISITORS);
        db.close();
    }

    public long updateVisID(Visitor visitor) {
        Log.d("QR_SCAN_DB", "Updating Vis ID :" + visitor.getDbId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_ID, visitor.getDbId());

        // updating row
        return db.update(TABLE_VISITORS, values, KEY_VIS_SQL_ID + " = ?",
                new String[] { String.valueOf(visitor.getId()) });

    }

    public long chageStatus(Visitor visitor) {
        Log.d("QR_SCAN_DB","Updating Vis status :"+visitor.getStatus());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_STATUS, visitor.getStatus());

        // updating row
        return db.update(TABLE_VISITORS, values, KEY_VIS_SQL_ID + " = ?",
                new String[] { String.valueOf(visitor.getId()) });

    }

    public boolean CheckVisDataAlreadyInDBorNot(Visitor visitor) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_VISITORS + " WHERE " + KEY_VIS_ID + "=? ",
                new String[]{visitor.getDbId()});

        if(mCursor.getCount() <= 0){
            mCursor.close();
            Log.d("QR_SCAN_DB", "Not present "+visitor.getName());
            return false;
        }
        mCursor.close();
        return true;
    }

    public long setInTime(Visitor visitor, String newtime) {
        Log.d("QR_SCAN_DB","Updating Vis inTime :"+visitor.getInTime());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_TIME_IN, newtime);

        // updating row
        return db.update(TABLE_VISITORS, values, KEY_VIS_SQL_ID + " = ?",
                new String[] { String.valueOf(visitor.getId()) });
    }

    public long setOutTime(Visitor visitor, String newtime) {
        Log.d("QR_SCAN_DB","Updating Vis setOutTime :"+visitor.getOutTime());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_TIME_OUT, newtime);

        // updating row
        return db.update(TABLE_VISITORS, values, KEY_VIS_SQL_ID + " = ?",
                new String[] { String.valueOf(visitor.getId()) });
    }

    public long setFullTime(Visitor visitor, String fullTime) {
        Log.d("QR_SCAN_DB","Updating Vis setFullTime :"+visitor.getTime());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VIS_TIME, fullTime);

        // updating row
        return db.update(TABLE_VISITORS, values, KEY_VIS_SQL_ID + " = ?",
                new String[] { String.valueOf(visitor.getId()) });
    }

    public synchronized void deleteAllVisitors(String date)
    {
        Log.d("QR_SCAN_DB","deleteAllVisitors:"+date);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VISITORS, KEY_VIS_DATE + " = ? ",
                new String[]{date});
        db.close();
    }
}
