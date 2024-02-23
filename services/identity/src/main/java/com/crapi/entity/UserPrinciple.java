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

package com.crapi.entity;

import com.crapi.enums.ERole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrinciple implements UserDetails {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String name;

  private String email;

  private boolean mfaRequired;

  @JsonIgnore private String password;

  private ERole role;

  private GrantedAuthority authorities;

  public UserPrinciple(
      Long id,
      String email,
      String password,
      ERole role,
      GrantedAuthority authorities,
      boolean mfaRequired) {
    this.id = id;
    this.name = email;
    this.email = email;
    this.role = role;
    this.password = password;
    this.authorities = authorities;
    this.mfaRequired = mfaRequired;
  }

  public static UserPrinciple build(User user) {
    GrantedAuthority authorities = new SimpleGrantedAuthority(user.getRole().toString());
    return new UserPrinciple(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getRole(),
        authorities,
        user.isMfaRequired());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return Arrays.asList(authorities);
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return mfaRequired ? false : true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public ERole getRole() {
    return role;
  }
}
