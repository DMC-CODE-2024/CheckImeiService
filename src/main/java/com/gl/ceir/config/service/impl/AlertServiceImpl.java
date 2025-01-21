/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.configuration.ApiHttpConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl {

    private static final Logger logger = LogManager.getLogger(AlertServiceImpl.class);

    @Autowired
    ApiHttpConnection apiHttpConnection;

    public void raiseAnAlert(String alertId, int userId) {
        try {
            apiHttpConnection.httpConnectionForApp(alertId , "", "Check IMEI");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
