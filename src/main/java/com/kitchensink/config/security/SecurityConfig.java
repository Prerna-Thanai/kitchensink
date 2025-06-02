package com.kitchensink.config.security;

import com.kitchensink.exception.ExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * The Class SecurityConfig.
 *
 * @author prerna
 */
@Configuration
@Slf4j
public class SecurityConfig {

    /** The Constant PUBLIC_URLS */
    public static final String[] PUBLIC_URLS = { "/api/auth/login", "/api/auth/register", "/api/token",
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs", "/v3/api-docs/swagger-config",
            "/actuator/**", "/api/version" };

    /** The jwt auth filter */
    private final JwtAuthFilter jwtAuthFilter;

    /** The allowed origins */
    private final String allowedOrigins;

    /**
     * SecurityConfig constructor
     *
     * @param jwtAuthFilter
     *            the jwt auth filter
     * @param allowedOrigins
     *            the allowed origins
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
        @Value("${cors.allowed-origins:${CORS_ALLOWED_ORIGINS:http://localhost:4200}}") String allowedOrigins) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Password Encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager.
     *
     * @param authConfig
     *            the auth config
     * @return the authentication manager
     * @throws Exception
     *             the exception
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Filter Chain
     *
     * @param http
     *            the http
     * @return the security filter chain
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ExceptionAdvice exceptionAdvice) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URLS).permitAll().anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(conf -> conf.authenticationEntryPoint(exceptionAdvice));

        return http.build();
    }

    /**
     * Cors Configuration Source
     *
     * @return the cors configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = List.of(allowedOrigins.trim().split("\\s*,\\s*"));
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        log.info("[CORS] Allowed Origins/Patterns: {}", origins);
        log.info("[CORS] Allow Credentials: {}", config.getAllowCredentials());
        log.info("[CORS] Allowed Methods: {}", config.getAllowedMethods());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
