package com.spbn.womenprotection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class HomeActivity extends Fragment implements View.OnClickListener {
    View v;
    ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home, container, false);
        init();
        initClickListeners();
        return v;

    }

    private void init() {
        imageView1 = (ImageView) v.findViewById(R.id.emergency);
        imageView2 = (ImageView) v.findViewById(R.id.contact);
        imageView3 = (ImageView) v.findViewById(R.id.message);
        imageView4 = (ImageView) v.findViewById(R.id.sharelocation);
        imageView5 = (ImageView) v.findViewById(R.id.helpline);
        imageView6 = (ImageView) v.findViewById(R.id.quit);
    }

    private void initClickListeners() {
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        imageView3.setOnClickListener(this);
        imageView4.setOnClickListener(this);
        imageView5.setOnClickListener(this);
        imageView6.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.emergency:
                fragmentTransaction.replace(R.id.content_frame, new EmergencyActivity());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.contact:
                fragmentTransaction.replace(R.id.content_frame, new ContactActivity());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.message:
                fragmentTransaction.replace(R.id.content_frame, new MessageActivity());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.sharelocation:
                fragmentTransaction.replace(R.id.content_frame, new ShareLocationActivity());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.helpline:
                fragmentTransaction.replace(R.id.content_frame, new HelpLineActivity());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.quit:
                if (getActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure want to exit")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (getActivity() != null)
                                        getActivity().finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;
        }
    }


}


