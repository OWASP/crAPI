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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class JwtAuthTokenFilter extends OncePerRequestFilter {

  private static final Logger tokenLogger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

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
          tokenLogger.error("User not found");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UserMessage.INVALID_CREDENTIALS);
        }
        if (userDetails.isAccountNonLocked()) {
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          tokenLogger.error(UserMessage.ACCOUNT_LOCKED_MESSAGE);
          response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, UserMessage.ACCOUNT_LOCKED_MESSAGE);
        }
      }
    } catch (Exception e) {
      tokenLogger.error("Can NOT set user authentication -> Message:%d", e);
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
        username = tokenProvider.getUserNameFromApiToken(token);
      } else {
        tokenProvider.validateJwtToken(token);
        username = tokenProvider.getUserNameFromJwtToken(token);
      }
      // checking username from token
      if (username != null) return username;
    }
    return EStatus.INVALID.toString();
  }
}
