package com.house.security;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class util implements UncaughtExceptionHandler  {


	public static String getPreference(Context context, String name, String defaultValue) {
		SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		return sp.getString(name, defaultValue);

	}

	public static String getPreference(Context context, String name) {
		SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		return sp.getString(name, null);

	}

	public static boolean getPreference(Context context, String name, boolean defaultValue) {
		SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		return sp.getBoolean(name, defaultValue);
	}

	public static int getIntPreference(Context context, String name, String defaultValue) {
		SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		
		return Integer.parseInt(sp.getString(name, defaultValue));

	}	
	

	public static void savePreference(Context context, String name, String value) {
			SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
			Editor editor = sp.edit();
			editor.putString(name, value);
			editor.commit();
		}

	public static void savePreference(Context context, String name, boolean value) {
		SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		Editor editor = sp.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	 @SuppressLint("NewApi")
	public static boolean addToHashSet(Context context, String user_id)
	 {
		 SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		 LinkedHashSet<String> s = new LinkedHashSet<String>(sp.getStringSet("user_ids", new HashSet<String>()));		 

		util.savePreference(context, user_id + "-NwUpdatedSyncFromIndex", "0");
		util.savePreference(context, user_id + "-NwUpdatedIndex", "0");
		util.savePreference(context, user_id+"-NwUpdatedSyncForIndex", "0");
		util.savePreference(context, user_id+"-duplicate_depth", "0");
		util.savePreference(context, user_id+"-SyncFromDate", null);
		util.savePreference(context, user_id+"-SyncForDate", null);

		if (user_id.equals("clear"))
		{
			Set<String> null_orderedStringList = new HashSet<String>();
			SharedPreferences.Editor editor = sp.edit();						  
			editor.putStringSet("user_ids", null_orderedStringList);					  
			editor.apply();
			return true;
		}
		
		 {
			 Set<String> orderedStringList = new HashSet<String>();
			 Set<String> StringList = new HashSet<String>(s);
			 if(StringList.add(user_id))
			 {
				  Log.d("QR_SCAN_ADMIN","Set values ..... "+StringList.size());  
				  int i = 0;
				  for (String alpha : StringList)
				  { 				 
					  Log.d("QR_SCAN_ADMIN","Set values ..... "+alpha); 			  
					  orderedStringList.add(alpha);
					  i++;
				  }
	 
				  SharedPreferences.Editor editor = sp.edit();						  
				  editor.putStringSet("user_ids", orderedStringList);					  
				  editor.apply();
			 }
		 }
	 
		 return true;
	 }


	 @SuppressLint("NewApi")
	public static LinkedHashSet<String> getUserIdsHash(Context context){
		 SharedPreferences sp = context.getSharedPreferences("SmartSecura", 0);
		 LinkedHashSet<String> s = new LinkedHashSet<String>(sp.getStringSet("user_ids", new HashSet<String>()));		 
	 
		 return s;
	 }

	 public static boolean isNetworkOnline(Context context) {
		 boolean status = false;
		 try {
			 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			 //NetworkInfo netInfo = cm.getNetworkInfo(0);
			 NetworkInfo netInfo = cm.getActiveNetworkInfo();
			 
			 if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				 status = true;
			 } else {
				 netInfo = cm.getNetworkInfo(1);
				 if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					 status = true;
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 return false;
		 }
	 
		 Log.d("QR_SCAN_UTIL","isNetworkOnline returns "+status);
	 
		 return status;
	 }


	 @Override
	    public void uncaughtException(Thread thread, Throwable ex)
	    {
	        ex.printStackTrace();
	        
	       // String timestamp = TimestampFormatter.getInstance().getTimestamp();
	        final Writer result = new StringWriter();
	        final PrintWriter printWriter = new PrintWriter(result);

	        ex.printStackTrace(printWriter);
	        String stacktrace = result.toString();
	        printWriter.close();

	        String filename = "crashReport.txt";
			String inputfilePath = Environment.getExternalStorageDirectory()
						.getPath() + "/"+filename;
							
	        if (inputfilePath != null) {
	            writeToFile(stacktrace, inputfilePath);
	        }
	    

	        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
	        
	        Log.d("QR_SCAN_UTIL"," uncaughtException "+ex.toString());
	    }

	 private void writeToFile(String stacktrace, String filename) {
	        try {
	            BufferedWriter bos = new BufferedWriter(new FileWriter(filename));
	            bos.write(stacktrace);
	            bos.flush();
	            bos.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
