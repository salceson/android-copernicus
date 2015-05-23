package edu.agh.copernic.iot.net.json;

import lombok.NonNull;
import lombok.Value;

@Value
public class LightsRequestJson {
    public interface Room {
        String KITCHEN = "kitchen";
        String ROOM = "corridor";
        String ALL = "*";
    }

    public interface Operation {
        String ON = "on";
        String OFF = "off";
        String TOGGLE = "toggle";
    }

    private final String floor;
    private final String room;
    private final String operation;

    public static LightsRequestJson create(@NonNull String room, @NonNull String operation) {
        return new LightsRequestJson("1", room, operation);
    }

}
