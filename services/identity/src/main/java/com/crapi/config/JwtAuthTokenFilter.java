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

package com.crapi.config;

import com.crapi.enums.EStatus;
import com.crapi.service.Impl.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Traceable AI
 */
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private static final Logger tokenLogger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String username = getUserFromToken(request);
            if (username != null &&  !username.equalsIgnoreCase(EStatus.INVALID.toString())){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch(Exception e){
            tokenLogger.error("Can NOT set user authentication -> Message:%d", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @param request
     * @return jwt token
     */
    public String getJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        //checking token is there or not
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        return null;
    }

    /**
     * @param request
     * @return return username from HttpServletRequest
     * if request have token we are returning username from request token
     */
    public String getUserFromToken(HttpServletRequest request) throws UnsupportedEncodingException {

        String jwt = getJwt(request);
        if (jwt != null && tokenProvider.validateJwtToken(jwt)) {
            String username = tokenProvider.getUserNameFromJwtToken(jwt);
            //checking username from token
            if (username!=null)
                return username;
        }
        return EStatus.INVALID.toString();

    }
}
