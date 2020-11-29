package com.crapi.service.Impl;

import com.crapi.entity.User;
import com.crapi.entity.UserPrinciple;
import com.crapi.enums.ERole;
import com.crapi.repository.UserRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;


@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsernameSuccess(){
        User user = getDummyUser();
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Assertions.assertNotNull(userDetails);
    }

    @Test
    public void loadUserByUsernameFailure(){
        User user = getDummyUser();
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenThrow(new RuntimeException());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Assertions.assertNull(userDetails);
    }

    private User getDummyUser() {
        User user = new User("email@gmail.com", "9798789212", "Pass", ERole.ROLE_USER);
        user.setId(1l);
        return user;
    }


}