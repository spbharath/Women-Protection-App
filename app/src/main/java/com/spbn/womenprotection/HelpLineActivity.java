package com.spbn.womenprotection;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class HelpLineActivity extends Fragment {
    private ImageView listen_b;
    private String s;
    private final static int SPEECH_RECOGNITION_CODE = 1;
    private static final int REQUEST_READ_PHONE_STATE = 3;
    private DatabaseReference userPhoneRef;
    private String reference;
    ArrayList<String> number = new ArrayList<>();
    String[] number_array = new String[number.size()];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getContactDetails();
        speech();
        View v = inflater.inflate(R.layout.helpline, container, false);


        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, 3);

        }
        listen_b = (ImageView) v.findViewById(R.id.b_listen);

        listen_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech();

            }
        });
        return v;
    }

    private void speech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Cry For Help!!");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Sorry!Some error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    processResult(result.get(0));
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processResult(String s) {
        s = s.toLowerCase();
        if (((s.contains("help") || s.contains("danger") || s.contains("emergency")) && (s.contains("me") || s.contains("in"))) && ((s.contains("i'm") || s.contains("i am")) && s.contains("safe"))) {
            Toast.makeText(getContext(), "Invalid code", Toast.LENGTH_SHORT).show();
        } else if (((s.contains("help") || s.contains("danger") || s.contains("emergency")) && (s.contains("me") || s.contains("in")) || s.contains("save"))) {
            sendEmergencyInfo();
            Toast.makeText(getContext(), "Emergency", Toast.LENGTH_SHORT).show();
        } else if ((s.contains("i'm") || s.contains("i am")) && s.contains("safe")) {
            sendSafeInfo();
            Toast.makeText(getContext(), "Safe", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid code", Toast.LENGTH_SHORT).show();
        }
    }

    private void getContactDetails() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String email = sharedPreferences.getString("email", "");
        reference = email.substring(0, email.length() - 10);
        userPhoneRef = FirebaseDatabase.getInstance().getReference("user_phone").child(reference);

        userPhoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                number.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String phone_number = ds.getValue().toString();
                        number.add(phone_number);

                    }
                } else {
                    number.add("No Contacts to Display");

                }
                number_array = number.toArray(number_array);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendEmergencyInfo() {
        String sms_content = "Eagle Eye Alert ! : Your Friend " + reference + " is in trouble and needs your help.";
        if (!number_array.toString().contains("No Contacts to Display")) {
            //track location
            startTrackerService();
            for (String phone : number_array) {

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, sms_content, null, null);
                Toast.makeText(getContext(), "Sending Message to " + phone, Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), "Message Sent to " + phone, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getContext(), "You will be RESCUED by your friends!! Don't worry..!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "No Contacts to Send Location. Add Contact to start tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSafeInfo() {
        String sms_content = "Eagle Eye Safe ! : Your Friend " + reference + " is in safe place.";
        if (ContextCompat
                .checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (!number_array.toString().contains("No Contacts to Display")) {
                int i = 0;
                getActivity().stopService(new Intent(getContext(), TrackingService.class));
                for (String phone : number_array) {
                    if (i <= 4) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone, null, sms_content, null, null);
                        Toast.makeText(getContext(), "Sending Message to " + phone, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getContext(), "Message Sent to " + phone, Toast.LENGTH_SHORT).show();
                    } else {
                        break;
                    }

                    i++;
                }
            } else {
                Toast.makeText(getContext(), "No Contacts to Send Initiation. Add Contact to start Emergency Services", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                    .READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void startTrackerService() {
        getActivity().startService(new Intent(getContext(), TrackingService.class));

        Toast.makeText(getContext(), "GPS tracking enabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                }
                break;
        }
    }
}
