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
