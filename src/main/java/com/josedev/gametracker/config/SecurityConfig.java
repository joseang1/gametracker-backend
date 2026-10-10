package com.josedev.gametracker.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.josedev.gametracker.security.JpaUserDetailsService;
import com.josedev.gametracker.security.UserStatusJwtConverter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${api-endpoint}")
    private String endpoint;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserStatusJwtConverter jwtConverter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, endpoint + "/auth/register", endpoint + "/auth/login").permitAll()
                        .requestMatchers(endpoint + "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, endpoint + "/games").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, endpoint + "/games/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, endpoint + "/games/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, endpoint + "/games/*/me").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                endpoint + "/games", endpoint + "/games/**",
                                endpoint + "/reviews", endpoint + "/reviews/**",
                                endpoint + "/users/*", endpoint + "/users/*/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(JpaUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
