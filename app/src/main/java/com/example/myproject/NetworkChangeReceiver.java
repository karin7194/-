package com.example.myproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    AlertDialog.Builder adb;
    AlertDialog ad;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!isConnectedToWifi(context)) {
                // If not connected to Wi-Fi, show dialog and exit the app
                showDialogAndExit(context);
            }
        }
    }

    private boolean isConnectedToWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }
        return false;
    }

    private void showDialogAndExit(Context context) {
        adb = new AlertDialog.Builder(context);
        adb.setTitle("No Wi-Fi Connection");
        adb.setMessage("The app requires Wi-Fi connection to function. Please connect to Wi-Fi and try again.");
        adb.setCancelable(false);
        adb.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                System.exit(0);
            }
        });
        ad = adb.create();
        ad.show();
    }
}

