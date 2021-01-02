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

import com.crapi.config.MailConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;

import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Date;
/**
 * @author Traceabel AI
 */
@Component
public class SMTPMailServer {
    @Autowired
    MailConfiguration mailConfiguration;

    /**
     * @param sendMail
     * @param body
     * @param subject
     * send mail to given email with dynamic subject and body
     */
    public void sendMail(String sendMail, String body, String subject) {
        try {
            Message msg = new MimeMessage(mailConfiguration.sendmail());

            msg.setFrom(new InternetAddress(sendMail, false));

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendMail));
            msg.setSubject(subject);
            msg.setContent(body, "text/html");
            msg.setSentDate(new Date());

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html");

            Transport.send(msg);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
