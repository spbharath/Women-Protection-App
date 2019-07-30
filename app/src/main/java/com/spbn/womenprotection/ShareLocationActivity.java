package com.spbn.womenprotection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ShareLocationActivity extends Fragment implements View.OnClickListener {
    private static final int REQUEST_READ_PHONE_STATE = 3;
    private DatabaseReference userPhoneRef;
    private String reference;
    ArrayList<String> number = new ArrayList<>();
    String[] number_array = new String[number.size()];


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.share_location, container, false);
        ImageView curr_loc = (ImageView) v.findViewById(R.id.current_location);
        ImageView home_loc = (ImageView) v.findViewById(R.id.home_location);
        curr_loc.setOnClickListener(this);
        home_loc.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        }

        getContactDetails();
        return v;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.current_location:
                createEmergencyDialog();
                break;

            case R.id.home_location:
                createSafeDialog();
                break;
            default:
                Toast.makeText(getActivity(), "Invalid Selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void createSafeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("I'm Safe")
                .setMessage("Say your contacts that you are SAFE?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(true)
                .show();
    }


    private void createEmergencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Share Location")
                .setMessage("Allow your contacts to track your current location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(true)
                .show();
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

    private void startTrackerService() {
        getActivity().startService(new Intent(getContext(), TrackingService.class));

        Toast.makeText(getContext(), "GPS tracking enabled", Toast.LENGTH_SHORT).show();

    }
}