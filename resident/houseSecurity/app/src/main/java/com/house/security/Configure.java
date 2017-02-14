package com.house.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Configure<SearchResultActivity> extends Activity  implements OnItemSelectedListener{

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
	String exportFile = null;

	EditText mEmergencyView;
	private View mProgressView;

	Spinner date_spinner;
	Spinner rules_spinner;
	ListView eventsList;

	List<item> myList;
	EventListAdapter adapter;

	String selected_location;
	String selected_rules;
	
	DatabaseHandler db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configure);
		Log.d("QR_SCAN_CONFIGURE","activity_configure entering ");

		db = new DatabaseHandler(this);

		date_spinner = (Spinner) findViewById(R.id.ScanLocationList);
		date_spinner.setOnItemSelectedListener(this);
		List<String> dates = db.getAllScanCodes();
		ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
		date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date_spinner.setAdapter(date_dataAdapter);

		myList = new ArrayList<item>();
		rules_spinner = (Spinner) findViewById(R.id.ScanRules);
		rules_spinner.setOnItemSelectedListener(this);
		List<String> scan_rules = new ArrayList<String>();
		scan_rules.add("Locations visited 5 times a week ");
		scan_rules.add("Locations visited 2 times a week");
		scan_rules.add("Locations visited 0 times in a week");
		scan_rules.add("must visit in all rounds");
		
		
		
		ArrayAdapter<String> rules_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, scan_rules);
		rules_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rules_spinner.setAdapter(rules_dataAdapter);

		mEmergencyView = (EditText) findViewById(R.id.EmergencyNum);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));

		String emergency_num = util.getPreference(Configure.this,"emergency_num", null);

		if (null != emergency_num)
			mEmergencyView.setText(emergency_num);

		Log.d("QR_SCAN_CONFIGURE","onCreateAccount ");
		mProgressView = findViewById(R.id.configure_progress);

		adapter = new EventListAdapter(this, myList, null);
		eventsList = (ListView)findViewById(R.id.eventsListView);
		// Assign adapter to ListView
		eventsList.setAdapter(adapter); 
		
	// ListView Item Click Listener
		eventsList.setOnItemClickListener(new OnItemClickListener() {

			  @Override
			  public void onItemClick(AdapterView<?> parent, View view,
				 int position, long id) {
				
			   // ListView Clicked item index
			   int itemPosition 	= position;
			   
			   // ListView Clicked item value
			   String  itemValue	= (String) eventsList.getItemAtPosition(position);
				  
				// Show Alert 
				Toast.makeText(getApplicationContext(),
				  "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
				  .show();
			 
			  }

		 });
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	
	   Log.d("QR_SCAN_CONFIGURE","onItemSelected id "+id +" view id "+parent.getId());
		   
	   if (R.id.ScanLocationList == parent.getId())
	   {			
		   	selected_location = parent.getItemAtPosition(position).toString();
		   	Log.d("QR_SCAN_CONFIGURE","onItemSelected ScanDateList selected location "+selected_location);
		   return ;
	   }
	   
	   if (R.id.ScanRules == parent.getId())
	   {
		   	selected_rules = parent.getItemAtPosition(position).toString();
		   	Log.d("QR_SCAN_CONFIGURE","onItemSelected ScanDateList selected rules "+selected_rules);
		   return ;
	   }
		 
	  }



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
		}
		return true;
	}
	/**
	 * Shows the progress UI and finish form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}


		public void onReupload(View v)
		{			
			Log.d("QR_SCAN_CONFIGURE","onReupload");

		}


	public void onEmergencyNumber(View v) {			
		Log.d("QR_SCAN_CONFIGURE","onEmergencyNumber");

			Log.d("QR_SCAN_CONFIGURE","onEmergencyNumber "+mEmergencyView.getText().toString());
			showProgress(true);

			String emergency_num = util.getPreference(Configure.this, "emergency_num", null);

			if(emergency_num == null || (emergency_num != null && !mEmergencyView.getText().toString().equals(emergency_num)))
			{
				util.savePreference(Configure.this, "emergency_number", mEmergencyView.getText().toString());
				
				Intent intent = new Intent();
				intent.setAction("com.sec.net.create.emergency.num");
				sendBroadcast(intent);
			}
			
			showProgress(false);
			
			finish();
		}

	@SuppressLint("InflateParams")
	public void onCreateAccount(View v) {			
		Log.d("QR_SCAN_CONFIGURE","onCreateAccount");
		
			Log.d("QR_SCAN_CONFIGURE","onCreateAccount "+selected_location +" "+selected_rules);
			
			myList.add(new item(selected_location, selected_rules));
			adapter.notifyDataSetChanged();

			Map<String, String> rules_hash = new HashMap<String, String>();
			rules_hash.put(selected_location, selected_rules);
			
			SharedPreferences keyValues = getBaseContext().getSharedPreferences("rules_list", Context.MODE_PRIVATE);
			SharedPreferences.Editor keyValuesEditor = keyValues.edit();
			
			for (String s : rules_hash.keySet()) {
				// use the name as the key, and the icon as the value
				keyValuesEditor.putString(s, rules_hash.get(s));
			}
			keyValuesEditor.commit();
			
			String value = keyValues.getString(selected_location, null);
			Log.d("QR_SCAN_CONFIGURE","onCreateAccount from hash location "+selected_location+" value "+value);	
		}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.configure, menu);
			return true;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

}
