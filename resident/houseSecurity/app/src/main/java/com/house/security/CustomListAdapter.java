package com.house.security;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<String> {

	private final Activity context;

	List<Contact> mContact;

	public CustomListAdapter(Activity context, String date) {
		super(context, R.layout.mylist);

		DatabaseHandler db;	
		db = new DatabaseHandler(context);
		if (null == date)
			mContact = db.getAllContacts();
		else
			mContact = db.getContactsFromDate(date);
		
		this.context=context;


	}

	public int setDate(String date) {

			DatabaseHandler db; 
			db = new DatabaseHandler(context);
			if (null == date)
				mContact = db.getAllContacts();
			else
				mContact = db.getContactsFromDate(date);

			return mContact.size();
		}


	public int getCount() {
		return mContact.size();
	}


	public View getView(int position,View view,ViewGroup parent) {

		LayoutInflater inflater=context.getLayoutInflater();
		View rowView=inflater.inflate(R.layout.mylist, null,true);

		Log.d("QR_SCAN","getView "+position +" contact "+"list size "+mContact.size());

		if(position < mContact.size())
		{

			final Contact contact_details = mContact.get(position);

			Log.d("QR_SCAN","getView "+position +" contact "+"list size "+mContact.size()+" "+contact_details.getName() +" "+contact_details.getDate());

			TextView date = (TextView) rowView.findViewById(R.id.date);
			TextView imageView = (TextView) rowView.findViewById(R.id.textView);
			TextView ssid = (TextView) rowView.findViewById(R.id.ssid);
//			TextView latitude = (TextView) rowView.findViewById(R.id.latitude);
//			TextView gap = (TextView) rowView.findViewById(R.id.gap);

			imageView.setText(contact_details.getName());
			date.setText(contact_details.getDate() + " " + contact_details.getTime() );
			ssid.setText(contact_details.getSSID());
//			latitude.setText(contact_details.getLocation());
//			gap.setText(contact_details.getGapTime());

			rowView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(context)
							.setTitle(contact_details.getName())
							.setMessage(contact_details.getLocation() + "\n GAP: " + contact_details.getGapTime()+"\n Round Type: "+ contact_details.getRoundType() )
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// continue with delete
								}
							})

							.show();
				}
			});
		}

		return rowView;

	};



}

