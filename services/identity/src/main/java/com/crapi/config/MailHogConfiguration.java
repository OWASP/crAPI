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

package com.crapi.config;

import com.crapi.entity.ChangeEmailRequest;
import com.crapi.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import java.io.IOException;
import java.util.Properties;

@Component
public class MailHogConfiguration {

    @Value("${mail.mailhog.auth}")
    private String auth;

    @Value("${mail.mailhog.starttls.enable}")
    private String enable;

    @Value("${mail.mailhog.host}")
    private String host;

    @Value("${mail.mailhog.port}")
    private String port;

    @Value("${mail.mailhog.email}")
    private String email;

    @Value("${mail.mailhog.password}")
    private String password;

    @Value("${mail.mailhog.domain}")
    private String mhogDomain;

    @Value("${mail.from}")
    private String fromMail;

    /**
     * @return session with all the configuration for send Email
     * @throws AddressException
     * @throws MessagingException
     * @throws IOException
     */
    public Session sendmail() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", enable);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);


        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        return session;

    }

    public String getDomain() {
        return mhogDomain;
    }

    public String getFrom() {
        if (fromMail != null && !fromMail.isEmpty()) {
           return fromMail.trim();
        } else {
            return "no-reply@example.com";
        }
    }
}

