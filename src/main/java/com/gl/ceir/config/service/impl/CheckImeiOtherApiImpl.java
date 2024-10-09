package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import com.gl.ceir.config.model.app.DeviceidBaseUrlDb;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.repository.app.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CheckImeiOtherApiImpl {

    private static final Logger logger = LogManager.getLogger(CheckImeiOtherApiImpl.class);


    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    AppDeviceDetailsRepository appDeviceDetailsRepository;

    @Autowired
    CheckImeiPreInitRepository checkImeiPreInitRepository;


    public void saveDeviceDetails(AppDeviceDetailsDb appDeviceDetailsDb) {
        try {
            appDeviceDetailsRepository.saveDetails(
                    appDeviceDetailsDb.getOsType(),
                    appDeviceDetailsDb.getDeviceId(),
                    appDeviceDetailsDb.getDeviceDetails().toJSONString(),
                    appDeviceDetailsDb.getLanguageType());
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1104.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), "internal server error");
        }
    }


    public DeviceidBaseUrlDb getPreinitApi(String deviceId) {
        try {
            var response = checkImeiPreInitRepository.getByDeviceId(deviceId);
            if (response == null) {
                response = checkImeiPreInitRepository.getByDeviceId("default_setup");
            }
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage() + " : " + e.getLocalizedMessage());
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1106.getName(), 0);
            throw new InternalServicesException(this.getClass().getName(), e.getLocalizedMessage());
        }
    }

}

