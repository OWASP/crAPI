package com.crapi.service;

import com.crapi.constant.TestUsers;
import com.crapi.entity.User;
import com.crapi.model.SeedUser;
import com.crapi.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class UserResetJobTest {

  private static final LocalDate DEFAULT_PASSWORD_DATE = LocalDate.of(2000, 1, 1);

  @InjectMocks private UserResetJob userResetJob;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder encoder;

  @Test
  public void resetSkipsWhenPasswordNotChanged() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    for (SeedUser seedUser : testUsers) {
      User user =
          new User(seedUser.getEmail(), seedUser.getNumber(), "encoded", seedUser.getRole());
      user.setPasswordUpdatedAt(DEFAULT_PASSWORD_DATE);
      Mockito.when(userRepository.findByEmail(seedUser.getEmail())).thenReturn(user);
    }

    userResetJob.resetTestUserCredsIfChanged();

    Mockito.verify(userRepository, Mockito.never()).saveAndFlush(Mockito.any());
  }

  @Test
  public void resetResetsWhenPasswordChanged() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    SeedUser firstUser = testUsers.get(0);

    // First user has changed password (non-default date)
    User changedUser =
        new User(firstUser.getEmail(), firstUser.getNumber(), "encoded", firstUser.getRole());
    changedUser.setPasswordUpdatedAt(LocalDate.now());
    Mockito.when(userRepository.findByEmail(firstUser.getEmail())).thenReturn(changedUser);
    Mockito.when(encoder.encode(firstUser.getPassword())).thenReturn("resetEncoded");
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(changedUser);

    // Remaining users have default date
    for (int i = 1; i < testUsers.size(); i++) {
      SeedUser seedUser = testUsers.get(i);
      User user =
          new User(seedUser.getEmail(), seedUser.getNumber(), "encoded", seedUser.getRole());
      user.setPasswordUpdatedAt(DEFAULT_PASSWORD_DATE);
      Mockito.when(userRepository.findByEmail(seedUser.getEmail())).thenReturn(user);
    }

    userResetJob.resetTestUserCredsIfChanged();

    Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    Assertions.assertEquals("resetEncoded", changedUser.getPassword());
    Assertions.assertEquals(DEFAULT_PASSWORD_DATE, changedUser.getPasswordUpdatedAt());
  }

  @Test
  public void resetResetsAllWhenAllPasswordsChanged() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    for (SeedUser seedUser : testUsers) {
      User user =
          new User(seedUser.getEmail(), seedUser.getNumber(), "encoded", seedUser.getRole());
      user.setPasswordUpdatedAt(LocalDate.now());
      Mockito.when(userRepository.findByEmail(seedUser.getEmail())).thenReturn(user);
      Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    }
    Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("resetEncoded");

    userResetJob.resetTestUserCredsIfChanged();

    Mockito.verify(userRepository, Mockito.times(testUsers.size())).saveAndFlush(Mockito.any());
  }

  @Test
  public void resetSkipsNullUser() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    for (SeedUser seedUser : testUsers) {
      Mockito.when(userRepository.findByEmail(seedUser.getEmail())).thenReturn(null);
    }

    userResetJob.resetTestUserCredsIfChanged();

    Mockito.verify(userRepository, Mockito.never()).saveAndFlush(Mockito.any());
  }

  @Test
  public void resetSkipsWhenPasswordUpdatedAtNull() {
    ArrayList<SeedUser> testUsers = new TestUsers().getUsers();
    for (SeedUser seedUser : testUsers) {
      User user =
          new User(seedUser.getEmail(), seedUser.getNumber(), "encoded", seedUser.getRole());
      user.setPasswordUpdatedAt(null);
      Mockito.when(userRepository.findByEmail(seedUser.getEmail())).thenReturn(user);
    }

    userResetJob.resetTestUserCredsIfChanged();

    Mockito.verify(userRepository, Mockito.never()).saveAndFlush(Mockito.any());
  }
}
