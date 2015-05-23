package edu.agh.copernic.iot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import edu.agh.copernic.iot.net.IoTContract;
import lombok.NonNull;
import retrofit.RestAdapter;

public class CopernicApplication extends Application {
    public static final String TAG = "CopernicApp";

    public static final String KEY = "AGH_COPERNIC";

    private static CopernicApplication instance;
    private IoTContract contract;

    public static CopernicApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        buildContract();
    }

    public IoTContract getContract() {
        return contract;
    }

    @SuppressLint("CommitPrefEdits")
    public void setEndpoint(@NonNull String newEndpoint) {
        SharedPreferences sp = getSharedPreferences(KEY, MODE_PRIVATE);
        sp.edit().putString(KEY, newEndpoint).commit();
        buildContract();
    }

    private void buildContract() {
        SharedPreferences sp = getSharedPreferences(KEY, MODE_PRIVATE);
        String endpoint = sp.getString(KEY, "10.22.112.219:20666");
        Log.i(TAG, endpoint);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + endpoint)
                .build();

        contract = restAdapter.create(IoTContract.class);
    }
}
