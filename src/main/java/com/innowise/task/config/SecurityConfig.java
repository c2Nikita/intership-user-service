package com.innowise.task.config;

import com.innowise.security.CommonJwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    private final CommonJwtFilter commonJwtFilter;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public SecurityConfig(CommonJwtFilter commonJwtFilter) {
        this.commonJwtFilter = commonJwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .access((authentication, context) -> {
                            String incomingSecret = context.getRequest().getHeader("X-Internal-Secret");
                            boolean isAuthorized = internalApiKey.equals(incomingSecret);
                            return new AuthorizationDecision(isAuthorized);
                        })
                        .anyRequest().authenticated()
                )
                .addFilterBefore(commonJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}