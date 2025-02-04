package com.gl.ceir.config.validate;

import com.gl.ceir.config.configuration.ApiHttpConnection;
import com.gl.ceir.config.exceptions.MissingRequestParameterException;
import com.gl.ceir.config.exceptions.ServiceUnavailableException;
import com.gl.ceir.config.exceptions.UnAuthorizationException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import com.gl.ceir.config.model.app.*;
import com.gl.ceir.config.repository.app.EirsResponseParamRepository;
import com.gl.ceir.config.repository.app.FeatureIpAccessListRepository;
import com.gl.ceir.config.repository.app.SystemConfigListRepository;
import com.gl.ceir.config.repository.app.UserFeatureIpAccessListRepository;
import com.gl.ceir.config.service.impl.CheckImeiServiceImpl_V3;
import com.gl.ceir.config.service.impl.SystemParamServiceImpl;
import com.gl.ceir.config.service.userlogic.UserFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Service
public class CheckImeiValidator {

        private static final Logger logger = LogManager.getLogger(CheckImeiValidator.class);

        @Value("${authUserIpNotMatch}")
        private String authUserIpNotMatch;
        @Value("${authFeatureIpNotMatch}")
        private String authFeatureIpNotMatch;
        @Value("${authFeatureIpNotPresent}")
        private String authFeatureIpNotPresent;
        @Value("${authUserPassNotMatch}")
        private String authUserPassNotMatch;
        @Value("${authOperatorNotPresent}")
        private String authOperatorNotPresent;
        @Value("${authNotPresent}")
        private String authNotPresent;
        @Value("${requiredValueNotPresent}")
        private String requiredValueNotPresent;
        @Value("${mandatoryParameterMissing}")
        private String mandatoryParameterMissing;
        // @Value("${nullPointerException}")
        // private String nullPointerException;
        // @Value("${sqlException}")
        // private String sQLException;
        // @Value("${someWentWrongException}")
        // private String someWentWrongException;
        // @Value("#{'${languageType}'.split(',')}")
        // public List<String> languageType;

        @Autowired
        UserFactory userFactory;

        @Autowired
        CheckImeiServiceImpl_V3 checkImeiServiceImpl;

        @Autowired
        private HttpServletRequest request;

        @Autowired
        SystemConfigListRepository systemConfigListRepository;

        @Autowired
        SystemParamServiceImpl sysPrmSrvcImpl;

        @Autowired
        FeatureIpAccessListRepository featureIpAccessListRepository;

        @Autowired
        UserFeatureIpAccessListRepository userFeatureIpAccessListRepository;
        @Autowired
        EirsResponseParamRepository eirsResponseParamRepository;

        @Autowired
        ApiHttpConnection apiHttpConnection;

        public void errorValidationChecker(CheckImeiRequest checkImeiRequest, long startTime) {
                String userIp = request.getHeader("HTTP_CLIENT_IP") == null
                                ? (request.getHeader("X-FORWARDED-FOR") == null ? request.getRemoteAddr()
                                                : request.getHeader("X-FORWARDED-FOR"))
                                : request.getHeader("HTTP_CLIENT_IP");
                checkImeiRequest.setHeader_browser(request.getHeader("user-agent"));
                checkImeiRequest.setHeader_public_ip(userIp);
                logger.info(checkImeiRequest.toString());
                var sysConfigsServiceDownFlag = sysPrmSrvcImpl.getValueByTag("service_down_flag");
                if (!StringUtils.isBlank(sysConfigsServiceDownFlag)
                                && sysConfigsServiceDownFlag.toLowerCase()
                                                .contains(checkImeiRequest.getChannel().toLowerCase())) {
                        logger.info("Service Not available for Channel--" + checkImeiRequest.getChannel());
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                        "Service Down for " + checkImeiRequest.getChannel());
                        throw new ServiceUnavailableException(checkImeiRequest.getLanguage(),
                                        checkImeiServiceImpl.checkImeiServiceDownMsg(checkImeiRequest.getLanguage()));
                }
                if (checkImeiRequest.getImei() == null || checkImeiRequest.getChannel() == null) {
                        logger.debug("Null Values " + checkImeiRequest.getImei());
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                        mandatoryParameterMissing);
                        throw new MissingRequestParameterException(checkImeiRequest.getLanguage(),
                                        checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                }
                if (checkImeiRequest.getImei().isBlank()
                                || (checkImeiRequest.getChannel().isBlank())
                                || (!Arrays.asList("web", "ussd", "sms", "phone", "app")
                                                .contains(checkImeiRequest.getChannel().toLowerCase()))
                                || (checkImeiRequest.getImsi() != null && (checkImeiRequest.getImsi().length() != 15
                                                || !(checkImeiRequest.getImsi().matches("[0-9]+"))))
                                || (checkImeiRequest.getMsisdn() != null
                                                && (checkImeiRequest.getMsisdn().trim().length() > 20
                                                                || !(checkImeiRequest.getMsisdn().matches("[0-9 ]+"))))
                                || (checkImeiRequest.getLanguage() != null
                                                && checkImeiRequest.getLanguage().trim().length() > 2)
                                || (checkImeiRequest.getOperator() != null
                                                && checkImeiRequest.getOperator().trim().length() > 20)
                                || (checkImeiRequest.getChannel().equalsIgnoreCase("ussd")
                                                && (checkImeiRequest.getMsisdn() == null
                                                                || checkImeiRequest.getOperator() == null
                                                                || checkImeiRequest.getOperator().isBlank()
                                                                || checkImeiRequest.getMsisdn().isBlank()))
                                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms")
                                                && (checkImeiRequest.getMsisdn() == null
                                                                || checkImeiRequest.getMsisdn().isBlank()
                                                                || checkImeiRequest.getOperator() == null
                                                                || checkImeiRequest.getOperator().isBlank()))) {
                        logger.info("Not allowed " + checkImeiRequest.getChannel());
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                        requiredValueNotPresent);
                        throw new UnprocessableEntityException(checkImeiRequest.getLanguage(),
                                        checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                }
                // if (!luhnAlgoCheck(checkImeiRequest.getImei())) {
                // logger.info("Luhn Failed for imei : " + checkImeiRequest.getImei() + ".
                // Getting value for luhnFailMsg from eirs response param");
                // var response = eirsResponseParamRepository.getByTagAndLanguage("luhnFailMsg"
                // ,checkImeiRequest.getLanguage() .equalsIgnoreCase("kh")? "km"
                // :checkImeiRequest.getLanguage() ).getValue();
                // checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                // response);
                // throw new UnprocessableEntityException(checkImeiRequest.getLanguage(),
                // response);
                // }
        }

        public void authorizationChecker(CheckImeiRequest checkImeiRequest, long startTime) {
                if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd")
                                || (checkImeiRequest.getChannel().equalsIgnoreCase("sms"))) {
                        if (!Optional.ofNullable(request.getHeader("Authorization")).isPresent()
                                        || !request.getHeader("Authorization").startsWith("Basic ")) {
                                logger.info("Rejected Due to  Authorization  Not Present"
                                                + request.getHeader("Authorization"));
                                checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                                authNotPresent);
                                throw new UnAuthorizationException(checkImeiRequest.getLanguage(),
                                                checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                        }
                        logger.info("Basic Authorization present " + request.getHeader("Authorization").substring(6));
                        try {
                                var systemConfig = systemConfigListRepository.findByTagAndInterp("OPERATORS",
                                                checkImeiRequest.getOperator().toUpperCase());
                                if (systemConfig == null) {
                                        logger.info("Operator Not allowed ");
                                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                                        authOperatorNotPresent);
                                        throw new UnprocessableEntityException(checkImeiRequest.getLanguage(),
                                                        checkImeiServiceImpl.globalErrorMsgs(
                                                                        checkImeiRequest.getLanguage()));
                                }
                                logger.info("Found operator with  value " + systemConfig.getValue());
                                var decodedString = new String(
                                                Base64.getDecoder().decode(
                                                                request.getHeader("Authorization").substring(6)));
                                logger.info("user:" + decodedString.split(":")[0] + "pass:"
                                                + decodedString.split(":")[1]);
                                // User userValue =
                                // userRepository.getByUsernameAndPasswordAndParentId(decodedString.split(":")[0],
                                // decodedString.split(":")[1], systemConfig.getValue());

                                UserVars userValue = (UserVars) userFactory.createUser()
                                                .getUserDetailDao(decodedString.split(":")[0],
                                                                decodedString.split(":")[1],
                                                                systemConfig.getValue());
                                if (userValue == null || !userValue.getUsername().equals(decodedString.split(":")[0])
                                                || !userValue.getPassword().equals(decodedString.split(":")[1])) {
                                        logger.info("username password not match");
                                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime,
                                                        authUserPassNotMatch);
                                        throw new UnAuthorizationException(checkImeiRequest.getLanguage(),
                                                        checkImeiServiceImpl.globalErrorMsgs(
                                                                        checkImeiRequest.getLanguage()));
                                }

                                if (sysPrmSrvcImpl.getValueByTag("CHECK_IMEI_AUTH_WITH_IP").equalsIgnoreCase("true")) {
                                        var checkimeiFeatureType = sysPrmSrvcImpl
                                                        .getValueByTag("CHECK_IMEI_FEATURE_ID");
                                        FeatureIpAccessList featureIpAccessList = featureIpAccessListRepository
                                                        .getByFeatureId(checkimeiFeatureType);
                                        logger.info(" data in featureIpAccessList  " + featureIpAccessList);
                                        if (featureIpAccessList == null) {
                                                checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest,
                                                                startTime,
                                                                authFeatureIpNotPresent);
                                                throw new UnAuthorizationException(checkImeiRequest.getLanguage(),
                                                                checkImeiServiceImpl.globalErrorMsgs(
                                                                                checkImeiRequest.getLanguage()));
                                        }
                                        if (featureIpAccessList.getTypeOfCheck() == 1) {
                                                if (!featureIpAccessList.getIpAddress()
                                                                .contains(checkImeiRequest.getHeader_public_ip())) {
                                                        logger.info("Type Check 1 But Ip not allowed ");
                                                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest,
                                                                        startTime,
                                                                        authFeatureIpNotMatch);
                                                        throw new UnAuthorizationException(
                                                                        checkImeiRequest.getLanguage(),
                                                                        checkImeiServiceImpl.globalErrorMsgs(
                                                                                        checkImeiRequest.getLanguage()));
                                                }
                                        } else {
                                                logger.info("Type Check 2 with featureid  "
                                                                + featureIpAccessList.getFeatureIpListId()
                                                                + " And User id " + userValue.getId());
                                                UserFeatureIpAccessList userFeatureIpAccessList = userFeatureIpAccessListRepository
                                                                .getByFeatureIpListIdAndUserId(
                                                                                featureIpAccessList
                                                                                                .getFeatureIpListId(),
                                                                                userValue.getId());
                                                logger.info("Response from  UserFeatureIpAccessList "
                                                                + userFeatureIpAccessList);
                                                if (userFeatureIpAccessList == null || !(userFeatureIpAccessList
                                                                .getIpAddress()
                                                                .contains(checkImeiRequest.getHeader_public_ip()))) {
                                                        logger.info("Type Check 2 But Ip not allowed ");
                                                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest,
                                                                        startTime,
                                                                        authUserIpNotMatch);
                                                        throw new UnAuthorizationException(
                                                                        checkImeiRequest.getLanguage(),
                                                                        checkImeiServiceImpl.globalErrorMsgs(
                                                                                        checkImeiRequest.getLanguage()));
                                                }
                                        }
                                }
                                logger.debug("Authentication Pass ");
                        } catch (NullPointerException | UnsupportedOperationException e) {
                                logger.warn("Authentication fail" + e);
                                throw new UnAuthorizationException(checkImeiRequest.getLanguage(),
                                                checkImeiServiceImpl.globalErrorMsgs(checkImeiRequest.getLanguage()));
                        }
                }
        }

        public void errorValidationChecker(AppDeviceDetailsDb appDeviceDetailsDb) {
                logger.info(appDeviceDetailsDb.toString());
                if (appDeviceDetailsDb.getDeviceDetails() == null || appDeviceDetailsDb.getDeviceId() == null
                                || appDeviceDetailsDb.getLanguageType() == null
                                || appDeviceDetailsDb.getOsType() == null) {
                        throw new MissingRequestParameterException("en", mandatoryParameterMissing);
                }
                if (appDeviceDetailsDb.getDeviceId().isBlank()
                                || appDeviceDetailsDb.getLanguageType().trim().length() < 2
                                || appDeviceDetailsDb.getOsType().isBlank()
                                || appDeviceDetailsDb.getDeviceId().trim().length() > 50) {
                        throw new UnprocessableEntityException("en", requiredValueNotPresent);
                }
        }

        public String checkLocalIp(CheckImeiRequest checkImeiRequest, long startTime) {
                String t = checkIpAddress(checkImeiRequest.getIp_type(), checkImeiRequest.getClient_ip());
                if (!t.equalsIgnoreCase("True")) {
                        var tag = t.equalsIgnoreCase("False") ? "country_ip_error_msg" : "country_ip_service_down_msg";
                        logger.info("Getting response for " + tag);
                        var response = eirsResponseParamRepository.getByTagAndLanguage(tag,
                                        checkImeiRequest.getLanguage().equalsIgnoreCase("kh") ? "km"
                                                        : checkImeiRequest.getLanguage())
                                        .getValue();
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, response);
                }
                return t;
        }

        private String checkIpAddress(String ip_type, String ip) {
                var response = apiHttpConnection.httpConnectionForIpCheck(ip_type, ip);
                logger.info("Response for ip " + response);
                return response;
        }

        public boolean checkLuhnAlgorithm(CheckImeiRequest checkImeiRequest, long startTime) {
                if (!luhnAlgoCheck(checkImeiRequest.getImei())) {
                        logger.info("Luhn Failed for imei : " + checkImeiRequest.getImei()
                                        + ". Getting value for luhnFailMsg from eirs response param");
                        var response = eirsResponseParamRepository.getByTagAndLanguage("luhnFailMsg",
                                        checkImeiRequest.getLanguage().equalsIgnoreCase("kh") ? "km"
                                                        : checkImeiRequest.getLanguage())
                                        .getValue();
                        checkImeiServiceImpl.saveCheckImeiFailDetails(checkImeiRequest, startTime, response);
                        return false;
                }
                return true;
        }

        boolean luhnAlgoCheck(String imeiNo) {
                if (imeiNo.length() != 15 || !imeiNo.matches("\\d+")) {
                        logger.debug("IMEI Number must contain exactly 15 digits");
                        return false;
                }
                int sum = 0;
                for (int i = 0; i < 15; i++) {
                        int digit = imeiNo.charAt(i) - '0';
                        if (i % 2 == 1) {
                                digit *= 2;
                                if (digit > 9) {
                                        digit -= 9;
                                }
                        }
                        sum += digit;
                }

                if (sum % 10 == 0) {
                        logger.info(" Luhn Valid IMEI");
                        return true;
                } else {
                        logger.info("Luhn Invalid IMEI");
                        return false;
                }
        }

}
