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

import com.crapi.constant.UserMessage;
import com.crapi.enums.EStatus;
import com.crapi.service.Impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

enum ApiType {
  JWT,
  APIKEY;
}

/**
 * Servlet filter that turns the {@code Authorization} header into a Spring Security principal.
 *
 * <p>Runs once per request, ahead of the standard filters (see {@link WebSecurityConfig}). It
 * strips the {@code Bearer } / {@code ApiKey } prefix, asks {@link JwtProvider} for the subject
 * (API keys skip signature validation entirely, JWTs go through {@code validateJwtToken}), loads
 * the user via {@code UserDetailsServiceImpl}, and, if the account is not locked, stores the
 * authentication in the {@code SecurityContext}. Any failure is logged and the request continues
 * unauthenticated, so the route rules decide whether it gets a 401.
 *
 * <p>The services also call {@link #getUserFromToken} directly to find out who is making a request.
 */
@Slf4j
public class JwtAuthTokenFilter extends OncePerRequestFilter {

  @Autowired private JwtProvider tokenProvider;

  @Autowired private UserDetailsServiceImpl userDetailsService;

  /**
   * @param request
   * @param response
   * @param filterChain
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String username = getUserFromToken(request);
      if (username != null && !username.equalsIgnoreCase(EStatus.INVALID.toString())) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
          log.error("User not found");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UserMessage.INVALID_CREDENTIALS);
        }
        if (userDetails.isAccountNonLocked()) {
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          log.error(UserMessage.ACCOUNT_LOCKED_MESSAGE);
          response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, UserMessage.ACCOUNT_LOCKED_MESSAGE);
        }
      }
    } catch (Exception e) {
      log.error("Can NOT set user authentication -> Message:%d", e);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * @param request
   * @return key/token
   */
  public String getToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    // checking token is there or not
    if (authHeader != null && authHeader.length() > 7) {
      return authHeader.substring(7);
    }
    return null;
  }

  /**
   * @param request
   * @return api type from HttpServletRequest
   */
  public ApiType getKeyType(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    ApiType apiType = ApiType.JWT;
    if (authHeader != null && authHeader.startsWith("ApiKey ")) {
      apiType = ApiType.APIKEY;
    }
    return apiType;
  }

  /**
   * @param request
   * @return return username from HttpServletRequest if request have token we are returning username
   *     from request token
   */
  public String getUserFromToken(HttpServletRequest request) throws ParseException {
    ApiType apiType = getKeyType(request);
    String token = getToken(request);
    String username = null;
    if (token != null) {
      if (apiType == ApiType.APIKEY) {
        log.debug("Token is api token");
        username = tokenProvider.getUserNameFromJwtToken(token);
      } else {
        log.debug("Token is jwt token");
        if (tokenProvider.validateJwtToken(token)) {
          username = tokenProvider.getUserNameFromJwtToken(token);
        }
      }
      // checking username from token
      if (username != null) return username;
    }
    return EStatus.INVALID.toString();
  }
}
