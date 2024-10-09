/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.app;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mobile_app_dev_info")
public class AppDeviceDetailsDb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String languageType;
    private String deviceId;
    private String osType;


    @Column(name = "device_detail")
    private JSONObject deviceDetails;

    public AppDeviceDetailsDb(String languageType, String deviceId, String osType, JSONObject deviceDetails) {
        this.languageType = languageType;
        this.deviceId = deviceId;
        this.osType = osType;
        this.deviceDetails = deviceDetails;
    }

    @Override
    public String toString() {
        return "AppDeviceDetailsDb{" + "id=" + id + ", languageType=" + languageType + ", deviceId=" + deviceId + ", osType=" + osType + ", deviceDetails=" + deviceDetails + '}';
    }


}
