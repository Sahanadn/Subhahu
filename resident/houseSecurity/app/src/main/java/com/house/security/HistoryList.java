package com.house.security;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

@SuppressLint("NewApi")
public class HistoryList<SearchResultActivity> extends Activity implements OnItemSelectedListener {

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
	String exportFile = null;
	ListView list;
	DatabaseHandler db;
	Spinner date_spinner;
	CustomListAdapter adapter;

	String selected_date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_list);
		Log.d("QR_SCAN_HISTORY","activity_history_list entering ");

		db = new DatabaseHandler(this);
		boolean syncStatus = util.getPreference(this,"refreshing",false);

/*

		if(true == syncStatus)
		{
			ProgressBar mprogressBar;
			mprogressBar = (ProgressBar) findViewById(R.id.circular_progress_bar);
					ObjectAnimator anim = ObjectAnimator.ofInt(mprogressBar, "progress", 0, 100);
					anim.setDuration(1500);
					anim.setInterpolator(new DecelerateInterpolator());
					anim.start();
					
					mprogressBar.setVisibility(GONE);
		}
*/

		String userName = util.getPreference(this,"client_userName",null);

		date_spinner = (Spinner) findViewById(R.id.ScanDateList);
		date_spinner.setOnItemSelectedListener(this);
		List<String> dates = db.getAllScanDates(userName);
		ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
		date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date_spinner.setAdapter(date_dataAdapter);


		adapter = new CustomListAdapter(this, null);
		list = (ListView)findViewById(R.id.listView);
		list.setAdapter(adapter);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("History");
		
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
	}

		 public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	
			Log.d("QR_SCAN_HISTORY","onItemSelected id "+id +" view id "+parent.getId());
				
			if (R.id.ScanDateList == parent.getId())
			{
				selected_date = parent.getItemAtPosition(position).toString();

				Log.d("QR_SCAN_HISTORY","onItemSelected ScanDateList selected date "+selected_date);
	
				adapter.setDate(selected_date);
				//list=(ListView)findViewById(R.id.listView);
				list.setAdapter(adapter);

				return ;
			}
			
			  
		   }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.admin_action, menu);
		return  true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int mYear;
		int mMonth;
		int mDay;
		switch (item.getItemId()) {

			case R.id.Export:
				Log.d("QR_SCAN_HISTORY", "onClick export button");
				exportFile = db.export();
				Toast.makeText(getApplicationContext(), "File saved at "+exportFile,
															Toast.LENGTH_LONG).show();

				return true;
			case R.id.Erase:
				Log.d("QR_SCAN_HISTORY", "Erase");
				eraseScanDetails();
				return true;
			case R.id.ErashForDate:
					eraseScanDetailsFromDate(selected_date);
			return true;

			case R.id.SyncForDate:
			{
				Log.d("QR_SCAN_HISTORY", "SyncForDate ");
				
				final Calendar c = Calendar.getInstance();
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);
						 
				DatePickerDialog dpd = new DatePickerDialog(this,
				        new DatePickerDialog.OnDateSetListener() {
				 
				            @Override
				            public void onDateSet(DatePicker view, int year,
				                    int monthOfYear, int dayOfMonth) {
				                								
								String SyncForDate = ""+year+"-"+(monthOfYear + 1)+"-"+dayOfMonth;
								Log.d("QR_SCAN_HISTORY", "SyncForDate "+SyncForDate);
								
								
								
								List<Contact> mContact = db.getContactsFromDate(SyncForDate);
								if(mContact.size() == 0)
								{
									LinkedHashSet<String> s = util.getUserIdsHash(HistoryList.this);
									int size  = s.size();
									Object[] array = s.toArray();
									int i = 0;
									while (i < size)
									{
										String user_id = (String) array [i++];
										util.savePreference(HistoryList.this,user_id+"-SyncForDate",SyncForDate);
									}
								}
								HistoryList.this.finish();
				            }

				        }, mYear, mMonth, mDay);
				dpd.show();

				Log.d("QR_SCAN_HISTORY", "SyncForDate outside" + mDay + "-"
						+ (mMonth + 1) + "-" + mYear);

 				Intent intent_br = new Intent();
				intent_br.setAction("com.sec.net.syncForDate");
				sendBroadcast(intent_br);
			}
			return true;
			case R.id.SyncFromDate:					
			{
					Log.d("QR_SCAN_HISTORY", "SyncFromDate ");

					final Calendar c_ = Calendar.getInstance();
					mYear = c_.get(Calendar.YEAR);
					mMonth = c_.get(Calendar.MONTH);
					mDay = c_.get(Calendar.DAY_OF_MONTH);
							 
					DatePickerDialog dpd_ = new DatePickerDialog(this,
							new DatePickerDialog.OnDateSetListener() {
					 
								@Override
								public void onDateSet(DatePicker view, int year,
										int monthOfYear, int dayOfMonth) {
																	
									String dateFrom = ""+year+"-"+(monthOfYear + 1)+"-"+dayOfMonth;
									Log.d("QR_SCAN_HISTORY", "dateFrom "+dateFrom);
									
									if(null == dateFrom) {
										HistoryList.this.finish();
									}
									
									List<Contact> mContact = db.getContactsFromDate(dateFrom);
									Log.d("QR_SCAN_HISTORY", "dateFrom "+dateFrom +" size "+mContact.size());									
									if(mContact.size() == 0)
									{

										LinkedHashSet<String> s = util.getUserIdsHash(HistoryList.this);
										int size  = s.size();
										Object[] array = s.toArray();
										int i = 0;
										while (i < size)
										{
											String user_id = (String) array [i++];
											util.savePreference(HistoryList.this,user_id+"-SyncFromDate",dateFrom);
										}
										HistoryList.this.finish();
									}				 
								}
							}, mYear, mMonth, mDay);
					dpd_.show();

				Intent intent_br = new Intent();
				intent_br.setAction("com.sec.net.syncForDate");
				sendBroadcast(intent_br);
					
			}		
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

					util.addToHashSet(HistoryList.this,"clear");
					dialog.dismiss();
					HistoryList.this.finish();
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

	public void eraseScanDetailsFromDate(final String date)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
			builder.setTitle("Confirm");
			builder.setMessage("Are you sure?");
		
			builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog
						db.deleteAllContact(date);
					dialog.dismiss();
					HistoryList.this.finish();
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


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	


}
