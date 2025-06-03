package com.preeti.sansarcart.filter;


import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.security.service.JWTService;
import com.preeti.sansarcart.security.UserDetailsServiceImp;
import com.preeti.sansarcart.service.AccessTokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import  org.springframework.security.authentication.AccountStatusUserDetailsChecker;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AccessTokenBlacklistService accessTokenBlacklistService;

    @Autowired
    private AccountStatusUserDetailsChecker accountStatusUserDetailsChecker;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader= request.getHeader("Authorization");
        String token=null;
        String username= null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtService.extractUserName(token);

                if (accessTokenBlacklistService.isBlacklisted(token)) {
                    Util.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                    return;
                }
            } catch (ExpiredJwtException e) {
                Util.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
                return;
            } catch (JwtException e) {
                Util.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }




        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails= context.getBean(UserDetailsServiceImp.class).loadUserByUsername(username);
            try{
                accountStatusUserDetailsChecker.check(userDetails);
                if(jwtService.validateToken(token,userDetails)){
                    UsernamePasswordAuthenticationToken authenticationToken=
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (RuntimeException e) {
                Util.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getLocalizedMessage());
                return;
            }

        }


        filterChain.doFilter(request,response);
    }
}

