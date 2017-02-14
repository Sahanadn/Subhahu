package com.house.security;

public class Login {

    //private variables
    int _id;
    String _phone_number;
    String _pass_index;

    String _reg_date;
	String _reg_status;
    String _owner_name;
        
    // Empty constructor
    public Login(){

    }
    // constructor
    public Login(int id, String phone_number, String pass_index, String reg_date, String reg_status, String owner_name){
        this._id = id;
        this._phone_number = phone_number;
        this._pass_index = pass_index;
        this._reg_date = reg_date;
        this._reg_status = reg_status;
		this._owner_name = owner_name; 		
    }

    // constructor
    public Login(String phone_number, String pass_index, String reg_date, String reg_status, String owner_name){

    	this._phone_number = phone_number;
        this._pass_index = pass_index;
        this._reg_date = reg_date;
        this._reg_status = reg_status;
		this._owner_name = owner_name; 
		
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
    public String getPhoneNumer(){
        return this._phone_number;
    }

    // setting name
    public void setPhoneNumber(String name){
        this._phone_number = name;
    }

    // getting location
    public String getStatus(){
        return this._reg_status;
    }

    // setting location
    public void setStatus(String qr_location){
        this._reg_status = qr_location;
    }

    // getting date
    public String getRegDate(){
        return this._reg_date;
    }

    // setting date
    public void setRegDate(String qr_date){
        this._reg_date = qr_date;
    }

	// getting time
	public String getPassIndex(){
		return this._pass_index;
	}

	public void setPassIndex(String qr_time){
		this._pass_index = qr_time;
	}

	public String getOwnerName(){
		return this._owner_name;
	}

	
	public void setOwnerName(String qr_time){
		this._owner_name = qr_time;
	}

}
