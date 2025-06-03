package com.preeti.sansarcart.config;

import com.preeti.sansarcart.common.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;

@EnableScheduling
@Configuration
@EnableJpaAuditing
public class CommonConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
    @Bean
    public AccountStatusUserDetailsChecker accountStatusUserDetailsChecker() {
        return new AccountStatusUserDetailsChecker();
    }

}
