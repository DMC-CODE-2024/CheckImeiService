package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.CheckImeiResponseParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckImeiResponseParamRepository extends JpaRepository<CheckImeiResponseParam, Long>, JpaSpecificationExecutor<CheckImeiResponseParam> {

    public CheckImeiResponseParam getByTagAndLanguage(String tag, String language);

}
