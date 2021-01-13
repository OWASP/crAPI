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
import com.crapi.config.MailHogConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;

import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Date;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Traceable AI
 */
@Component
public class SMTPMailServer {
    @Autowired
    MailConfiguration mailConfiguration;

    @Autowired
    MailHogConfiguration mailhogConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(SMTPMailServer.class);

    /**
     * @param sendMail
     * @param body
     * @param subject
     * send mail to given email with dynamic subject and body
     */
    public void sendMail(String sendMail, String body, String subject) {
        String mhogDomain = mailhogConfiguration.getDomain();
        Session session = mailhogConfiguration.sendmail();
        boolean useMailHog = false;
        try {
            logger.info("sendMail  mhogDomain: {}, emails: {}", mhogDomain, sendMail);
            InternetAddress[] emails = InternetAddress.parse(sendMail);
            if (mhogDomain != null && !mhogDomain.isEmpty()) {
                if (mailConfiguration.getHost().trim().endsWith(mhogDomain)) {
                    logger.info("SMTP host matches MailHog host. Using MailHog Configuration for sending emails");
                    useMailHog = true;
                }
                for (InternetAddress emailAddress: emails){
                    String email = emailAddress.toString();
                    String domain = email.substring(email.indexOf("@") + 1).trim();
                    logger.debug("sendMail  mhogDomain: {}, email: {}, domain: {}", mhogDomain, email, domain);
                    if (mhogDomain.trim().equals(domain)) {
                        logger.info("Using MailHog Configuration for sending email for domain: " + domain);
                        useMailHog = true;
                    }
                }
            }
            if (! useMailHog) {
                session = mailConfiguration.sendmail();
                logger.info("Using Mail Configuration for sending email: " + sendMail);
            }
        
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(mailhogConfiguration.getFrom(), false));

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
