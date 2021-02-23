/*
 * Copyright 2020 Traceable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crapi.utils;

import com.crapi.entity.ChangeEmailRequest;
import com.crapi.entity.Otp;
import com.crapi.entity.User;
import com.crapi.entity.VehicleDetails;
import com.crapi.model.ChangeEmailForm;

/**
 * @author Traceable AI
 */
public class MailBody {


    /**
     * @param otp
     * @return Mail body for OTP
     */
    public static String otpMailBody(Otp otp) {
        String body = "";
        body += "<table border='0' cellpadding='0' cellspacing='0' class='btn btn-primary'>";
        body += "<tr>";
        body += "<td align='center'>";
        body += "<table border='0' cellpadding='0' cellspacing='0'>";
        body += "<tr>";
        body += "<td>Hi,</td>";
        body += "<td> </td>";
        body += "<td>Your one time generated otp is: " + otp.getOtp() + "</td>";
        body += "</tr>";
        body += "</table>";
        body += "</td>";
        body += "</tr>";
        body += "</table>";

        return body;
    }

    /**
     * @param vehicleDetails
     * @param name
     * @return Mail body for user Signup
     */
    public static String signupMailBody(VehicleDetails vehicleDetails, String name) {
        String msgBody = "<html><body>"
                + "<font face='calibri' style = 'font-size:15px; color:#000;'>Hi "+name

                + "<font>,"
                + "<br><font face='calibri'><p style = 'font-size:15px; color:#000;'>We are glad to have you on-board. Your newly purchased vehiche details are provided below. Please add it on your crAPI dashboard.</p>"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>Your vehicle information is <b>VIN: </font><font face='calibri' font color='#0000ff'>" + vehicleDetails.getVin() + "</font></b> and <b>Pincode: <font face='calibri' font color='#0000ff'>" + vehicleDetails.getPincode() + "</font></b></p>"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>We're here to help you build a relationship with your vehicles.</font></p>"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>Thank You & have a wonderful day !</font></p>"
                + "<font face='calibri' style = 'font-size:15px;color:#000;'>Warm Regards,<br/><b>crAPI - Team</b></font><font face='calibri' font color='#0000ff'></font><br/>"
                + "<strong>Email:</strong>&nbsp;<a href='mailto:support@crapi.io'>support@crapi.io</a></font><br><font face='calibri'>&nbsp;&nbsp;<br> "
                + "<em style= 'color:#000;'>This E-mail and any attachments are private, intended solely for the use of the addressee. If you are not the intended recipient, they have been sent to you in error: any use of information in them is strictly prohibited. </em>"
                + "</body>" + "</html>";

        return msgBody;
    }

    /**
     * @param changeEmailRequest
     * @return Mail Body, for Chnage Email.
     */
    public static String changeMailBody(ChangeEmailForm changeEmailRequest) {
        String msgBody = "<html><body>"
                + "<font face='calibri' style = 'font-size:15px; color:#000;'>Hi"

                + "<font>,"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>We received a request to change your account email address. The previous e-mail address is: </font><font face='calibri' font color='#0000ff'><b>" + changeEmailRequest.getOld_email()+ "</b></font>"
                + "<font face='calibri' style = 'font-size:15px;color:#000;'> and the new one is: <b>" + changeEmailRequest.getNew_email()+ "</b></font></p>"
                + "<font face='calibri' style = 'font-size:15px;color:#000;'>To complete the process, please use the following token: <b>" + changeEmailRequest.getToken()+"</b>"
                + "<br>"
                + "<br>"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>If you haven not sent a request to change your email address, please ignore this message.</font></p>"
                + "<p><font face='calibri' style = 'font-size:15px;color:#000;'>Thank You & have a wonderful day !</font></p>"
                + "<font face='calibri' style = 'font-size:15px;color:#000;'>Warm Regards,<br/><b>crAPI - Team</b></font><font face='calibri' font color='#0000ff'></font><br/>"
                + "<strong>Email:</strong>&nbsp;<a href='mailto:support@crapi.io'>support@crapi.io</a></font><br><font face='calibri'>&nbsp;&nbsp;<br> "
                + "<em style= 'color:#000;'>This E-mail and any attachments are private, intended solely for the use of the addressee. If you are not the intended recipient, they have been sent to you in error: any use of information in them is strictly prohibited. </em>"
                + "</body>" + "</html>";

        return msgBody;
    }
}
