package com.house.security;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SuppressLint("NewApi") public class MainService extends Service{

    private static String TAG = "QR_SCAN_SERVICE";
	IBinder mBinder = new LocalBinder();
	LoginDataHandler	db_login;
	boolean gps_enabled = false;
	boolean isNetworkEnabled = false;
	boolean loc_enabled = false;
	DatabaseHandler db;
	AttendenceDataBaseHandler attendence_db;
	VisitorDBHandler visitor_db;
	private static final int NOTIFY_ME_ID = 1337;

	public static final int NW_SEND_MSG = 1;
	public static final int NW_SEND_CREATE_AUTH = 2;
	public static final int NW_RECEIVE_MSG = 3;
	private static final int NW_SEND_MSG_VM = 4;
	private static final int NW_RECEIVE_MSG_VM =5;
	private static final int NW_CHANGE_MSG_VM =6;

	// The minimum time between nw sync
	private static final long MIN_ALARM_TIME_NW_UPDATES = 1000 * 60 * 60 * 6; // 5 hrs
	Contact m_contact;
	
	public MainService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		  public MainService getServerInstance() {
		   return MainService.this;
		  }
		 }
	private PendingIntent pendingIntent;
	
	@Override
    public void onCreate() {
		Log.d("QR_SCAN_SERVICE", "The new Service was Created");

		db = new DatabaseHandler(this);
		attendence_db = new AttendenceDataBaseHandler(this);
		visitor_db = new VisitorDBHandler(this);
//		Thread.setDefaultUncaughtExceptionHandler(new util());             
    }


    @Override
    public void onStart(Intent intent, int startId) {
    	// For time consuming an long tasks you can launch a new thread here...
       Log.d("QR_SCAN_SERVICE","Service Started "+intent.getAction());
		
		Boolean login_avilable = util.getPreference(this, "LoginAvilable", false);
		if (login_avilable == false)
		{
			util.savePreference(this, "LoginAvilable", true);
			util.savePreference(this, "LoginDuration", "6000");
		}
		
		try {
            
            //Create a new PendingIntent and add it to the AlarmManager
            Intent alarm_intent = new Intent(this, MainBootComplete.class);
            alarm_intent.setAction(ALARM_SERVICE);                        
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
            
            AlarmManager am =
                (AlarmManager)getSystemService(Activity.ALARM_SERVICE);            
			if (am != null)
			{
            	am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    MIN_ALARM_TIME_NW_UPDATES,pendingIntent);		// 60 min
			}

			db_login = new LoginDataHandler(this);
			
		 } catch (Exception e) {}
    }
  protected void onStop() {
	  		Log.d("QR_SCAN_SERVICE","Service Stop");
  }

  /* Remove the locationlistener updates when Activity is paused */
  protected void onPause() {
	  Log.d("QR_SCAN_SERVICE","Service pause");		   

 }
 

	// location end
	
	public Boolean isLoginAllowed()
	{
		return util.getPreference(this, "LoginAvilable", true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent == null || intent.getAction() == null )
		{
			Log.d("QR_SCAN_SERVICE","Service StartCommand null intent flags "+flags +" startId "+startId);
			return START_STICKY;
		}

    	Log.d("QR_SCAN_SERVICE","Service StartCommand "+ intent.getAction());
		
		if (intent.getAction().equals("com.sec.location.start"))
		{
			onStart(intent, 0);
		}
		if (intent.getAction().equals("com.sec.sync.start"))
		{
			try {
            
            //Create a new PendingIntent and add it to the AlarmManager
            Intent alarm_intent = new Intent(this, MainBootComplete.class);
            alarm_intent.setAction(ALARM_SERVICE);                        
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
            
//            AlarmManager am =
//                (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
//			if (am != null)
//			{
//            	am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
//                    MIN_ALARM_TIME_NW_UPDATES,pendingIntent);		// 60 min
//			}
			pendingIntent.send();
		 } catch (Exception e) {}

		}		
		else if (intent.getAction().equals("com.sec.alarm.received"))
		{
			if (util.isNetworkOnline(this)) {
				if (util.getPreference(this,"pending_auth",false))
				{
					mNwHander.sendEmptyMessage(NW_SEND_CREATE_AUTH);
				}
				
				mNwHander.sendEmptyMessage(NW_RECEIVE_MSG);


				boolean syncStatus = util.getPreference(this,"syncStatus", false);

				if(true == syncStatus)
				{
					//Create a new PendingIntent and add it to the AlarmManager
					Intent alarm_intent = new Intent(this, MainBootComplete.class);
					alarm_intent.setAction(ALARM_SERVICE);						  
					pendingIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
					
					AlarmManager am =
						(AlarmManager)getSystemService(Activity.ALARM_SERVICE); 		   
					if (am != null)
					{
						am.cancel(pendingIntent);
					}

				}
			}
		}				
		else if (intent.getAction().equals("com.sec.sms.received"))
		{
			String sms = intent.getStringExtra("sms");
			
			if (intent.getStringExtra("sms").contains("reply"))
			{
				int login = 10080;	// 7 full days
				util.savePreference(this, "LoginDuration", ""+login);
			}
			else if (intent.getStringExtra("sms").equals("configure"))
			{
				String address = intent.getStringExtra("address");
				int count = db_login.get_loginCounts(address);
				if (count == 0)
				{
					String content = "reply";
					Login login = new Login();
					login.setOwnerName("NA");
					login.setPhoneNumber(address);
					int min = 1;
					int max = 255;

					Random r = new Random();
					int i1 = r.nextInt(max - min + 1) + min;

					login.setPassIndex(_password[i1]);
					login.setRegDate(db_login.Datetime());
					db_login.addLogin(login);

				try 
					 {		
						SmsManager smsManager = SmsManager.getDefault();
						smsManager.sendTextMessage(address, null, content+" "+_password[i1], null, null);    
						Toast.makeText(getApplicationContext(), "Message Sent",
					   Toast.LENGTH_LONG).show();
					 } catch (Exception ex) 
					 {
						Toast.makeText(getApplicationContext(),
						ex.getMessage().toString(),
						Toast.LENGTH_LONG).show();
						ex.printStackTrace();
					 }

					
				}
			}
		}
		else if(intent.getAction().equals("com.sec.net.connected"))
		{
			if (util.getPreference(this,"pending_auth",false))
			{
				mNwHander.sendEmptyMessage(NW_SEND_CREATE_AUTH);
			}
			
//			mNwHander.sendEmptyMessage(NW_SEND_MSG);
		}
		else if(intent.getAction().equals("com.sec.net.create.auth"))
		{
			mNwHander.sendEmptyMessage(NW_SEND_CREATE_AUTH);
		}
		else if(intent.getAction().equals( "com.sec.net.vismngmt.post"))
		{
			mNwHander.sendEmptyMessage(NW_SEND_MSG_VM);
		}
	    else if(intent.getAction().equals("com.sec.net.vismngmt.get")) {
			mNwHander.sendEmptyMessage(NW_RECEIVE_MSG_VM);
		}
		else if(intent.getAction().equals("com.sec.net.vismngmt.edit")) {
			mNwHander.sendMessage(mNwHander.obtainMessage(NW_CHANGE_MSG_VM, intent.getIntExtra("id", 0), 0));
		}
    	return START_STICKY;
    }

	@SuppressLint("NewApi") private Handler mNwHander = new Handler() {
		@SuppressLint("NewApi") @Override
		public void handleMessage(Message msg) {
			
			switch (msg.what)
			{
				case NW_SEND_CREATE_AUTH:
				{
/*
					final Integer notificationID = 100;					
					final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					
					//Set notification information:
					final Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
					notificationBuilder.setOngoing(true)
									   .setContentTitle("Security Rounding Info")
									   .setContentText("Loging to Web")
									   .setProgress(100, 0, false);
					
					//Send the notification:
					final Notification notification = notificationBuilder.build();
					notificationManager.notify(notificationID, notification);
*/
					new Thread(new Runnable(){
						@Override
						public void run() {
							
							String username = util.getPreference(MainService.this,"client_name");
							String password = util.getPreference(MainService.this,"client_password");

							if (null == username)
								return ;

							try {
								makeLogin (username+"@subhahu.com", password);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}).start();
				}
				break;
				case NW_RECEIVE_MSG:
				{
					final Integer notificationID = 100;					
/*
					final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					
					//Set notification information:
					final Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
					notificationBuilder.setOngoing(true)
									   .setContentTitle("Security Rounding Info")
									   .setContentText("Donloading from Web")
									   .setProgress(100, 0, false);
					
					//Send the notification:
					final Notification notification = notificationBuilder.build();
					notificationManager.notify(notificationID, notification);
*/
					new Thread(new Runnable(){
						@Override
						public void run() {
							
							boolean isFirst = util.getPreference(MainService.this,"isFirstTime",false);
							if (false == isFirst)
								return ;

							LinkedHashSet<String> s = util.getUserIdsHash(MainService.this);
							int size  = s.size();
							Object[] array = s.toArray();
							int i = 0;
							while (i < size)
							{
								String user_id = (String) array [i++];
								
								try {
									makeGetRequest(user_id, (i == size)?true:false);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}).start();
				}
				break;
				case NW_SEND_MSG_VM :
				{
					new Thread(new Runnable(){
						@Override
						public void run() {

							int index = 0;
							int progress = 0;

							index = util.getIntPreference(MainService.this, "NwUpdatedVisIndex", "0");
							List<Visitor> visitors = visitor_db.getAllVisitors();
							int listSize = visitors.size();

							Log.d("QR_SCAN_SERVICE","makeVisPostRequest prepare index "+index+" size "+listSize);


							//index = 0;
							while (index < listSize)
							{
								Visitor visitor = visitors.get(index++);

								try {
									if (false == makeVisPostRequest(visitor))
										break;

									progress += progress;
									//Update notification information:

									Log.d(TAG,"***Updating index NwUpdatedVisIndex : "+index);
									util.savePreference(MainService.this, "NwUpdatedVisIndex", ""+index);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					}).start();
				}
				break;
				case NW_RECEIVE_MSG_VM :
				{
					new Thread(new Runnable(){
						@Override
						public void run() {
							boolean isFirst = util.getPreference(MainService.this,"isFirstTime",false);
							if (false == isFirst)
								return ;

							LinkedHashSet<String> s = util.getUserIdsHash(MainService.this);
							int size  = s.size();
							Object[] array = s.toArray();
							int i = 0;
							Log.d(TAG,"size ="+size);
							while (i < size)
							{
								String user_id = (String) array [i++];
								Log.d(TAG,"size ="+size);
								try {
									makeVisGetRequest(user_id, (i == size) ? true : false);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}).start();
				}
				break;
				case NW_CHANGE_MSG_VM :  {
                    final int visid = msg.arg1;
					new Thread(new Runnable(){
						@Override
						public void run() {
							try {
								editVisitorDetails(visid);
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
				break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};




	private void makeLogin (String username, String password) throws JSONException
	{

		Log.d("QR_SCAN_SERVICE","makeLogin "+username +" "+password); 

		HttpClient httpClient = new DefaultHttpClient();
	  	HttpPost httpPost = new HttpPost("http://ck-monitor.herokuapp.com/api/users/auth");

		httpPost.addHeader("Content-Type", "application/json");
		String json = "";

		//JSONArray jsonObject = new JSONArray();
		JSONObject jsonObject = new JSONObject();

		//JSONObject jsonObject_location = new JSONObject();  
		try {
			jsonObject.accumulate("email", username);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			jsonObject.accumulate("password", password);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// 4. convert JSONObject to JSON to String
		json = jsonObject.toString();
		Log.d("QR_SCAN_SERVICE","json string "+json);
		
		StringEntity se = null;
		try {
			se = new StringEntity(json);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Encoding data
		httpPost.setEntity(se);

		// making request
		try {
			HttpResponse response = httpClient.execute(httpPost);
			// write response to log				
			String resp = entityToString(response.getEntity());
			Log.d("QR_SCAN_SERVICE","makeLogin response entity "+resp);

			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(resp);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String auth_key = jsonObj.getString("authorizeKey");
			Log.d("QR_SCAN_SERVICE","makeLogin auth_key ddd"+auth_key);

			JSONArray jsonArray_user = jsonObj.optJSONArray("user");
			Log.d("QR_SCAN_SERVICE","makeLogin length() "+jsonArray_user.length());
			if (0 != jsonArray_user.length())
			{
				for (int j = 0; j < jsonArray_user.length(); j++)
				{
					Log.d("QR_SCAN_SERVICE","********************" +
							"");
					JSONObject user_jsonObject = jsonArray_user.getJSONObject(j);
					String _role = user_jsonObject.getString("role");
					String company = user_jsonObject.getString("company");
					Log.d("QR_SCAN_SERVICE","makeLogin company :"+company);

					if(_role.equals("comapany_admin"))
					{
						JSONArray jsonArray = user_jsonObject.optJSONArray("companies");
						if (0 != jsonArray.length())
						{
							Log.d("QR_SCAN_SERVICE","makeLogin response companies arrayLen "+jsonArray.length());
							int arrLen = jsonArray.length();
							for (int i = 0; i < jsonArray.length(); i++)
							{
								JSONObject _jsonObject = jsonArray.getJSONObject(i);
								String _id = _jsonObject.optString("_id").toString();
								String displayName = _jsonObject.optString("displayName").toString();
								Log.d("QR_SCAN_SERVICE","displayName "+displayName +" _id "+_id);
							}
						}
					}
				}
			}
			
		} catch (ClientProtocolException e) {
			// Log exception
			e.printStackTrace();
		} catch (IOException e) {
			// Log exception
			e.printStackTrace();
		}

	}

	private void updateNwIndexDuplicateDepthCase(String user_id, String selectedDate)
	{	
		String SyncForDate = null;
		String SyncFromDate = null;
		
		SyncForDate = util.getPreference(MainService.this,user_id+"-SyncForDate",null);
		SyncFromDate = util.getPreference(MainService.this,user_id+"-SyncFromDate",null);

		if (selectedDate.equals(SyncFromDate))
		{
			util.savePreference(MainService.this, user_id+"-SyncFromDate", null);
			util.savePreference(MainService.this, user_id+"-NwUpdatedSyncFromIndex", "0");
		}
		else if (selectedDate.equals(SyncForDate))
		{
			util.savePreference(MainService.this, user_id+"-SyncForDate", null);
			util.savePreference(MainService.this, user_id+"-NwUpdatedSyncForIndex", "0");
		}
	}

	private void updateNwIndex(String user_id, String selectedDate, int nw_index)
	{
		String SyncForDate = null;
		String SyncFromDate = null;
		String Date_login = null;
		String Visitorget = null;
		
		SyncForDate = util.getPreference(MainService.this,	user_id+"-SyncForDate",null);
		SyncFromDate = util.getPreference(MainService.this,	user_id+"-SyncFromDate",null);
		Date_login = util.getPreference(MainService.this,"loginDate",null);
		Visitorget = util.getPreference(MainService.this,"visDate",null);
		if (selectedDate.equals(Date_login))
		{
			util.savePreference(MainService.this, user_id+"-NwUpdatedIndex", ""+nw_index);
		}
		else if (selectedDate.equals(SyncFromDate))
		{
			util.savePreference(MainService.this, user_id+"-NwUpdatedSyncFromIndex", ""+nw_index);
		}
		else if (selectedDate.equals(SyncForDate))
		{
			util.savePreference(MainService.this, user_id+"-NwUpdatedSyncForIndex", ""+nw_index);
		}
		else if (selectedDate.equals(Visitorget))
		{
			util.savePreference(MainService.this, user_id+"-NwVisitorGet", ""+nw_index);
		}
	}

	private int getNwIndex(String user_id, String selectedDate)
	{
		int nw_index = 0;
		String SyncForDate = null;
		String SyncFromDate = null;
		String Date_login = null;
		
		SyncForDate = util.getPreference(MainService.this,	user_id+"-SyncForDate",null);
		SyncFromDate = util.getPreference(MainService.this,	user_id+"-SyncFromDate",null);
		Date_login = util.getPreference(MainService.this,"loginDate",null);
		
		if (selectedDate.equals(Date_login))
		{			
			nw_index = util.getIntPreference(MainService.this, user_id+"-NwUpdatedIndex", "0" );
		}
		else if (selectedDate.equals(SyncFromDate))
		{
			nw_index = util.getIntPreference(MainService.this, user_id+"-NwUpdatedSyncFromIndex", "0" );
		}
		else if (selectedDate.equals(SyncForDate))
		{
			nw_index = util.getIntPreference(MainService.this, user_id+"-NwUpdatedSyncForIndex", "0" );
		}

		return nw_index;
	}

	private String getSelectedDate(String user_id)
	{
		String selectedDate = null;
		String SyncForDate = null;
		String SyncFromDate = null;
		String Date_login = null;
		
		SyncForDate = util.getPreference(MainService.this,	user_id+"-SyncForDate",null);
		if(null == SyncForDate)
		{
			SyncFromDate = util.getPreference(MainService.this, user_id + "-SyncFromDate", null);
			if(null == SyncFromDate)
			{
				Date_login = util.getPreference(MainService.this,"loginDate",null);
				selectedDate = Date_login;
				Log.d("QR_SCAN_SERVICE","getSelectedDate sync from login date "+Date_login);
			}
			else
			{
				selectedDate = SyncFromDate;
				Log.d("QR_SCAN_SERVICE","getSelectedDate sync from date "+SyncFromDate);
			}
		}
		else
		{
			selectedDate = SyncForDate;
			Log.d("QR_SCAN_SERVICE","getSelectedDate sync for date "+SyncForDate);
		}

		return selectedDate;
	}


	@SuppressWarnings("deprecation")
	private boolean makeGetRequestForAttendence()throws JSONException, UnsupportedEncodingException
	{
		Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence "); 	
		int arrLen = 0;
		int _index = 0;
		int local_index = 0;		

		boolean isFirst = util.getPreference(MainService.this,"isFirstTime",false);
		Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence login "+isFirst);	
		if (false == isFirst)
			return false;

		if (util.isNetworkOnline(this) == false)
		{
			Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence network offline returns flase");	
			return false;
		}

		
		/* Invoking the default notification service */
		 int noti_index = 0;

		String noti_resp = null;
		Boolean notify = true;

		String Date_login = null;
		String SyncForDate = null;
		String SyncFromDate = null;

		String selectedDate = null;

		SyncForDate = util.getPreference(MainService.this,	"SyncForDate",null);
		if(null == SyncForDate)
		{
			SyncFromDate = util.getPreference(MainService.this,"SyncFromDate",null);
			if(null == SyncFromDate)
			{
				Date_login = util.getPreference(MainService.this,"loginDate",null);
				selectedDate = Date_login;
				Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence sync from login date "+Date_login);
			}
			else
			{
				selectedDate = SyncFromDate;
				Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence sync from date "+SyncFromDate);
			}
		}
		else
		{
			selectedDate = SyncForDate;
			Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence sync for date "+SyncForDate);
		}


		if (selectedDate.equals(Date_login))
		{			
			_index = util.getIntPreference(MainService.this, "NwUpdatedIndex", "0" );
		}
		else if (selectedDate.equals(SyncFromDate))
		{
			_index = util.getIntPreference(MainService.this, "NwUpdatedSyncFromIndex", "0" );
		}
		else if (selectedDate.equals(SyncForDate))
		{
			_index = util.getIntPreference(MainService.this, "NwUpdatedSyncForIndex", "0" );
			
		}

		int duplicate_depth = util.getIntPreference(MainService.this, "duplicate_depth", "0" );


		Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence "+_index+" "+local_index +" duplicate_depth "+duplicate_depth); 

		while( local_index < 50)
		{
			HttpClient httpClient = new DefaultHttpClient();
			String Uri_nw = "http://ck-monitor.herokuapp.com/api/users/attendance?";
			local_index++;

			if(selectedDate != null)
			{
				Uri_nw += "fromDate="+selectedDate+"&";
			}

			Uri_nw += "offset="+""+_index;
			Uri_nw += "&limit=1&";
			
			String token = util.getPreference(MainService.this,"authorizeKey");
			if (null == token)
			{
				Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence returns false ");	
				return false;
			}
			else
				Uri_nw += "token=" + token;

			HttpGet httpGet = new HttpGet(Uri_nw);
			httpGet.addHeader("Content-Type", "application/json");

			Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence  "+Uri_nw);


			String response_value = null;
			try {
				HttpResponse response = httpClient.execute(httpGet);			// making get request
				// write response to log	
				response_value = entityToString(response.getEntity());
				Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response entity response "+response_value);
				
			} catch (ClientProtocolException e) {
				// Log exception
				e.printStackTrace();
			} catch (IOException e) {
				// Log exception
				e.printStackTrace();
			}
			if (null != response_value)
			{
				JSONObject reader = new JSONObject(response_value);
				JSONArray jsonArray = reader.optJSONArray("data");
				Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response arrayLen "+jsonArray.length());
				
				if (0 == jsonArray.length())
				{
					Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response arrayLen break "+jsonArray.length());
					notify = false;
					break;
				}
				arrLen = jsonArray.length();
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String timeGap = jsonObject.optString("timeGap").toString();
					String ssid = jsonObject.optString("SSID").toString();
					String scan_name = jsonObject.optString("name").toString();
					String loc_details = jsonObject.optString("loc").toString();
					String scanTime = jsonObject.optString("scanTime").toString();
					String Date = jsonObject.optString("createdAt").toString();
					String round = jsonObject.optString("round").toString();

					String roundType = jsonObject.optString("roundType").toString();
					String isAttendance = jsonObject.optString("isAttendance").toString();

					//String user = jsonObject.optString("user").toString();
					//String username = jsonObject.optString("username").toString();
					JSONObject jsonObject_username = jsonObject.getJSONObject("user");
					String username = jsonObject_username.optString("username");

					Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response entity scanTime "+Date +" "+scanTime);
					Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response entity scan_name "+scan_name+" "+loc_details);
					Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response entity ssid "+ssid+" "+timeGap);
					Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence response entity username "+username);


					String loc_to_set = null;
					if (null != loc_details)
					{
						String[] pieces = loc_details.split(",");
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence location "+pieces[0]+" "+pieces[1]);						
						
						//String[] final_str_lat = pieces[0].split("[");
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence location_1 "+pieces[0].substring(1));
						String Latitude = "Latitude: "+pieces[0].substring(1);

						String[] final_str_long = pieces[1].split("]");
						String Longitude = "Longitude: "+final_str_long[0];

						loc_to_set = Latitude +" "+Longitude;
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence loc_to_set "+loc_to_set);
					}

					String full_time = null;
					String date_received = null;
					
					if (null != scanTime)
					{
						String[] pieces_scanTime = scanTime.split("T");
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence scanTime "+pieces_scanTime[0]+" "+pieces_scanTime[1]);

						full_time = pieces_scanTime[0]+" ";
						date_received = pieces_scanTime[0];
						String upToNCharacters = pieces_scanTime[1].substring(0, 8);
						
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence scanTime "+upToNCharacters);						
						full_time += upToNCharacters;
						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence fullScanTime "+full_time);						
					}

					Calendar c = Calendar.getInstance();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDate = null;
					String formattedTime = null;
					try {
						
						java.util.Date date = formatter.parse(full_time);
						Calendar cal = Calendar.getInstance();
						  cal.setTime(date);
						  int year = cal.get(Calendar.YEAR);
						  String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
							String shortDayName = dayLongName.substring(0, Math.min(dayLongName.length(), 3));
							int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
							String monthLongName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
							String shortMonthName = monthLongName.substring(0, Math.min(monthLongName.length(), 3));

							formattedDate = shortDayName+" "+shortMonthName+" "+dayofmonth+" "+year;						  

					int minute = cal.get(Calendar.MINUTE);
						int hour = cal.get(Calendar.HOUR);		//12 hour format
						int hourofday = cal.get(Calendar.HOUR_OF_DAY);
					
						String am_pm = hourofday > 12 ? "pm":"am";
						formattedTime = hour+((minute<=9)?":0":":")+minute+" "+am_pm;

						  
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Attendence attendence = new Attendence();
					
					attendence.setGapTime(timeGap);
					attendence.setSSID(ssid);
					attendence.setName(scan_name);
					attendence.setLocation(loc_to_set);
					attendence.setTime(formattedTime);
					attendence.setDate(formattedDate);
					attendence.setFullTime(full_time);
					attendence.setRound(round);

					attendence.setRoundType(roundType);
//					contact.setAttendence(isAttendance);

					if (username != null)
					{
						attendence.setUsername(username);
					}

					if(false == attendence_db.CheckIsDataAlreadyInDBorNot(attendence))
					{
						attendence_db.addAttendence(attendence);

						if(noti_resp == null)
						{
							noti_resp = scan_name;
						}
		
						_index ++;

						if (selectedDate.equals(Date_login))
						{
							util.savePreference(MainService.this, "NwUpdatedIndex", ""+_index);
						}
						else if (selectedDate.equals(SyncFromDate))
						{
							util.savePreference(MainService.this, "NwUpdatedSyncFromIndex", ""+_index);
						}
						else if (selectedDate.equals(SyncForDate))
						{
							util.savePreference(MainService.this, "NwUpdatedSyncForIndex", ""+_index);
						}

						util.savePreference(MainService.this, "duplicate_depth", "0");
						duplicate_depth = 0;
					
						
					}
					else
					{

						Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence date_received "+date_received +" Date_login "+Date_login);	

						if (duplicate_depth >= 5)
						{							
							if (selectedDate.equals(SyncFromDate))
							{
								util.savePreference(MainService.this, "SyncFromDate", null);
								util.savePreference(MainService.this, "NwUpdatedSyncFromIndex", "0");
							}
							else if (selectedDate.equals(SyncForDate))
							{
								util.savePreference(MainService.this, "SyncForDate", null);
								util.savePreference(MainService.this, "NwUpdatedSyncForIndex", "0");
							}

							util.savePreference(MainService.this, "duplicate_depth", "0");

							Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence returns true: Not sure why it should return "); 
							return true;
						}
						else
						{
							util.savePreference(MainService.this, "duplicate_depth", ""+(++duplicate_depth));
							_index ++;

							if (selectedDate.equals(SyncFromDate))
							{
								util.savePreference(MainService.this, "NwUpdatedSyncFromIndex", ""+_index);
							}
							else if (selectedDate.equals(SyncForDate))
							{
								util.savePreference(MainService.this, "NwUpdatedSyncForIndex", ""+_index);
							}							
						}

					}
				}
			}
		}

		Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence notification "+noti_resp +" notify "+notify);	

		if(noti_resp == null)
		{

			Log.d("QR_SCAN_SERVICE","makeGetRequestForAttendence notification "+noti_resp +" selectedDate "+selectedDate +" SyncFromDate "+SyncFromDate +" "+SyncForDate);	

			if (selectedDate.equals(SyncFromDate))
			{
				util.savePreference(MainService.this, "SyncFromDate", null);
				util.savePreference(MainService.this, "NwUpdatedSyncFromIndex", "0");
			}
			else if (selectedDate.equals(SyncForDate))
			{
				util.savePreference(MainService.this, "SyncForDate", null);
				util.savePreference(MainService.this, "NwUpdatedSyncForIndex", "0");
			}
	
			util.savePreference(MainService.this, "duplicate_depth", "0");

		}

		if (notify == true && noti_resp != null)
		{
			/* Creates an explicit intent for an Activity in your app */
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.notification_icon)
					.setContentTitle("Attendance Update")
					.setContentText(noti_resp);
			
			Intent resultIntent = new Intent(this, NotificationView.class);
			
			// Because clicking the notification opens a new ("special") activity, there's
			// no need to create an artificial back stack.
			PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
				this,
				0,
				resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
			
			mBuilder.setContentIntent(resultPendingIntent);
			
			NotificationManager mNotifyMgr = 
					(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Builds the notification and issues it.
			mNotifyMgr.notify(NOTIFY_ME_ID, mBuilder.build());
		}

		Log.d("QR_SCAN_SERVICE", "makeGetRequestForAttendence returns true ");
		return true;
	}


	@SuppressWarnings("deprecation")
	private boolean makeGetRequest(String user_id, boolean last_user)throws JSONException, UnsupportedEncodingException
	{
		Log.d("QR_SCAN_SERVICE","makeGetRequest last_user "+last_user); 	
		int arrLen = 0;
		int _index = 0;
		int local_index = 0;		

		boolean isFirst = util.getPreference(MainService.this,"isFirstTime",false);
		Log.d("QR_SCAN_SERVICE","makeGetRequest login "+isFirst); 	
		if (false == isFirst)
			return false;

		if (util.isNetworkOnline(this) == false)
		{
			Log.d("QR_SCAN_SERVICE","makeGetRequest network offline returns flase");	
			return false;
		}

		/* Invoking the default notification service */
		int noti_index = 0;
		String noti_resp = null;
		Boolean notify = true;

		String Date_login = null;
		String SyncForDate = null;
		String SyncFromDate = null;
		String selectedDate = null;

		SyncForDate = util.getPreference(MainService.this,user_id+"-SyncForDate",null);
		SyncFromDate = util.getPreference(MainService.this,user_id+"-SyncFromDate",null);
		Date_login = util.getPreference(MainService.this,"loginDate",null);

		selectedDate = getSelectedDate(user_id);
		_index = getNwIndex(user_id, selectedDate);
		
		int duplicate_depth = util.getIntPreference(MainService.this, user_id+"-duplicate_depth", "0" );

		Log.d("QR_SCAN_SERVICE","makeGetRequest "+_index+" "+local_index +" duplicate_depth "+duplicate_depth);	

		String authorizeKey = util.getPreference(MainService.this, "authorizeKey", null);
		if (authorizeKey == null)
			Log.d("QR_SCAN_SERVICE","makeGetRequest authorize key is null"); 

		String Uri_nw = null;
		
		while( local_index < 2)
		{
			HttpClient httpClient = new DefaultHttpClient();
			if (user_id.equals("authKey"))
			{
				Uri_nw = "http://ck-monitor.herokuapp.com/api/users/geo?";
			}
			else
			{
				Uri_nw = "http://ck-monitor.herokuapp.com/api/company/" +user_id +"/"+"geo?";
			}
			
//			String Uri_nw = "http://ck-monitor.herokuapp.com/api/company/582e6544c099cd04003c95ee/geo?offset=0&limit=1&fromDate=2016-11-26&token=582e6531c099cd04003c95ed";
//			String Uri_nw = "http://ck-monitor.herokuapp.com/api/company/582e6531c099cd04003c95ed/geo?offset=0&limit=2&fromDate=2016-11-20&token=582e6531c099cd04003c95ed"

//			String Uri_nw = "http://ck-monitor.herokuapp.com/api/users/geo?offset=0&limit=2&fromDate=2016-11-20&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI1Nzk0MzIzZWUyNmYwZDAzMDAyNWQ5MTMifQ.J7pOiOl7MYUhmhR8c38Ptw2NzLRqDFq2-1g3RP_pjik";
///			String Uri_nw = "http://ck-monitor.herokuapp.com/api/users/geo?fromDate=2016-11-28&offset=0&limit=100&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI1Nzk0MzIzZWUyNmYwZDAzMDAyNWQ5MTMifQ.J7pOiOl7MYUhmhR8c38Ptw2NzLRqDFq2-1g3RP_pjik"


			local_index++;			

			Uri_nw += "offset="+""+_index;
			Uri_nw += "&limit=100&";

			if(selectedDate != null)
			{
				Uri_nw += "fromDate="+selectedDate+"&";
			}
			
//			String token = util.getPreference(MainService.this,"authorizeKey");
			if (null == user_id)
			{
				Log.d("QR_SCAN_SERVICE","makeGetRequest returns false ");	
				return false;
			}
			else
			{
				if (user_id.equals("authKey") && authorizeKey != null)
					Uri_nw += "token=" + authorizeKey;

				else
					Uri_nw += "token=" + user_id;
			}
			HttpGet httpGet = new HttpGet(Uri_nw);
			httpGet.addHeader("Content-Type", "application/json");

			Log.d("QR_SCAN_SERVICE","makeGetRequest  "+Uri_nw);

			String response_value = null;
			try {
				HttpResponse response = httpClient.execute(httpGet);			// making get request
				// write response to log	
				response_value = entityToString(response.getEntity());
				Log.d("QR_SCAN_SERVICE","makeGetRequest response entity response "+response_value);
				
			} catch (ClientProtocolException e) {
				// Log exception
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// Log exception
				e.printStackTrace();
				return false;				
			}
			if (null != response_value)
			{
				JSONObject reader = new JSONObject(response_value);
				JSONArray jsonArray = reader.optJSONArray("data");
				
				if (jsonArray == null || 0 == jsonArray.length())
				{
					if (null != jsonArray)
					{
						Log.d("QR_SCAN_SERVICE","makeGetRequest response arrayLen break "+jsonArray.length());
					}
					notify = false;
					break;
				}
				Log.d("QR_SCAN_SERVICE","makeGetRequest response arrayLen "+jsonArray.length());

				arrLen = jsonArray.length();
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String timeGap = jsonObject.optString("timeGap").toString();
					String ssid = jsonObject.optString("SSID").toString();
					String scan_name = jsonObject.optString("name").toString();
					String loc_details = jsonObject.optString("loc").toString();
					String scanTime = jsonObject.optString("scanTime").toString();
					String Date = jsonObject.optString("createdAt").toString();
					String round = jsonObject.optString("round").toString();

					String roundType = jsonObject.optString("roundType").toString();
					String isAttendance = jsonObject.optString("isAttendance").toString();

					//String user = jsonObject.optString("user").toString();
					//String username = jsonObject.optString("username").toString();
					JSONObject jsonObject_username = jsonObject.getJSONObject("user");
					String username = jsonObject_username.optString("username");

					Log.d("QR_SCAN_SERVICE","makeGetRequest response entity scanTime "+Date +" "+scanTime);
					Log.d("QR_SCAN_SERVICE","makeGetRequest response entity scan_name "+scan_name+" "+loc_details);
					Log.d("QR_SCAN_SERVICE","makeGetRequest response entity ssid "+ssid+" "+timeGap);
					Log.d("QR_SCAN_SERVICE","makeGetRequest response entity username "+username);


					String loc_to_set = null;
					if (null != loc_details)
					{
						String[] pieces = loc_details.split(",");
						Log.d("QR_SCAN_SERVICE","makeGetRequest location "+pieces[0]+" "+pieces[1]);						
						
						//String[] final_str_lat = pieces[0].split("[");
						Log.d("QR_SCAN_SERVICE","makeGetRequest location_1 "+pieces[0].substring(1));
						String Latitude = "Latitude: "+pieces[0].substring(1);

						String[] final_str_long = pieces[1].split("]");
						String Longitude = "Longitude: "+final_str_long[0];

						loc_to_set = Latitude +" "+Longitude;
						Log.d("QR_SCAN_SERVICE","makeGetRequest loc_to_set "+loc_to_set);
					}

					String full_time = null;
					String date_received = null;
					
					if (null != scanTime)
					{
						String[] pieces_scanTime = scanTime.split("T");
						Log.d("QR_SCAN_SERVICE","makeGetRequest scanTime "+pieces_scanTime[0]+" "+pieces_scanTime[1]);

						full_time = pieces_scanTime[0]+" ";
						date_received = pieces_scanTime[0];
						String upToNCharacters = pieces_scanTime[1].substring(0, 8);
						
						Log.d("QR_SCAN_SERVICE","makeGetRequest scanTime "+upToNCharacters);						
						full_time += upToNCharacters;
						Log.d("QR_SCAN_SERVICE","makeGetRequest fullScanTime "+full_time);						
					}

					Calendar c = Calendar.getInstance();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDate = null;
					String formattedTime = null;
					try {
						
						java.util.Date date = formatter.parse(full_time);
						Calendar cal = Calendar.getInstance();
						  cal.setTime(date);
						  int year = cal.get(Calendar.YEAR);
						  String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
							String shortDayName = dayLongName.substring(0, Math.min(dayLongName.length(), 3));
							int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
							String monthLongName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
							String shortMonthName = monthLongName.substring(0, Math.min(monthLongName.length(), 3));

							formattedDate = shortDayName+" "+shortMonthName+" "+dayofmonth+" "+year;						  

						int minute = cal.get(Calendar.MINUTE);
						int hour = cal.get(Calendar.HOUR);		//12 hour format
						int hourofday = cal.get(Calendar.HOUR_OF_DAY);
					
						String am_pm = hourofday > 12 ? "pm":"am";
						formattedTime = hour+((minute<=9)?":0":":")+minute+" "+am_pm;

						  
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Contact contact = new Contact();
					contact.setGapTime(timeGap);
					contact.setSSID(ssid);
					contact.setName(scan_name);
					contact.setLocation(loc_to_set);
					contact.setTime(formattedTime);
					contact.setDate(formattedDate);
					contact.setFullTime(full_time);
					contact.setRound(round);

					contact.setRoundType(roundType);
//					contact.setAttendence(isAttendance);

					if (username != null)
					{
						contact.setUsername(username);
					}

					if(false == db.CheckIsDataAlreadyInDBorNot(contact))
					{
						db.addContact(contact);

						if(noti_resp == null)
						{
							noti_resp = scan_name;
						}
		
						_index ++;
						updateNwIndex(user_id, selectedDate, _index);
						
						util.savePreference(MainService.this, user_id+"-duplicate_depth", "0");
						duplicate_depth = 0;	
					}
					else
					{
						Log.d("QR_SCAN_SERVICE","makeGetRequest date_received "+date_received +" Date_login "+Date_login);	

						if (duplicate_depth >= 5)
						{	
							updateNwIndexDuplicateDepthCase(user_id, selectedDate);							
							util.savePreference(MainService.this, user_id+"-duplicate_depth", "0");

							Log.d("QR_SCAN_SERVICE","makeGetRequest returns true: Not sure why it should return ");	
							return true;
						}
						else
						{
							util.savePreference(MainService.this, user_id+"-duplicate_depth", ""+(++duplicate_depth));
							_index ++;

							updateNwIndex(user_id, selectedDate,_index);														
						}

					}
				}
			}
		}

		Log.d("QR_SCAN_SERVICE","makeGetRequest notification "+noti_resp +" notify "+notify);	

		if(noti_resp == null && last_user == true)
		{
			Log.d("QR_SCAN_SERVICE","makeGetRequest notification "+noti_resp +" selectedDate "+selectedDate +" SyncFromDate "+SyncFromDate +" "+SyncForDate);	

			updateNwIndexDuplicateDepthCase(user_id, selectedDate);
			util.savePreference(MainService.this, user_id+"-duplicate_depth", "0");
		}

		if (notify == true && noti_resp != null)
		{
			/* Creates an explicit intent for an Activity in your app */
			NotificationCompat.Builder mBuilder =
				    new NotificationCompat.Builder(this)
				    .setSmallIcon(R.drawable.notification_icon)
				    .setContentTitle("Security Roundings Update")
				    .setContentText(noti_resp);
			
			Intent resultIntent = new Intent(this, NotificationView.class);
			
			// Because clicking the notification opens a new ("special") activity, there's
			// no need to create an artificial back stack.
			PendingIntent resultPendingIntent =
			    PendingIntent.getActivity(
			    this,
			    0,
			    resultIntent,
			    PendingIntent.FLAG_UPDATE_CURRENT
			);
			
			mBuilder.setContentIntent(resultPendingIntent);
			
			NotificationManager mNotifyMgr = 
			        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Builds the notification and issues it.
			mNotifyMgr.notify(NOTIFY_ME_ID, mBuilder.build());
		}

		Log.d("QR_SCAN_SERVICE","makeGetRequest returns true ");	
		return true;
	}

	private void makePostRequest(Contact _contact) throws JSONException, UnsupportedEncodingException
	{
		Log.d("QR_SCAN_SERVICE","makePostRequest "); 	
		HttpClient httpClient = new DefaultHttpClient();
		String Uri_nw = "http://ck-monitor.herokuapp.com/api/users/geo?token=";
		String token = util.getPreference(MainService.this,"auth_key");
		if (null == token)
			Uri_nw += "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI1NzI4ZThkMjNlNmRiYzAzMDBmMmEyNWEifQ._6GwriQZ-yey_dsQNZXddZO6LV-Z74PILGr6MuHfljQ";
		else
			Uri_nw += token;
				
		HttpPost httpPost = new HttpPost(Uri_nw);
		httpPost.addHeader("Content-Type", "application/json");
		String json = "";

		// 3. build jsonObject
		JSONArray jsonObject = new JSONArray();
		String location = _contact.getLocation();
		String [] separated = location.split("Latitude:");	
		String [] info = separated[1].split(" Longitude:");

		Log.d("QR_SCAN_SERVICE","Lati "+info[0]+" Longi "+info[1] );
					Log.d("QR_SCAN_SERVICE","loc updating " +"[\""+info[0]+"\""+",\""+info[1]+"\"]");
		Log.d("QR_SCAN_SERVICE","Full Time "+_contact.getFullTime()+" name "+_contact.getName());

		String [] separated_time = _contact.getFullTime().split(" ");	
		String final_time = separated_time[0]+"T"+separated_time[1]+"Z";
		String location_tonw = "["+"\""+info[0]+"\""+","+"\""+info[1]+"\"]";
		Log.d("QR_SCAN_SERVICE","final Time "+final_time +" "+location_tonw);

		
		JSONObject jsonObject_location = new JSONObject();  
		jsonObject_location.accumulate("loc", info[0]);
		jsonObject_location.accumulate("loc", info[1]);
		jsonObject_location.accumulate("name", _contact.getName());
		jsonObject_location.accumulate("scanTime", final_time);
		jsonObject_location.accumulate("timeGap", _contact.getGapTime());
		jsonObject_location.accumulate("SSID", _contact.getSSID());
		jsonObject_location.accumulate("round", _contact.getRound());
		
		jsonObject.put(0, jsonObject_location);		

		// 4. convert JSONObject to JSON to String
		json = jsonObject.toString();
		Log.d("QR_SCAN_SERVICE","json string "+json);
		StringEntity se = new StringEntity(json);
		httpPost.setEntity(se);			// Encoding data

		
/*
 * working start 
 * 		
		JSONObject jsonObject_location = new JSONObject();  
		jsonObject_location.accumulate("loc", info[0]);
		jsonObject_location.accumulate("loc", info[1]);
		jsonObject.put(0, jsonObject_location);
		
		
		
		JSONObject jsonObject_name = new JSONObject();
		jsonObject_name.accumulate("name", _contact.getName());
		//jsonObject.accumulate("scanTime", _contact.getFullTime());
		jsonObject.put(1, jsonObject_name);
		
		
		
		
		JSONObject jsonObject_time = new JSONObject();
		jsonObject_time.accumulate("scanTime", final_time);
		jsonObject.put(2, jsonObject_time);

		JSONObject jsonObject_gap_time = new JSONObject();
		jsonObject_gap_time.accumulate("scanGapTime", _contact.getGapTime());		
		jsonObject.put(3, jsonObject_gap_time);

		JSONObject jsonObject_ssid = new JSONObject();
		jsonObject_ssid.accumulate("SSID", _contact.getSSID());
		jsonObject.put(4, jsonObject_ssid);		

		// 4. convert JSONObject to JSON to String
		json = jsonObject.toString();
		Log.d("QR_SCAN_SERVICE","json string "+json);
		StringEntity se = new StringEntity(json);
		httpPost.setEntity(se);			// Encoding data

*working end
*/

		try {
			HttpResponse response = httpClient.execute(httpPost);			// making request
			// write response to log	
			Log.d("QR_SCAN_SERVICE","makePostRequest response entity "+entityToString(response.getEntity()));
		} catch (ClientProtocolException e) {
			// Log exception
			e.printStackTrace();
		} catch (IOException e) {
			// Log exception
			e.printStackTrace();
		}

	}

	private boolean makeVisPostRequest(Visitor visitor) throws JSONException, UnsupportedEncodingException
	{

		if (null == visitor || null == visitor.getName())
			return false;

		Log.d("QR_SCAN_SERVICE", "makeVisPostRequest ");
		HttpClient httpClient = new DefaultHttpClient();
		String companyId = util.getPreference(MainService.this, "companyId", "");
		Log.d("QR_SCAN_SERVICE","companyId : "+companyId);
		String Uri_nw = "http://ck-monitor.herokuapp.com/api/visitors/"+companyId+"?token=";
		String token = util.getPreference(MainService.this,"authorizeKey");
		Log.d("QR_SCAN_SERVICE", "makePostRequest authorizeKey " + token);

		if (null == token)
			return false;
		else
			Uri_nw += token;

		HttpPost httpPost = new HttpPost(Uri_nw);
		httpPost.addHeader("Content-Type", "application/json");
		String json = "";

		// 3. build jsonObject
		JSONArray jsonObject = new JSONArray();

		JSONObject jsonObject_vis = new JSONObject();

		jsonObject_vis.accumulate("name", visitor.getName());
		jsonObject_vis.accumulate("phone", visitor.getPhone());
		jsonObject_vis.accumulate("inTime", visitor.getTime());
		jsonObject_vis.accumulate("visitDate", visitor.getTime());
		jsonObject_vis.accumulate("vehicleNumber", visitor.getVehicleNumber());
		jsonObject_vis.accumulate("scheduleStatus", visitor.getStatus());
		jsonObject_vis.accumulate("note", visitor.getNote());

		// 4. convert JSONObject to JSON to String
		json = jsonObject_vis.toString();
		Log.d("QR_SCAN_SERVICE","json string :  "+json);
//		jsonObject.put(0, jsonObject_vis);



		StringEntity se = new StringEntity(json);
		Log.d("QR_SCAN_SERVICE","json string "+se);
		httpPost.setEntity(se);			// Encoding data

		boolean return_value = false;

		try {
			HttpResponse response = httpClient.execute(httpPost);			// making request
			// write response to log
			String result = entityToString(response.getEntity());
			//TODO CHECK THIS return_value = result.contains("success");
			return_value = true;
			Log.d("QR_SCAN_SERVICE","makeVisPostRequest response entity "+result);
			updateVisID(visitor, result);
		} catch (ClientProtocolException e) {

			// Log exception
			e.printStackTrace();
		} catch (IOException e) {
			// Log exception
			e.printStackTrace();
		}
		return return_value;
	}

	private boolean editVisitorDetails(int visid) throws JSONException, UnsupportedEncodingException {
		Log.d("QR_SCAN_SERVICE","editVisitorDetails "+visid );
		Visitor visitor = visitor_db.getVisitor(visid);
		if(visitor != null) {

			Log.d("QR_SCAN_SERVICE","visitor_id : "+visitor.getDbId());
			String Uri_nw = "http://ck-monitor.herokuapp.com/api/visitors/"+visitor.getDbId()+"?token=";
			String token = util.getPreference(MainService.this,"authorizeKey");
			Log.d("QR_SCAN_SERVICE", "makePutRequest authorizeKey " + token);

			if (null == token)
				return false;
			else
				Uri_nw += token;

			HttpPut httpPut = new HttpPut(Uri_nw);
			HttpClient httpClient = new DefaultHttpClient();
			httpPut.addHeader("Content-Type", "application/json");
			String json = "";

			// 3. build jsonObject
			JSONArray jsonObject = new JSONArray();

			JSONObject jsonObject_vis = new JSONObject();

			jsonObject_vis.accumulate("name", visitor.getName());
			jsonObject_vis.accumulate("phone", visitor.getPhone());
			if(visitor.getStatus().equals("inTime")||(visitor.getStatus().equals("scheduled")))
				jsonObject_vis.accumulate("inTime", visitor.getTime());
			if(visitor.getStatus().equals("outTime"))
				jsonObject_vis.accumulate("outTime", visitor.getTime());
			jsonObject_vis.accumulate("visitDate", visitor.getTime());
			jsonObject_vis.accumulate("vehicleNumber", visitor.getVehicleNumber());
			jsonObject_vis.accumulate("scheduleStatus", visitor.getStatus());
			jsonObject_vis.accumulate("note", visitor.getNote());

			// 4. convert JSONObject to JSON to String
			json = jsonObject_vis.toString();
			Log.d("QR_SCAN_SERVICE","json string :  "+json);

			StringEntity se = new StringEntity(json);
			Log.d("QR_SCAN_SERVICE","json string "+se);
			httpPut.setEntity(se);			// Encoding data

			boolean return_value = false;
			try {
				HttpResponse response = httpClient.execute(httpPut);			// making request
				// write response to log
				String result = entityToString(response.getEntity());
				//TODO CHECK THIS return_value = result.contains("success");
				return_value = true;
				Log.d("QR_SCAN_SERVICE","makeVisPostRequest response entity "+result);
				updateVisID(visitor, result);
			} catch (ClientProtocolException e) {

				// Log exception
				e.printStackTrace();
			} catch (IOException e) {
				// Log exception
				e.printStackTrace();
			}
			return return_value;

		}
			return false;
	}

	private void updateVisID(Visitor visitor, String return_value) throws JSONException {
		JSONObject reader = new JSONObject(return_value);

		Log.d("QR_SCAN_SERVICE", "updateVisID string=" + return_value);

		JSONObject visitorRsp = reader.getJSONObject("data");


		int arrLen = 0;
		if (visitorRsp == null)
		{
			Log.d("QR_SCAN_SERVICE","visitor resp obj null");
			return;
		}

		String id = visitorRsp.optString("_id").toString();
		Log.d("QR_SCAN_SERVICE", " response entity id:" + id);
		if(!id.equals("")) {
			visitor.setDbId(id);
			visitor_db.updateVisID(visitor);
			Log.d(TAG,"vis after update id :"+visitor);
		}
	}

	private boolean makeVisGetRequest(String user_id, boolean last_user)throws JSONException, UnsupportedEncodingException
	{
		Log.d("QR_SCAN_SERVICE","makeVisGetRequest");
		int arrLen = 0;
		int _index = util.getIntPreference(MainService.this, "NwUpdatedVisIndex", "0");;
		int local_index = 0;

		boolean isFirst = util.getPreference(MainService.this,"isFirstTime",false);
		Log.d("QR_SCAN_SERVICE","makeVisGetRequest login "+isFirst);
		if (false == isFirst)
			return false;

		if (util.isNetworkOnline(this) == false)
		{
			Log.d("QR_SCAN_SERVICE","makeVisGetRequest network offline returns flase");
			return false;
		}
		String Uri_nw = null;
		String companyId = util.getPreference(MainService.this,"companyId","");
		Log.d("QR_SCAN_SERVICE","companyId : "+companyId);
		Uri_nw = "http://ck-monitor.herokuapp.com/api/visitors/"+companyId+"?token=";
		String token = util.getPreference(MainService.this,"authorizeKey");
		Log.d("QR_SCAN_SERVICE", "makePostRequest authorizeKey " + token);

		if (null == token)
			return false;
		else
			Uri_nw += token;

		HttpClient httpClient = new DefaultHttpClient();


		local_index++;

		HttpGet httpGet = new HttpGet(Uri_nw);
		httpGet.addHeader("Content-Type", "application/json");

		Log.d("QR_SCAN_SERVICE","makeVisGetRequest  "+Uri_nw);

		String response_value = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);			// making get request
			// write response to log
			response_value = entityToString(response.getEntity());
			Log.d("QR_SCAN_SERVICE","makeVisGetRequest response entity response "+response_value);

		} catch (ClientProtocolException e) {
			// Log exception
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// Log exception
			e.printStackTrace();
			return false;
		}
		if (null != response_value)
		{
			JSONObject reader = new JSONObject(response_value);
			JSONArray jsonArray = reader.optJSONArray("data");

			if (jsonArray == null || 0 == jsonArray.length())
			{
				if (null != jsonArray)
				{
					Log.d("QR_SCAN_SERVICE","makeGetRequest response arrayLen break "+jsonArray.length());
				}

				return false;
			}
			Log.d("QR_SCAN_SERVICE","makeGetRequest response arrayLen "+jsonArray.length());

			arrLen = jsonArray.length();
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.optString("name").toString();
				String id = jsonObject.optString("_id").toString();
				String phone = jsonObject.optString("phone").toString();
				String inTime = jsonObject.optString("inTime").toString();
				String visitDate = jsonObject.optString("visitDate").toString();
				String vehicleNumber = jsonObject.optString("vehicleNumber").toString();
				String scheduleStatus = jsonObject.optString("scheduleStatus").toString();
				String note = jsonObject.optString("note").toString();
				String outTime = jsonObject.optString("outTime").toString();

				Log.d("QR_SCAN_SERVICE","makeGetRequestVm response entity name:"+name +" phone:"+phone);
				Log.d("QR_SCAN_SERVICE","makeGetRequestVm response entity inTime:"+inTime+" visitDate:"+visitDate);
				Log.d("QR_SCAN_SERVICE","makeGetRequestVm response entity vehicleNumber:"+vehicleNumber+" scheduleStatus:"+scheduleStatus);
				Log.d("QR_SCAN_SERVICE","makeGetRequestVm response entity note "+note);

				String full_time = null;
				String date_received = null;

				if (!inTime.equals(""))
				{
					String[] pieces_scanTime = inTime.split("T");
					Log.d("QR_SCAN_SERVICE","makeGetRequest inTime "+pieces_scanTime[0]+" "+pieces_scanTime[1]);

					full_time = pieces_scanTime[0]+" ";
					date_received = pieces_scanTime[0];
					String upToNCharacters = pieces_scanTime[1].substring(0, 8);

					Log.d("QR_SCAN_SERVICE","makeGetRequest inTime "+upToNCharacters);
					full_time += upToNCharacters;
					Log.d("QR_SCAN_SERVICE","makeGetRequest inTime "+full_time);
				}

				Calendar c = Calendar.getInstance();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = null;
				String formattedTime = null;
				try {

					java.util.Date date = formatter.parse(inTime);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					int year = cal.get(Calendar.YEAR);
					String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
					String shortDayName = dayLongName.substring(0, Math.min(dayLongName.length(), 3));
					int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
					String monthLongName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
					String shortMonthName = monthLongName.substring(0, Math.min(monthLongName.length(), 3));

					formattedDate = shortDayName+" "+shortMonthName+" "+dayofmonth+" "+year;

					int minute = cal.get(Calendar.MINUTE);
					int hour = cal.get(Calendar.HOUR);		//12 hour format
					int hourofday = cal.get(Calendar.HOUR_OF_DAY);

					String am_pm = hourofday > 12 ? "pm":"am";
					formattedTime = hour+((minute<=9)?":0":":")+minute+" "+am_pm;


				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Visitor visitor = new Visitor();
				visitor.setPhone(phone);
				visitor.setName(name);
				visitor.setDate(formattedDate);
				visitor.setTime(formattedTime);
				visitor.setNote(note);
				visitor.setStatus(scheduleStatus);
				visitor.setDbId(id);
				visitor.setOutTime(outTime);
				visitor.setInTime(formattedTime);

				if(false == visitor_db.CheckVisDataAlreadyInDBorNot(visitor))
				{
					long val = visitor_db.addVisitor(visitor);

					_index ++;
//					updateNwIndex(Integer.toString(visitor.getId()), "visitor_get", _index);
					Log.d(TAG,"***Updating index NwUpdatedVisIndex : "+_index);
					util.savePreference(MainService.this, "NwUpdatedVisIndex", "" + _index);

				}
				else
				{
					Log.d(TAG,"visitor is already present");
				}
			}
		}



//		Log.d("QR_SCAN_SERVICE", "makeGetRequest notification " + noti_resp + " notify " + notify);
//
//		if(noti_resp == null && last_user == true)
//		{
//			Log.d("QR_SCAN_SERVICE","makeGetRequest notification "+noti_resp +" selectedDate "+selectedDate +" SyncFromDate "+SyncFromDate +" "+SyncForDate);
//
//			updateNwIndexDuplicateDepthCase(user_id, selectedDate);
//			util.savePreference(MainService.this, user_id+"-duplicate_depth", "0");
//		}
//
//		if (notify == true && noti_resp != null)
//		{
//			/* Creates an explicit intent for an Activity in your app */
//			NotificationCompat.Builder mBuilder =
//					new NotificationCompat.Builder(this)
//							.setSmallIcon(R.drawable.notification_icon)
//							.setContentTitle("Security Roundings Update")
//							.setContentText(noti_resp);
//
//			Intent resultIntent = new Intent(this, NotificationView.class);
//
//			// Because clicking the notification opens a new ("special") activity, there's
//			// no need to create an artificial back stack.
//			PendingIntent resultPendingIntent =
//					PendingIntent.getActivity(
//							this,
//							0,
//							resultIntent,
//							PendingIntent.FLAG_UPDATE_CURRENT
//					);
//
//			mBuilder.setContentIntent(resultPendingIntent);
//
//			NotificationManager mNotifyMgr =
//					(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//			// Builds the notification and issues it.
//			mNotifyMgr.notify(NOTIFY_ME_ID, mBuilder.build());
//		}

		Log.d("QR_SCAN_SERVICE","makeGetRequest returns true ");
		return true;
	}


	public static String entityToString(HttpEntity entity) throws IllegalStateException, IOException {
	  InputStream is = entity.getContent();
	  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
	  StringBuilder str = new StringBuilder();
	
	  String line = null;
	  try {
		while ((line = bufferedReader.readLine()) != null) {
		  str.append(line + "\n");
		}
	  } catch (IOException e) {
		throw new RuntimeException(e);
	  } finally {
		try {
		  is.close();
		} catch (IOException e) {
			//tough luck...
		}
	  }
		return str.toString();
	}


	
    @Override
    public void onDestroy() {
       Log.d("QR_SCAN_SERVICE","Service Destroyed");
    }

	
	String _password[] = {
		"($5*k",
		"@n9k-",
		"bKr87",
		"3N/A5",
		"B37uj",
		"h=*wX",
		"WRbm#",
		")RP(d",
		"JXpet",
		"^#QNc",
		"hS-BT",
		"YAVCZ",
		"jXfJ8",
		"4U6-6",
		"=5^fJ",
		"Bj+(-",
		"*XqJ8",
		")btU_",
		"XQL-(",
		"6%LmY",
		"cf9%g",
		"F2dV8",
		"P3bB*",
		"mL$jR",
		"(3Vd-",
		"/)5Bf",
		"m=TV6",
		"Dq/m/",
		"Umh*n",
		"n99Lh",
		"b%*9a",
		"$j=pf",
		")4x2W",
		"@aHLj",
		"ptC7U",
		"PBLu9",
		"jtu(k",
		"zvgcm",
		"=+f*Sz",
		"d8e*n",
		"c(KhC",
		"6D2@e",
		"P2AHp",
		"6tX$y",
		"*YNrN",
		"VmMcc",
		"$6B/d",
		"MXwGR",
		"kuwr_",
		"/Ty)!",
		"dJS!G",
		"sK=8Q",
		"YUX2h",
		"AAMf/",
		"@A!-V",
		"DLTyX",
		"MqJ^6",
		"c2AeP",
		")hS4D",
		"=_649",
		"sRDSK",
		"r6y+Q",
		"Q7x/h",
		"MpQx3",
		"mcMtM",
		"*ZnZL",
		"RXmJj",
		"BThnM",
		"N9jt^",
		"*)sB+",
		"TNUDq",
		"=+qMvf",
		"FZV2T",
		"Z^/rb",
		"(z5dn",
		"pn9ka",
		"=eb)9",
		"^JBHe",
		"7FZhe",
		"Hpy-q",
		"hxGdb",
		"RUAGh",
		"%hM6Y",
		"LNjxY",
		"CLyS^",
		"pbXx*",
		"!$$w7",
		"UJ!jn",
		"**y7P",
		"^cWDD",
		"4TUW/",
		"bqA2y",
		"@^VHC",
		"SHXjt",
		"9@@CW",
		"qS$W#",
		"p^ZM-",
		"LDaNg",
		"%8MeU",
		"S4Z/D",
		"*ZtH!",
		"E$bB@",
		"dq2G)",
		"^7CX%",
		"@P(pG",
		"$SJg-",
		"hpqLh",
		"(axrN",
		"(RdXu",
		"SG@NG",
		"fW_+5",
		"x)p*H",
		"F!kgy",
		"AF$+G",
		"pAmMu",
		"U!93k",
		"aTL-S",
		"G8Ecq",
		"yejr(",
		"m2K+T",
		"daLf+",
		"fwRsw",
		"tBT)L",
		"5Ynp%",
		"X7vev",
		"BK)Cj",
		"qdGuV",
		"g!nTJ",
		"z=WAT",
		"rj$nb",
		"Jj^3Z",
		"qSa7H",
		"V_aSR",
		"rQp+Z",
		"b9n4q",
		"Qu=zy",
		"Gs#D%",
		"Ufs*e",
		"CErm5",
		"Hf(FV",
		"#6=T@",
		"Rz^aL",
		"!FJW+",
		"Y7=@8",
		"j2HBa",
		"fWU2Z",
		"z)QBg",
		"%AXm=",
		"2SDXQ",
		"MFc--",
		"ZEAh7",
		"T7@u8",
		"Q@J=@",
		"yrkRm",
		"-K$Lq",
		"LEWc^",
		"HUyPN",
		"Q*#(Y",
		"b*s-c",
		"gtYn(",
		"WRtFN",
		"KpwA(",
		"*PapS",
		"W-!pk",
		"A)Hs8",
		"f)-Hd",
		"!yr42",
		"PRKf*",
		"c)9(K",
		"4=fJ/",
		"Dwq@s",
		"7eAzj",
		"/a(3#",
		"(AWVr",
		"KfxJn",
		"pS3$U",
		"hV)=7",
		"xKF8G",
		"KdKq%",
		"M/PSV",
		"6u=44",
		"P7qNX",
		"BCMpk",
		"%mh6#",
		"Mrvm!",
		"tJj9H",
		"R%D9^",
		"$P$nx",
		"VPRm5",
		"HvWa-",
		"Jy3yS",
		"uWvPv",
		"P%Y(t",
		"_JH#A",
		"sf$wM",
		"qcMFM",
		"@-LF$",
		"K*8mH",
		"$9(qV",
		"jaCS2",
		"xfK@E",
		"HpLzL",
		"z+e#n",
		"TThZw",
		"4@AnE",
		")8m57",
		"KqT++",
		"r#QMV",
		"r4WuE",
		"k$8fa",
		"!Gmw_",
		"=cF9c",
		"kVD85",
		"eBegg",
		"QZ9zH",
		"-Ae)!",
		"te%#4",
		"%cV/n",
		"_rpG+",
		"6z$Q4",
		"xNT/2",
		"x9L^F",
		"bMfQ8",
		"p4Pcz",
		"_^VJ2",
		"6Q/7#",
		"NkSuR",
		"4Wc@7",
		"@CZM*",
		"dPdpz",
		"a@#6j",
		"WK*%-",
		"x)hM=",
		"8mtEv",
		"Wq)Ey",
		"$T4jp",
		"=+eBKu",
		"3ETe/",
		"KzvS6",
		"%$Q-w",
		"y/StP",
		"KBctN",
		"qRy)C",
		"Trr@y",
		"QHmrV",
		"C7Ysa",
		"QTJ47",
		"bmu)2",
		"jbFhz",
		"%zNuS"};

	
	;
}
