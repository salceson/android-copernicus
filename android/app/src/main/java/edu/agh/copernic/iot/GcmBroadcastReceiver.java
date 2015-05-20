package edu.agh.copernic.iot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "GcmReceiver";

    public static final String FLOOR_TAG = "floor";
    public static final String ROOM_TAG = "room";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got gcm msg");

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String type = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(type)) {
            Bundle bundle = intent.getExtras();
            if (!bundle.isEmpty()) {
                Log.i(FLOOR_TAG, bundle.getString(FLOOR_TAG));
                Log.i(ROOM_TAG, bundle.getString(ROOM_TAG));
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.common_signin_btn_icon_light)
                                .setContentTitle("ALARM!")
                                .setContentText("Some is in the " + bundle.get(ROOM_TAG) + " on the floor no " + bundle.getString(FLOOR_TAG));
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());
            }
        }


    }
}
