package com.spbn.womenprotection;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MessageActivity extends Fragment {
    private static final int REQUEST_PHONE_CALL = 1;
    private static final int REQUEST_SEND_SMS = 2;
    private static final int REQUEST_READ_PHONE_STATE = 3;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> phonearrayAdapter;
    private ArrayAdapter<String> smsarrayAdapter;
    private String reference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ListView phonedata, smsdata;
    private ArrayList<String> number = new ArrayList<>();
    private ArrayList<String> sms = new ArrayList<>();
    private String call_number;
    private String sms_name;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.message, container, false);
        phonedata = (ListView) v.findViewById(R.id.phone_list_message);
        phonearrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, number);
        phonedata.setAdapter(phonearrayAdapter);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String email = sharedPreferences.getString("email", "");
        reference = email.substring(0, email.length() - 10);
        smsdata = v.findViewById(R.id.sms_data);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 5);

        }
        setPhoneNumberList();
        setSentSmsList();

        phonedata.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (phonedata.getItemAtPosition(position).equals("No Contacts to Display")) {
                    Toast.makeText(getContext(), "No Contacts to Display", Toast.LENGTH_SHORT).show();
                } else {
                    createDialog(position);
                }
                return true;
            }
        });

        smsdata.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (smsdata.getItemAtPosition(position).equals("No Messages to Display")) {
                    Toast.makeText(getContext(), "No Messages to Display", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder remove = new AlertDialog.Builder(getContext());
                    remove.setTitle("Remove Message")
                            .setMessage(smsdata.getItemAtPosition(position).toString())
                            .setCancelable(true)
                            .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StringTokenizer tokenizer = new StringTokenizer(smsdata.getItemAtPosition(position).toString(), ":");
                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference = firebaseDatabase.getReference("user_message").child(reference).child(tokenizer.nextToken().trim());
                                    databaseReference.removeValue();
                                    Toast.makeText(getContext(), "Message Removed", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                return true;
            }
        });


        return v;
    }

    private void setSentSmsList() {
        smsarrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, sms);
        smsdata.setAdapter(smsarrayAdapter);
        DatabaseReference userSmsRef = FirebaseDatabase.getInstance().getReference("user_message").child(reference);
        userSmsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sms.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String message = ds.getValue().toString();
                        sms.add(name + " : " + message);
                    }
                } else {
                    sms.add("No Messages to Display");

                }
                smsarrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPhoneNumberList() {
        phonearrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, number);
        phonedata.setAdapter(phonearrayAdapter);
        DatabaseReference userPhoneRef = FirebaseDatabase.getInstance().getReference("user_phone").child(reference);
        userPhoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                number.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String phone_number = ds.getValue().toString();
                        number.add(name + " : " + phone_number);
                    }
                } else {
                    number.add("No Contacts to Display");

                }
                phonearrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void createDialog(int position) {
        builder = new AlertDialog.Builder(getContext());
        final View dialog = getLayoutInflater().inflate(R.layout.message_layout, null);
        final TextView contact_name = dialog.findViewById(R.id.display_contact);
        final TextView contact_phone = dialog.findViewById(R.id.display_phno);
        final ImageView call_contact = dialog.findViewById(R.id.contact_call);
        final ImageView message_contact = dialog.findViewById(R.id.contact_message);
        final String user_name = phonedata.getItemAtPosition(position).toString().trim();

        contact_name.setText(user_name.substring(0, user_name.length() - 13));
        contact_phone.setText(user_name.substring(user_name.length() - 10, user_name.length()));
        call_number = contact_phone.getText().toString().trim();
        sms_name = contact_name.getText().toString().trim();

        call_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + call_number));
                    startActivity(callIntent);
                }
            }
        });


        message_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                } else {
                    createSmsDialog();
                }

            }
        });

        builder.setView(dialog);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + call_number));
                    startActivity(callIntent);
                } else {
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                }

            }
            break;
            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                }

            }
            break;

            case REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                }
                break;
        }
    }

    public void createSmsDialog() {
        AlertDialog dialog;
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(getContext());
        final View sms_dialog = getLayoutInflater().inflate(R.layout.send_message, null);
        final EditText content = sms_dialog.findViewById(R.id.message_content);
        final ImageView send = sms_dialog.findViewById(R.id.send_sms);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sms_content = content.getText().toString().trim();
                Toast.makeText(getContext(), sms_content, Toast.LENGTH_LONG).show();
                if (!sms_content.equals("")) {
                    try {
                        if (ContextCompat
                                .checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) !=
                                PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                                    .READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                        } else {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(call_number, null, sms_content, null, null);
                            Toast.makeText(getContext(), "Sending Message to " + call_number, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getContext(), "Message Sent to " + call_number, Toast.LENGTH_SHORT).show();
                            setSmsListToDb(sms_content);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    alertDialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Message must not be EMPTY ", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        });

        builder.setView(sms_dialog);
        alertDialog = builder.create();
        alertDialog.show();

    }

    private void setSmsListToDb(String sms_content) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user_message").child(reference);
        databaseReference.child(sms_name).setValue(sms_content);
    }


}
