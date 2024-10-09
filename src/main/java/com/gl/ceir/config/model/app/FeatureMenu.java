package com.gl.ceir.config.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FeatureMenu {
    private static final long serialVersionUID = 1L;
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logo, name;
    @JsonIgnore
    private String language; //,link, tag ,

   // @OneToMany(mappedBy = "featureMenuId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "featureMenuId")
    List<FeatureSubmenu> featureSubmenus;

    public FeatureMenu(List<FeatureSubmenu> featureSubmenus, String logo, String name) {
        this.featureSubmenus = featureSubmenus;
        this.logo = logo;
        this.name = name;
    }
}

//    @JsonManagedReference
//    @OneToMany(mappedBy = "featureId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    List<FeatureSubmenu> featureSubmenus;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        FeatureMenu that = (FeatureMenu) o;
//        return status == that.status && Objects.equals(id, that.id) && Objects.equals(logo, that.logo) && Objects.equals(name, that.name) && Objects.equals(language, that.language) && Objects.equals(featureSubmenus, that.featureSubmenus);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, logo, name, status, language, featureSubmenus);
//    }