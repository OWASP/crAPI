package com.crapi.utils;

import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.enums.ERole;
import com.crapi.enums.EStatus;



public class UserData {


    public UserDetails getPredefineUser(String name, User loginForm){
        UserDetails userDetails = null;
        userDetails = getUserDetails(name, loginForm);
        return userDetails;
    }

    public UserDetails getUserDetails(String name, User user){
        UserDetails userDetails = new UserDetails();
        userDetails.setName(name);
        userDetails.setUser(user);
        userDetails.setAvailable_credit(100.0);
        userDetails.setStatus(EStatus.ACTIVE.toString());
        return userDetails;
    }
}
