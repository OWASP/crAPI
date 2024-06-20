package com.crapi.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("server.ssl")
public class SSLConfig {
  private Boolean enabled;

  @AssertTrue
  boolean isEmabledValid() {
    return enabled != null;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(String enabled) {
    if (enabled != null) {
      if (enabled.equalsIgnoreCase("true")
          || enabled.equalsIgnoreCase("yes")
          || enabled.equalsIgnoreCase("1")) {
        this.enabled = true;
        System.out.println("TLS Enabled");
      } else {
        this.enabled = false;
        System.out.println("TLS Disabled");
      }
    }
  }
}
