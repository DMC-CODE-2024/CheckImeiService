package com.gl.ceir.config.model.app;

import java.util.Date;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "check_imei_req_detail")
@DynamicInsert
@ToString
public class CheckImeiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imei;
    private String msisdn;
    private String operator;
    private String imsi;
    private String language;
    private String channel;
    private String utm_source;
    private String browser;
    private String public_ip;
    private String header_browser;
    private String header_public_ip;
    private String os_type;

    private String requestProcessStatus;
    private String imeiProcessStatus;
    private String checkProcessTime;
    private String complianceStatus;

    private String symbol_color;
    private String device_id;
    private String fail_process_description;
    private int complianceValue;
    private String requestId;
    @Transient
    private String client_ip;
    @Transient
    private String ip_type;

    private String brandName, modelName, manufacturer, marketingName, deviceType;

    @Transient
    private Date createdOn;

    public CheckImeiRequest(String imei, String channel, String header_browser, String header_public_ip,
            String requestId) {
        this.imei = imei;
        this.channel = channel;
        this.header_browser = header_browser;
        this.header_public_ip = header_public_ip;
        this.requestId = requestId;
    }
}
