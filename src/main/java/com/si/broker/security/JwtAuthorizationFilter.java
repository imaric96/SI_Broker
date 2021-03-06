
package com.si.broker.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.si.broker.utils.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final MyUserDetailsService userDetailsService;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MyUserDetailsService userDetailsService) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(Constants.HEADER_STRING);

        if (header == null || !header.startsWith(Constants.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

//       UsernamePasswordAuthenticationToken
        Authentication authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);

    }

    //Util
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER_STRING);

        if (token != null) {

            String username = JWT.require(Algorithm.HMAC512(Constants.SECRET_KEY.getBytes()))
                    .build()
                    .verify(token.replace(Constants.TOKEN_PREFIX, ""))
                    .getSubject();

            if (username == null) return null;

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken forTheUser = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                    userDetails.getPassword(), userDetails.getAuthorities());

            return forTheUser;

        }
        return null;
    }

}