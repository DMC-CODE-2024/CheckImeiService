package com.gl.ceir.config.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity

@Setter
@Getter
public class EirsResponseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tag;
    private String value, language;

    public EirsResponseParam(String tag, String value, String language) {
        this.tag = tag;
        this.value = value;
        this.language = language;
    }

    public EirsResponseParam(){}
}
