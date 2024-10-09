package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.Notification;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.repository.app.CheckImeiResponseParamRepository;
import com.gl.ceir.config.repository.app.SystemConfigurationDbRepository;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
public class CheckImeiServiceSendSMS {
    private static final Logger logger = LogManager.getLogger(CheckImeiServiceSendSMS.class);

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    SystemConfigurationDbRepository systemConfigurationDbRepositry;

    @Autowired
    CheckImeiResponseParamRepository checkImeiResponseParamRepository;

    public void sendSMSforUSSD_SMS(CheckImeiRequest checkImeiRequest, String tag, CheckImeiRequest response) {
        if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd")
                && systemConfigurationDbRepositry.getByTag("send_sms_flag").getValue().equalsIgnoreCase("true")) {
            logger.info("Going for ussd and send_sms_flag true; getting value of tag   : " + tag + "_MsgForSms");
            var smsMessage = checkImeiResponseParamRepository.getByTagAndLanguage(
                            tag + "_MsgForSms",
                            checkImeiRequest.getLanguage())
                    .getValue()
                    .replace("<imei>", checkImeiRequest.getImei())
                    .replace("<compliance_status>", checkImeiRequest.getComplianceStatus());
            createPostRequestForNotification(checkImeiRequest, smsMessage, response.getId().intValue());
        }
    }

    private void createPostRequestForNotification(CheckImeiRequest checkImeiRequest, String smsMessage, int id) {
        logger.info(" Notification ::  :");
        var notification = new Notification("SMS", smsMessage, "CheckImei", 0, 0, checkImeiRequest.getMsisdn(),
                checkImeiRequest.getOperator(), checkImeiRequest.getLanguage(), id);
        Gson gson = new Gson();
        String body = gson.toJson(notification, Notification.class);
        logger.info("Going to send notification::  :" + body);
        String url = systemConfigurationDbRepositry.getByTag("notificationTableUrl")
                .getValue();
        sendPostRequestToUrl(url, body);
    }

    public String sendPostRequestToUrl(String url, String body) {
        logger.info("POST  Start Url-> " + url + " ;Body->" + body);
        StringBuffer response = new StringBuffer();
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();
            os.close();
            // For POST only - END
            int responseCode = con.getResponseCode();
            logger.info("POST Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                logger.info("Notification Response:" + response.toString());
            } else {
                logger.warn("POST request not worked");
                alertServiceImpl.raiseAnAlert(Alerts.ALERT_1107.getName(), 0);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return response.toString();
    }

}
