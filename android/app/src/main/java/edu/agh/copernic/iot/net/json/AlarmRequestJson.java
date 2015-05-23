package edu.agh.copernic.iot.net.json;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "create")
public class AlarmRequestJson {
    public interface Mode {
        String ON = "on";
        String OFF = "off";
    }

    private final String mode;
}
