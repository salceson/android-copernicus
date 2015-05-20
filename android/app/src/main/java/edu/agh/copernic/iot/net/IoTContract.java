package edu.agh.copernic.iot.net;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface IoTContract {
    @POST("/device/")
    Response sendGCMI(@Body GcmIdJson body);
}
