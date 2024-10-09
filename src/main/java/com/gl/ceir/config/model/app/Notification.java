package com.gl.ceir.config.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@DynamicInsert
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelType;

    private String message;
    private String featureName;

    private Integer status;

    private Integer retryCount;

    private String email;

    private String msisdn;

    private String operatorName;

    private String msgLang;

    private Integer checkImeiId;


    public Notification(String channelType, String message, String featureName, Integer status, Integer retryCount, String msisdn, String operatorName, String msgLang, Integer checkImeiId) {
        this.channelType = channelType;
        this.message = message;
        this.featureName = featureName;
        this.status = status;
        this.retryCount = retryCount;
        this.msisdn = msisdn;
        this.operatorName = operatorName;
        this.msgLang = msgLang;
        this.checkImeiId = checkImeiId;
    }


}
