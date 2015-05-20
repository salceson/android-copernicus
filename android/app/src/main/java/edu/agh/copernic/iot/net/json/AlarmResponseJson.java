package edu.agh.copernic.iot.net.json;

import lombok.Value;

@Value
public class AlarmResponseJson {
    public enum Mode {
        ON("on", true), OFF("off", false);

        private String stringValue;
        private boolean isSet;

        Mode(String stringValue, boolean isSet) {
            this.stringValue = stringValue;
            this.isSet = isSet;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public boolean toBoolean() {
            return isSet;
        }
    }

    private final String mode;
}
