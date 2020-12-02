package com.crapi.service.Impl;

import com.crapi.entity.User;
import com.crapi.entity.UserPrinciple;
import com.crapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author Traceabel AI
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    /**
     * @param {String} email
     * @return UserDatils after validating the user into database
     * @throws UsernameNotFoundException
     */
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email)  {
        try {
            User user = userRepository.findByEmail(email);
            System.out.println(user);
            return UserPrinciple.build(user);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }
}
