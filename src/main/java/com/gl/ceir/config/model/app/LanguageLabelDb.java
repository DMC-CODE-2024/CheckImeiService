/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity

@Getter
@Setter
@Table(name = "label_mul_lingual_text")
public class LanguageLabelDb implements Serializable {

    @Transient
    String featureName;
    @Id
    String label;
    String english_name;
    String khmer_name;

}









     


/* 6:04    working 


  @Transient
    String featureName;
    @Id
    String label;
    String englishName;
    String khmerName;







*/