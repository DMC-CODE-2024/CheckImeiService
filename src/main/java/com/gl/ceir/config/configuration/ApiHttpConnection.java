package com.gl.ceir.config.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;


@Service
public class ApiHttpConnection {

    @Value("${eirs.alert.url}")
    public String alerturl;

    static final Logger logger = LogManager.getLogger(ApiHttpConnection.class);

    public void httpConnectionForApp_v2(String alertId, String alertMessage, String alertProcess) {
        try {
            HttpHeaders headers = null;
            MultiValueMap<String, String> map = null;
            HttpEntity<MultiValueMap<String, String>> request = null;
            ResponseEntity<String> httpResponse = null;
            String respons = null;
            URI uri = new URI(alerturl);
            final RestTemplate restTemplate = new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofMillis(10000))
                    .setReadTimeout(Duration.ofMillis(10000))
                    .build();
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.info("Http Connection Header Created ");

            map = new LinkedMultiValueMap<>();
            map.add("alertId", alertId);
            map.add("alertMessage", alertMessage);
            map.add("alertProcess", alertProcess);
            logger.info("Http Connection Body Created ");
            request = new HttpEntity<>(map, headers);
            httpResponse = restTemplate.postForEntity(uri, request, String.class);
            respons = httpResponse.getBody();
            logger.info("Request:" + alerturl + " Body:" + map.toString() + "alertId:" + alertId + " Response :" + respons);

        } catch (Exception e) {
            logger.error("Not able to http Api  " + e + " :: " + e.getCause());
        }
    }

    private RestTemplate restTemplate=null;


    public void httpConnectionForApp(  String alertId,   String alertMessage,   String alertProcess) {

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
            logger.info("Alert Sent Request:{}, TimeTaken:{} Response:{}", alertDto, responseEntity, (System.currentTimeMillis() - start));
        } catch (org.springframework.web.client.ResourceAccessException resourceAccessException) {
            logger.error("Error while Sending Alert resourceAccessException:{} Request:{}", resourceAccessException.getMessage(), alertDto, resourceAccessException);
        } catch (Exception e) {
            logger.error("Error while Sending Alert Error:{} Request:{}", e.getMessage(), alertDto, e);
        }

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
