package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.FeatureSubmenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureSubMenuRepository extends JpaRepository<FeatureSubmenu, Long> {
    @Override
    public List<FeatureSubmenu> findAll();

    //@Query(value = "SELECT * FROM feature_menu f INNER JOIN feature_submenu fs on f.id=fs.feature_menu_id WHERE f.status=:label and f.language=:language and fs.status=:status  ", nativeQuery = true)
    // public List<FeatureSubmenu> getByStatusAndFeatureMenuLanguageAndFeatureMenuStatus(int label,String language ,int status);
    public List<FeatureSubmenu> getByStatus(int status);

}
