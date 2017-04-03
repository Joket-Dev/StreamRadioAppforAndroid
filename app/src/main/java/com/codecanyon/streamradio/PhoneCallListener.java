package com.codecanyon.streamradio;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by User on 2014.07.11..
 */
public class PhoneCallListener extends PhoneStateListener {

    private boolean notRunWhenStart = true;
    public static boolean message = false;
    public static int state;
    private int prev_state;

    public void onCallStateChanged(int state, String incomingNumber) {
        try {
            if (notRunWhenStart)
                notRunWhenStart = false;
            else {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:

                        if (MusicPlayer.isStarted()) {
                            message = true;

                        }
                        if((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)){
                            MainActivity.phoneState = 100;
                            prev_state=state;

                        }
                        if((prev_state == TelephonyManager.CALL_STATE_RINGING)){
                            MainActivity.phoneState = 101;
                            prev_state=state;
                            //Rejected or Missed call
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (MusicPlayer.isStarted()) {
                            message = true;
                            MusicPlayer.pauseMediaPlayer ();
                        }
                        MainActivity.phoneState = TelephonyManager.CALL_STATE_OFFHOOK;
                        prev_state=state;
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (MusicPlayer.isStarted()) {
                            message = true;
                            MusicPlayer.pauseMediaPlayer ();
                        }
                        MainActivity.phoneState = TelephonyManager.CALL_STATE_RINGING;
                        prev_state=state;
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}