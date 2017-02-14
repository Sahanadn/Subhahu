package com.house.security;


import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EventListAdapter extends ArrayAdapter<item> {

	private final Activity context;

	List<item> mItems;

	public EventListAdapter(Activity context, List<item> objects, String date) {
		super(context, R.layout.eventlist, objects);
	
		this.context=context;
		mItems = objects;
	}

	


	public int getCount() {
		return mItems.size();
	}


	public View getView(int position,View view,ViewGroup parent) {

		LayoutInflater inflater=context.getLayoutInflater();
		View rowView=inflater.inflate(R.layout.eventlist, null,true);

		Log.d("QR_SCAN","getView "+position +" contact "+"list size "+mItems.size());

		if(position < mItems.size())
		{

			item item_details = mItems.get(position);

			Log.d("QR_SCAN","getView "+position +" contact "+"list size "+mItems.size()+" "+item_details.getName() 
				+" "+item_details.getDesc());

			TextView locationTextView = (TextView) rowView.findViewById(R.id.locationTextView);
			TextView eventsView = (TextView) rowView.findViewById(R.id.eventDetails);


			locationTextView.setText(item_details.getName());
			eventsView.setText(item_details.getDesc());
		}

		return rowView;

	};
}

