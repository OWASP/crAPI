/*
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

import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds the JavaMail {@code Session} that points at the Mailhog container.
 *
 * <p>Reads the {@code mail.mailhog.*} properties (host, port, auth, STARTTLS, sender) from {@code
 * application.properties} and hands a configured session to {@code utils.SMTPMailServer}, which
 * every mail-sending flow uses: signup verification, OTP for password reset, change-email tokens
 * and the vehicle-details mail. Nothing here reaches a real mail server; messages land in Mailhog's
 * UI on port 8025, which is where the OTP and reset challenges are solved.
 */
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

    Session session =
        Session.getInstance(
            props,
            new jakarta.mail.Authenticator() {
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
