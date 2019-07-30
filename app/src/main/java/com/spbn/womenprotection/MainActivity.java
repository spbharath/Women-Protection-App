package com.spbn.womenprotection;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;


public class MainActivity extends AppCompatActivity implements AddressDialog.AddressDialogListener {
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    SharedPreferences sharedPreferences;
    private MapboxMap mapboxMap;
    private BroadcastReceiver broadcastReceiverSms;
    private String sms_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        setUpToolbar();
        init();
        int Permission_All = 1;
        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE};
        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, Permission_All);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                switch (menuItem.getItemId()) {
                    case R.id.help_seeker:
                        Toast.makeText(MainActivity.this, "Clicked Help", Toast.LENGTH_SHORT).show();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new HomeActivity());
                        fragmentTransaction.commit();
                        break;
                    case R.id.guard:
                        Toast.makeText(MainActivity.this, "Clicked Guard", Toast.LENGTH_SHORT).show();
                        if (sharedPreferences.getString("sms_name", "").equals("unknown")) {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, new NoTracking());
                            fragmentTransaction.commit();
                        } else {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, new GuardActivity());
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                        break;

                    case R.id.logout:
                        sharedPreferences.edit().putString("email", null).commit();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

                return false;
            }
        });
        if (extras == null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_frame, new HomeActivity());
            fragmentTransaction.commit();

        }
        if (extras != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new GuardActivity());
            fragmentTransaction.commit();
        }
    }


    private void init() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeActivity()).commit();
        navigationView = (NavigationView) findViewById(R.id.nav_view);

    }

    private void setUpToolbar() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawlayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        actionBarDrawerToggle.syncState();
    }


    @Override
    public void applyTexts(String address, double latitude, double longitude) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        sms_data = sharedPreferences.getString("sms_name", "");
        final IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        broadcastReceiverSms = new

                BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle pdusBundle = intent.getExtras();
                        Object[] pdus = (Object[]) pdusBundle.get("pdus");
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
                        String msg = message.getMessageBody();
                        if (msg.contains("Eagle Eye Safe !") && msg.contains("safe place") && msg.contains(sms_data)) {
                            sharedPreferences.edit().putString("sms_name", "unknown").commit();
                        }
                    }
                };


        registerReceiver(broadcastReceiverSms, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiverSms);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
