package com.house.security;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
 
public class RingAlarm extends Activity {
         
       MediaPlayer mp=null ;
       
        @Override
        protected void onCreate(Bundle savedInstanceState) {
             
                    super.onCreate(savedInstanceState);
                    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    
                    Log.d("QR_SCAN_ALARM","Ring Alarm");
                    
                    //setContentView(R.layout.alarm);
                    //Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
                     
                    //mp = MediaPlayer.create(getBaseContext(),R.raw.audio);
                     
               /*      
                    stopAlarm.setOnTouchListener(new OnTouchListener() {
                         
                        @Override
                        public boolean onTouch(View arg0, MotionEvent arg1) {
                            // TODO Auto-generated method stub
                            mp.stop();
                            finish();
                            return false;
                        }
                    });
              */
                    playSound(this, getAlarmUri());
                }
              
                private void playSound(final Context context, Uri alert) {
                      
                     
                    Thread background = new Thread(new Runnable() {
                        public void run() {
                            try {
                                 
                               mp.start();
                                
                            } catch (Throwable t) {
                                Log.i("Animation", "Thread  exception "+t);
                            }   
                        }
                 });
                 background.start();
               }
              
                @Override
                protected void onDestroy() {
                    super.onDestroy();
                    mp.stop();
                }               //Get an alarm sound. Try for an alarm. If none set, try notification,
                    //Otherwise, ringtone.
                private Uri getAlarmUri() {
                     
                    Uri alert = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_ALARM);
                    if (alert == null) {
                        alert = RingtoneManager
                                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (alert == null) {
                            alert = RingtoneManager
                                    .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                        }
                    }
                    return alert;
                }
             
}
