package com.gl.ceir.config.model.app;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class Result {

    private boolean isValidImei;
    private String symbol_color;
    private String complianceStatus;
    private String message;
    private Map deviceDetails;
  

            
    public Result(boolean isValidImei, String symbol_color, String complianceStatus, String message, Map deviceDetails) {
        this.isValidImei = isValidImei;
        this.symbol_color = symbol_color;
        this.complianceStatus = complianceStatus;
        this.message = message;
        this.deviceDetails = deviceDetails;
    }

    @Override
    public String toString() {
        return "Result{" + "isValidImei=" + isValidImei + ", message=" + message + ", deviceDetails=" + deviceDetails + ", complianceStatus=" + complianceStatus + ", symbol_color=" + symbol_color + '}';
    }

}
