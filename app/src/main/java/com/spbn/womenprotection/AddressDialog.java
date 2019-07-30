package com.spbn.womenprotection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class AddressDialog extends AppCompatDialogFragment {
    private EditText editText;
    private AddressDialogListener listener;
    private double latitude, longitude;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.address_dialog, null);

        builder.setView(view)
                .setTitle("Address :")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Process Canceled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editText = (EditText) view.findViewById(R.id.address_et);
                        String address = editText.getText().toString();

                        if (!address.equals("")) {
                            List<Address> addressList;

                            Geocoder geocoder = new Geocoder(getContext());
                            try {
                                addressList = geocoder.getFromLocationName(address, 5);
                                for (int i = 0; i < addressList.size(); i++) {
                                    Address myAddress = addressList.get(i);
                                    //  LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                                    //  Toast.makeText(getActivity(), "Latitude of " + address + " is " + myAddress.getLatitude() + ",", Toast.LENGTH_SHORT).show();
                                    // Toast.makeText(getActivity(), "Longitude of " + address + " is " + myAddress.getLongitude(), Toast.LENGTH_SHORT).show();
                                    latitude = myAddress.getLatitude();
                                    longitude = myAddress.getLongitude();
                                    Toast.makeText(getContext(), "Click On the Map To get route", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                            listener.applyTexts(address, latitude, longitude);
                        } else {
                            Toast.makeText(getActivity(), "Try Again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddressDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must Implement AddressDialogListener");
        }
    }


    public interface AddressDialogListener {
        void applyTexts(String address, double latitude, double longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
