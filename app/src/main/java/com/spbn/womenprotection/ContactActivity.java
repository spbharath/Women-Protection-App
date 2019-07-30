package com.spbn.womenprotection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ContactActivity extends Fragment {
    private DatabaseReference userPhoneRef;
    private EditText user, phone;
    private Button submit;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    ListView phonedata;
    ArrayList<String> number = new ArrayList<>();
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    ArrayAdapter<String> arrayAdapter;
    String reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact, container, false);
        user = (EditText) v.findViewById(R.id.user);
        phone = (EditText) v.findViewById(R.id.phone_number);
        submit = (Button) v.findViewById(R.id.submit);
        phonedata = (ListView) v.findViewById(R.id.phone_list);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, number);
        phonedata.setAdapter(arrayAdapter);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
                        number.add(name + " : " + phone_number);
                    }
                } else {
                    number.add("No Contacts to Display");

                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getText().toString().trim().equals("") || phone.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Must not be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (phone.getText().toString().length() < 10) {
                        Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                    } else {
                        saveData(Double.parseDouble(phone.getText().toString()));
                    }
                }
            }
        });

        phonedata.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (phonedata.getItemAtPosition(position).equals("No Contacts to Display")) {
                    Toast.makeText(getContext(), "No Contacts to Display", Toast.LENGTH_SHORT).show();
                } else {
                    createDialog(position);
                }

                return false;
            }
        });
        return v;
    }

    private void createDialogDatabase() {
        builder = new AlertDialog.Builder(getContext());
        final View dialog = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        final EditText edit_name = (EditText) dialog.findViewById(R.id.name_edit);
        final EditText edit_ph_no = (EditText) dialog.findViewById(R.id.ph_no_edit);
        Button edit_remove = (Button) dialog.findViewById(R.id.edit_phone_remove);
        Button edit_modify = (Button) dialog.findViewById(R.id.edit_phone_modify);
        edit_remove.setVisibility(View.GONE);
        edit_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_ph_no.getText().toString().length() == 10) {
                    double ph_no = Double.parseDouble(edit_ph_no.getText().toString().trim());
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("user_phone").child(reference);
                    String id = edit_name.getText().toString().trim();
                    databaseReference.child(id).setValue(ph_no);
                } else {
                    Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
            }
        });
        builder.setView(dialog);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void createDialog(int position) {
        builder = new AlertDialog.Builder(getContext());
        final View dialog = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        final EditText edit_name = (EditText) dialog.findViewById(R.id.name_edit);
        final EditText edit_ph_no = (EditText) dialog.findViewById(R.id.ph_no_edit);
        Button edit_remove = (Button) dialog.findViewById(R.id.edit_phone_remove);
        Button edit_modify = (Button) dialog.findViewById(R.id.edit_phone_modify);
        final String user_name = phonedata.getItemAtPosition(position).toString().trim();
        edit_name.setText(user_name.substring(0, user_name.length() - 13));
        edit_ph_no.setText(user_name.substring(user_name.length() - 10, user_name.length()));


        edit_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String child = user_name.substring(0, user_name.length() - 13);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("user_phone").child(reference).child(child);
                databaseReference.removeValue();
                alertDialog.cancel();
            }
        });

        edit_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String child = user_name.substring(0, user_name.length() - 13);
                if (edit_ph_no.getText().toString().length() == 10) {
                double ph_no = Double.parseDouble(edit_ph_no.getText().toString().trim());
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("user_phone").child(reference);
                databaseReference = databaseReference.child(child);
                databaseReference.removeValue();
                databaseReference = firebaseDatabase.getReference("user_phone").child(reference);
                String id = edit_name.getText().toString().trim();
                databaseReference.child(id).setValue(ph_no);}
                else {
                    Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
            }
        });
        builder.setView(dialog);
        alertDialog = builder.create();
        alertDialog.show();
    }


    private void saveData(double phoneNumber) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user_phone").child(reference.trim());
        String id = user.getText().toString().trim();
        databaseReference.child(user.getText().toString()).setValue(phoneNumber);
        user.setText("");
        phone.setText("");


    }
}
