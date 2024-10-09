package com.gl.ceir.config.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FeatureSubmenu {
    private static final long serialVersionUID = 1L;
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String link, name;

    private int status;

    //    @JsonBackReference
 //  @ManyToOne( fetch = FetchType.LAZY)
//    @JoinColumn(name = "feature_menu_id")
//    private FeatureMenu featureMenuId;
    @JsonIgnore
    private Integer featureMenuId;
}

//  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
//   @JoinColumn(name = "feature_list_id", nullable = false)
//  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
//    @JsonIgnore
//    @ManyToOne
//    @JoinColumn(name = "featureMenuId")
//    private FeatureMenu featureMenu;
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof FeatureSubmenu)) return false;
//        FeatureSubmenu that = (FeatureSubmenu) o;
//        return status == that.status && Objects.equals(id, that.id) && Objects.equals(featureId, that.featureId) && Objects.equals(link, that.link) && Objects.equals(name, that.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, featureId, link, name, status);
//    }