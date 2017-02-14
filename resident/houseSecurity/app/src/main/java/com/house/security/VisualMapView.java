package com.house.security;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressLint("NewApi")
public class VisualMapView<SearchResultActivity> extends Activity implements  OnItemSelectedListener {

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
	String exportFile = null;
	Spinner scancode_spinner;
	Spinner date_spinner;
	
	String selected_date_;
	String selected_round;
	int selected_round_position;
	
	Spinner timesVisit_spinner;
	
	ListView list;
	DatabaseHandler db;
    //render google map on a web view
    private WebView mWebView;
//    private GoogleMap mMap;
   
	String g_Lati;
	String g_Longi;
	String lati_prev;
	String longi_prev;

	boolean g_HtmlInit = false;
	List<Rounds> rounds_info = null;
	boolean  g_first_time = true;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.history_list_action,menu);
		return  true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_visual_map_view);
		Log.d("QR_SCAN_MAP_VIEW", "VisualMapView entering ");
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));

		db = new DatabaseHandler(this);

		 String userName = util.getPreference(this,"client_userName",null);

		 // date Spinner element
	      date_spinner = (Spinner) findViewById(R.id.ScanDateList);
	      date_spinner.setOnItemSelectedListener(this);
	      List<String> dates = db.getAllScanDates(userName);
	      ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
	      date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	      date_spinner.setAdapter(date_dataAdapter);
		  
	      selected_date_ = null;
	      selected_round = null;
		  selected_round_position = 0;
	      if(dates.size() != 0)
	      {
	      	selected_date_ = dates.get(0);
	    	Log.d("QR_SCAN_MAP_VIEW","activity_map_view date 1 "+dates.get(0));
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
			Log.d("QR_SCAN_MAP_VIEW","activity_map_view rounds_info size "+rounds_info.size());
							
			if (0 != rounds_info.size())
			{
				Rounds round = rounds_info.get(0);
				if (0 != round.getIndexSize())
					selected_round = ""+round.getIndex(1);

				Log.d("QR_SCAN_MAP_VIEW","activity_map_view round index "+round.getIndexSize() +" "+selected_round);				
			}
			
			Log.d("QR_SCAN_MAP_VIEW","activity_map_view rounds_info size "+getIndextoString (rounds_info).size());

			ArrayAdapter<String> timeVisitAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				getIndextoString (rounds_info));
			
			timeVisitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timesVisit_spinner.setAdapter(timeVisitAdapter);		

		  }
	      
	        mWebView = (WebView) findViewById(R.id.webview);

	     // Enable Javascript
	        WebSettings webSettings = mWebView.getSettings();
	        webSettings.setBuiltInZoomControls(true);
	        mWebView.setWebViewClient(new GeoWebViewClient());
	        webSettings.setJavaScriptEnabled(true);
	        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        webSettings.setGeolocationEnabled(true);

	        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
	        mWebView.loadUrl("file:///android_asset/map_security.html");
	}

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
     * WebViewClient subclass loads all hyperlinks in the existing WebView
     */
    public class GeoWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }
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
   
   
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        // Show a toast from the web page
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        // Let javascript log debug messages here
        @JavascriptInterface
        public void log(String msg) {
            //Log.d(LocationUtils.APPTAG, " HTML: " + msg);
			Log.d("QR_SCAN_MAP_VIEW"," HTML: "+msg);
        }


        // Let android know html is initialized
        @JavascriptInterface
        public void onHtmlInitialized() {
			Log.d("QR_SCAN_MAP_VIEW"," HTML: onHtmlInitialized");
			g_HtmlInit = true;	
            Log.d(LocationUtils.APPTAG, " HTML: onHtmlInitialized "+g_Lati+" "+g_Longi);
            //Tell javascript about current location and load the map
            runOnUiThread(new Runnable() {
                public void run() {

				
				mWebView.loadUrl("javascript:deleteMarkers()");
				mWebView.loadUrl("javascript:setStartingPosOnMap("+g_Lati+","+g_Longi+")");
							Log.d("QR_SCAN_MAP_VIEW"," HTML: onHtmlInitialized"+g_Lati+" "+g_Longi);
			/*
                    Location lastLocation = getLocation();
                    if(lastLocation != null) {
                        mWebView.loadUrl("javascript:setStartingPosOnMap("+lastLocation.getLatitude()+","+lastLocation.getLongitude()+")");
                    }
					*/
                }
            });
        }

        @JavascriptInterface
        public void startLocationtUpdates() {
        	Log.d("QR_SCAN_MAP_VIEW"," HTML: startLocationtUpdates");
            runOnUiThread(new Runnable() {
                public void run() {
                    //startUpdates();
                }
            });
        }

        @JavascriptInterface
        public void stopLocationtUpdates() {
			Log.d("QR_SCAN_MAP_VIEW"," HTML: stopUpdates");
            runOnUiThread(new Runnable() {
                public void run() {
                    //stopUpdates();
                }
            });
        }

        @JavascriptInterface
        public void indicateTurnAhead(int value) {
            Log.d(LocationUtils.APPTAG, " HTML: indicateTurnAhead= " + value);
			Log.d("QR_SCAN_MAP_VIEW","HTML: indicateTurnAhead= ");
            
                        
        }

        // for debugging spit speak the instruction
        @JavascriptInterface
        public void speakInstruction(final String instruction) {
            // DO not repeat the instruction
			Log.d("QR_SCAN_MAP_VIEW","HTML: speakInstruction= ");            
        }

    }
    
	 public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		Log.d("QR_SCAN_MAP_VIEW","onItemSelected id "+id +" view id "+parent.getId());
			
		if (R.id.ScanDateList == parent.getId())
		{
			  String _date = parent.getItemAtPosition(position).toString();
			  selected_date_ = _date;
			  
		      Log.d("QR_SCAN_MAP_VIEW","onItemSelected ScanDateList selected date "+_date);

			  rounds_info = null;
		      rounds_info = db.getAllRounds(_date);
		      timesVisit_spinner.setOnItemSelectedListener(this);

		      ArrayAdapter<String> timeVisitAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
			  	getIndextoString (rounds_info));
		      timeVisitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      timesVisit_spinner.setAdapter(timeVisitAdapter);

		      if (0 != rounds_info.get(0).getIndexSize())
		    	  selected_round = ""+rounds_info.get(0).getIndex(1);

		     // mWebView.loadUrl("javascript:deleteMarkers()");
			  	
			return ;
		}
		
		if (R.id.timesVisitList == parent.getId())
		{		
			selected_round = parent.getItemAtPosition(position).toString();
			Log.d("QR_SCAN_MAP_VIEW","onItemSelected timesVisitList selected date "+selected_round + " position "+position);
			selected_round_position = position;

			List<String> scan_codes = rounds_info.get(position).getAllNames();

			//List<String> categories = db.getAllScanCodesbyDate(selected_date_);
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, scan_codes);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			scancode_spinner.setAdapter(dataAdapter);

			String location = db.get_scanLocation(scan_codes.get(0), selected_date_);
			List<String> locations = db.get_scanAllLocation(scan_codes.get(0), selected_date_, rounds_info.get(position).getRealIndex());
			int index = 0;

			Log.d("QR_SCAN_MAP_VIEW","onItemSelected timesVisitList locations size "+locations.size());

			
			g_first_time = true;			
			mWebView.loadUrl("javascript:deleteMarkers()"); 					
			while (index < locations.size())
			{
				location = locations.get(index);
				
				String [] separated = location.split("Latitude:");	
				String [] info = separated[1].split(" Longitude:");
				
				Log.d("QR_SCAN_MAP_VIEW","Lati "+info[0]+" Longi "+info[1]);
				
				g_Lati = info[0];
				g_Longi = info[1];

				if (index == 0)
				{
					lati_prev = g_Lati;
					longi_prev = g_Longi;
				}
				index ++;
				
				if( true == g_HtmlInit)
				{
					runOnUiThread(new Runnable() {
						public void run() {
						Log.d("QR_SCAN_MAP_VIEW","runOnUiThread "+g_HtmlInit);
												
							if (g_first_time == true)
							{
								g_first_time = false; 
								mWebView.loadUrl("javascript:setStartingPosOnMap("+g_Lati+","+g_Longi+")");
								Log.d("QR_SCAN_MAP_VIEW","runOnUiThread first time");
							}
							else
							{
								mWebView.loadUrl("javascript:setStartingPosOnMap("+g_Lati+","+g_Longi+")");
								Log.d("QR_SCAN_MAP_VIEW","setStartingPosOnMap with Lati "+g_Lati+" Longi "+g_Longi);
							}
/*
							if (lati_prev != g_Lati)
							{
								mWebView.loadUrl("javascript:drawLines("+lati_prev+","+longi_prev+","+g_Lati+","+g_Longi+")");
							}
							*/
						}
					});
				}

				lati_prev = g_Lati;
				longi_prev = longi_prev;

			}
						
			return ;
		}

		if (R.id.ScanCodeList == parent.getId())
		{

			Log.d("QR_SCAN_MAP_VIEW","onItemSelected ScanCodeList selected date "+selected_date_ + "position "+position);

			String scan_code = parent.getItemAtPosition(position).toString();
			String location = db.get_scanLocation(scan_code, selected_date_);

			List<String> locations = db.get_scanOnlyLocation(scan_code, selected_date_, 
				rounds_info.get(selected_round_position).getRealIndex());

			int index = 0;
			Log.d("QR_SCAN_MAP_VIEW","ScanCodeList selected locations size "+locations.size() +" "+location);

			while (index < locations.size())
			{
				location = locations.get(index++);
				
				String [] separated = location.split("Latitude:");	
				String [] info = separated[1].split(" Longitude:");
				
				Log.d("QR_SCAN_MAP_VIEW","ScanCodeList selected date Lati "+info[0]+" Longi "+info[1]);
	
			}

			if(null != location && location.contains("Latitude"))
			{
				String [] separated = location.split("Latitude:");
				
				Log.d("QR_SCAN_MAP_VIEW","separated "+separated);

				if( null != separated && null != separated[0] && null != separated[1])
				{
					Log.d("QR_SCAN_MAP_VIEW","separated[0] "+separated[0]+" separated[1] "+separated[1]);
					String [] info = separated[1].split(" Longitude:");			
					Log.d("QR_SCAN_MAP_VIEW","setCircleOnMap Lati "+info[0]+" Longi "+info[1]);
					mWebView.loadUrl("javascript:setCircleOnMap("+info[0]+","+info[1]+")");
				}
			}

				
/*
			if(null != date_spinner && null != date_spinner.getSelectedItem()) 
				selected_date = date_spinner.getSelectedItem().toString();
			  // On selecting a spinner item
			
			  String item = parent.getItemAtPosition(position).toString();
			  Log.d("QR_SCAN_HIS_MAIN","scan selected date "+selected_date +" scan "+item);
			  
			  int gap_conf = 30;
			  String max_gap = null;
			  String above_conf = null;
	
			  int scan_count = db.get_scancounts(item, selected_date);
			  String avg_gap_time = db.get_scangap_details(item, selected_date,gap_conf, max_gap, above_conf);
			  Log.d("QR_SCAN_HIS_MAIN","avg_gap "+avg_gap_time+" code "+item+" max_gap "+max_gap+" above_conf "+above_conf );
*/
	
	//		  EditText scanCountText = (EditText)findViewById(R.id.timesVisitText);
	//		  scanCountText.setText(""+scan_count);
/*	
			  EditText scanAvgGapText = (EditText)findViewById(R.id.avgTimeGapText);
			  scanAvgGapText.setText(""+avg_gap_time+" (" +scan_count+" times)");
	
			  EditText scanMaxGapText = (EditText)findViewById(R.id.maxTimeGapLayoutText);
			  if (0 != scan_count) 
				scanMaxGapText.setText(""+db.m_max_gap );
			  else
				scanMaxGapText.setText("0 min");
	
				EditText ssidText = (EditText)findViewById(R.id.scanSSIDlist);
				int pos_round = Integer.parseInt(selected_round);
				ssidText.setText(""+db.get_scanSSIDFromRounds(item, selected_date, rounds_info.get(pos_round-1).getRealIndex()));
				//ssidText.setText(""+db.get_scanSSID(item, selected_date));
*/				
		//		Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
			return ;
		}
		
//		  setLocation("Latitude: "+lat+" Longitude: "+mService.getLongitude());

				
				//mWebView.loadUrl("javascript:setStartingPosOnMap("+info[0]+","+info[1]+")");

				//mWebView.loadUrl("javascript:updateCurrPosOnMap("+info[0]+","+info[1]+")");
				
	      // Showing selected spinner item
	      
	   }



	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	    case R.id.Export:
			Log.d("QR_SCAN_MAP_VIEW", "onClick export button");
			exportFile = db.export();
            return true;
            
	    case R.id.History:
	            Log.d("QR_SCAN_MAP_VIEW", "History");
				try {
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();		
					intent.setClass(getApplicationContext(), HistoryList.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
					
				}				
	            return true;
/*	            
	        case R.id.Configure:
	            Log.d("QR_SCAN_MAP_VIEW", "Configure");
	            try {
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();		
					intent.setClass(getApplicationContext(), Configure.class);
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException anfe) {
					
				} 
	            return true;
	        case R.id.Emergency:
	            Log.d("QR_SCAN_MAP_VIEW", "Emergency Off");
				 
				   	String phoneNo = util.getPreference(getApplicationContext(), "emergency_num", null);
					if(phoneNo == null)
						phoneNo = "09886562738";
					
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
*/ 
	        case R.id.Share:
	            Log.d("QR_SCAN_MAP_VIEW", "Share");
	            shareIt();
	            return true;	      	                 
	        case R.id.Erase:
	            Log.d("QR_SCAN_MAP_VIEW", "Erase");

				eraseScanDetails();
	            
	            return true;
			case android.R.id.home:
				this.finish();
				return true;
	        default:
	            return false;
	    }
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

		Log.d("QR_SCAN_MAP_VIEW",  formattedDate);

		Calendar c1 = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate_new = formatter.format(c1.getTime());
		Log.d("QR_SCAN_MAP_VIEW New ",  formattedDate_new+"-"+formattedDate);

		return formattedDate_new+"-"+formattedDate;
	}

	private void shareIt ()
	{
		String shareBody = "Daily roundings exported in xls file";
		
		exportFile = db.export();
		
		String inputfilePath = Environment.getExternalStorageDirectory()
								.getPath() + "/"+exportFile;

		File saveFile = new File(inputfilePath);
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("image/*");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Datetime());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

		sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(saveFile) );  

		{
			String filename = "crashReport.txt";
			String crashFile = Environment.getExternalStorageDirectory()
					.getPath() + "/"+filename;
			File file = new File(crashFile);
 			if(file.exists()){
				sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file) );
 			}
		}
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		Log.d("QR_SCAN_MAP_VIEW", "onNothingSelected");
	}

}
