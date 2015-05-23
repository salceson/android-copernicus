package edu.agh.copernic.iot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.agh.copernic.iot.net.json.AlarmRequestJson;
import edu.agh.copernic.iot.net.json.AlarmResponseJson;
import edu.agh.copernic.iot.net.json.GcmIdJson;
import edu.agh.copernic.iot.net.json.LightsRequestJson;
import edu.agh.copernic.iot.util.Toasts;
import rx.android.schedulers.AndroidSchedulers;


public class MainActivity extends AppCompatActivity {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "744222889715";

    /**
     * Tag used on log messages.
     */
    private static final String TAG = "MainActivity";

    private GoogleCloudMessaging gcm;
    private Context context;

    private String regid;


    @InjectView(R.id.kitchen_btn)
    Button kitchenBtn;

    @InjectView(R.id.room_btn)
    Button roomBtn;

    @InjectView(R.id.floor_btn)
    Button floorBtn;

    @InjectView(R.id.gcm_send_btn)
    Button gcmBtn;

    @InjectView(R.id.alarm_switch)
    Switch alarmSwitch;

    @InjectView(R.id.endpoint_edit_text)
    EditText endpointEditText;

    @InjectView(R.id.endpoint_btn)
    Button enpointBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        kitchenBtn.setOnClickListener(v -> CopernicApplication.getInstance()
                .getContract()
                .sendLightsOperation(LightsRequestJson
                        .create(LightsRequestJson.Room.KITCHEN, LightsRequestJson.Operation.TOGGLE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> Toasts.show(context, "Toggled lights in the kitchen"),
                        error -> Toasts.show(context, "Couldn't toggle lights. Try again.")));

        roomBtn.setOnClickListener(v -> CopernicApplication.getInstance()
                .getContract()
                .sendLightsOperation(LightsRequestJson
                        .create(LightsRequestJson.Room.ROOM, LightsRequestJson.Operation.TOGGLE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> Toasts.show(context, "Toggled lights in the corridor"),
                        error -> Toasts.show(context, "Couldn't toggle lights. Try again.")));

        floorBtn.setOnClickListener(v -> CopernicApplication.getInstance()
                .getContract()
                .sendLightsOperation(LightsRequestJson
                        .create(LightsRequestJson.Room.ALL, LightsRequestJson.Operation.OFF))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> Toasts.show(context, "Turned lights off on the whole floor"),
                        error -> Toasts.show(context, "Couldn't turn off lights. Try again.")));

        gcmBtn.setOnClickListener(v -> sendRegistrationIdToBackend(regid));

        alarmSwitch.setEnabled(false);
        Toasts.show(context, "I need to check if alarm is turned on. Wait a sec.");
        CopernicApplication.getInstance().getContract()
                .getAlarmStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.getAlarm().equals(AlarmResponseJson.Mode.ON.toString())) {
                                alarmSwitch.setChecked(true);
                            } else {
                                alarmSwitch.setChecked(false);
                            }
                            alarmSwitch.setEnabled(true);
                            Toasts.show(context, "Alarms is " + response.getAlarm());
                        },
                        error -> {
                            Toasts.show(context, "Couldn't check if alarm is turned on");
                            alarmSwitch.setChecked(false);
                            alarmSwitch.setEnabled(true);
                        }
                );

        alarmSwitch.setOnClickListener(v -> {
            String mode;
            if (alarmSwitch.isChecked()) {
                mode = AlarmResponseJson.Mode.ON.toString();
            } else {
                mode = AlarmResponseJson.Mode.OFF.toString();
            }
            CopernicApplication.getInstance().getContract().setAlarmState(
                    AlarmRequestJson.create(mode))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> Toasts.show(context, "Alarm turned " + mode),
                            error -> {
                                Toasts.show(context, "Couldn't turn the alarm " + mode);
                                alarmSwitch.setChecked(!alarmSwitch.isChecked());
                            });
        });

        enpointBtn.setOnClickListener(v -> {
            String value = endpointEditText.getText().toString();
            CopernicApplication.getInstance().setEndpoint(value);
        });

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    @SuppressWarnings("unchecked")
    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected String doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;

            }

        }.execute(null, null, null);

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     *
     * @param regid
     */
    private void sendRegistrationIdToBackend(String regid) {
        // Your implementation here.
        CopernicApplication.getInstance().getContract().sendGCMI(new GcmIdJson(regid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> Log.i(TAG, regid + " has been sent as GCM"),
                        error -> Log.w(TAG, error.getMessage())
                );

    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
