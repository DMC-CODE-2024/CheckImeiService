package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.model.app.FeatureMenu;
import com.gl.ceir.config.repository.app.FeatureMenuRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class FeatureMenuServiceImpl {

    @Autowired
    FeatureMenuRepository featureMenuRepository;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public List<FeatureMenu> getAll() {
        try {
            var v = featureMenuRepository.findAll();
            logger.info("Response {}", v.toString());
            return v;
        } catch (Exception e) {
            logger.error("Exp::: : " + e.getMessage() + " : " + e.getLocalizedMessage());
            return null;
        }
    }

    public List<FeatureMenu> getByStatusAndLanguageAndFeatureSubmenusStatus(String language) {
        // var a =featureMenuRepository.getByLanguageAndFeatureSubmenusStatus(language, 1);
        // logger.info("feature Sub menu  -1:   {}", a);
        var b =   featureMenuRepository.getByLanguageAndStatus(language, 1);
        // logger.info(" feature menu   by status -1->  {} ", b);
        // List<FeatureMenu> fm = featureMenuRepository.getByLanguage(language);
        // logger.info(" feature menu by lang {}", fm);

        List<FeatureMenu> newFm = b.stream()
                .map(f -> new FeatureMenu(
                        f.getFeatureSubmenus().stream()
                                .filter(fs -> fs.getStatus() == 1)
                                .collect(Collectors.toList()),
                        f.getLogo(),
                        f.getName()))
                .collect(Collectors.toList());
        return newFm;
    }

}

