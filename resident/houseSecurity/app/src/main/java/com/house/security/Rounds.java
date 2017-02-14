package com.house.security;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Rounds {

    //private variables
    int _id;
    List <String> _qr_table_id;
    List <String> _qr_name;
    List <String> _qr_gap_name;
    List <String> _qr_scan_time;	
    String _date;
	String _roundType;

    // Empty constructor
    public Rounds(){
		_qr_table_id = new ArrayList <String>();
		_qr_name = new ArrayList <String>();
		_qr_gap_name = new ArrayList <String>();
		_qr_scan_time = new ArrayList <String>();
		_roundType = null;
    }
 
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }
    public void setDate (String date)
    {
    	this._date = date;
    }
    public String getDate ()
    {
    	return this._date;
    }
    
    // getting name
    public boolean setIndex(int index){
    	if(_qr_table_id.contains(""+index))
    		return false;
    	
    	_qr_table_id.add(""+index);
        return true;//this._qr_name[0];
    }

    public int getIndex(int index){

		if (0 != index)
			return 0;
    	return  Integer.parseInt(_qr_table_id.get(--index));
    }

    public void setRoundType (String roundType)
    {
    	if (this._roundType == null)
    		this._roundType = roundType;		
    }
    public String getRoundType ()
    {
    	return this._roundType;
    }

    public List<String> getIndexAll()
    {
		List<String> index_list = new ArrayList <String>();
		int index = 0;

		while (index++ < _qr_table_id.size())
		{
			index_list.add(""+index);
		}
		
    	return index_list;
    }

	public List <String> getRealIndex()
	{
		return _qr_table_id;
	}
    public int getIndexSize()
    {
    	return _qr_table_id.size();
    }
    
    public List<String> getAllNames()
    {
    	return _qr_name;
    }
    // setting name
    public boolean setName(String name, String gap_time, String scan_time){
    	if (_qr_name.contains(name))
    	{
			Log.d("QR_SCAN_ROUNDS","scan location "+name +" present ");	
    		return false;
    	}


		// if time difference 180 min between last scan and the current scan, consider the new round
		// making this false as dynamic patrolling make sures duplicate doesnt occur, other wise make this uncomment

		while (true)
		{
			//access last scan time from scan time list
			if(_qr_scan_time.size() == 0)
				break;
			
			String scan_time_last = _qr_scan_time.get(_qr_scan_time.size() - 1);
			if(scan_time_last == null)
				break;
			
			String[] last_am_pm = scan_time_last.split(" ");
			String[] last_hr = last_am_pm[0].split(":");

			int last_hour = Integer.parseInt(last_hr[0]);
			int last_min = Integer.parseInt(last_hr[1]);

			if(last_am_pm[1] != null && last_am_pm[1].equals("am") && (last_hour <= 11 && last_hour >= 0))
				last_hour += 12;

			last_min += last_hour * 60;
			
			Log.d("QR_SCAN_ROUNDS","scan location "+name +" last scan time "+scan_time_last+" current scan time "+scan_time);

			String[] am_pm = scan_time.split(" ");
			String[] hr = am_pm[0].split(":");

			int hour = Integer.parseInt(hr[0]);
			int current_min = Integer.parseInt(hr[1]);

			if(am_pm[1] != null && am_pm[1].equals("am") && (hour <= 11 && hour >= 0))
				hour += 12;

			current_min += hour * 60;			

			boolean result = false;
			if( last_min > current_min)
			{
				result = (last_min - current_min) >= 180;	
				Log.d("QR_SCAN_ROUNDS"," scan time scenario return "+result +" difference min "+(current_min - last_min));
			}
			else
			{
				result = (current_min - last_min) >= 180;	
				Log.d("QR_SCAN_ROUNDS"," scan time scenario return "+result +" difference min "+(current_min - last_min));
			}

			
			if (result == true)
			{
				Log.d("QR_SCAN_ROUNDS"," hour "+hour +" last_hour "+last_hour); 			
				Log.d("QR_SCAN_ROUNDS"," last_min "+last_min +" current_min "+current_min);
				Log.d("QR_SCAN_ROUNDS"," last_am_pm "+last_am_pm[1] +" am_pm "+am_pm[1]);				

				return false;
			}
			
			break;	
		}
		
    	_qr_name.add(name);
		_qr_gap_name.add(gap_time);
		_qr_scan_time.add(scan_time);
		
    	return true;
        //this._qr_name = name;
    }
  
}
