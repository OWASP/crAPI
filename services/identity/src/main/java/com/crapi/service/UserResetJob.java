package com.crapi.service;

import com.crapi.constant.TestUsers;
import com.crapi.entity.User;
import com.crapi.model.SeedUser;
import com.crapi.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserResetJob {

  private static final Logger logger = LoggerFactory.getLogger(UserResetJob.class);

  private static final LocalDate DEFAULT_PASSWORD_DATE = LocalDate.of(2000, 1, 1);

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder encoder;

  @Scheduled(fixedRate = 3600000) // every hour
  @Transactional
  public void resetTestUserCredsIfChanged() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    int resetCount = 0;

    for (SeedUser seedUser : testUsers) {
      User user = userRepository.findByEmail(seedUser.getEmail());
      if (user == null) {
        continue;
      }
      LocalDate updatedAt = user.getPasswordUpdatedAt();
      if (updatedAt != null && !updatedAt.equals(DEFAULT_PASSWORD_DATE)) {
        user.setPassword(encoder.encode(seedUser.getPassword()));
        user.setPasswordUpdatedAt(DEFAULT_PASSWORD_DATE);
        userRepository.saveAndFlush(user);
        resetCount++;
        logger.info("Reset password for test user: {}", seedUser.getEmail());
      }
    }

    if (resetCount > 0) {
      logger.info("Reset credentials for {} test user(s)", resetCount);
    } else {
      logger.debug("All test user credentials are unchanged, no reset needed");
    }
  }
}
