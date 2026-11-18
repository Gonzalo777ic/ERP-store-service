package com.valeerp.store.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String ROLES_CLAIM = "https://valeerp.local/roles";

    @Value("${valeerp.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/store/sales/**", "/api/store/promotions/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDOR")
                        .requestMatchers("/api/store/quotations/client/**", "/api/store/sales/client/**").hasAnyAuthority("ROLE_CLIENT", "ROLE_ADMIN", "ROLE_VENDOR")
                        .requestMatchers(HttpMethod.GET, "/api/store/complaints/catalog").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/store/complaints").permitAll()
                        .requestMatchers("/api/store/complaints", "/api/store/complaints/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_VENDOR")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Crea un conversor personalizado para extraer los roles de los claims de Auth0.
     * **FIX:** Utiliza jwt.getClaim() para asegurar compatibilidad al obtener el List<String> de roles.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object rolesObject = jwt.getClaim(ROLES_CLAIM);

            if (rolesObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObject;
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
        return converter;
    }

    /**
     * Configuración básica de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOriginPatterns.split(",")).stream()
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}