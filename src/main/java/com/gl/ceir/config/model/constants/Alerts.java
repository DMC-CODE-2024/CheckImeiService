package com.gl.ceir.config.model.constants;

public enum Alerts {
    ALERT_1103("alert1103"), // check imei fail
    ALERT_1104("alert1104"), // saveCheckImeiRequest ,appDeviceDetailsRepo save
    ALERT_1105("alert1105"), // language
    ALERT_1106("alert1106"), // pri init api
    ALERT_1107("alert1107"), // sendPostForSms
    ALERT_1108("alert1108"), // notification impl
    ALERT_1110("alert1110");  // simp


    private String name;

    Alerts(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
