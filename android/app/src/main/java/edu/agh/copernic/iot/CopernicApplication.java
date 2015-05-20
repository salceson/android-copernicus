package edu.agh.copernic.iot;

import android.app.Application;

import edu.agh.copernic.iot.net.IoTContract;
import retrofit.RestAdapter;

public class CopernicApplication extends Application {
    private static CopernicApplication instance;
    private static IoTContract contract;

    public static CopernicApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://10.22.112.219:20666")
                .build();

        contract = restAdapter.create(IoTContract.class);
    }

    public static IoTContract getContract() {
        return contract;
    }
}
