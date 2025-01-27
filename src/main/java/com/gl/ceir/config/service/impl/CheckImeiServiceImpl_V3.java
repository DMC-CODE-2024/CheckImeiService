package com.gl.ceir.config.service.impl;

import com.gl.RuleEngineAdaptor;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.model.app.CheckImeiRequest;
import com.gl.ceir.config.model.app.CheckImeiResponse;
import com.gl.ceir.config.model.app.Result;
import com.gl.ceir.config.model.constants.Alerts;
import com.gl.ceir.config.model.constants.StatusMessage;
import com.gl.ceir.config.repository.app.CheckImeiRequestRepository;
import com.gl.ceir.config.repository.app.CheckImeiResponseParamRepository;
import com.gl.ceir.config.repository.app.GsmaTacDetailsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Write a code :
 * get all values at the starting of api. like language sys_param checkImeiResponseParam etc.
 * create a function to check in database , if value of that tag is true:
 *  Call all values from db again. and once values are obtained. change sysParam_tag ->false
 * If  change sysParam_tag ->false. not to get values
 *
 * */

@Service
public class CheckImeiServiceImpl_V3 {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Value("${nullPointerException}")
    private String nullPointerException;

    @Value("${sqlException}")
    private String sQLException;

//    @Value("${ruleResponseError}")
//    private String ruleResponseError;
//
//    @Value("${someWentWrongException}")
//    private String someWentWrongException;
//
//    @Value("${stolenRule}")
//    private String stolenRule;

    @Value("#{'${combinationRules}'.split(',')}")
    public List<String> combinationRules;

    @Value("${appdbName}")
    private String appdbName;

    @Value("${auddbName}")
    private String auddbName;

    @Value("${repdbName}")
    private String repdbName;

    @Value("${edrappdbName}")
    private String edrappdbName;

    @Autowired
    AlertServiceImpl alertServiceImpl;

    @Autowired
    CheckImeiResponseParamRepository chkImeiRespPrmRepo;

    @Autowired
    GsmaTacDetailsRepository gsmaTacDetailsRepository;

    @Autowired
    CheckImeiRequestRepository checkImeiRequestRepository;

    @Autowired
    CheckImeiServiceSendSMS checkImeiServiceSendSMS;

    @Autowired
    SystemParamServiceImpl systemParamServiceImpl;

    @Value("${module_name}")
    private String module_name;
    

    public CheckImeiResponse getImeiDetailsDevicesNew(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            logger.info("Starting ....... ");
            LinkedHashMap<String, Boolean> rules = getRuleListResponseFromRuleEngine(checkImeiRequest);
            var responseMap = getFinalResponseTag(rules);
            logger.info("Response Tag :: {} ", responseMap);
            var result = getResult(checkImeiRequest, rules, responseMap);
            checkImeiServiceSendSMS.sendSMSforUSSD_SMS(checkImeiRequest, responseMap.get("messageTag"), saveCheckImeiRequest(checkImeiRequest, startTime));
            return new CheckImeiResponse(String.valueOf(HttpStatus.OK.value()), StatusMessage.FOUND.getName(), checkImeiRequest.getLanguage(), result);
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, nullPointerException);
            } else if (e instanceof SQLException) {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, sQLException);
            } else {
                saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());
            }//  else if (e instanceof SQLGrammarException) { saveCheckImeiFailDetails(checkImeiRequest, startTime, e.getLocalizedMessage());            }
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1103.getName(), 0);
            logger.error(e.getLocalizedMessage() + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    private Map<String, String> getFinalResponseTag(LinkedHashMap<String, Boolean> r) {
        var lastRule = r.keySet().stream().reduce((first, second) -> second).orElse("");  // opt
        logger.info("Last Rule :{} and its value {}", lastRule, r.get(lastRule));
        var compliantTag = getCompliantRegisteredTag(r);
        var messageTag = combinationRules.contains(lastRule) || r.get(lastRule) ? compliantTag : lastRule;
        logger.info("CompliantTag  :{} and MessageTag:  {}", compliantTag, messageTag);
        return Map.of("compliantTag", compliantTag, "messageTag", messageTag);
    }

    private String getCompliantRegisteredTag(LinkedHashMap<String, Boolean> r) {
        String value = "";
        boolean isWhitelisted = r.getOrDefault("NATIONAL_WHITELISTS", true);
        boolean isNWLValid = r.getOrDefault("NWL_VALIDITYFLAG", true);
        boolean isCustomManufacturer = r.getOrDefault("CUSTOM_LOCAL_MANUFACTURER", true);
        boolean isMDR = r.getOrDefault("MDR", true);
        logger.info(" isWhitelisted- {}, isNWLValid- {} , isCustomManufacturer -{},   isMDR -{}", isWhitelisted, isNWLValid, isCustomManufacturer, isMDR);
        if (isWhitelisted) {
            value = isNWLValid ? (isCustomManufacturer ? "eirs_compliant_registered" : "eirs_compliant_gdce_not_compliant_registered") : "eirs_not_compliant_registered";
        } else {
            value = isMDR ? (isCustomManufacturer ? "eirs_compliant_not_registered" : "eirs_not_gdce_compliant_not_registered") : "eirs_not_compliant_not_registered";
        }
        return value;
    }

    private LinkedHashMap<String, Boolean> getRuleListResponseFromRuleEngine(CheckImeiRequest checkImeiRequest) {
        try {
            var startTime = System.currentTimeMillis();
            logger.info("Rule  deviceInfo {} :-", getDeviceInfoMap(checkImeiRequest));
            LinkedHashMap<String, Boolean> rules = RuleEngineAdaptor.startAdaptor(getDeviceInfoMap(checkImeiRequest));
            logger.info("Rule  Time Taken is  :->{} . [] response  {}", System.currentTimeMillis() - startTime, rules);
            return rules;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage() + ". Not able to fetch rules  in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
            return null;
        }
    }

    private Map<String, String> getDeviceInfoMap(CheckImeiRequest checkImeiRequest) {
        var map = Map.of("appdbName", appdbName, "auddbName", auddbName, "repdbName", repdbName, "edrappdbName", edrappdbName,
                "userType", "default", "imei", checkImeiRequest.getImei(), "msisdn", checkImeiRequest.getMsisdn() == null ? "" : checkImeiRequest.getMsisdn(),
                "imsi", checkImeiRequest.getImsi() == null ? "" : checkImeiRequest.getImsi(),
                "feature", module_name, "operator", checkImeiRequest.getOperator() == null ? "" : checkImeiRequest.getOperator());
        return map;
    }

    private Result getResult(CheckImeiRequest checkImeiRequest, LinkedHashMap<String, Boolean> rules, Map<String, String> responseMap) {
        getComplinaceStatus(checkImeiRequest, responseMap.get("compliantTag"));
        var message = getMessage(checkImeiRequest, responseMap.get("messageTag"));
        var symbolColor = getSymbolColor(checkImeiRequest, responseMap.get("messageTag"));
        var devDetails = getDeviceDetails(responseMap.get("messageTag"), checkImeiRequest);
        checkImeiRequest.setRequestProcessStatus("Success");
        return new Result(checkImeiRequest.getImeiProcessStatus().equalsIgnoreCase("valid"),
                symbolColor, checkImeiRequest.getComplianceStatus(),
                message, devDetails);
    }

    private String getMessage(CheckImeiRequest checkImeiRequest, String responseTag) {
        var value = chkImeiRespPrmValue(checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? responseTag + "_MsgForUssd"
                : checkImeiRequest.getChannel().equalsIgnoreCase("sms") ? responseTag + "_MsgForSms"
                : responseTag + "_Msg", checkImeiRequest.getLanguage())
                .replace("<imei>", checkImeiRequest.getImei())
                .replace("<compliance_status>", checkImeiRequest.getComplianceStatus());
        logger.info("Message :::->" + value);
        return value;
    }

    private String getSymbolColor(CheckImeiRequest checkImeiRequest, String responseTag) {
        var symbolTag = responseTag + "_SymbolColor";
        var symbolColor = chkImeiRespPrmValue(symbolTag, "en");
        logger.info("SymbolColor Response :->" + symbolColor);
        checkImeiRequest.setSymbol_color(symbolColor);
        return symbolColor;
    }

    private void getComplinaceStatus(CheckImeiRequest checkImeiRequest, String responseTag) {
        var complianceStatus = chkImeiRespPrmValue(checkImeiRequest.getChannel().equalsIgnoreCase("ussd") ? responseTag + "_ComplianceForUssd"
                : checkImeiRequest.getChannel().equalsIgnoreCase("sms") ? responseTag + "_ComplianceForSms"
                : responseTag + "_Compliance", checkImeiRequest.getLanguage())
                .replace("<imei>", checkImeiRequest.getImei());
        logger.info("Compliance Status:::->" + complianceStatus);
        checkImeiRequest.setComplianceStatus(complianceStatus);
    }

    public LinkedHashMap<String, String> getDeviceDetails(String tag, CheckImeiRequest checkImeiRequest) {
        LinkedHashMap<String, String> deviceDetailMap = null;
        try {
            var list = systemParamServiceImpl.getValueByTag("CheckImeiDeviceDetailShowRule");
            checkImeiRequest.setImeiProcessStatus("Invalid");
            if (Arrays.stream(list.split(",")).collect(Collectors.toList()).contains(tag)) {
                checkImeiRequest.setImeiProcessStatus("Valid");
                var gsmaTacDetails = gsmaTacDetailsRepository.getBydeviceId(checkImeiRequest.getImei().substring(0, 8));
                if (gsmaTacDetails == null) {
                    logger.info("No MDR detail Found ");
                } else {
                    deviceDetailMap = deviceDetailsNew(gsmaTacDetails.getBrand_name(), gsmaTacDetails.getModel_name(), gsmaTacDetails.getDevice_type(), gsmaTacDetails.getManufacturer(), gsmaTacDetails.getMarketing_name(), checkImeiRequest.getLanguage());
                    checkImeiRequest.setBrandName(gsmaTacDetails.getBrand_name());
                    checkImeiRequest.setModelName(gsmaTacDetails.getModel_name());
                    checkImeiRequest.setDeviceType(gsmaTacDetails.getDevice_type());
                    checkImeiRequest.setManufacturer(gsmaTacDetails.getManufacturer());
                    checkImeiRequest.setMarketingName(gsmaTacDetails.getMarketing_name());
                }
            }
        } catch (
                Exception e) {
            logger.warn("Not able to get MDR details:" + e.getLocalizedMessage());
        }
        logger.info(" MDR detail Response {}", deviceDetailMap);
        return deviceDetailMap;
    }

    public void saveCheckImeiFailDetails(CheckImeiRequest checkImeiRequest, long startTime, String desc) {
        checkImeiRequest.setRequestProcessStatus("Fail");
        checkImeiRequest.setFail_process_description(desc);
        logger.info(" CHECK_IMEI :Start Time = " + startTime + "; End Time  = " + System.currentTimeMillis() + "  !!! Request = " + checkImeiRequest.toString() + ", Response =" + desc);
        new Thread(() -> saveCheckImeiRequest(checkImeiRequest, startTime)).start();//  saveCheckImeiRequest(checkImeiRequest, startTime);
    }

    public CheckImeiRequest saveCheckImeiRequest(CheckImeiRequest checkImeiRequest, long startTime) {
        try {
            checkImeiRequest.setCheckProcessTime(String.valueOf(System.currentTimeMillis() - startTime));
            return checkImeiRequestRepository.save(checkImeiRequest);
        } catch (Exception e) {
            alertServiceImpl.raiseAnAlert(Alerts.ALERT_1110.getName(), 0);
            logger.error(e.getLocalizedMessage() + "in able to save in chk imei req table [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
            throw new InternalServicesException(checkImeiRequest.getLanguage(), globalErrorMsgs(checkImeiRequest.getLanguage()));
        }
    }

    public String globalErrorMsgs(String language) {
        return chkImeiRespPrmValue("CheckImeiErrorMessage", language);
    }

    private LinkedHashMap<String, String> deviceDetailsNew(String brand_name, String model_name, String device_type, String manufacturer, String marketing_name, String lang) {
        LinkedHashMap<String, String> item = new LinkedHashMap();
        item.put(chkImeiRespPrmValue("brandName", lang), brand_name);
        item.put(chkImeiRespPrmValue("marketingName", lang), marketing_name);
        item.put(chkImeiRespPrmValue("manufacturer", lang), manufacturer);
        item.put(chkImeiRespPrmValue("modelName", lang), model_name);
        item.put(chkImeiRespPrmValue("deviceType", lang), device_type);
        return item;
    }

    public String chkImeiRespPrmValue(String tag, String language) {
        String value = null;
        try {
            value = chkImeiRespPrmRepo.getByTagAndLanguage(tag, language).getValue();
            logger.info("Response:: {} for tag {} and {} ", value, tag, language);
        } catch (Exception e) {
            logger.error("Not able get response from check_imei_response for tag {} . Error is {} ", tag, e);
        }
        return value;
    }

    public String checkImeiServiceDownMsg(String language) {
        return chkImeiRespPrmRepo.getByTagAndLanguage("CheckImeiServiceDownMessage", language).getValue();
    }
}
