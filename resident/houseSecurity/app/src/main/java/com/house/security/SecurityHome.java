package com.house.security;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.house.security.MainService.LocalBinder;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost.Settings;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SecurityHome extends Activity implements OnMenuItemClickListener{
	/** Called when the activity is first created. */

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

	String LastScanTime = null;
	MainService mService;
    boolean mBound = false;

  //private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String login_name = util.getPreference(this, "client_name", null);
		String login_password = util.getPreference(this, "client_password", null);
	    	    
//		Thread.setDefaultUncaughtExceptionHandler(new util());

// client page
		boolean isFirst = util.getPreference(this,"isFirstTime",false);
		
		Log.d("QR_SCAN_HOME","login success "+isFirst);

		if (false == isFirst)
		{
			Intent serviceIntent = new Intent(getBaseContext(), MainService.class);
			//serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			serviceIntent.setAction("com.sec.location.start");
			this.startService(serviceIntent);
	
			String apt_name = util.getPreference(this, "apt_name", null);
			{
				//Intent intent = new Intent(ACTION_SCAN);
				Intent intent = new Intent(this, Admin.class);
				intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
				startActivityForResult(intent, 0);
			}
		}
		else
		{

			Intent intent_service = new Intent(this, MainService.class);
			bindService(intent_service, mConnection, 0);

			try {
					Intent intent = new Intent();		
					intent.setClass(this, HistoryMainPage.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
					
				}
		}
			

	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
                
			Log.d("QR_SCAN_HOME","onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            SecurityHome.this.mService = (MainService)binder.getServerInstance();
            mBound = true;

			
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
			Log.d("QR_SCAN_HOME","onServiceDisconnected");
			SecurityHome.this.mService = null;
        }
    };
	

	  public void onProviderEnabled(String provider) {
//	    Toast.makeText(this, "Enabled new provider " + provider,
//	        Toast.LENGTH_SHORT).show();
		 Log.d("QR_SCAN_HOME","onProviderEnabled");

	  }

	  public void onProviderDisabled(String provider) {
//	    Toast.makeText(this, "Disabled provider " + provider,
//	        Toast.LENGTH_SHORT).show();
	    Log.d("QR_SCAN_HOME","onProviderDisabled");
	  }
	  public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub
		  Log.d("QR_SCAN_HOME","onStatusChanged "+status);
	  }

	 	  
	public void scanBar(View v) {
		try {
			//Intent intent = new Intent(ACTION_SCAN);
			Intent intent = new Intent(this, Admin.class);
			intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(SecurityHome.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void scanQR(View v) {
		try {


			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);

//			appinventor.ai_progetto2003.SCAN/com.QRBS.activity.QrActivity

		} catch (ActivityNotFoundException anfe) {
			showDialog(SecurityHome.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}


	 public void onDestroy() {
	 	super.onDestroy();
		Log.d("QR_SCAN_HOME","on Destroyed"); 
	 }

	 protected void onResume() {
		    super.onResume();
		    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
		  }

		  /* Remove the locationlistener updates when Activity is paused */
		  @Override
		  protected void onPause() {
		    super.onPause();
		    //locationManager.removeUpdates(this);
		  }


@SuppressLint({ "SimpleDateFormat", "NewApi" })
public static String Datetime()
{
	String formattedDate;
	Calendar cal = Calendar.getInstance();

	int year = cal.get(Calendar.YEAR);

	String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
	String shortDayName = dayLongName.substring(0, Math.min(dayLongName.length(), 3));
	int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
	String monthLongName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
	String shortMonthName = monthLongName.substring(0, Math.min(monthLongName.length(), 3));

	formattedDate = shortDayName+" "+shortMonthName+" "+dayofmonth+" "+year;
	Log.d("QR_SCAN_HOME", " "+shortDayName +" "+dayofmonth+" "+year );
	return formattedDate;
}


public static String GetTime()
{
	String formattedDate;
	Calendar cal = Calendar.getInstance();

	int minute = cal.get(Calendar.MINUTE);
	int hour = cal.get(Calendar.HOUR);	    //12 hour format
	int hourofday = cal.get(Calendar.HOUR_OF_DAY);

	String am_pm = hourofday > 12 ? "pm":"am";
	formattedDate = hour+((minute<=9)?":0":":")+minute+" "+am_pm;
	Log.d("QR_SCAN_HOME", hour+":"+minute+" "+am_pm);
	return formattedDate;
}

@SuppressLint("NewApi")
public void showPopup(View v) {
	PopupMenu popup = new PopupMenu(this, v);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.action, popup.getMenu());
	popup.setOnMenuItemClickListener(this);	
    popup.show();
}

@Override
public boolean onMenuItemClick(MenuItem item) {
	
	String phoneNo = util.getPreference(getApplicationContext(), "emergency_num", null);
	if(phoneNo == null)
		phoneNo = "09886562738";
					
    switch (item.getItemId()) {
        case R.id.archive:
            Log.d("QR_SCAN_HOME", "Emergency");

			 String msg = "R1"; 
			 try 
			 {		
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(phoneNo, null, msg, null, null);    
				Toast.makeText(getApplicationContext(), "Message Sent",
			   Toast.LENGTH_LONG).show();
			 } catch (Exception ex) 
			 {
				Toast.makeText(getApplicationContext(),
			 	ex.getMessage().toString(),
			 	Toast.LENGTH_LONG).show();
				ex.printStackTrace();
			 }
			
            return true;
            
        case R.id.configure:
        	Log.d("QR_SCAN_HOME", "Configure");
        	
			 String content = "configure"; 
			 try 
			 {		
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(phoneNo, null, content, null, null);    
				Toast.makeText(getApplicationContext(), "Message Sent",
			   Toast.LENGTH_LONG).show();
			 } catch (Exception ex) 
			 {
				Toast.makeText(getApplicationContext(),
			 	ex.getMessage().toString(),
			 	Toast.LENGTH_LONG).show();
				ex.printStackTrace();
			 }
        	
        	return true;
            
        default:
            return false;
    }
}


private void simpleTest()
        throws UnsupportedEncodingException, DataFormatException {
    // Encode a String into bytes
    String inputString = "blahblahblah??";
    byte[] input = inputString.getBytes("UTF-8");

    // Compress the bytes
    byte[] output = new byte[100];
    Deflater compresser = new Deflater();    
    compresser.setInput(input);
    compresser.finish();
    int compressedDataLength = compresser.deflate(output);

    // Decompress the bytes
    Inflater decompresser = new Inflater();
    decompresser.setInput(output, 0, compressedDataLength);
    byte[] result = new byte[100];
    int resultLength = decompresser.inflate(result);

    // Decode the bytes into a String
    String outputString = new String(result, 0, resultLength, "UTF-8");

    Log.d("QR_SCAN_HOME", "InputStromg "+outputString +"and Output String "+inputString);       
    if(inputString.equals(outputString))
    {   	
    	Log.d("QR_SCAN_HOME", "Input and Output String matches");  	
    }
    
    Log.d("QR_SCAN_HOME", "InputCompressed "+output.toString() +" OutputCompressed "+output.length);  	
    

    decompresser.end();
}


	public void loginExpired()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
			builder.setTitle("login expired");
			builder.setMessage("Inform?");
		
			builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog
					dialog.dismiss();
				}
		
			});
		
			builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
					dialog.dismiss();
				}
			});
		
			AlertDialog alert = builder.create();
			alert.show();

	}

	

}



