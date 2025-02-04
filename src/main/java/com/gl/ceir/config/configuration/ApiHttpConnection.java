package com.gl.ceir.config.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.gl.ceir.config.model.app.Notification;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.time.Duration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class ApiHttpConnection {

    @Value("${eirs.alert.url}")
    public String alerturl;

    @Value("${eirs.iplocation.url}")
    public String ipurl;

    static final Logger logger = LogManager.getLogger(ApiHttpConnection.class);

    private RestTemplate restTemplate = null;

    public void httpConnectionForApp(String alertId, String alertMessage, String alertProcess) {
        AlertDto alertDto = new AlertDto();
        alertDto.setAlertId(alertId);
        alertDto.setUserId(String.valueOf(0));
        alertDto.setAlertMessage(alertMessage);
        alertDto.setAlertProcess(alertProcess);
        long start = System.currentTimeMillis();
        try {
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(1000);
            clientHttpRequestFactory.setReadTimeout(1000);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AlertDto> request = new HttpEntity<AlertDto>(alertDto, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(alerturl, request, String.class);
            logger.info("Alert Sent Request:{}, TimeTaken:{} Response:{}", alertDto, responseEntity,
                    (System.currentTimeMillis() - start));
        } catch (org.springframework.web.client.ResourceAccessException resourceAccessException) {
            logger.error("Error while Sending Alert resourceAccessException:{} Request:{}",
                    resourceAccessException.getMessage(), alertDto, resourceAccessException);
        } catch (Exception e) {
            logger.error("Error while Sending Alert Error:{} Request:{}", e.getMessage(), alertDto, e);
        }

    }

    public String httpConnectionForIpCheck(String ipType, String ip) {
        IpLocation ipL = new IpLocation();
        ipL.setIpType(ipType);
        ipL.setIp(ip);
        try {
            logger.info("Going to check ip - {} for type -}{} ", ip, ipType);
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(1000);
            clientHttpRequestFactory.setReadTimeout(1000);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var request = new HttpEntity<IpLocation>(ipL, headers);
            var responseEntity = restTemplate.postForEntity(ipurl, request, IpApiResponse.class);
            logger.info("Response:{} ,// ", responseEntity.toString());
            return responseEntity.getBody().getStatusCode().equals("200") ? "True" : "False";
        } catch (Exception e) {
            logger.error(" Error:{}  ", e.getMessage());
            return "Error";
        }
    }

}

class IpLocation {
    private String ipType, ip;

    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}

class AlertDto {

    private String alertId;
    private String alertMessage;
    private String alertProcess;
    private String userId;

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getAlertProcess() {
        return alertProcess;
    }

    public void setAlertProcess(String alertProcess) {
        this.alertProcess = alertProcess;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

class IpApiResponse {
    private String statusCode, statusMessage, countryCode, countryName;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public String toString() {
        return "IpApiResponse [statusCode=" + statusCode + ", statusMessage=" + statusMessage + ", countryCode="
                + countryCode + ", countryName=" + countryName + "]";
    }

}