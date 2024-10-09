/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FeatureIpAccessList {

    private static long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int featureIpListId;

    private String featureId;

    private int typeOfCheck;

    private String ipAddress;

    @Override
    public String toString() {
        return "FeatureIpAccessList{" + "id=" + id + ", featureIpListId=" + featureIpListId + ", featureId=" + featureId + ", typeOfCheck=" + typeOfCheck + ", ipAddress=" + ipAddress + '}';
    }

}
