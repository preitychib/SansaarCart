package com.preeti.sansarcart.config;

import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.filter.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtFilter jwtFilter;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // contains all public url matchers
        // more clean for future use
        final String[] PUBLIC_URLS_MATCHERS = {
                "/auth/**",
                "/image/**"
        };

        final String[] PRIVATE_URL_MATCHERS= {
                "/auth/logout",
//                "/auth/refresh-token",
                "/image/profile-image/**",
                "/image/upload/**",
                "/products/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
        };

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request->request
                        .requestMatchers(PRIVATE_URL_MATCHERS)
                        .authenticated()
                        .requestMatchers("/admin/**", "/actuator/**").hasRole(RoleType.ADMIN.name())
                        .requestMatchers("/customer/**").hasRole(RoleType.CUSTOMER.name())
                        .requestMatchers("/seller/**","/image/upload/product/**").hasRole(RoleType.SELLER.name())
                        .requestMatchers(PUBLIC_URLS_MATCHERS)
                        .permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                Util.sendErrorResponse(response,HttpServletResponse.SC_UNAUTHORIZED, "exception.authentication.error"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                Util.sendErrorResponse(response,HttpServletResponse.SC_FORBIDDEN,  "exception.access.denied"))
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return  provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws  Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

}
