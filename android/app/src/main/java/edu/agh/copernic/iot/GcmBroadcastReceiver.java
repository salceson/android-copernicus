package edu.agh.copernic.iot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class GcmBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "GcmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got gcm msg");
        Toast.makeText(context, "DZIALO", Toast.LENGTH_SHORT).show();
    }
}
