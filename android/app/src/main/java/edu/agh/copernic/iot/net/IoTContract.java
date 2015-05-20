package edu.agh.copernic.iot.net;

import edu.agh.copernic.iot.net.json.AlarmRequestJson;
import edu.agh.copernic.iot.net.json.AlarmResponseJson;
import edu.agh.copernic.iot.net.json.GcmIdJson;
import edu.agh.copernic.iot.net.json.LightsRequestJson;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

public interface IoTContract {
    @POST("/device/")
    Response sendGCMI(@Body GcmIdJson body);

    @POST("/lights/")
    Observable<Response> sendLightsOperation(@Body LightsRequestJson body);

    @GET("/alarm/")
    Observable<AlarmResponseJson> getAlarmStatus();

    @POST("/alarm/")
    Observable<Response> setAlarmState(@Body AlarmRequestJson body);
}
