package com.ustech.bloodhope.Receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ustech.bloodhope.Utils.Constants;

/**
 * Created by awais on 02/14/2018.
 */

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(Constants.TAG,"wifi sate changed");
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Log.d(Constants.TAG, "Internet YAY");
                //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Log.d(Constants.TAG, "No internet :(");
                Toast.makeText(context, "no network found", Toast.LENGTH_SHORT).show();
                new MaterialDialog.Builder(context)
                        .title("No network found")
                        .content("can't reach to the hope drop network please check you connectivity")
                        .positiveText("WiFi").onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        })
                        .negativeText("cancel").cancelable(false)
                        .show();
            }
        }
    }
}