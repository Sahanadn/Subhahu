package com.house.security;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by user on 11-01-2017.
 */
public class CustomVisAdapter extends ArrayAdapter<String> {
    private final Activity context;

    List<Visitor> mVisitor;
    VisitorDBHandler db;

    public CustomVisAdapter(Activity context, String date) {
        super(context, R.layout.mylist);


        db = new VisitorDBHandler(context);
        if (null == date)
            mVisitor = db.getAllVisitors();
        else
            mVisitor = db.getallVisitorsFromDate(date);

        this.context=context;


    }

    public int setDate(String date) {

        db = new VisitorDBHandler(context);
        if (null == date)
            mVisitor = db.getAllVisitors();
        else
            mVisitor = db.getallVisitorsFromDate(date);

        return mVisitor.size();
    }


    public int getCount() {
        return mVisitor.size();
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public View getView(int position,View view,ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.visitor_list_item, null,true);

        Log.d("QR_SCAN", "getView " + position + " contact " + "list size " + mVisitor.size());

        if(position < mVisitor.size())
        {

            final Visitor vis_details = mVisitor.get(position);

            Log.d("QR_SCAN","getView "+position +" visitor "+"list size "+mVisitor.size()+" "+vis_details.getName() +" "+vis_details.getDate());


            TextView visname = (TextView) rowView.findViewById(R.id.visitorname);
            TextView visPhone = (TextView) rowView.findViewById(R.id.phone);
            final TextView status = (TextView) rowView.findViewById(R.id.inout);
            visname.setText(vis_details.getName());
            visPhone.setText(vis_details.getPhone());

            if(vis_details.status.equals("scheduled")) {
                status.setText("SCHEDULED");
                status.setTextColor(Color.BLUE);
            } else if(vis_details.status.equals("in")) {
                status.setText("IN");
                status.setTextColor(Color.GREEN);
            } else if(vis_details.status.equals("out")) {
                status.setText("OUT");
                status.setTextColor(Color.RED);
            }

            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupEditSchedule(v,context,vis_details, status);
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle(vis_details.getName())
                            .setMessage("Visit Date : \n"+vis_details.getDate() + "\n In Time:\n" + vis_details.getInTime()+"\n Out Time: \n"+ vis_details.getOutTime()
                            +"\nNOTE:\n"+vis_details.getNote())
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                }
            });

            rowView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("ONLONG CLICK")
                                  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                    return true;
                }
            });
        }

        return rowView;

    }

    private void popupEditSchedule(View v, Activity context, final Visitor visitor, final TextView scheduletxt) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        final LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_schedule, null);
        dialogBuilder.setView(dialogView);
        final RadioGroup schedule = (RadioGroup)dialogView.findViewById(R.id.radio);
        dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String status = "scheduled";
                switch(schedule.getCheckedRadioButtonId()) {
                    case R.id.scheduled :
                        status = "scheduled";
                        break;
                    case R.id.in :
                        status = "in";
                        break;
                    case R.id.out :
                        status = "out";
                        break;
                }
                visitor.status = status;

                String newtime = GetTime();
                String fulltime = Datetime();
                visitor.setTime(fulltime);
                db.setFullTime(visitor,fulltime);
                if(status.equals("scheduled")) {
                    scheduletxt.setText("SCHEDULED");
                    scheduletxt.setTextColor(Color.BLUE);
                    visitor.setInTime(newtime);
                    db.setInTime(visitor, newtime);
                } else if(status.equals("in")) {
                    scheduletxt.setText("IN");
                    visitor.setInTime(newtime);
                    db.setInTime(visitor,newtime);
                    scheduletxt.setTextColor(Color.GREEN);
                } else if(status.equals("out")) {
                    scheduletxt.setText("OUT");
                    scheduletxt.setTextColor(Color.RED);
                    visitor.setOutTime(newtime);
                    db.setOutTime(visitor,newtime);
                }

                changeStatus(visitor);

            }
        });
        dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void changeStatus(Visitor visitor) {
        db.chageStatus(visitor);
        Intent intent = new Intent(context, MainBootComplete.class);
        intent.setAction("com.sec.vismgmt.edit");
        intent.putExtra("id", visitor.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
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
        Log.d("QR_SCAN_HOME", hour + ":" + minute + " " + am_pm);
        return formattedDate;
    }

    public static String Datetime()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getDefault());
        String formattedDate = formatter.format(cal.getTime());
        return formattedDate;
    }

}
