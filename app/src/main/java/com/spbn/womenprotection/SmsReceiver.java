package com.spbn.womenprotection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;


public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECIEVED = "android.provider.Telephony.SMS_RECEIVED";
            String msg,phoneno;

    @Override
    public void onReceive(Context context, Intent intent) {
       Bundle pdusBundle = intent.getExtras();
        Object[] pdus = (Object[]) pdusBundle.get("pdus");
        String format = pdusBundle.getString("format");
        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0],format);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Intent smsIntent = new Intent(context, MainActivity.class);
        String msg = message.getMessageBody();
        if (msg.contains("Eagle Eye Alert !") && msg.contains("trouble") && msg.contains("help")) {
            String name = msg.substring(31, msg.length() - 35);
            smsIntent.putExtra("help_name", name.trim());
            sharedPreferences.edit().putString("sms_name",name.trim()).commit();

            context.startActivity(smsIntent);
        } else if (msg.contains("Eagle Eye Safe !") && msg.contains("safe place")) {

            sharedPreferences.edit().putString("sms_name","unknown").commit();
        } else {
            smsIntent.putExtra("help_name", "");

        }



    }

}