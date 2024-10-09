package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.SystemConfigListDb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigListRepository extends JpaRepository<SystemConfigListDb, Long> {
    public SystemConfigListDb findByTagAndInterp(String tag, String interp);
}
