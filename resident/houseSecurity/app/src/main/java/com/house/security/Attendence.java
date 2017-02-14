package com.house.security;

public class Attendence {

    //private variables
    int _id;
    String _qr_name;
    String _qr_time;

    String _qr_full_time;
	String _qr_gap_time;
    String _qr_date;
    String _qr_location;

    String _qr_ssid_list;
    String _qr_round;
    String _qr_username;

	String _qr_roundType;

    // Empty constructor
    public Attendence(){

    }
    // constructor
    public Attendence(int id, String name, String time, String date, String location, String full_time, String timegap, String qr_ssid_list, 
    				String qr_round, String qr_username, String qr_roundtype){
        this._id = id;
        this._qr_name = name;
        this._qr_time = time;
        this._qr_date = date;
        this._qr_location = location;

		this._qr_full_time = full_time; 
		this._qr_gap_time = timegap;		
		this._qr_ssid_list = qr_ssid_list;
		this._qr_round = qr_round;

		this._qr_roundType = qr_roundtype;
		this._qr_username= qr_username;		
    }

    // constructor
    public Attendence(String name, String time, String date, String location, String full_time, String timegap, String qr_ssid_list, 
    					String qr_round, String qr_username, String qr_roundtype){

		this._qr_name = name;
		this._qr_time = time;
		this._qr_date = date;
		this._qr_location = location;
		this._qr_full_time = full_time; 
		this._qr_gap_time = timegap;
		this._qr_ssid_list = qr_ssid_list;
		this._qr_round = qr_round;

		this._qr_roundType = qr_roundtype;
		this._qr_username= qr_username;				
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public String getName(){
        return this._qr_name;
    }

    // setting name
    public void setName(String name){
        this._qr_name = name;
    }

    // getting location
    public String getLocation(){
        return this._qr_location;
    }

    // setting location
    public void setLocation(String qr_location){
        this._qr_location = qr_location;
    }

    // getting date
    public String getDate(){
        return this._qr_date;
    }

    // setting date
    public void setDate(String qr_date){
        this._qr_date = qr_date;
    }

	// getting time
	public String getTime(){
		return this._qr_time;
	}

	// setting date
	public void setTime(String qr_time){
		this._qr_time = qr_time;
	}

	// getting full_time
	public String getFullTime(){
		return this._qr_full_time;
	}

	// setting full_date
	public void setFullTime(String qr_time){
		this._qr_full_time = qr_time;
	}

	// getting gap_time
	public String getGapTime(){
		return this._qr_gap_time;
	}

	// setting gap_date
	public void setGapTime(String qr_time){
		this._qr_gap_time = qr_time;
	}

	// getting ssid list
	public String getSSID(){
		return this._qr_ssid_list;
	}

	// setting SSID list
	public void setSSID(String qr_ssid_list){
		this._qr_ssid_list = qr_ssid_list;
	}

	// set round index
	public void setRound(String round){
		this._qr_round = round;
	}
	// get round index
	public String getRound(){
		return this._qr_round;
	}

	// set user name
	public void setUsername(String username){
		this._qr_username = username;
	}
	// get round index
	public String getUsername(){
		return this._qr_username;
	}

	// set round type
	public void setRoundType(String roundType){
		this._qr_roundType = roundType;
	}
	// get Round type
	public String getRoundType(){
		return this._qr_roundType;
	}


}
