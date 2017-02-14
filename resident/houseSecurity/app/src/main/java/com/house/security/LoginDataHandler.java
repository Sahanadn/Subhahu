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

public class LoginDataHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "loginManager";

    // Contacts table name
    private static final String TABLE_lOGIN = "login";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_PASSWORD_INDEX = "pass_index";
    private static final String KEY_REG_DATE = "reg_date";
    private static final String KEY_REG_STATUS = "reg_status";
    private static final String KEY_OWNER_NAME = "owner_name";
//    private static final String KEY_LOC_DETAILS = "location_details"; // TODO:

	static final int INDEX_LOGIN_ID = 0;
	static final int INDEX_PHONE_NUMBER = 1;
	static final int INDEX_PWD_INDX = 2;
	static final int INDEX_REG_DATE = 3;
	static final int INDEX_REG_STATUS = 4;
	static final int INDEX_OWNER_NAME = 5;

    public LoginDataHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_lOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHONE_NUMBER + " TEXT,"
                + KEY_PASSWORD_INDEX + " TEXT," + KEY_REG_DATE + " TEXT," + KEY_REG_STATUS + " TEXT," 
                + KEY_OWNER_NAME + " TEXT" +")";

        Log.d("QR_SCAN_LOGIN_DB", "onCreate Login In ");

        db.execSQL(CREATE_LOGIN_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_lOGIN);

        Log.d("QR_SCAN_LOGIN_DB", "onUpgrade In ");

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new loging
    void addLogin(Login login) {
        SQLiteDatabase db = this.getWritableDatabase();

		Log.d("QR_SCAN_LOGIN_DB", "addLogin In "+ login.getPhoneNumer() +" "+login.getPassIndex() 
			+" "+login.getRegDate() +" "+login.getOwnerName());

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, login.getPhoneNumer());
        values.put(KEY_PASSWORD_INDEX, login.getPassIndex());
        values.put(KEY_REG_DATE, login.getRegDate());
        values.put(KEY_REG_STATUS, login.getStatus());
        values.put(KEY_OWNER_NAME, login.getOwnerName());

        // Inserting Row
        Log.d("QR_SCAN_LOGIN_DB", "addLogin In "+db.insert(TABLE_lOGIN, null, values));

        db.close(); // Closing database connection
    }

    // Getting single login
    Login getLogin(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_lOGIN, new String[] { KEY_ID,
                KEY_PHONE_NUMBER, KEY_PASSWORD_INDEX, KEY_REG_DATE, KEY_REG_STATUS, KEY_OWNER_NAME }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Login login = new Login(Integer.parseInt(cursor.getString(INDEX_LOGIN_ID)),
                cursor.getString(INDEX_PHONE_NUMBER), 
                cursor.getString(INDEX_PWD_INDX), 
                cursor.getString(INDEX_REG_DATE), 
                cursor.getString(INDEX_REG_STATUS), 
                cursor.getString(INDEX_OWNER_NAME));
        // return loging
        return login;
    }

     public List<Login> getLoginsFromPhone(String phone) {
        List<Login> phoneList = new ArrayList<Login>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_LOGIN_DB", "getContactsFromQr In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do 
			{
				if(phone.equals(cursor.getString(INDEX_PHONE_NUMBER)))
				{
	                Login login = new Login();
	                login.setID(Integer.parseInt(cursor.getString(INDEX_LOGIN_ID)));
	                login.setPhoneNumber(cursor.getString(INDEX_PHONE_NUMBER));
	                login.setPassIndex(cursor.getString(INDEX_PWD_INDX));
					login.setRegDate(cursor.getString(INDEX_REG_DATE));
					login.setStatus(cursor.getString(INDEX_REG_STATUS));
					login.setOwnerName(cursor.getString(INDEX_OWNER_NAME));

	                // Adding login to list
	                phoneList.add(login);
	            }
            } while (cursor.moveToNext());
        }

        // return login list
        return phoneList;
    }


    // Getting All Contacts
    public List<Login> getAllContacts() {
        List<Login> phoneList = new ArrayList<Login>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_LOGIN_DB", "getAllContacts In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Login login = new Login();
                login.setID(Integer.parseInt(cursor.getString(INDEX_LOGIN_ID)));
				login.setPhoneNumber(cursor.getString(INDEX_PHONE_NUMBER));
				login.setPassIndex(cursor.getString(INDEX_PWD_INDX));
				login.setRegDate(cursor.getString(INDEX_REG_DATE));
				login.setStatus(cursor.getString(INDEX_REG_STATUS));
				login.setOwnerName(cursor.getString(INDEX_OWNER_NAME));

                // Adding login to list
                phoneList.add(login);
            } while (cursor.moveToNext());
        }

        // return login list
        return phoneList;
    }

		// Getting Login Dates
		public List<String> getAllLoginRegDates() {
			List<String> phoneList = new ArrayList<String>();
			// Select All Query
			
			String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;
	
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
	
			Log.d("QR_SCAN_LOGIN_DB", "getAllLoginRegDates In "+cursor.getCount());
	
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {		 
					if(false == phoneList.contains(cursor.getString(INDEX_REG_DATE)))
					{
						phoneList.add(cursor.getString(2));
						Log.d("QR_SCAN_LOGIN_DB", "ScanDate present Is "+cursor.getString(2));
	
					}
				} while (cursor.moveToNext());
			}
	 
			Log.d("QR_SCAN_LOGIN_DB", "getAllLoginRegDates In "+phoneList.size());
			return phoneList;
		}


	   // Getting Logins by date
		public List<String> getAllPhonebyDate(String Date) {
			List<String> phoneList = new ArrayList<String>();
			// Select All Query
			
			String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;
	
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
	
			Log.d("QR_SCAN_LOGIN_DB", "getAllPhonebyDate In "+cursor.getCount());
	
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {		 
					if(false == phoneList.contains(cursor.getString(INDEX_PHONE_NUMBER)) && 
						Date.equals(cursor.getString(INDEX_REG_DATE)))
					{
						phoneList.add(cursor.getString(1));
						Log.d("QR_SCAN_LOGIN_DB", "Phone present Is "+cursor.getString(INDEX_PHONE_NUMBER));
	
					}
				} while (cursor.moveToNext());
			}
	 
			Log.d("QR_SCAN_LOGIN_DB", "getAllPhonebyDate In "+phoneList.size());
			return phoneList;
		}


    // Getting Scan Codes
    public List<String> getAllScanCodes() {
        List<String> phoneList = new ArrayList<String>();
        // Select All Query
        
        String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

		Log.d("QR_SCAN_LOGIN_DB", "getAllContacts In "+cursor.getCount());

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {         
                if(false == phoneList.contains(cursor.getString(1)))
                {
                	phoneList.add(cursor.getString(1));
            		Log.d("QR_SCAN_LOGIN_DB", "ScanCode present Is "+cursor.getString(1));

                }
            } while (cursor.moveToNext());
        }
 
		Log.d("QR_SCAN_LOGIN_DB", "getAllScanCodes In "+phoneList.size());

        // looping through all rows and adding to list
  /*      if (cursor.moveToFirst()) {
            do {
                // Adding String to list
                phoneList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
*/
        // return login list
        return phoneList;
    }

	// Getting login counts
	public int get_loginCounts(String code_name) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int login_count = 0;
		if (null == code_name)
			return login_count;

		Log.d("QR_SCAN_LOGIN_DB", "get_loginCounts In "+cursor.getCount());

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {		 
				if(true == code_name.equals(cursor.getString(INDEX_PHONE_NUMBER)))
				{
					login_count++;
				}
			} while (cursor.moveToNext());
		}
 
		Log.d("QR_SCAN_LOGIN_DB", "get_loginCounts out "+login_count);

		return login_count;
	}


    // Deleting single login
    public void deleteLogin(Login login) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_lOGIN, KEY_ID + " = ?",
                new String[] { String.valueOf(login.getID()) });
        db.close();
    }

	public void deleteAllLogin()
	{
        SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from "+ TABLE_lOGIN);
        db.close();
	}
	
    // Getting logins Count
    public int getLoginCount() {
        String countQuery = "SELECT  * FROM " + TABLE_lOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public String Datetime()
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

		//Log.d("QR_SCAN_LOGIN_DB", " "+days[dayofweek] +" "+dayofmonth+" "+year+" "+" "+hour+":"+minute+" "+am_pm);
		Log.d("QR_SCAN_LOGIN_DB",  formattedDate);


		Calendar c1 = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate_new = formatter.format(c1.getTime());

		Log.d("QR_SCAN_LOGIN_DB New ",  formattedDate_new+"-"+formattedDate);

		return formattedDate_new+"-"+formattedDate;
	}

	public String export()
	{

		SQLiteDatabase sqldb = this.getReadableDatabase(); //My Database class
		Cursor c = null;
		String filename = null;
		 { //main code begins here
			try {
				String selectQuery = "SELECT  * FROM " + TABLE_lOGIN;

				c = sqldb.rawQuery(selectQuery, null);
				int rowcount = 0;
				int colcount = 0;
//				File sdCardDir = Environment.getExternalStorageDirectory();
				filename = "/MyBackUp1"+Datetime()+".csv";

				String inputfilePath = Environment.getExternalStorageDirectory()
										.getPath() + "/"+filename;


				Log.d("QR_SCAN_LOGIN_DB", "export In "+inputfilePath);

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
					Log.d("QR_SCAN_LOGIN_DB", "Exported Successfully");
				}
			} catch (Exception ex) {
				if (sqldb.isOpen()) {
					sqldb.close();
					Log.d("QR_SCAN_LOGIN_DB", "Exported failure "+ex.getMessage().toString());
				}
			} finally {
			}
		}

		return filename;
	}


}
