package com.spbn.womenprotection;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class GuardActivity extends Fragment implements OnMapReadyCallback {

    private static final int REQUEST_PHONE_CALL = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private double latitude, longitude;
    private DatabaseReference reference;
    private static final int REQUEST_LOCATION_CODE = 1;
    public ListView police_station;
    public ArrayList<String> police_station_list = new ArrayList<>();
    public ArrayAdapter<String> police_adapter;
    public String url;
    private static final int PROXIMITY_RADIUS = 10000;
    private BroadcastReceiver broadcastReceiverSms;
    private String change_sms = "";
    private SharedPreferences sharedPreferences;
    private String sms_data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Mapbox.getInstance(getActivity(), getString(R.string.access_token));

        View v = inflater.inflate(R.layout.activity_gaurd, container, false);
        int Permission_All = 1;
        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE};
        if (!hasPermissions(getContext(),Permissions)){
            ActivityCompat.requestPermissions(getActivity(),Permissions,Permission_All);
        }
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        police_station = (ListView) v.findViewById(R.id.policeStation);
        police_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, police_station_list);
        police_station.setAdapter(police_adapter);
        police_station.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Places.initialize(getApplicationContext(), "AIzaSyAGT78EGeKYmntvpzRWKkyE9HIcqEgYJdY");
                PlacesClient placesClient = Places.createClient(getApplicationContext());
                String gettingId = police_station.getItemAtPosition(position).toString();
                int index = gettingId.lastIndexOf(':') + 2;
                int length = gettingId.length();
                String sub = gettingId.substring(index, length).trim();

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER);
                FetchPlaceRequest request = FetchPlaceRequest.builder(sub, placeFields)
                        .build();
                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        createDialog(position, place);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    int statusCode = apiException.getStatusCode();
                                    // Handle error with given status code.
                                    Log.d("Place", "Place not found: " + e.getMessage());
                                }
                            }
                        });


                return false;
            }
        });
        return v;

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sms_data = sharedPreferences.getString("sms_name", "");
        Toast.makeText(getContext(), sms_data, Toast.LENGTH_SHORT).show();
        reference = FirebaseDatabase.getInstance().getReference("user_location").child(sms_data).child("location");
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            int i = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String lat = dataSnapshot.child("latitude").getValue().toString();
                String lng = dataSnapshot.child("longitude").getValue().toString();
                String locality = null;
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lng);
                if (i == 0) {
                    searchPoliceStationNear();
                }
                LatLng latLng = new LatLng(latitude, longitude);
                Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        locality = addresses.get(0).getLocality();
                        mapboxMap.clear();
                        mapboxMap.addMarker(new MarkerOptions().position(latLng).title("I'm Here in " + locality));
                    } else {
                        locality = "Unable to find locality";
                        mapboxMap.clear();
                        mapboxMap.addMarker(new MarkerOptions().position(latLng).title("I'm Here"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                i++;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void createDialog(int position, Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialog = getLayoutInflater().inflate(R.layout.call_layout_phone, null);
        final TextView station_name = dialog.findViewById(R.id.display_station_contact);
        final TextView station_phone = dialog.findViewById(R.id.display_station_phno);
        final ImageView station_call = dialog.findViewById(R.id.station_call);

        if (place.getPhoneNumber() != null) {
            station_name.setText(place.getName());
            station_phone.setText(place.getPhoneNumber());


            station_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    } else {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + place.getPhoneNumber().toString()));
                        startActivity(callIntent);
                    }
                }
            });

            builder.setView(dialog);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(getContext(), "No Phone Number Available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        final IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        broadcastReceiverSms = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pdusBundle = intent.getExtras();
                Object[] pdus = (Object[]) pdusBundle.get("pdus");
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
                String msg = message.getMessageBody();
                if (msg.contains("Eagle Eye Safe !") && msg.contains("safe place") && msg.contains(sms_data)) {
                    getActivity().finish();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiverSms, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        getActivity().unregisterReceiver(broadcastReceiverSms);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void searchPoliceStationNear() {

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        String hospital = "police";
        url = getUrl(latitude, longitude, hospital);
        getNearbyPlacesData.execute();
    }


    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location" + "=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyB5uObhgaVeg4xvrjENRz5bH3yh9kJzTgA");
        Log.d("URL", googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }




    private class GetNearbyPlacesData extends AsyncTask<Void, Void, String> {

        String googlePlacesData;

        @Override
        protected String doInBackground(Void... voids) {
            DownloadURL downloadURL = new DownloadURL();
            try {
                googlePlacesData = downloadURL.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String s) {

            List<HashMap<String, String>> nearbyPlaceList = null;

            DataParser parser = new DataParser();
            nearbyPlaceList = parser.parse(s);

            showNearbyPlaces(nearbyPlaceList);

        }


        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            police_station_list.clear();
            for (int i = 0; i < nearbyPlaceList.size(); i++) {
                //show all the places in the list
                //we are going to create marker options


                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                String place_id = googlePlace.get("place_id");

                police_station_list.add("Place Name : " + placeName + ",\nVicinity : " + vicinity + ",\nPlace ID : " + place_id);

            }
            police_adapter.notifyDataSetChanged();

        }


    }

    public static boolean hasPermissions(Context context,String... permissions){
        for (String permission : permissions){
            if (ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

}