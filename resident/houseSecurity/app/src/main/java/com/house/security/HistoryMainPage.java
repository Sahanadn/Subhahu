package com.house.security;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.house.security.MainService.LocalBinder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressLint("NewApi")
public class HistoryMainPage<SearchResultActivity> extends Activity implements OnItemSelectedListener {

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
	String exportFile = null;
	Spinner scancode_spinner;
	Spinner date_spinner;
	Spinner username_spinner;
	String selected_date_;
	String selected_userName_;
	String selected_round;
	int selected_round_position;
		
	Spinner timesVisit_spinner;
	
	ListView list;
	DatabaseHandler db;
	VisitorDBHandler vis_db;
	MainService mService;
    boolean mBound = false;
	   
	String g_Lati;
	String g_Longi;
	List<Rounds> rounds_info = null;

	TextView roundStartTimeText;
	TextView roundLastTimeText;
	TextView roundTypeText;	
	boolean logout_result = false;
	boolean sync_result = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		Log.d("QR_SCAN_HIS_MAIN","activity_history entering ");


		boolean isFirst = util.getPreference(this,"isFirstTime",false);
		
		Log.d("QR_SCAN_HIS_MAIN","login success "+isFirst);

		if (false == isFirst)
		{
			Intent serviceIntent = new Intent(getBaseContext(), MainService.class);
			//serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			serviceIntent.setAction("com.sec.location.start");
			this.startService(serviceIntent);
	
			{
				//Intent intent = new Intent(ACTION_SCAN);
				Intent intent = new Intent(this, Admin.class);
				intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
				startActivityForResult(intent, 0);
				this.finish();

				return ;
			}
		}
		else
		{
			Intent intent_service = new Intent(this, MainService.class);
			bindService(intent_service, mConnection, 0);
		}
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Subhahu");
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
		actionBar.setHomeButtonEnabled(false);
		db = new DatabaseHandler(this);
		vis_db = new VisitorDBHandler(this);

		 // Spinner element

		 username_spinner = (Spinner) findViewById(R.id.userNameList);
		 username_spinner.setOnItemSelectedListener(this);

		 List<String> usernames = db.getAllUsernames();

		 ArrayAdapter<String> username_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, usernames);
		 username_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 username_spinner.setAdapter(username_dataAdapter);

		 String userName = util.getPreference(this,"client_userName",null);
		 if(userName == null)
		 {		 	

		 	if (usernames.size() == 0)
		 	{
				Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
				return ;
			}
		 	util.savePreference(HistoryMainPage.this,"client_userName", usernames.get(0));
		 }
	
	      date_spinner = (Spinner) findViewById(R.id.ScanDateList);
	      date_spinner.setOnItemSelectedListener(this);
	      List<String> dates;
	      if(null == userName)		  
	      	dates = db.getAllScanDates(usernames.get(0));		  
		  else
		  	dates = db.getAllScanDates(userName);
		  
		  ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
	      date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	      date_spinner.setAdapter(date_dataAdapter);
		  
	      selected_date_ = null;
	      selected_round = null;
		  selected_round_position = 0;

	      if(dates.size() != 0)
	      {
	      	selected_date_ = dates.get(0);
	    	Log.d("QR_SCAN_HIS_MAIN","activity_history date 1 "+dates.get(0));
	      }
		 // Spinner element
	      scancode_spinner = (Spinner) findViewById(R.id.ScanCodeList);
	      scancode_spinner.setOnItemSelectedListener(this);
	      List<String> categories;
	      if(dates.size() != 0)
	    	  categories = db.getAllScanCodesbyDate(dates.get(0));
	      else
	    	  categories = db.getAllScanCodes();
	      
	      ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, categories);
	      dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	      scancode_spinner.setAdapter(dataAdapter);		  

	      timesVisit_spinner = (Spinner) findViewById(R.id.timesVisitList);
	      timesVisit_spinner.setOnItemSelectedListener(this);

		  if (0 != dates.size())
		  {
	      	rounds_info = db.getAllRounds(dates.get(0));
			Log.d("QR_SCAN_HIS_MAIN","activity_history rounds_info size "+rounds_info.size());
							
			if (0 != rounds_info.size())
			{
				Rounds round = rounds_info.get(0);

				if (0 != round.getIndexSize())
					selected_round = ""+round.getIndex(1);

				
				Log.d("QR_SCAN_HIS_MAIN","activity_history round index "+round.getIndexSize() +" "+selected_round);				
			}
			
			Log.d("QR_SCAN_HIS_MAIN","activity_history rounds_info size "+getIndextoString (rounds_info).size());

			ArrayAdapter<String> timeVisitAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				getIndextoString (rounds_info));
			
			timeVisitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timesVisit_spinner.setAdapter(timeVisitAdapter);		

		  }
	      
	  }


	  /** Defines callbacks for service binding, passed to bindService() */
	  private ServiceConnection mConnection = new ServiceConnection() {
	  
		  @Override
		  public void onServiceConnected(ComponentName className,
				  IBinder service) {
				  
			  Log.d("QR_SCAN_HIS_MAIN","onServiceConnected");
			  // We've bound to LocalService, cast the IBinder and get LocalService instance
			  LocalBinder binder = (LocalBinder) service;
			  HistoryMainPage.this.mService = (MainService)binder.getServerInstance();
			  mBound = true;
	  
			  
		  }
	  
		  @Override
		  public void onServiceDisconnected(ComponentName arg0) {
			  mBound = false;
			  Log.d("QR_SCAN_HIS_MAIN","onServiceDisconnected");
			  HistoryMainPage.this.mService = null;
		  }
	  };


	  public List<String> getIndextoString(List <Rounds> round_info)
	  {
		  List<String> index_list = new ArrayList <String>();
		  int index = 0;
	  
		  while (index++ < round_info.size())
		  {
			  index_list.add(""+index);
		  }
		  
		  return index_list;
	  }

    
    /**
    *
    * Calls getLastLocation() to get the current location
    *
    */
   public Location getLocation() {
       Location currentLocation = null;

       
       // If Google Play Services is available
       //if (servicesConnected()) 
       {

           // Get the current location
          // currentLocation = mLocationClient.getLastLocation();
           Log.d(LocationUtils.APPTAG, "getLocation:" + LocationUtils.getLatLng(this, currentLocation));
           // Display the current location in the UI
           //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
       }
       return currentLocation;
   }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.history_list_action,menu);
		return  true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.Export:
				Log.d("QR_SCAN_HIS_MAIN", "onClick export button");
				exportFile = db.export();
				
				Toast.makeText(getApplicationContext(), "File saved at "+exportFile,
											Toast.LENGTH_LONG).show();
				return true;

			case R.id.History:
								
				Log.d("QR_SCAN_HIS_MAIN", "History");
				try {
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), HistoryList.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {

				}
				return true;
			case R.id.Attendence:				
				Log.d("QR_SCAN_HIS_MAIN", "Attendence");
				try {
						//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), AttendenceMainPage.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
	
				}
				return true;
				
			case R.id.Sync:
				Log.d("QR_SCAN_HIS_MAIN", "Refresh");				
/*
					boolean syncStatus = util.getPreference(HistoryMainPage.this,"syncStatus",false);
					if(false == syncStatus)
					{
						enableDislabeSyncDetails("OFF", item);

					}
					else
					{
						enableDislabeSyncDetails("ON",item);

					}		
*/
					boolean syncStatus = util.getPreference(HistoryMainPage.this,"refreshing",false);

//					if(false == syncStatus)
					{
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction("com.sec.loin.done");
						sendBroadcast(broadcastIntent);
						//startService(broadcastIntent);
						item.setTitle("Refreshing");
						
//						util.savePreference(HistoryMainPage.this,"refreshing",true);

						try {
							Intent intent = new Intent();
							intent.setClass(getApplicationContext(), HistoryList.class);
							intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
							startActivityForResult(intent, 0);
						} catch (ActivityNotFoundException anfe) {
		
						}
					}

				Toast.makeText(getApplicationContext(), "Refreshing",
														Toast.LENGTH_LONG).show();


			return true;
	/*		
			case R.id.Configure:
				Log.d("QR_SCAN_HIS_MAIN", "Configure");
				try {
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), Configure.class);
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {

				}
				return true;
			case R.id.Emergency:
				Log.d("QR_SCAN_HIS_MAIN", "Emergency Off");

				String phoneNo = "9986046041";
				String msg = "O1";
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
			case R.id.Events:
				Log.d("QR_SCAN_HIS_MAIN", "Events");
				return true;
*/				
			case R.id.Share:
				Log.d("QR_SCAN_HIS_MAIN", "Share");
				shareIt();
				return true;

				
			case R.id.Erase:
				Log.d("QR_SCAN_HIS_MAIN", "Erase");
				eraseScanDetails();
				eraseVisDetails();
				return true;
				
			case R.id.Logout:
				Log.d("QR_SCAN_HIS_MAIN", "Logout");

				if (true == handleLogout())
				{
					finish();
				}
				else
					Log.d("QR_SCAN_HIS_MAIN", "handlelogout return  false");
			
				return true;

            case android.R.id.home:
//                this.finish();
                return true;

			case R.id.VisualMapView:
			{
				Log.d("QR_SCAN_HIS_MAIN", "VisualMapView");
				try {
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), VisualMapView.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {

				}

			}
			return true;

			case R.id.AboutUs : {
				popUpAbout();
			}
			return true;

			case R.id.Visitor : {

				Log.d("QR_SCAN_HIS_MAIN", "Schedule VisitorActivity");
				try {
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), VisitorActivity.class);
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
					anfe.printStackTrace();
				}
			}
			return true;

			case R.id.Visitor_list : {

				Log.d("QR_SCAN_HIS_MAIN", "visitor list");
				try {
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), VisitorListActivity.class);
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
					anfe.printStackTrace();
				}
			}
			return true;
			default:
				return false;
		}
	}

	private void eraseVisDetails() {
		vis_db.deleteAllVisitors_onlogout();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		Log.d("QR_SCAN_HIS_MAIN","onItemSelected id "+id +" view id "+parent.getId());

		if (R.id.userNameList == parent.getId())
		{
			String _userName = parent.getItemAtPosition(position).toString();
			Log.d("QR_SCAN_HIS_MAIN","onItemSelected selected username "+_userName);

			if (null != selected_userName_ && selected_userName_.equals(_userName))
				return ;

			selected_userName_ = _userName;
			util.savePreference(HistoryMainPage.this,"client_userName", _userName);

			List<String> dates;
			dates = db.getAllScanDates(_userName); 		
			
			ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
			date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			date_spinner.setAdapter(date_dataAdapter);


			return ;
		}
			
		if (R.id.ScanDateList == parent.getId())
		{
			String _date = parent.getItemAtPosition(position).toString();
			selected_date_ = _date;

			Log.d("QR_SCAN_HIS_MAIN","onItemSelected ScanDateList selected date "+_date);

			rounds_info = null;
			rounds_info = db.getAllRounds(_date);
			timesVisit_spinner.setOnItemSelectedListener(this);

			ArrayAdapter<String> timeVisitAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				getIndextoString (rounds_info));
			timeVisitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timesVisit_spinner.setAdapter(timeVisitAdapter);

			List <String> list_index = null;

			if (0 != rounds_info.get(0).getIndexSize())
			{
			  	selected_round = ""+rounds_info.get(0).getIndex(1);
				list_index = rounds_info.get(0).getRealIndex();	
			}
/*
			if (list_index.size() != 0)
			{
				Contact db_entry_first = db.getContact(list_index.get(0));					
				roundStartTimeText = (EditText)findViewById(R.id.roundStartTimeValue);
				roundStartTimeText.setText(""+db_entry_first.getTime());

				Contact db_entry_last = db.getContact(list_index.get(list_index.size()-1));					
				roundLastTimeText = (EditText)findViewById(R.id.roundLastTimeValue);
				roundStartTimeText.setText(""+db_entry_last.getTime());	
			}
			*/
			return ;
		}
		
		if (R.id.timesVisitList == parent.getId())
		{
		
			selected_round = parent.getItemAtPosition(position).toString();
			Log.d("QR_SCAN_HIS_MAIN","onItemSelected timesVisitList selected date "+selected_round + " position "+position);
			selected_round_position = position;
			
			List<String> scan_codes = rounds_info.get(position).getAllNames();


			List<String> list_index = rounds_info.get(position).getRealIndex(); 
			Log.d("QR_SCAN_HIS_MAIN","onItemSelected timesVisitList rounds size "+list_index);

			if (list_index.size() != 0)
			{
//				Contact db_entry_first = db.getContact(list_index.get(0));					
				String start_time = db.get_startTimeFromRounds(list_index);
				String [] start_time_separated = start_time.split(" "); 

				Log.d("QR_SCAN_HIS_MAIN","onItemSelected timesVisitList round start time "+start_time);

				roundStartTimeText = (TextView)findViewById(R.id.roundStartTimeValue);
				if(null != roundStartTimeText)
				{
					roundStartTimeText.setText(""+start_time_separated[1]);

					if (start_time_separated[1] != null)
					{
						
						String [] hour = start_time_separated[1].split(":"); 
						int hour_time = Integer.parseInt(hour[0]);
						String am_pm =  hour_time > 12 ? "pm":"am";
						String [] min = hour[1].split(":");
	
						roundStartTimeText.setText(""+((hour_time > 12)?(hour_time-12):((hour_time == 0)?12:hour_time))+"."+min[0] +" "+am_pm);
					}
				}

/*				Contact db_entry_last = db.getContact(list_index.get(list_index.size()==0?0:list_index.size()-1));					
				String lasttime = db_entry_last.getFullTime();
*/
				String lasttime = db.get_lastTimeFromRounds(list_index);			
				String [] lasttime_separated = lasttime.split(" ");	

				roundLastTimeText = (TextView)findViewById(R.id.roundLastTimeValue);
				if (roundLastTimeText != null)
				{
					roundLastTimeText.setText(""+lasttime_separated[1]);
					Log.d("QR_SCAN_HIS_MAIN","onItemSelected timesVisitList round last time "+lasttime_separated[1]);

					if (lasttime_separated[1] != null)	
					{						
						String [] hour_last = lasttime_separated[1].split(":"); 
						int hour_time_last = Integer.parseInt(hour_last[0]);
						String am_pm_last =  hour_time_last > 12 ? "pm":"am";
						String [] min_last = hour_last[1].split(":");
	
						roundLastTimeText.setText(""+((hour_time_last > 12)?(hour_time_last-12):((hour_time_last == 0)?12:hour_time_last))+"."+min_last[0] +" "+am_pm_last);
					}
				}

				roundTypeText = (TextView)findViewById(R.id.roundType);
				String roundType = db.get_RoundType(list_index);			
				if(null != roundType)
				{
					roundTypeText.setText(roundType);
				}
				Log.d("QR_SCAN_HIS_MAIN","onItemSelected roundType "+roundType); 

			}
			
			//List<String> categories = db.getAllScanCodesbyDate(selected_date_);
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, scan_codes);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			scancode_spinner.setAdapter(dataAdapter);

			String location = db.get_scanLocation(scan_codes.get(0), selected_date_);
			List<String> locations = db.get_scanAllLocation(scan_codes.get(0), selected_date_, rounds_info.get(position).getRealIndex());
			int index = 0;

			Log.d("QR_SCAN_HIS_MAIN","onItemSelected timesVisitList locations size "+locations.size());	
						
			return ;
		}

		if (R.id.ScanCodeList == parent.getId())
		{
			String selected_date = null;

			Log.d("QR_SCAN_HIS_MAIN","onItemSelected ScanCodeList selected date "+selected_date + "position "+position);
				
			if(null != date_spinner && null != date_spinner.getSelectedItem()) 
				selected_date = date_spinner.getSelectedItem().toString();
			  // On selecting a spinner item
			
			  String item = parent.getItemAtPosition(position).toString();
			  Log.d("QR_SCAN_HIS_MAIN","scan selected date "+selected_date+" selected_date_"+selected_date_+" " +" scan "+item);
			  
			  int gap_conf = 30;
			  String max_gap = null;
			  String above_conf = null;
	
			  int scan_count = db.get_scancounts(item, selected_date);
			  String avg_gap_time = db.get_scangap_details(item, selected_date,gap_conf, max_gap, above_conf);
			  Log.d("QR_SCAN_HIS_MAIN","avg_gap "+avg_gap_time+" code "+item+" max_gap "+max_gap+" above_conf "+above_conf );

			TextView scanAvgGapText = (TextView)findViewById(R.id.avgTimeGapText);
			scanAvgGapText.setText(""+avg_gap_time+" (" +scan_count+" times)");

			TextView scanMaxGapText = (TextView)findViewById(R.id.maxTimeGapLayoutText);
			if (0 != scan_count)
				scanMaxGapText.setText(""+db.m_max_gap );
			else
				scanMaxGapText.setText("0 min");

			TextView ssidText = (TextView)findViewById(R.id.scanSSIDlist);
			int pos_round = Integer.parseInt(selected_round);
			ssidText.setText(""+db.get_scanSSIDFromRounds(item, selected_date, rounds_info.get(selected_round_position).getRealIndex()));
				
			//ssidText.setText(""+db.get_scanSSID(item, selected_date));

//			Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
			return ;
		}
	      
	   }


	public boolean handleLogout()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
			builder.setTitle("Logout");
			builder.setMessage("Are you sure? It will erase all data ");
		
			builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog
					logout_result = true;

			        db.deleteAllContact_logoutCase();
					vis_db.deleteAllVisitors_onlogout();
					
					Log.d("QR_SCAN_HIS_MAIN", "handlelogout click YES ");
					util.savePreference(HistoryMainPage.this,"isFirstTime",false);
					util.savePreference(HistoryMainPage.this, "client_name", null);
					util.savePreference(HistoryMainPage.this, "client_password", null);
					util.savePreference(HistoryMainPage.this, "authorizeKey", null);

					util.savePreference(HistoryMainPage.this,"syncStatus",false);
					util.savePreference(HistoryMainPage.this,"refreshing",false);
					util.savePreference(HistoryMainPage.this,"client_userName",null);

					util.addToHashSet(HistoryMainPage.this,"clear");

					dialog.dismiss();
					HistoryMainPage.this.finish();

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

		Log.d("QR_SCAN_HIS_MAIN", "handlelogout return "+logout_result);
		return logout_result;
	}


	public boolean enableDislabeSyncDetails(String status, MenuItem item)
	{
		final String _stats = status;	
		final MenuItem _item = item;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		sync_result = false;
	
			builder.setTitle("Confirm");
			builder.setMessage("Are you sure want to Sync "+status+" ?");
		
			builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog

					if (_stats.equals("OFF"))
					{
						_item.setTitle("Sync (OFF)");
						util.savePreference(HistoryMainPage.this,"syncStatus", true);
					}
					else
					{
						_item.setTitle("Sync (ON)");
						util.savePreference(HistoryMainPage.this,"syncStatus", false);		

						Intent broadcastIntent = new Intent(getBaseContext(), MainService.class);
						broadcastIntent.setAction("com.sec.sync.start");
						startService(broadcastIntent);	
							
					}
					
					sync_result = true;
					Log.d("QR_SCAN_HIS_MAIN", "enableDislabeSyncDetails On Click YES "+sync_result);
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

		Log.d("QR_SCAN_HIS_MAIN", "enableDislabeSyncDetails return "+sync_result);
		return sync_result;
	}


	public void eraseScanDetails()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
			builder.setTitle("Confirm");
			builder.setMessage("Are you sure?");
		
			builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog
			            db.deleteAllContact();
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


	@SuppressLint("SimpleDateFormat")
	public static String Datetime()
	{
		String formattedDate = null;
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR);		//12 hour format
		int hourofday = cal.get(Calendar.HOUR_OF_DAY);

		String am_pm = hourofday > 12 ? "pm":"am";
		formattedDate = hour+((minute<=9)?"0":"")+minute+""+am_pm;

		Log.d("QR_SCAN_HIS_MAIN",  formattedDate);

		Calendar c1 = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate_new = formatter.format(c1.getTime());
		Log.d("QR_SCAN_HIS_MAIN New ",  formattedDate_new+"-"+formattedDate);

		return formattedDate_new+"-"+formattedDate;
	}


	private void shareIt ()
	{
		String shareBody = "Daily roundings exported in xls file";
		
		exportFile = db.export();

		if(exportFile == null)
			return ;
		
		String inputfilePath = Environment.getExternalStorageDirectory()
								.getPath() + "/"+exportFile;

		File saveFile = new File(inputfilePath);
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("image/*");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Datetime());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

		sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(saveFile) );  
	
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		Log.d("QR_SCAN_HIS_MAIN", "onNothingSelected");
	}

	void popUpAbout() {
		String userName = util.getPreference(this,"client_userName",null);
		String aboutus = "Hi "+userName+",\n\nContact us at admin@subhahu.com \nCurrent App version is: 18(1.0)";
		new AlertDialog.Builder(this)
				.setTitle("About Us")
				.setMessage(aboutus)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
					}
				})
				.show();
	}


}
