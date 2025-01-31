package com.gl.ceir.config.controller;

import com.gl.ceir.config.model.app.AppDeviceDetailsDb;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.constants.LanguageFeatureName;
import com.gl.ceir.config.service.impl.*;
import com.gl.ceir.config.validate.CheckImeiValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController


public class CheckImeiController {

    private static final Logger logger = LogManager.getLogger(CheckImeiController.class);

    @Autowired
    CheckImeiValidator checkImeiValidator;

    @Autowired
    CheckImeiOtherApiImpl checkImeiOtherApiImpl;

    @Autowired
    CheckImeiServiceImpl_V3 checkImeiServiceImplV3;

    @Autowired
    LanguageServiceImpl languageServiceImpl;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    SystemParamServiceImpl sysPrmSrvcImpl;

    @Autowired
    FeatureMenuServiceImpl featureMenuServiceImpl;

    @Value("#{'${languageType}'.split(',')}")
    public List<String> languageType;


    @CrossOrigin(origins = "", allowedHeaders = "")
    @RequestMapping(path = "services/mobile_api/preInit", method = RequestMethod.GET)
    public MappingJacksonValue getPreInit(@RequestParam("deviceId") String deviceId) {
        String host = request.getHeader("Host");
        // logger.info("MENU LIST ::: " + featureMenuServiceImpl.getAll());
        MappingJacksonValue mapping = new MappingJacksonValue(checkImeiOtherApiImpl.getPreinitApi(deviceId));
        logger.info("Response of View =" + mapping);
        return mapping;
    }

    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/mobile_api/mobileDeviceDetails/save")
    public MappingJacksonValue getMobileDeviceDetails(@RequestBody AppDeviceDetailsDb appDeviceDetailsDb) {
        checkImeiValidator.errorValidationChecker(appDeviceDetailsDb);
        logger.info("Request = " + appDeviceDetailsDb);
        checkImeiOtherApiImpl.saveDeviceDetails(appDeviceDetailsDb);
        logger.info("Going to fetch response according to  = " + appDeviceDetailsDb.getLanguageType());
        return new MappingJacksonValue(languageServiceImpl.getLanguageLabels(LanguageFeatureName.CHECKIMEI.getName(),
                appDeviceDetailsDb.getLanguageType()));
    }

    @CrossOrigin(origins = "", allowedHeaders = "")
    @PostMapping("services/checkIMEI")
    public ResponseEntity checkImeiDevice(@RequestBody CheckImeiRequest checkImeiRequest) {
        return startCheckImei(checkImeiRequest);
    }

    public ResponseEntity startCheckImei(CheckImeiRequest checkImeiRequest) {
        var startTime = System.currentTimeMillis();
        var defaultLang = sysPrmSrvcImpl.getValueByTag("systemDefaultLanguage");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Map<String, String> headers = Collections.list(httpRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
        logger.info("Headers->  {}", headers);

        if (checkImeiRequest.getLanguage() == null || !languageType.contains(checkImeiRequest.getLanguage()))
        {checkImeiRequest.setLanguage(defaultLang);}

        // checkImeiRequest.setLanguage(checkImeiRequest.getLanguage() == null ?
        // defaultLang : checkImeiRequest.getLanguage().equalsIgnoreCase("kh") ? "kh" :
        // defaultLang); // needs refactoring
        checkImeiValidator.errorValidationChecker(checkImeiRequest, startTime);
        checkImeiValidator.authorizationChecker(checkImeiRequest, startTime);

        if (!checkImeiValidator.checkLuhnAlgorithm(checkImeiRequest, startTime)) {
            return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY)
                    .body(new MappingJacksonValue(responseBuilder(checkImeiRequest)));
        }

        var value = checkImeiServiceImplV3.getImeiDetailsDevicesNew(checkImeiRequest, startTime);
        logger.info("   Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + " ########## Response =" + value.toString());
        return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaders.EMPTY).body(new MappingJacksonValue(value));
    }

    public CheckImeiResponse responseBuilder(CheckImeiRequest cImeiRes) {
        logger.info("   Start Time = ");
        var resul = Result.builder().complianceStatus("")
                .deviceDetails(null)
                .isValidImei(false)
                .message(cImeiRes.getFail_process_description())
                .symbol_color("").build();
        logger.info("   resul Time = " + resul);
        return CheckImeiResponse.builder().statusCode("200")
                .statusMessage("Found")
                .language(cImeiRes.getLanguage())
                .result(resul).build();
    }
}
