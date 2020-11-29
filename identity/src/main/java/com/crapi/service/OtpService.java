package com.crapi.service;

import com.crapi.entity.Otp;
import com.crapi.model.ForgetPassword;
import com.crapi.model.OtpForm;
import com.crapi.model.CRAPIResponse;

/**
 * @author Traceabel AI
 */
public interface OtpService {

    boolean invalidateOtp(Otp validateOtp);

    CRAPIResponse generateOtp(ForgetPassword user);

    CRAPIResponse validateOtp(OtpForm otpForm);

    CRAPIResponse secureValidateOtp(OtpForm otpForm);
}
