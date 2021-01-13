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
        User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
        user.setId(1l);
        return user;
    }


}