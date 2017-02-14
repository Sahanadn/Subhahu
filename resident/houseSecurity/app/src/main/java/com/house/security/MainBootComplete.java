package com.house.security;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class MainBootComplete extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("QR_SCAN_BROADCAST","onReceive BroadcastReceiver "+intent);
		
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("QR_SCAN_BROADCAST", "onReceive BootCompleted");
			Intent serviceIntent = new Intent(context, MainService.class);
			serviceIntent.setAction("com.sec.location.start");
			serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(serviceIntent);
			}
		
		if (intent.getAction().equalsIgnoreCase(android.content.Context.ALARM_SERVICE) || 
				intent.getAction().equals("com.sec.loin.done")	) {
			Log.d("QR_SCAN_BROADCAST", "onReceive Alarm Service");

			Intent broadcastIntent = new Intent(context, MainService.class);
			broadcastIntent.setAction("com.sec.alarm.received");
			context.startService(broadcastIntent);
        
		}		
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		{
			// Get the data (SMS data) bound to intent
	        Bundle bundle = intent.getExtras();
	        
	        final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	        String action = intent.getAction();

	        if(action.equals(ACTION_SMS_RECEIVED)){

	            String address = null, str = "";
	            int contactId = -1;

	            SmsMessage[] msgs = getMessagesFromIntent(intent);
	            if (msgs != null) {
	                for (int i = 0; i < msgs.length; i++) {
	                    address = msgs[i].getOriginatingAddress();
	                    //contactId = ContactsUtils.getContactId(mContext, address, "address");
	                    str += msgs[i].getMessageBody().toString();
	                    str += "\n";
	                }
	            }   

	            showNotification(address, str);
	           

	            // ---send a broadcast intent to update the SMS received in the
	            // activity---
	            Intent broadcastIntent = new Intent(context, MainService.class);
	            broadcastIntent.setAction("com.sec.sms.received");
	            broadcastIntent.putExtra("address", address);
	            broadcastIntent.putExtra("sms", str);
	            context.startService(broadcastIntent);
	        }
		}
	
		if (intent.getAction().equals("com.sec.net.create.auth"))
		{
			if (util.isNetworkOnline(context)) {
				Log.d("QR_SCAN_BROADCAST","onReceive connectivity sucess");
				Intent broadcastIntent = new Intent(context, MainService.class);
		        broadcastIntent.setAction("com.sec.net.create.auth");
		        context.startService(broadcastIntent);	
			}
			else
			{
				util.savePreference(context,"pending_auth",true);
			}

		}

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()) ||
			intent.getAction().equals("com.sec.data.db.added") ||
			intent.getAction().equals("com.sec.net.reupload") ||
			intent.getAction().equals("com.sec.net.syncForDate")) 
		{
			Log.d("QR_SCAN_BROADCAST","onReceive action "+intent.getAction());
			if (util.isNetworkOnline(context)) {
				Log.d("QR_SCAN_BROADCAST","onReceive connectivity sucess");
				Intent broadcastIntent = new Intent(context, MainService.class);
		        broadcastIntent.setAction("com.sec.net.connected");
		        context.startService(broadcastIntent);	
			}
		}

		if (intent.getAction().equals("com.sec.vismgmt.post"))
		{
			if (util.isNetworkOnline(context)) {
				Log.d("QR_SCAN_BROADCAST", "onReceive visitor management post");
				Intent broadcastIntent = new Intent(context, MainService.class);
				broadcastIntent.setAction("com.sec.net.vismngmt.post");
				context.startService(broadcastIntent);
			}
			else
			{
				util.savePreference(context, "pending_auth", true);
			}

		}

		if (intent.getAction().equals("com.sec.vismgmt.get"))
		{
			if (util.isNetworkOnline(context)) {
				Log.d("QR_SCAN_BROADCAST", "onReceive visitor management post");
				Intent broadcastIntent = new Intent(context, MainService.class);
				broadcastIntent.setAction("com.sec.net.vismngmt.get");
				context.startService(broadcastIntent);
			}
			else
			{
				util.savePreference(context, "pending_auth", true);
			}

		}

		if (intent.getAction().equals("com.sec.vismgmt.edit"))
		{
			if (util.isNetworkOnline(context)) {
				Log.d("QR_SCAN_BROADCAST", "onReceive visitor management edit");
				Intent broadcastIntent = new Intent(context, MainService.class);
				broadcastIntent.putExtra("id",intent.getIntExtra("id",0));
				broadcastIntent.setAction("com.sec.net.vismngmt.edit");
				context.startService(broadcastIntent);
			}
			else
			{
				util.savePreference(context, "pending_auth", true);
			}

		}


	}


	protected void showNotification(String address, String message) {
        //Display notification...
		Log.d("QR_SCAN_BROADCAST","onReceive SMS Received "+address +" "+message);
    }
	@SuppressWarnings("deprecation")
	public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
	/*
	ServiceConnection mConnection = new ServiceConnection;
	private class ServiceConnection () {

		  public void onServiceDisconnected(ComponentName name) {
		   Toast.makeText(Client.this, "Service is disconnected", 1000).show();
		   mBounded = false;
		   mServer = null;
		  }

		  public void onServiceConnected(ComponentName name, IBinder service) {
		   Toast.makeText(Client.this, "Service is connected", 1000).show();
		   mBounded = true;
		   LocalBinder mLocalBinder = (LocalBinder)service;
		   mServer = mLocalBinder.getServerInstance();
		  }
		 };
*/
}
