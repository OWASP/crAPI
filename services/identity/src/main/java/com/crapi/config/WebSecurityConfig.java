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

import com.crapi.service.Impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {"com.crapi"})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

  @Autowired UserDetailsServiceImpl userDetailsService;

  @Autowired JwtAuthEntryPoint unauthorizedHandler;

  @Bean
  public JwtAuthTokenFilter authenticationJwtTokenFilter() {
    return new JwtAuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    DaoAuthenticationProvider authProvider = authenticationProvider();
    return new AuthenticationManager() {
      @Override
      public org.springframework.security.core.Authentication authenticate(
          org.springframework.security.core.Authentication authentication)
          throws org.springframework.security.core.AuthenticationException {
        return authProvider.authenticate(authentication);
      }
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(
            (csrf) -> {
              csrf.disable();
            })
        .authorizeHttpRequests(
            (requests) ->
                requests
                    .requestMatchers("/identity/api/auth/**")
                    .permitAll()
                    .requestMatchers("/identity/health_check")
                    .permitAll()
                    .requestMatchers("/identity/api/v2/user/dashboard")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandler));
    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(
        authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
