package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.EirsResponseParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EirsResponseParamRepository extends JpaRepository<EirsResponseParam, Long>, JpaSpecificationExecutor<EirsResponseParam> {
	public EirsResponseParam getByTagAndLanguage(String tag , String language);
}
