/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Entity

@Getter
@Setter
@ToString
@Table(name = "preinit_deviceid_baseurl")
public class DeviceidBaseUrlDb implements Serializable {

    @Id
    private String deviceId;

    private String baseUrl;


}
