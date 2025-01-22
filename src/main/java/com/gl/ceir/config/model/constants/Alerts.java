package com.gl.ceir.config.model.constants;

public enum Alerts {
    ALERT_1103("alert1303"), // check imei fail
    ALERT_1104("alert1304"), // saveCheckImeiRequest ,appDeviceDetailsRepo save
    ALERT_1105("alert1305"), // language
    ALERT_1106("alert1306"), // pri init api
    ALERT_1107("alert1307"), // sendPostForSms
    ALERT_1108("alert1308"), // notification impl
    ALERT_1110("alert1309");  // simp


    private String name;

    Alerts(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
