package com.house.security;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

public class VisitorListActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "VisitorListActivity";
    VisitorDBHandler db;
    Spinner date_spinner;
    ListView list;
    CustomVisAdapter adapter;
    List<String> dates;
    String selectedDate = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_list);
        db = new VisitorDBHandler(this);
        date_spinner = (Spinner) findViewById(R.id.ScanDateList);
        date_spinner.setOnItemSelectedListener(this);
        dates = db.getallVisDates();

        ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dates);
        date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_spinner.setAdapter(date_dataAdapter);
        adapter = new CustomVisAdapter(this, null);
        list=(ListView)findViewById(R.id.listView);
        list.setAdapter(adapter);
        Log.d(TAG, "setAdapter");
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Visitor list");
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        dates = db.getallVisDates();
        adapter = new CustomVisAdapter(this, dates.get((int)id));
        adapter.notifyDataSetChanged();
        list.setAdapter(adapter);
        selectedDate = dates.get((int)id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.vis_list_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int mYear;
        int mMonth;
        int mDay;
        switch (item.getItemId()) {

            case R.id.Refresh:
                Log.d("QR_SCAN_HISTORY", "onClick export button");
                Intent alarm_intent = new Intent(this, MainBootComplete.class);
                alarm_intent.setAction("com.sec.vismgmt.get");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.Delete:
                Log.d("QR_SCAN_HISTORY", "onClick export button");
                new AlertDialog.Builder(VisitorListActivity.this)
                        .setTitle("Delete")
                        .setMessage("Delete all visitors from this date?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteAllVisitors(selectedDate);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();


                break;
            default:break;
        }
        return true;

    }

}