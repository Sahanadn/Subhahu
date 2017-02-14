package com.house.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

public class AttendenceDataBaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "attendenceManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "attendence";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_QR_NAME = "name";
    private static final String KEY_SCAN_DATE = "scan_date";
    private static final String KEY_SCAN_TIME = "scan_time";
    private static final String KEY_SCAN_LOCATION = "scan_location";

    private static final String KEY_SCAN_GAP = "scan_gap";
    private static final String KEY_SCAN_FULLTIME = "scan_fulltime";
    private static final String KEY_SCAN_SSID = "scan_ssid";
    private static final String KEY_SCAN_ROUND = "scan_round";
    private static final String KEY_SCAN_USERNAME = "scan_username";
	private static final String KEY_SCAN_ROUNDTYPE = "scan_roundtype";

	public static String m_max_gap;
	public static String m_above_conf;

    public AttendenceDataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d("QR_SCAN_DB", "DatabaseHandler constructor ");
	}

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_QR_NAME + " TEXT,"
                + KEY_SCAN_DATE + " TEXT," + KEY_SCAN_TIME + " TEXT," + KEY_SCAN_LOCATION + " TEXT," 
                + KEY_SCAN_FULLTIME + " TEXT," + KEY_SCAN_GAP + " TEXT," + KEY_SCAN_SSID + " TEXT," 
                + KEY_SCAN_ROUND + " TEXT," + KEY_SCAN_USERNAME + " TEXT," + KEY_SCAN_ROUNDTYPE + " TEXT" +")";

        Log.d("QR_SCAN_DB", "onCreate In ");

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        Log.d("QR_SCAN_DB", "onUpgrade In ");

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new attendence
    synchronized void  addAttendence(Attendence attendence) {
        SQLiteDatabase db = this.getWritableDatabase();

		Log.d("QR_SCAN_DB", "addContact In "+ attendence.getName() +", "+attendence.getDate() +",getTime "+attendence.getTime() +", "+attendence.getLocation());
		Log.d("QR_SCAN_DB", "addContact In getFullTime "+ attendence.getFullTime() +",getGapTime "+attendence.getGapTime() );

        ContentValues values = new ContentValues();
        values.put(KEY_QR_NAME, attendence.getName());
        values.put(KEY_SCAN_DATE, attendence.getDate());
        values.put(KEY_SCAN_TIME, attendence.getTime());
        values.put(KEY_SCAN_LOCATION, attendence.getLocation());
        values.put(KEY_SCAN_FULLTIME, attendence.getFullTime());
        values.put(KEY_SCAN_GAP, attendence.getGapTime());
        values.put(KEY_SCAN_SSID, attendence.getSSID());
		values.put(KEY_SCAN_ROUND, attendence.getRound());
		values.put(KEY_SCAN_USERNAME, attendence.getUsername());
		values.put(KEY_SCAN_ROUNDTYPE, attendence.getRoundType());		
		

		
        // Inserting Row
        Log.d("QR_SCAN_DB", "addContact In "+db.insert(TABLE_CONTACTS, null, values));

        db.close(); // Closing database connection
    }

	public boolean CheckIsDataAlreadyInDBorNot(Attendence attendence) {
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " WHERE	"+KEY_QR_NAME+"=? AND "+KEY_SCAN_DATE+"=? AND "+KEY_SCAN_FULLTIME+"=? AND "
			+KEY_SCAN_USERNAME+"=?", 
			new String[]{attendence.getName(),attendence.getDate(),attendence.getFullTime(),attendence.getUsername()});

			if(mCursor.getCount() <= 0){
				mCursor.close();
				Log.d("QR_SCAN_DB", "Not present "+attendence.getName()+" "+attendence.getDate()+" "+attendence.getUsername());
				return false;
			}

		Log.d("QR_SCAN_DB", " present "+attendence.getName()+" "+attendence.getDate()+" "+attendence.getUsername());

		mCursor.close();
		return true;
	}


    // Getting single attendence
    synchronized Attendence getAttendence(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                KEY_QR_NAME, KEY_SCAN_DATE, KEY_SCAN_TIME, KEY_SCAN_LOCATION, KEY_SCAN_FULLTIME, KEY_SCAN_GAP, KEY_SCAN_SSID, 
                KEY_SCAN_ROUND, KEY_SCAN_USERNAME, KEY_SCAN_ROUNDTYPE }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
		
        if (cursor != null)
            cursor.moveToFirst();

        Attendence attendence = new Attendence(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
                cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10));
        // return attendence
        return attendence;
    }

	 // Getting single attendence
    synchronized Attendence getAttendence(String id) {
		 SQLiteDatabase db = this.getReadableDatabase();
 
		 Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
				 KEY_QR_NAME, KEY_SCAN_DATE, KEY_SCAN_TIME, KEY_SCAN_LOCATION, KEY_SCAN_FULLTIME, KEY_SCAN_GAP, 
				 KEY_SCAN_SSID, KEY_SCAN_ROUND, KEY_SCAN_USERNAME, KEY_SCAN_ROUNDTYPE}, KEY_ID + "=?",
				 new String[] { id }, null, null, null, null);
		 
		 if (cursor != null)
			 cursor.moveToFirst();

		if (null == cursor)
		{
			Log.d("QR_SCAN_DB", "getContact return cursor is null "+cursor);
			return null;
		}
		 Attendence attendence = new Attendence(Integer.parseInt(cursor.getString(0)),
				 cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
				 cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10));

		 return attendence;
	 }


	 public synchronized List<Attendence> getAttendencesFromDate(String date) {
		 List<Attendence> contactList = new ArrayList<Attendence>();
		 // Select All Query
		 String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 
		 SQLiteDatabase db = this.getWritableDatabase();
		 Cursor cursor = db.rawQuery(selectQuery, null);
 
		 Log.d("QR_SCAN_DB", "getContactsFromDate In "+cursor.getCount());
 
		 // looping through all rows and adding to list
		 if (cursor.moveToFirst()) {
			 do 
			 {
				 if(date.equals(cursor.getString(2)))
				 {
					 Attendence attendence = new Attendence();
					 attendence.setID(Integer.parseInt(cursor.getString(0)));
					 attendence.setName(cursor.getString(1));
					 attendence.setDate(cursor.getString(2));
					 attendence.setTime(cursor.getString(3));
					 attendence.setLocation(cursor.getString(4));
 
					 attendence.setFullTime(cursor.getString(5));
					 attendence.setGapTime(cursor.getString(6));
					 attendence.setSSID(cursor.getString(7));
 // 				 attendence.setRound(cursor.getString(8));
					 attendence.setUsername(cursor.getString(9));
 					attendence.setRoundType(cursor.getString(10));
 
					 // Adding attendence to list
					 contactList.add(attendence);
				 }
			 } while (cursor.moveToNext());
		 }
 
		 // return attendence list
		 return contactList;
	 }


     public synchronized List<Attendence> getContactsFromQr(String qrName) {
        List<Attendence> contactList = new ArrayList<Attendence>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getContactsFromQr In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do 
			{
				if(qrName.equals(cursor.getString(1)))
				{
	                Attendence attendence = new Attendence();
	                attendence.setID(Integer.parseInt(cursor.getString(0)));
	                attendence.setName(cursor.getString(1));
	                attendence.setDate(cursor.getString(2));
					attendence.setTime(cursor.getString(3));
					attendence.setLocation(cursor.getString(4));

					attendence.setFullTime(cursor.getString(5));
					attendence.setGapTime(cursor.getString(6));
					attendence.setSSID(cursor.getString(7));
//					attendence.setRound(cursor.getString(8));
					attendence.setUsername(cursor.getString(9));
					attendence.setRoundType(cursor.getString(10));

	                // Adding attendence to list
	                contactList.add(attendence);
	            }
            } while (cursor.moveToNext());
        }

        // return attendence list
        return contactList;
    }

     public synchronized Rounds getRounds(String date) {
     
	 Rounds round = new Rounds();
	 round.setDate(date);
	 
     // Select All Query
     String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

     SQLiteDatabase db = this.getWritableDatabase();
     Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllRounds In "+cursor.getCount());

     // looping through all rows and adding to list
     if (cursor.moveToFirst()) {
         do {
             
        	 if ( !round.getDate().equals(cursor.getString(2)) )
        		 continue;
        	 
             if(round.setName(cursor.getString(1), cursor.getString(6), cursor.getString(3)))
             {
            	 round.setID(Integer.parseInt(cursor.getString(0)));
            	 // Adding attendence to list
             }
             /*
             else
             {
            	 attendence = new Rounds();
            	 attendence.setName(cursor.getString(1));
            	 attendence.setID(Integer.parseInt(cursor.getString(0)));            	 
            	 contactList.add(attendence);
             }
             */
         } while (cursor.moveToNext());
     }

     return round;
 }
     
	public synchronized List<Rounds> getAllRounds(String date) 
	{
		List<Rounds> roundList = new ArrayList<Rounds>();
		Rounds round = new Rounds();
		round.setDate(date);
		roundList.add(round);


		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllRounds In "+cursor.getCount() +" "+date);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do {

				if ( !round.getDate().equals(cursor.getString(2)) )
				 continue;

				if(round.setName(cursor.getString(1), cursor.getString(6), cursor.getString(3)))
				{
					 round.setIndex(cursor.getPosition());
					 Log.d("QR_SCAN_DB", "getAllRounds addding  "+cursor.getString(1) +" "+cursor.getPosition()
					 	+" "+date);				 
					 // Adding attendence to list
				}             
				else
				{
					 round = new Rounds();
					 round.setDate(date);
					 round.setName(cursor.getString(1), cursor.getString(6), cursor.getString(3));
					 round.setIndex(cursor.getPosition());            	 
					 Log.d("QR_SCAN_DB", "getAllRounds addding  "+cursor.getString(1) +" "+cursor.getPosition());
					 roundList.add(round);
				}
			} while (cursor.moveToNext());
		}

		Log.d("QR_SCAN_DB", "getAllRounds returns "+roundList.size());

		// return attendence list
		return roundList;
	}

    // Getting All Attendence
    public synchronized List<Attendence> getAllAttendence() {
        List<Attendence> contactList = new ArrayList<Attendence>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllContacts In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Attendence attendence = new Attendence();
                attendence.setID(Integer.parseInt(cursor.getString(0)));
                attendence.setName(cursor.getString(1));
                attendence.setDate(cursor.getString(2));
				attendence.setTime(cursor.getString(3));
				attendence.setLocation(cursor.getString(4));

				attendence.setFullTime(cursor.getString(5));
				attendence.setGapTime(cursor.getString(6));
				attendence.setSSID(cursor.getString(7));
				attendence.setUsername(cursor.getString(9));
				attendence.setRoundType(cursor.getString(10));

				Log.d("QR_SCAN_DB", "getAllContacts size "+contactList.size()+" "+ cursor.getString(1));

                // Adding attendence to list
                contactList.add(attendence);
            } while (cursor.moveToNext());
        }

        // return attendence list
        return contactList;
    }

	// Getting Attendence Dates
	public synchronized List<String> getAllAttendenceDates() {
		List<String> contactList = new ArrayList<String>();
		// Select All Query
		
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllAttendenceDates In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(false == contactList.contains(cursor.getString(2)))
				{
					contactList.add(cursor.getString(2));
					Log.d("QR_SCAN_DB", "ScanDate present Is "+cursor.getString(2));

				}
			} while (cursor.moveToNext());
		}
 
		Log.d("QR_SCAN_DB", "getAllAttendenceDates Out "+contactList.size());
		return contactList;
	}

   // Getting Attendence Names
   public synchronized List<String> getAllUsernames() 
   {
	   List<String> usernameList = new ArrayList<String>();
	   // Select All Query
	   
	   String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
   
	   SQLiteDatabase db = this.getWritableDatabase();
	   Cursor cursor = db.rawQuery(selectQuery, null);
   
	   Log.d("QR_SCAN_DB", "getAllUsernames In "+cursor.getCount());
   
	   // looping through all rows and adding to list
	   if (cursor.moveToFirst()) {
		   do { 		
			   if(false == usernameList.contains(cursor.getString(9)))
			   {
				   usernameList.add(cursor.getString(9));
				   Log.d("QR_SCAN_DB", "Username present Is "+cursor.getString(9));
   
			   }
		   } while (cursor.moveToNext());
	   }
   
	   Log.d("QR_SCAN_DB", "getAllUsernames Out "+usernameList.size());
	   return usernameList;
   }


	// Getting Attendence Names by date
	public synchronized List<String> getAllAttendenceNamesbyDate(String Date) 
	{
		List<String> contactList = new ArrayList<String>();
		// Select All Query
		
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllAttendenceNamesbyDate In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(false == contactList.contains(cursor.getString(1)) && 
					Date.equals(cursor.getString(2)))
				{
					contactList.add(cursor.getString(1));
					Log.d("QR_SCAN_DB", "ScanCode present Is "+cursor.getString(1));

				}
			} while (cursor.moveToNext());
		}
 
		Log.d("QR_SCAN_DB", "getAllAttendenceNamesbyDate In "+contactList.size());
		return contactList;
	}


    // Getting Attendence Names
    public synchronized List<String> getAllAttendenceNames() 
   	{
        List<String> contactList = new ArrayList<String>();
        // Select All Query
        
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_DB", "getAllAttendenceNames In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {         
                if(false == contactList.contains(cursor.getString(1)))
                {
                	contactList.add(cursor.getString(1));
            		Log.d("QR_SCAN_DB", "ScanCode present Is "+cursor.getString(1));

                }
            } while (cursor.moveToNext());
        }
 
		Log.d("QR_SCAN_DB", "getAllAttendenceNames In "+contactList.size());

        // looping through all rows and adding to list
  /*      if (cursor.moveToFirst()) {
            do {
                // Adding String to list
                contactList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
*/
        // return attendence list
        return contactList;
    }

	public synchronized List<String> get_attendenceOnlyLocation(String code_name, String date, List<String> indices)
	{
		List<String> locationList = new ArrayList<String>();
		int index = 0;

		if (null == code_name || null == date || null == indices)
		{
			Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation Invalid params ");
			return locationList;
		}
		
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
				
		Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation In "+cursor.getCount()+" index_size "+indices.size() +" date "+date);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			
			while (index < indices.size())
			{
				int position = Integer.parseInt(indices.get(index++));
				//int position = Integer.valueOf(indices.get(index++));
				cursor.moveToPosition(position);

				Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation date position "+position+" date "+cursor.getString(2));
				Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation scancode position "+position+" code_name "+cursor.getString(1));		
				if(date.equals(cursor.getString(2)) && code_name.equals(cursor.getString(1)))
				{
					locationList.add(cursor.getString(4));
					Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation location "+cursor.getString(4));
				}
			
				cursor.moveToPrevious();
			}
		}

		Log.d("QR_SCAN_DB", "get_attendenceOnlyLocation list size "+locationList.size());
		return locationList;
	}


	public synchronized List<String> get_AttendenceAllLocation(String code_name, String date, List<String> indices)
	{
        List<String> locationList = new ArrayList<String>();
		int index = 0;
		
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
				
		Log.d("QR_SCAN_DB", "get_AttendenceAllLocation In "+cursor.getCount()+" index_size "+indices.size() +" date "+date);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			
			while (index < indices.size())
			{
				int position = Integer.parseInt(indices.get(index++));
				//int position = Integer.valueOf(indices.get(index++));
				cursor.moveToPosition(position);

				Log.d("QR_SCAN_DB", "get_AttendenceAllLocation position "+position+" date "+cursor.getString(2));
		
				if(date.equals(cursor.getString(2)))
				{
					locationList.add(cursor.getString(4));
					Log.d("QR_SCAN_DB", "get_AttendenceAllLocation location "+cursor.getString(4));
				}
			
				cursor.moveToPrevious();
			}
		}
		Log.d("QR_SCAN_DB", "get_AttendenceAllLocation list size "+locationList.size());
		return locationList;
	}

	// Getting attendence location
	public synchronized String get_attendenceLocation(String code_name, String date)
	{
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int loc_found = 0;

		if (null == code_name || null == date)
			return "NA";

		Log.d("QR_SCAN_DB", "get_attendenceLocation In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(true == code_name.equals(cursor.getString(1)) &&
					date.equals(cursor.getString(2)))
				{
					loc_found++;
					break;
				}
			} while (cursor.moveToNext());
		}

 		if (0 != loc_found)
 		{
			Log.d("QR_SCAN_DB", "get_attendenceLocation out "+cursor.getString(4));
			return cursor.getString(4);
 		}
		else
		{
			Log.d("QR_SCAN_DB", "get_attendenceLocation out NA");
			return "NA";
		}
	}

	public synchronized String get_startTimeFromRounds(List<String> indices) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int index = 0;

		if (null == indices)
		{
			Log.d("QR_SCAN_DB", "get_startTimeFromRounds Invalid params ");
			return "NA";
		}
		
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
						
			int position = Integer.parseInt(indices.get(index));
			cursor.moveToPosition(position);

			Log.d("QR_SCAN_DB", "get_startTimeFromRounds date position "+position+" start time "+cursor.getString(5));
			return cursor.getString(5);
				
		}

		return null;
	}


	public synchronized String get_lastTimeFromRounds(List<String> indices) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int index = 0;

		if (null == indices)
		{
			Log.d("QR_SCAN_DB", "get_lastTimeFromRounds Invalid params ");
			return "NA";
		}

		index = indices.size() == 0?0:(indices.size()-1);

		
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
						
			int position = Integer.parseInt(indices.get(index));
			cursor.moveToPosition(position);

			Log.d("QR_SCAN_DB", "get_lastTimeFromRounds date position "+position+" start time "+cursor.getString(5));
			return cursor.getString(5);
				
		}

		return null;
	}


	// Getting attendence ssid for the round
	public synchronized String get_attendenceSSIDFromRounds(String code_name, String date, List<String> indices) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int ssid_found = 0;
		int index = 0;

		if (null == code_name || null == date || null == indices)
		{
			Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds Invalid params ");
			return "NA";
		}
		
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			
			while (index < indices.size())
			{
				int position = Integer.parseInt(indices.get(index++));
				cursor.moveToPosition(position);
	
				Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds date position "+position+" date "+cursor.getString(2));
				Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds scancode position "+position+" code_name "+cursor.getString(1));		
				if(date.equals(cursor.getString(2)) && code_name.equals(cursor.getString(1)))
				{					
					Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds location "+cursor.getString(7));
					ssid_found++;
					break;
				}
			
				cursor.moveToPrevious();
			}
		}



 		if (0 != ssid_found)
 		{
			Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds out "+cursor.getString(7));
			return cursor.getString(7);
 		}
		else
		{
			Log.d("QR_SCAN_DB", "get_attendenceSSIDFromRounds out NA");
			return "NA";
		}
	}


	// Getting attendence ssid
	public synchronized String get_attendenceSSID(String code_name, String date) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int ssid_found = 0;

		if (null == code_name || null == date)
			return "NA";

		Log.d("QR_SCAN_DB", "get_attendenceSSID In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(true == code_name.equals(cursor.getString(1)) &&
					date.equals(cursor.getString(2)))
				{
					ssid_found++;
					break;
				}
			} while (cursor.moveToNext());
		}

 		if (0 != ssid_found)
 		{
			Log.d("QR_SCAN_DB", "get_attendenceSSID out "+cursor.getString(7));
			return cursor.getString(7);
 		}
		else
		{
			Log.d("QR_SCAN_DB", "get_attendenceSSID out NA");
			return "NA";
		}
	}

	// Getting attendence counts
	public synchronized int get_attendenceCounts(String code_name, String date) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int scan_count = 0;
		if (null == code_name || null == date)
			return scan_count;

		Log.d("QR_SCAN_DB", "get_attendenceCounts In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(true == code_name.equals(cursor.getString(1)) &&
					date.equals(cursor.getString(2)))
				{
					scan_count++;
				}
			} while (cursor.moveToNext());
		}
 
		Log.d("QR_SCAN_DB", "get_attendenceCounts out "+scan_count);

		return scan_count;
	}

	// Getting attendence code: avg gap time, max gap time and number of above limits
	public synchronized String get_attendencegap_details
			(String code_name, String date, int gap_conf, String max_gap, String above_conf)
	{
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int scan_count = 0;
		int gap_time = 0;
		float gap_sum_avg = 0;
		int gap_max = 0;
		int gap_count_above_conf = 0;

		Log.d("QR_SCAN_DB", "get_attendencegap_details In ");
		
		if (null == code_name || null == date)
			return "0 min";

		Log.d("QR_SCAN_DB", "get_attendencegap_details In "+code_name+" size"+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				if(true == code_name.equals(cursor.getString(1))&&
					date.equals(cursor.getString(2)))
				{
					String gap_timestr = cursor.getString(6);
					Log.d("QR_SCAN_DB", "get_attendencegap_details gap time "+gap_timestr);
					if (gap_timestr != null)
					{
						String parts [] = gap_timestr.split("hr ");
						String hour = parts[0];
						String min = parts [1];
						Log.d("QR_SCAN_DB", "get_attendencegap_details min "+min+" hour "+hour);

						String parts_min [] = min.split("min");
						min = parts_min[0];

						Log.d("QR_SCAN_DB", "get_attendencegap_details min "+min+" hour "+hour);
						
						int minutes = Integer.parseInt(hour) * 60 + Integer.parseInt(min);
						gap_sum_avg += minutes;
						
						Log.d("QR_SCAN_DB","sum_gap "+gap_sum_avg +" minutes "+minutes);
						scan_count ++;
		
						if (0 == gap_max)
							gap_max = minutes;
						else
							gap_max = gap_max > minutes ? gap_max : minutes;
						
						if (minutes > gap_conf)
							gap_count_above_conf ++;
					}
				}
			} while (cursor.moveToNext());
		}

		gap_sum_avg = gap_sum_avg/scan_count;
		m_above_conf = ""+gap_count_above_conf;
		m_max_gap = String.valueOf(gap_max)+" min";
		Log.d("QR_SCAN_DB", "get_attendencegap_details out "+scan_count);

		Log.d("QR_SCAN_DB","avg_gap "+String.valueOf(gap_sum_avg)+" max_gap "+max_gap
				+" min code "+code_name+" above_conf_cnt "+above_conf+" code_len "+cursor.getCount() );

		return String.valueOf(gap_sum_avg)+" min";
	}


    // Updating single attendence
    public synchronized int updateAttendence(Attendence attendence) 
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_QR_NAME, attendence.getName());
        values.put(KEY_SCAN_DATE, attendence.getDate());
        values.put(KEY_SCAN_TIME, attendence.getTime());
        values.put(KEY_SCAN_LOCATION, attendence.getLocation());

        values.put(KEY_SCAN_FULLTIME, attendence.getFullTime());
        values.put(KEY_SCAN_GAP, attendence.getGapTime());
        values.put(KEY_SCAN_SSID, attendence.getSSID());
        values.put(KEY_SCAN_ROUND, attendence.getRound());
		values.put(KEY_SCAN_USERNAME, attendence.getUsername());
		values.put(KEY_SCAN_ROUNDTYPE, attendence.getRoundType());
		
        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(attendence.getID()) });
    }

    // Deleting single attendence
    public synchronized void deleteAttendence(Attendence attendence) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(attendence.getID()) });
        db.close();
    }

	public synchronized void deleteAllAttendence(String date)
	{
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_SCAN_DATE+ " = ?",
                new String[] { date });
        db.close();
	}

	public synchronized void deleteAllAttendence()
	{
        SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from "+ TABLE_CONTACTS);
        db.close();
	}
	
    // Getting Attendence Count
    public synchronized int getAttendencesCount() 
    {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public static String Datetime()
	{
		Calendar c = Calendar .getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mms");
		String formattedDate = df.format(c.getTime());


		Calendar cal = Calendar.getInstance();

	  int minute = cal.get(Calendar.MINUTE);
	  int hour = cal.get(Calendar.HOUR);		//12 hour format
	 int hourofday = cal.get(Calendar.HOUR_OF_DAY);

	  String am_pm = hourofday > 12 ? "pm":"am";

	  formattedDate = hour+((minute<=9)?"0":"")+minute+""+am_pm;

		//Log.d("QR_SCAN_DB", " "+days[dayofweek] +" "+dayofmonth+" "+year+" "+" "+hour+":"+minute+" "+am_pm);
		Log.d("QR_SCAN_DB",  formattedDate);


		Calendar c1 = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate_new = formatter.format(c1.getTime());

		Log.d("QR_SCAN_DB New ",  formattedDate_new+"-"+formattedDate);

		return formattedDate_new+"-"+formattedDate;
	}

	public String export()
	{

		SQLiteDatabase sqldb = this.getReadableDatabase(); //My Database class
		Cursor c = null;
		String filename = null;
		 { //main code begins here
			try {
				String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

				c = sqldb.rawQuery(selectQuery, null);
				int rowcount = 0;
				int colcount = 0;
//				File sdCardDir = Environment.getExternalStorageDirectory();
				filename = "/MyBackUp1"+Datetime()+".csv";

				String inputfilePath = Environment.getExternalStorageDirectory()
										.getPath() + "/"+filename;


				Log.d("QR_SCAN_DB", "export In "+inputfilePath);

				// the name of the file to export with
				//File saveFile = new File(sdCardDir, filename);
				File saveFile = new File(inputfilePath);

				FileWriter fw = new FileWriter(saveFile);
				BufferedWriter bw = new BufferedWriter(fw);
				rowcount = c.getCount();
				colcount = c.getColumnCount();
				if (rowcount > 0) {
					c.moveToFirst();
					for (int i = 0; i < colcount; i++) {
						if (i != colcount - 1) {
							bw.write(c.getColumnName(i) + ",");
						} else {
							bw.write(c.getColumnName(i));
						}
					}
					bw.newLine();
					for (int i = 0; i < rowcount; i++) {
						c.moveToPosition(i);
						for (int j = 0; j < colcount; j++) {
							if (j != colcount - 1)
								bw.write(c.getString(j) + ",");
							else
								bw.write(c.getString(j));
						}
						bw.newLine();
					}
					bw.flush();
					Log.d("QR_SCAN_DB", "Exported Successfully");
				}
			} catch (Exception ex) {
				if (sqldb.isOpen()) {
					sqldb.close();
					Log.d("QR_SCAN_DB", "Exported failure "+ex.getMessage().toString());
				}
			} finally {
			}
		}

		return filename;
	}

}
