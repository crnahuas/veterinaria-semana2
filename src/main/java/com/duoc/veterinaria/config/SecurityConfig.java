package com.duoc.veterinaria.config;

import com.duoc.veterinaria.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (sslEnabled) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/pacientes", "/citas", "/pacientes.html", "/citas.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/static/**", "/login.css", "/style.css", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, Constants.LOGIN_URL, Constants.API_LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pacientes/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/citas/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/citas/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers("/greetings").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
