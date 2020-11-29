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
public class MailConfiguration {

    @Value("${mail.smtp.auth}")
    private String auth;

    @Value("${mail.smtp.starttls.enable}")
    private String enable;

    @Value("${mail.smtp.host}")
    private String host;
    @Value("${mail.smtp.port}")
    private String port;

    @Value("${mail.smtp.email}")
    private String email;

    @Value("${mail.smtp.password}")
    private String password;

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

        if (email.isEmpty() || password.isEmpty()){
            throw new EntityNotFoundException(ChangeEmailRequest.class,"email and Password not configure ", email+" "+password);
        }

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        return session;

    }
}

