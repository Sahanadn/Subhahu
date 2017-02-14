package com.house.security;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VisitorActivity extends Activity implements AdapterView.OnItemSelectedListener {
    EditText vName;
    EditText vNumber;
    EditText mNote;
    Button BtnTime;
    TextView timer;
    Button submit;
    int yeartxt;
    int monthtxt;
    int dayofMonthtxt;
    int hour;
    int min;
    String time;
    String ampm;
    String scheduleTime;
    String scheduleDate;
    Visitor visitor = new Visitor();
    Spinner prevVisitors;

    List<Visitor> visitors = new ArrayList<Visitor>();
    VisitorDBHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);
        vName = (EditText) findViewById(R.id.visitorname);
        vNumber = (EditText) findViewById(R.id.phone);
        mNote = (EditText)findViewById(R.id.note);
        BtnTime = (Button) findViewById(R.id.time);
        timer = (TextView) findViewById(R.id.timetxt);
        prevVisitors = (Spinner) findViewById(R.id.prev_vis);
        submit = (Button) findViewById(R.id.submit);
        db = new VisitorDBHandler(this);

        BtnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupCalender();
            }
        });

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("VisitorActivity");
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formattedDate = df.format(c.getTime());
        monthtxt = c.get(Calendar.MONTH);
        yeartxt = c.get(Calendar.YEAR);
        dayofMonthtxt = c.get(Calendar.DAY_OF_MONTH);

        min = c.get(Calendar.MINUTE);
        hour = c.get(Calendar.HOUR);
        scheduleTime = (String) Datetime(c);
        convertTime(c);
        timer.setText(formattedDate);
        getPrevVisitors();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPostRequest();
            }
        });
    }

    private void getPrevVisitors() {
        String userName = util.getPreference(this, "client_userName", null);
        prevVisitors.setOnItemSelectedListener(this);
        visitors = db.getAllVisitors();
        List<String> visitornames = new ArrayList<String>();
        for(int i=0 ;i<visitors.size();i++)
            visitornames.add(visitors.get(i).getName());
        ArrayAdapter<String> date_dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, visitornames);
        date_dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prevVisitors.setAdapter(date_dataAdapter);
    }


    void popupCalender() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.visitor_calender, null);
        dialogBuilder.setView(dialogView);

        CalendarView cal = (CalendarView) dialogView.findViewById(R.id.calendarView);
        cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                if(year > 0)
                    yeartxt = year;
                if(month > 0)
                    monthtxt = month;
                if(dayOfMonth > 0)
                    dayofMonthtxt = dayOfMonth;
                Log.d("VisitorActivity ","yeartxt:"+yeartxt+" monthtxt:"+monthtxt+" dayofMonthtxt:"+dayofMonthtxt);
            }
        });
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                popUpTime();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.show();
    }

    private void popUpTime() {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.visitor_time, null);
//        dialogBuilder.setView(dialogView);
//
//        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timePicker);
//        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                ViewGroup vg=(ViewGroup) view.getChildAt(0);
//
//                hour = hourOfDay;
//                min = minute;
//
//            }
//        });
//        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                timer.setText("date: "+dayofMonthtxt+"/"+monthtxt+"/"+yeartxt+" time: "+hour+":"+min+ " "+ampm);
//            }
//        });
//        AlertDialog alertDialog = dialogBuilder.create();
//
//        alertDialog.show();



        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        min = c.get(Calendar.MINUTE);


        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();

                    hour = hourOfDay;
                    min = minute;
                calendar.set(yeartxt, monthtxt, dayofMonthtxt, hour, min);
                Log.d("VisitorActivity ", "hour:" + hour + " min:" + min);

               // textView.setText((String) DateFormat.format("hh:mm aaa", calendar));
                timer.setText(Datetime(calendar));
                time = (String) Datetime(calendar);
                scheduleTime = (String) Datetime(calendar);
                convertTime(calendar);

                Log.d("VisitorActivity","converttime scheduleTime:"+scheduleTime+" scheduledate"+scheduleDate);
            }
        };

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog picker = new TimePickerDialog(VisitorActivity.this,listener, hour, min,
                DateFormat.is24HourFormat(VisitorActivity.this));

        picker.show();

    }

    private void convertTime(Calendar cal) {
//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedDate = null;
//        String formattedTime = null;

//            java.util.Date date = formatter.parse(scheduletime);
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        String shortDayName = dayLongName.substring(0, Math.min(dayLongName.length(), 3));
        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        String monthLongName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String shortMonthName = monthLongName.substring(0, Math.min(monthLongName.length(), 3));

        scheduleDate = shortDayName+" "+shortMonthName+" "+dayofmonth+" "+year;

        int minute = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR);		//12 hour format
        int hourofday = cal.get(Calendar.HOUR_OF_DAY);

        String am_pm = hourofday > 12 ? "pm":"am";
        scheduleTime = hour+((minute<=9)?":0":":")+minute+" "+am_pm;


    }

    void sendPostRequest(){

        visitor.setPhone(vNumber.getText().toString());
        visitor.setName(vName.getText().toString());
        visitor.setDate(scheduleDate);
        visitor.setTime(time);
        visitor.setInTime(scheduleTime);
        visitor.setNote(mNote.getText().toString());
        visitor.setStatus("scheduled");

        db.addVisitor(visitor);

        Intent alarm_intent = new Intent(this, MainBootComplete.class);
        alarm_intent.setAction("com.sec.vismgmt.post");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        finish();
    }

    @SuppressLint("SimpleDateFormat")
    public static String Datetime(Calendar c)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getDefault());
        String formattedDate = formatter.format(c.getTime());
        return formattedDate;
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(VisitorActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String   add = obj.getAddressLine(0);
            String  currentAddress = obj.getSubAdminArea() + ","
                    + obj.getAdminArea();
            double   latitude = obj.getLatitude();
            double longitude = obj.getLongitude();
            String currentCity= obj.getSubAdminArea();
            String currentState= obj.getAdminArea();
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();


            System.out.println("obj.getCountryName()"+obj.getCountryName());
            System.out.println("obj.getCountryCode()"+obj.getCountryCode());
            System.out.println("obj.getAdminArea()"+obj.getAdminArea());
            System.out.println("obj.getPostalCode()"+obj.getPostalCode());
            System.out.println("obj.getSubAdminArea()"+obj.getSubAdminArea());
            System.out.println("obj.getLocality()"+obj.getLocality());
            System.out.println("obj.getSubThoroughfare()"+obj.getSubThoroughfare());


            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Visitor visitor = visitors.get(position);
        if(visitor != null) {
            if (visitor.getName() != null)
                vName.setText(visitor.getName());
            if (visitor.getPhone() != null) {
                vNumber.setText(visitor.getPhone());
            }
            if (visitor.getNote() != null) {
                mNote.setText(visitor.getNote());
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

